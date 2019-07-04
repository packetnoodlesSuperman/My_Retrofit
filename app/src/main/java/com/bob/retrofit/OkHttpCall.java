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

    @GuardedBy("this")
    private @Nullable okhttp3.Call rawCall;

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
    public void enqueue(Callback<T> callback) {
        okhttp3.Call call;

        call = rawCall = createRawCall();

        call.enqueue(new okhttp3.Callback() {

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) throws IOException {
                Response<T> response;

                response = parseResponse(rawResponse);
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }
        });
    }

    private Response<T> parseResponse(okhttp3.Response rawResponse) {
        ResponseBody rawBody = rawResponse.body();


        return null;
    }

    private okhttp3.Call createRawCall() {
        okhttp3.Call call = serviceMethod.toCall(args);

        return call;
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return null;
    }
}
