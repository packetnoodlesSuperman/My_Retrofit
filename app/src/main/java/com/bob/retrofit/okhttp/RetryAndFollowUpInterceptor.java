package com.bob.retrofit.okhttp;

import java.io.IOException;

/**
 * @Desc  负责失败重试以及重定向的
 */
public class RetryAndFollowUpInterceptor implements Interceptor {

    private final OkHttpClient client;
    private final boolean forWebSocket;

    private Object callStackTrace;
    private volatile StreamAllocation streamAllocation;

    public RetryAndFollowUpInterceptor(OkHttpClient client, boolean forWebSocket) {
        this.client = client;
        this.forWebSocket = forWebSocket;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;

        Call call = realChain.call();
        EventListener eventListener = realChain.eventListener();

        StreamAllocation streamAllocation = new StreamAllocation(
                client.connectionPool(),
                createAddress(request.url()),
                call,
                eventListener,
                callStackTrace
        );

        this.streamAllocation = streamAllocation;

        int followUpCount = 0;
        Response priorResponse = null;
        while (true) {

            Response response;
            boolean releaseConnection = true;
            try {
                response = realChain.proceed(request, streamAllocation, null, null);
            }
        }

        return null;
    }

    private Address createAddress(HttpUrl url) {
        return null;
    }
}
