package com.bob.retrofit.okhttp;

import com.bob.retrofit.okhttp.http.HttpCodec;

import java.io.IOException;

public class ConnectInterceptor implements Interceptor {

    public final OkHttpClient client;

    public ConnectInterceptor(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();

        StreamAllocation streamAllocation = realChain.streamAllocation();
        boolean doExtensiveHealthChecks = !request.method().equals("GET");
        HttpCodec httpCodec = streamAllocation.newStream(client, chain, doExtensiveHealthChecks);
        return null;
    }
}
