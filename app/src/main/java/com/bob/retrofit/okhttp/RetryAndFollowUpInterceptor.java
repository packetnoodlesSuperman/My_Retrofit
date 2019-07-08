package com.bob.retrofit.okhttp;

import com.bob.retrofit.okhttp.connection.RouteException;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.Proxy;

import static java.net.HttpURLConnection.HTTP_PROXY_AUTH;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * @Desc  负责失败重试以及重定向的
 */
public class RetryAndFollowUpInterceptor implements Interceptor {

    private static final int MAX_FOLLOW_UPS = 20;

    private final OkHttpClient client;
    private final boolean forWebSocket;
    private volatile StreamAllocation streamAllocation;
    private Object callStackTrace;
    private volatile boolean canceled;


    public RetryAndFollowUpInterceptor(OkHttpClient client, boolean forWebSocket) {
        this.client = client;
        this.forWebSocket = forWebSocket;
    }

    public void cancel() {
        canceled = true;
        StreamAllocation streamAllocation = this.streamAllocation;
        if (streamAllocation != null) {
            streamAllocation.cancel();
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCallStackTrace(Object callStackTrace) {
        this.callStackTrace = callStackTrace;
    }

    public StreamAllocation streamAllocation() {
        return streamAllocation;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;


        Call call = realChain.call();
        EventListener eventListener = realChain.eventListener();

        //创建一个StreamAllocation 他内部处理创建流等操作
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

        //开启一个循环，然后一直处理下面的事
        while (true) {
            //如果取消了，则关闭流，并抛出异常 退出循环
            if (canceled) {
                streamAllocation.release();
                throw new IOException("Canceled");
            }
            Response response;
            boolean releaseConnection = true;
            try {
                //在这里调用下一个拦截器，获取结果，并在后续去处理他
                response = realChain.proceed(request, streamAllocation, null, null);
                releaseConnection = false;
            } catch (RouteException e) {
                if (!recover(e.getLoastConnectException(), streamAllocation, false, request)) {
                    throw e.getLastConnectException();
                }
                releaseConnection = false;
                continue;
            } catch (IOException e) {

            } finally {

            }

            if (priorResponse != null) {
                response = response.newBuilder()
                        .priorResponse(
                            priorResponse.newBuilder()
                                        .body(null)
                                        .build()
                        ).build();
            }

            Request followUp = followUpRequest(response, streamAllocation.route());


        }

        return null;
    }


    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        if (userResponse == null) {
            throw new IllegalStateException();
        }
        int responseCode = userResponse.code();

        final String method = userResponse.request().method();
        switch (responseCode) {
            case HTTP_PROXY_AUTH: //407 要求代理身份认证
                Proxy selectedProxy = route != null ? route.proxy() : client.proxy();
                if (selectedProxy.type() != Proxy.Type.HTTP) {
                    throw new ProtocolException();
                }
                return client.proxyAuthenticator().authenticate(route, userResponse);
            case HTTP_UNAUTHORIZED:
                return client.authenticator().authenticate(route, userResponse);
        }

    }

    private Address createAddress(HttpUrl url) {
        return null;
    }
}
