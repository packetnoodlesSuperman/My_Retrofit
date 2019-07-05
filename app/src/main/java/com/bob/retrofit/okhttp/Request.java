package com.bob.retrofit.okhttp;

import android.support.annotation.Nullable;

public class Request {

    final HttpUrl url;
    final String method;
    final Headers headers;
    final @Nullable RequestBody body;
    final Object tag;
    private volatile CacheControl cacheControl;

    public Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
    }

    public HttpUrl url() {
        return url;
    }

    public String method() {
        return method;
    }

    public static class Builder {
        HttpUrl url;
        String method;
        Headers.Builder headers;

        /**
         * @Desc 默认的方法为 ---> GET
         */
        public Builder() {
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

    }

}
