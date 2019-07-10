package com.bob.retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import okhttp3.Request;

public class ExecutorCallAdapterFactory extends CallAdapter.Factory {

    final Executor callbackExecutor;

    public ExecutorCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * @Desc 获取Android默认的 CallAdapter
     */
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }

        final Type responseType = Utils.getCallResponseType(returnType);
        return new CallAdapter<Object, Call<?>>() {
            @Override public Type responseType() {
                return responseType;
            }

            @Override public Call<Object> adapt(Call<Object> call) {
                return new ExecutorCallbackCall<>(callbackExecutor, call);
            }
        };
    }

    /**
     * Android默认的转换器
     */
    static final class ExecutorCallbackCall<T> implements Call<T> {
        final Executor callbackExecutor;
        final Call<T> delegate;

        public ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        /**
         * 执行请求
         */
        @Override
        public void enqueue(final Callback<T> callback) {
            delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(final Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (delegate.isCanceled()) {
                                // Emulate OkHttp's behavior of throwing/delivering an IOException on cancellation.
                                callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                            } else {
                                callback.onResponse(ExecutorCallbackCall.this, response);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override public void run() {
                            callback.onFailure(ExecutorCallbackCall.this, t);
                        }
                    });
                }
            });
        }


        /************************* 委托做的事情 不用切换线程 *************************/
        @Override
        public Request request() { return delegate.request(); }

        @Override
        public Response<T> execute() throws IOException { return delegate.execute(); }

        @Override
        public boolean isExecuted() { return delegate.isExecuted(); }

        @Override
        public void cancel() { delegate.cancel(); }

        @Override
        public boolean isCanceled() {    return delegate.isCanceled(); }

        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }
    }
}