package com.bob.retrofit;

import com.bob.retrofit.okhttp.OkHttpClient;
import com.bob.retrofit.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import okio.Buffer;

public final class Utils {

    public static <T> void validateServiceInterface(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        //不能继承
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
        }
    }

    /**
     * 看doc的type体系
     * @param type CallAdapter传入方法上的返回Type类型 returnType 比如 Call<Object>
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalArgumentException();
            }
            return (Class<?>) rawType;
        }
        if (type instanceof TypeVariable) {
            return Object.class;
        }
        return null;
    }

    static Type getCallResponseType(Type returnType) {
        return getParameterUpperBound(0, (ParameterizedType) returnType);
    }

    /**
     * @param index
     * @param type ParameterizedType表示参数化类型
     */
    static Type getParameterUpperBound(int index, ParameterizedType type) {
        //获取泛型中的实际类型，可能会存在多个泛型，例如Map<K,V>,所以会返回Type[]数组
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException(
                    "Index " + index + " not in range [0," + types.length + ") for " + type);
        }

        Type paramType = types[index];
        //当需要描述的类型是泛型类，而且泛型类中的泛型被定义为(? extends xxx)或者(? super xxx)这种类型
        //如果Call<T> 中的T是 (? extends xxx) 或者(? super xxx) 则responseType返回 getUpperBounds[0]类型 0代表的是继承 后面的都是接口
        if (paramType instanceof WildcardType) {

            //WildcardType接口有getUpperBounds()方法，得到的是类型的上边界的Type数组，实际上就是类型的直接父类，也就是extends后面的类型
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }

    public static boolean isAnnotationPresent(
            Annotation[] annotations, Class<? extends Annotation> clazz) {
       for (Annotation annotation : annotations) {
           if (clazz.isInstance(annotation)) {
               return true;
           }
       }
        return false;
    }

    public static ResponseBody buffer(ResponseBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.source().readAll(buffer);
        return ResponseBody.create(body.contentType(), body.contentLength(), buffer);
    }

    public static <T> T checkNotNull(T  object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}
