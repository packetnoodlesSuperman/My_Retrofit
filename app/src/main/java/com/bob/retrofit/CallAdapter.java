package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @param <R> 就是后台返回的JS对象 响应对象 泛型R是response的Body
 * @param <T>
 *
 * 我们知道Retrofit支持RxJava 那么如果RxJava是需要转化成一个Retrofit的Call<T>
 * 则需要一个适配器 把一个RxJava的Observable适配成一个Retrofit的Call<T>
 * 所以设计这个类的主要目的就是适配让Retrofit的Call<T>对业务层的请求适配
 *
 * 适配器就是将 R 转换成 T
 */
public interface CallAdapter<R, T> {

    //对Type的认识 https://blog.csdn.net/lkforce/article/details/82466893
    Type responseType();

    T adapt(Call<R> call);

    /**
     * @Desc CallAdapter的工厂
     */
    abstract class Factory {

        public abstract CallAdapter<?, ?> get(Type returnType,
                                           Annotation[] annotations,
                                           Retrofit retrofit);

        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }

    }

}
