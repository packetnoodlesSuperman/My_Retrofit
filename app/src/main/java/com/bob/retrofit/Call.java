package com.bob.retrofit;

import java.io.IOException;

import okhttp3.Request;

/**
 * @param <T> 该T就是业务对应解析的对象  也就是后台返回的JS对象 泛型T是response的Body
 * @Desc Call<T> 就是Rrofit的Service接口对应的返回类型
 *
 * 这个Call模拟一个客户端发起请求到服务器，然后服务器响应数据到客户端的整个流程
 * 通过这个Call 我们可以获取相应的请求和响应的数据
 * 这个Call既有同步请求 又有异步请求  可取消请求  可复制请求
 *
 * 特别的 这里的request() 返回OkHttp的Request说明指明底层的请求只能使用OkHttp
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
