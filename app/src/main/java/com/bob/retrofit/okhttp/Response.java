package com.bob.retrofit.okhttp;

public class Response implements Cloneable {

    final Request request;
    final Protocol protocol;
    final int code;
    final String message;
    final Handshake handshake;
    final Headers headers;
    final ResponseBody body;
    final Response networkResponse;
    final Response cacheResponse;
    final Response priorResponse;
    final long sentRequestAtMillis;
    final long receivedResponseAtMillis;

    private volatile CacheControl cacheControl;

    Response(Builder builder) {

    }

    public static class Builder {
        Request request;
        Protocol protocol;
        int code = -1;
        String message;
        Handshake handshake;
        Headers.Builder headers;
        ResponseBody body;
        Response networkResponse;
        Response cacheResponse;
        Response priorResponse;
        long sentRequestAtMillis;
        long receiveResponseAtMillis;

        public Builder() {
            headers = new Headers.Builder();
        }
    }

}