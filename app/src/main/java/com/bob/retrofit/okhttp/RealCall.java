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

    /**
     * 同步请求
     */
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
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        interceptors.add(new CacheInterceptor(client.internalCache()));
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
            interceptors.addAll(client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(forWebSocket));

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
/**
 * 1. OKio OKHttp Retrofit源码        《Retorfit 75%  OKHttp 30%  Okio 0%》
 * 2. RxJava 操作符 能自己封装一个RXView RXRouter   《Rxjava  5%》
 * 3. Thread Executor Android的异步 handler 多线程 HandlerThread 锁机制  《50%》
 * 4. apt aspjectJ  《apt PGYJSBridge 有简单实例，type体系 element体系需要巩固 65&   aspjectJ 60%  需要温习 笔记做一下》
 * 5. 注解 Dagger2 Annotations EventBus ButterKnife源码 《EventBus 50% 其他0%》
 * 6. 自定义view 窗口机制 绘制流程
 * 7. Activity启动流程 aidl binder 《aidl 20%》
 * 8. 事件分发
 * 9. Kotlin
 * 10. 组件化、插件化
 * 11.设计模式 原则等
 * 12. 反射 动态代理原理 类加载机制
 * 13. 测试 Junit AndroidTest
 * 14. jetPack
 * 15. gradle
 * 16. android动画 矢量动画等
 * 17. android适配
 * 18. 性能优化
 * 19. Glide。数据库GreenDao 手写数据库（动脑学院）
 * 20. WebView
 * 21. jni
 * 22. LruCache DiskLruCache HashMap LinkedHashMap 等等
 * 23. Java的泛型 element体系 type体系 泛型擦除
 * 24. jvm art与Dalvik apk打包流程 java8新特性 异常
 * 25. git adb-shell 等
 * 26. 小程序
 *
 * * 数据结构 数据安全与加密 Http协议
 */
