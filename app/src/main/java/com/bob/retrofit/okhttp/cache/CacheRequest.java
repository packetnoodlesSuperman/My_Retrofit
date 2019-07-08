package com.bob.retrofit.okhttp.cache;

import java.io.IOException;

import okio.Sink;

public interface CacheRequest {

    Sink body() throws IOException;

    void abort();

}
