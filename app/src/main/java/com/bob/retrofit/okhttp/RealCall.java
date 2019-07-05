package com.bob.retrofit.okhttp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RealCall implements Call {

    final OkHttpClient client;
    final Request originalRequest;
    final boolean forWebSocket;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;

    private EventListener eventListener;

    public RealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client, forWebSocket);
    }

    @Override
    public Request request() {
        return null;
    }

    @Override
    public Response execute() throws IOException {
        return null;
    }

    public static Call newRealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
        RealCall call = new RealCall(client, originalRequest, forWebSocket);
        call.eventListener = client.eventListenerFactory().create(call);
        return call;
    }

    @Override
    public void enqueue(Callback responseCallback) {
        eventListener.callStart(this);
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call clone() {
        return null;
    }

    String redactedUrl() {
        return null;
    }

    /**
     * @Desc 内部类 任务类
     * TODO 内部类与静态内部类的区别
     */
    final class AsyncCall extends NamedRunnable {

        private final Callback responseCallback;

        RealCall get() {
            return RealCall.this;
        }

        String host() {
            return originalRequest.url().host();
        }

        public AsyncCall(Callback responseCallback) {
            super("OkHttp %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        @Override
        public void execute() throws IOException {
            //Retrofit OkHttp Okio EventBus Dagger2 ButterKnife Glide RxJava
            //gradle kotlin flutter javaScript html css jni git sql vue
            //数据结构 设计模式 测试<AndroidTest、JavaTest>  Http相关 数据安全与加密
            //虚拟机jvm（内存模型、内存结构等）、多线程与反射、dalvik<art> WebView
            //Android源码  Android适配 Android架构 Android性能优化 Android新特性
            //组件化 插件化 lottie
            //零碎知识点
            boolean signalledCallback = false;
            Response response = getResponseWithInterceptorChain();
        }
    }

    private Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();

        interceptors.addAll(client.interceptors());
        interceptors.add(retryAndFollowUpInterceptor);

        interceptors.add(new ConnectInterceptor(client));

        Interceptor.Chain chain = new RealInterceptorChain(
                interceptors,
                null,
                null,
                null,
                0,
                originalRequest,
                this,
                eventListener,
                client.connectTimeoutMillis(),
                client.readTimeoutMillis(),
                client.writeTimeoutMillis()
        );
        return chain.proceed(originalRequest);
    }

}
