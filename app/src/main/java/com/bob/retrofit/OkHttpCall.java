package com.bob.retrofit;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * @param <T> 后台返回的JS对象
 */
public class OkHttpCall<T> implements Call<T> {

    private final ServiceMethod<T, ?> serviceMethod;
    private final @Nullable Object[] args;

    /**
     * @GuardedBy(lock) --> @GuardedBy( "this" ) 受对象内部锁保护
     */
    @GuardedBy("this")
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

    @Override
    public synchronized Request request() {
        okhttp3.Call call = rawCall;
        if (call != null) {
            return call.request();
        }

        return null;
    }

    @Override
    public Response<T> execute() throws IOException {
        return null;
    }

    @Override
    public void enqueue(final Callback<T> callback) {
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
                    throwIfFatal(t);
                    failure = creationFailure = t;
                }
            }
        }

        if (failure != null) {
            callback.onFailture(this, failure);
            return;
        }

        if (canceled) {
            call.cancel();
        }

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) throws IOException {
                Response<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
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

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }
        });
    }

    private void callFailure(Throwable e) {

    }

    private void throwIfFatal(Throwable t) {

    }

    private Response<T> parseResponse(okhttp3.Response rawResponse) {
        ResponseBody rawBody = rawResponse.body();


        return null;
    }

    private okhttp3.Call createRawCall() {
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

    @Override
    public Call<T> clone() {
        return null;
    }
}
