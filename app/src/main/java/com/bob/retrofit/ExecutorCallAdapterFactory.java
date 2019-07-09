package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

public class ExecutorCallAdapterFactory extends CallAdapter.Factory {

    final Executor callbackExecutor;

    public ExecutorCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }

        return null;
    }
}
