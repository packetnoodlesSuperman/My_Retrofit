package com.bob.retrofit.okhttp.cache;

import com.bob.retrofit.okhttp.Request;
import com.bob.retrofit.okhttp.Response;

import java.io.IOException;

public interface InternalCache {

    //查
    Response get(Request request) throws IOException;

    //增
    CacheRequest put(Response response) throws IOException;

    //删
    void remove(Request request) throws IOException;

    //改
    void update(Response cached, Response network);

    void trackConditionalCacheHit();

    void trackResponse(CacheStrategy cacheStrategy);

}
