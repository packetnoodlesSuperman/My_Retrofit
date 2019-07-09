package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @Desc 对应的返回Class类型是 Call
 */
public class DefaultCallAdapterFactory extends CallAdapter.Factory {

    static final CallAdapter.Factory INSTANCE = new DefaultCallAdapterFactory();

    /**
     * @param returnType 方法返回值的type类型
     * @param annotations 方法上的注解
     * @param retrofit retrofit
     */
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }
        final Type responseType = Utils.getCallResponseType(returnType);
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return call;
            }
        };
    }
}
