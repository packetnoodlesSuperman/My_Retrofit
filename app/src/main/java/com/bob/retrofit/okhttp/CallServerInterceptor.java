package com.bob.retrofit.okhttp;

import java.io.IOException;

public class CallServerInterceptor implements Interceptor {

    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        realChain.httpStream();

        return null;
    }
}
