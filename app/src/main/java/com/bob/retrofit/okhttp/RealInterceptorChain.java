package com.bob.retrofit.okhttp;

import com.bob.retrofit.okhttp.connection.RealConnection;
import com.bob.retrofit.okhttp.http.HttpCodec;

import java.io.IOException;
import java.util.List;

public final class RealInterceptorChain implements Interceptor.Chain{

    //所有的拦截器
    private final List<Interceptor> interceptors;
    private final StreamAllocation streamAllocation;
    private final HttpCodec httpCodec;
    private final RealConnection connection;
    private final int index;
    private final Request request;
    private final Call call;
    private final EventListener eventListener;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;

    private int calls;

    public RealInterceptorChain(List<Interceptor> interceptors, StreamAllocation streamAllocation, HttpCodec httpCodec, RealConnection connection, int index, Request request, Call call, EventListener eventListener, int connectTimeout, int readTimeout, int writeTimeout) {
        this.interceptors = interceptors;
        this.streamAllocation = streamAllocation;
        this.httpCodec = httpCodec;
        this.connection = connection;
        this.index = index;
        this.request = request;
        this.call = call;
        this.eventListener = eventListener;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
    }

    @Override
    public Request request() {
        return request;
    }

    public StreamAllocation streamAllocation() {
        return streamAllocation;
    }

    @Override
    public Response proceed(Request request) throws IOException {
        return proceed(request, streamAllocation, httpCodec, connection);
    }

    public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec httpCodec, RealConnection connection) throws IOException {
        if (index >= interceptors.size()) {
            throw new AssertionError();
        }

        calls++;

        if (this.httpCodec != null && !this.connection.supportsUrl(request.url())) {
            throw new IllegalStateException();
        }
        RealInterceptorChain next = new RealInterceptorChain(interceptors, streamAllocation, httpCodec,
                connection, index + 1, request, call, eventListener, connectTimeout, readTimeout,
                writeTimeout);
        Interceptor interceptor = interceptors.get(index);
        Response response = interceptor.intercept(next);

        return response;
    }

    public HttpCodec httpStream() {
        return httpCodec;
    }

    public Call call() {
        return null;
    }

    public EventListener eventListener() {
        return null;
    }
}
