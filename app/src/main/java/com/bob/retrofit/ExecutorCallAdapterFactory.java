package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

public class ExecutorCallAdapterFactory extends CallAdapter.Factory {


    public ExecutorCallAdapterFactory(Executor callbackExecutor) {

    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return null;
    }
}
