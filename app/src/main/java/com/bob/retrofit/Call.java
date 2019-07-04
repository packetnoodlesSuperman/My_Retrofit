package com.bob.retrofit;

import java.io.IOException;

import okhttp3.Request;

/**
 * @param <T> 该T就是业务对应解析的对象  也就是后台返回的JS对象
 * @Desc Call<T> 就是Rrofit的Service接口对应的返回类型
 */
public interface Call<T> extends Cloneable {

    Request request();

    Response<T> execute() throws IOException;

    void enqueue(Callback<T> callback);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Call<T> clone();

}
