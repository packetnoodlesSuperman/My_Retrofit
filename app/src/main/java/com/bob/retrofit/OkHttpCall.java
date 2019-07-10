package com.bob.retrofit;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

/**
 * @param <T> 后台返回的JS对象
 */
public class OkHttpCall<T> implements Call<T> {

    private final ServiceMethod<T, ?> serviceMethod;
    private final @Nullable Object[] args;

    /**
     * @GuardedBy(lock) --> @GuardedBy( "this" ) 受对象内部锁保护
     */
    @GuardedBy("this")   //RealCall 是通过 OkHttpClient.newCall(request)创造的
    private @Nullable okhttp3.Call rawCall;
    @GuardedBy("this")
    private @Nullable Throwable creationFailure;
    @GuardedBy("this")
    private boolean executed;

    private volatile boolean canceled;

    OkHttpCall(ServiceMethod<T, ?> serviceMethod, @Nullable Object[] args) {
        this.serviceMethod = serviceMethod;
        this.args = args;
    }

    /**
     * @return 克隆一个Call请求
     */
    @Override public OkHttpCall<T> clone() {
        return new OkHttpCall<>(serviceMethod, args);
    }

    /**
     * @return 获取request 如果没有那么则让MethodService创建出来
     */
    @Override
    public synchronized Request request() {
        okhttp3.Call call = rawCall;
        if (call != null) {
            return call.request();
        }

        //有没有异常
        if (creationFailure != null) {
            if (creationFailure instanceof IOException) {
                throw new RuntimeException("Unable to create request.", creationFailure);
            } else if (creationFailure instanceof RuntimeException) {
                throw (RuntimeException) creationFailure;
            } else {
                throw (Error) creationFailure;
            }
        }

        //如果rawCall为空 并且异常 != null
        try {
            return (rawCall = createRawCall()).request();
        } catch (RuntimeException | Error e) {
            creationFailure = e;
            throw e;
        } catch (IOException e) {
            creationFailure = e;
            throw new RuntimeException("Unable to create request.", e);
        }
    }

    /**
     * @Desc 开启请求
     */
    @Override
    public void enqueue(final Callback<T> callback) {
        Utils.checkNotNull(callback, "callback == null");

        okhttp3.Call call;
        Throwable failure;

        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("Already executed");
            }
            executed = true;

            call = rawCall;
            failure = creationFailure;

            if (call == null && failure == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    failure = creationFailure = t;
                }
            }
        }

        //如果有异常
        if (failure != null) {
            callback.onFailure(this, failure);
            return;
        }

        //是否取消了请求
        if (canceled) {
            call.cancel();
        }

        //没有异常 也没有取消 那么开启请求
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) throws IOException {
                Response<T> response;
                try {
                    //解析响应
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    //如果出现异常 那么调用callback的onFailure方法
                    callFailure(e);
                    return;
                }

                try {
                    //OkHttpCall.this 引用外部类this
                    //this指针只不过是这个“结构体”的地址而已所以，只要数据成员被分配了内存，它就有可用值
                    callback.onResponse(OkHttpCall.this, response);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            //请求出现错误
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callFailure(e);
            }
            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(OkHttpCall.this, e);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    /**
     * @Desc 解析响应
     */
    private Response<T> parseResponse(okhttp3.Response rawResponse) {
        ResponseBody rawBody = rawResponse.body();

        //重新构建响应体
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();

        int code = rawResponse.code();
        if (code < 200 || code >= 300) {
            try {
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        if (code == 204 || code == 205) {
            rawBody.close();
            return Response.success(null, rawResponse);
        }
        ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
        try {
            /**
             * @Desc ServiceMethod使用转换器 将ResponseBody转换成对应的Bean
             */
            T body = serviceMethod.toResponse(catchingBody);
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }


    @Override
    public Response<T> execute() throws IOException {
        return null;
    }


    private okhttp3.Call createRawCall() throws IOException {
        okhttp3.Call call = serviceMethod.toCall(args);
        if (call == null) {
            throw new NullPointerException("Call.Factory returned null.");
        }
        return call;
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {
        canceled = true;

        okhttp3.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    /************************* 内部类 *************************/
    static final class NoContentResponseBody extends ResponseBody {
        private final MediaType contentType;
        private final long contentLength;

        public NoContentResponseBody(MediaType contentType, long contentLength) {
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    static final class ExceptionCatchingRequestBody extends ResponseBody {
        private final ResponseBody delegate;
        IOException thrownException;

        public ExceptionCatchingRequestBody(ResponseBody delegate) {
            this.delegate = delegate;
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        @Override
        public BufferedSource source() {
            return Okio.buffer(new ForwardingSource(delegate.source()) {
                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        @Override public void close() {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }
}