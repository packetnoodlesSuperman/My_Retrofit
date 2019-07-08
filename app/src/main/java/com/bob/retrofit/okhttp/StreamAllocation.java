package com.bob.retrofit.okhttp;

import android.os.SystemClock;

import com.bob.retrofit.okhttp.connection.RealConnection;
import com.bob.retrofit.okhttp.http.HttpCodec;

import java.io.IOException;

public class StreamAllocation {

    /**
     * @param connectionPool 连接池 主要用来复用HTTP/2连接
     * @param address 和当前url相关的信息，包括url 还有协议等
     * @param call 将request包装成了一个call 有点类似一个任务
     * @param eventListener 用来跟踪一个call的执行流程
     * @param callStackTrace 调用栈
     */
    public StreamAllocation(ConnectionPool connectionPool, Address address, Call call, EventListener eventListener, Object callStackTrace) {

    }

    public HttpCodec newStream(OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {


        RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout,
                writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);
    }

    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                                              int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                                                 boolean doExtensiveHealthChecks) throws IOException {

    }

    public RealConnection connection() {
        SystemClock.sleep(2000);
        Thread.sleep(2000);
        thread.interrupt();
        thread.isInterrupt();
        Thread.interrupted(); //会重置状态
        InterruptedException  //抛出这个异常会重置状态
        return null;
    }
}
