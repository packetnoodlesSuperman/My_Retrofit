package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @param <R> 就是后台返回的JS对象 响应对象
 * @param <T>
 */
public interface CallAdapter<R, T> {

    //对Type的认识 https://blog.csdn.net/lkforce/article/details/82466893
    Type responseType();

    T adapt(Call<R> call);

    abstract class Factory {

        public abstract CallAdapter<?, ?> get(Type returnType,
                                           Annotation[] annotations,
                                           Retrofit retrofit);

        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }

    }

}
