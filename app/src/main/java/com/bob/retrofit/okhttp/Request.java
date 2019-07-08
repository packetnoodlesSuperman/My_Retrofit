package com.bob.retrofit.okhttp;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * @Desc OkHttp的请求
 */
public class Request {
    //请求的地址
    final HttpUrl url;
    //请求的方法， GET， POST等
    final String method;
    //请求头
    final Headers headers;
    //请求的实体，只有POST，PUT，PATCH这里请求有
    final @Nullable RequestBody body;
    //标示一个请求的
    final Object tag;
    //缓存控制，懒加载，也就是说在需要的时候才初始化
    private volatile CacheControl cacheControl;

    public Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tag = builder.tag != null ? builder.tag : this;
    }

    public HttpUrl url() { return url; }
    public String method() { return method; }
    public Headers headers() { return headers; }
    public String header(String name) { return headers.get(name); }
    public List<String> headers(String name) {
        return headers.values(name);
    }
    public @Nullable RequestBody body() {
        return body;
    }
    public Object tag() {
        return tag;
    }
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return result != null ? result : (cacheControl = CacheControl.parse(headers));
    }
    public boolean isHttps() {
        return url.isHttps();
    }
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * 构造器
     */
    public static class Builder {
        HttpUrl url;
        String method;
        Headers.Builder headers;
        Request.Builder headers;
        RequestBody body;
        Object tag;

        /**
         * @Desc 默认的方法为 ---> GET
         */
        public Builder() {
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

    }

}
