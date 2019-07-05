package com.bob.retrofit.okhttp;

import com.bob.retrofit.okhttp.connection.RealConnection;
import com.bob.retrofit.okhttp.http.HttpCodec;

import java.io.IOException;

public class StreamAllocation {

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

}
