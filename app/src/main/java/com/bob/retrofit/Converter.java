package com.bob.retrofit;

import android.support.annotation.Nullable;

import com.bob.retrofit.okhttp.RequestBody;
import com.bob.retrofit.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 在Http请求中实现对象的转化， 将F 转化成 T
 * 在Retrofit使用的时候 是直接添加工厂类
 */
public interface static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);Converter<F, T> {

    T convert(F value) throws IOException;

    /**
     * Converter的工厂类
     * 有两个 responseBodyConverter 和 requestBodyConverter
     */
    abstract class Factory {

        /**
         * 将响应体转换的转换器  ResponseBody --> T
         */
        public Converter<ResponseBody, ?> responseBodyConverter(
                Type type,
                Annotation[] annotations,
                Retrofit retrofit
        ) {
            return null;
        }

        /**
         * 将请求体转换的转换器  T --> RequestBody
         */
        public @Nullable Converter<?, RequestBody> requestBodyConverter(
                Type type,
                Annotation[] parameterAnnotations,
                Annotation[] methodAnnotations,
                Retrofit retrofit) {
            return null;
        }


        public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return null;
        }

        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
            return Utils.getParameterUpperBound(index, type);
        }

        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }
    }

}
