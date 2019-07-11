package com.bob.retrofit;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;

import com.bob.retrofit.okhttp.MediaType;
import com.bob.retrofit.okhttp.Request;
import com.bob.retrofit.okhttp.ResponseBody;

import java.io.IOException;

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
    private @Nullable com.bob.retrofit.okhttp.Call rawCall;
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
        com.bob.retrofit.okhttp.Call call = rawCall;
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

        com.bob.retrofit.okhttp.Call call;
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
        call.enqueue(new com.bob.retrofit.okhttp.Callback() {

            @Override
            public void onResponse(com.bob.retrofit.okhttp.Call call, com.bob.retrofit.okhttp.Response rawResponse) throws IOException {
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
            public void onFailure(com.bob.retrofit.okhttp.Call call, IOException e) {
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
    private Response<T> parseResponse(com.bob.retrofit.okhttp.Response rawResponse) throws IOException {
        //拿出body
        ResponseBody rawBody = rawResponse.body();
        /** {@link NoContentResponseBody} 用来清空body **/
        //重新构建响应体 这里清空了body
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();

        int code = rawResponse.code();
        //不是成功的code
        if (code < 200 || code >= 300) {
            try {
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        //204代表响应报文中包含若干首部和一个状态行，但是没有实体的主体内容
        //      (204表示响应执行成功，但没有数据返回，浏览器不用刷新，不用导向新页面)
        //205则是告知浏览器清除当前页面中的所有html表单元素，也就是表单重置
        //      (205表示响应执行成功，重置页面（Form表单），方便用户下次输入)
        //204也是访问成功，但是不返回任何数据，就像是a到b家玩了后，b什么都没给a，，
        //205差不多就是，a到b家玩了之后，b还送了点礼物给a带回去，我是这么理解的
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
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    //MethodService创建一个RealCall
    private com.bob.retrofit.okhttp.Call createRawCall() throws IOException {
        com.bob.retrofit.okhttp.Call call = serviceMethod.toCall(args);
        if (call == null) {
            throw new NullPointerException("Call.Factory returned null.");
        }
        return call;
    }

    /**
     * @Desc 同步请求 不用研究
     */
    @Override
    public Response<T> execute() throws IOException {
        com.bob.retrofit.okhttp.Call call;

        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("Already executed");
            }
            executed = true;

            //有一异常就不会请求
            if (creationFailure != null) {
                if (creationFailure instanceof IOException) {
                    throw (IOException) creationFailure;
                } else if (creationFailure instanceof RuntimeException) {
                    throw (RuntimeException) creationFailure;
                } else {
                    throw (Error) creationFailure;
                }
            }

            call = rawCall;
            if (call == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (IOException | RuntimeException | Error e) {
                    creationFailure = e;
                    throw e;
                }
            }
        }

        if (canceled) { call.cancel(); }

        return parseResponse(call.execute());
    }

    //是否执行了
    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;

        com.bob.retrofit.okhttp.Call call;
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