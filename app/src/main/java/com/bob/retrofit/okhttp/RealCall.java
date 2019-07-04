package com.bob.retrofit.okhttp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RealCall implements Call {

    OkHttpClient client;

    @Override
    public Request request() {
        return null;
    }

    @Override
    public Response execute() throws IOException {
        return null;
    }

    public static Call newRealCall(OkHttpClient okHttpClient, Request request, boolean forWebSocket) {
        return null;
    }

    @Override
    public void enqueue(Callback responseCallback) {
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


    final class AsyncCall extends NamedRunnable {

        private final Callback responseCallback;

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
            //组件化 插件化
            //零碎知识点

            List<Interceptor> interceptors = new ArrayList<>();

            RealInterceptorChain chain = new RealInterceptorChain(interceptors, 0, null);
            chain.proceed(null);
        }
    }

    String redactedUrl() {
        return null;
    }

}
