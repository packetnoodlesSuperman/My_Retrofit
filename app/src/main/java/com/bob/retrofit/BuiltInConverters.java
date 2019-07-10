package com.bob.retrofit;

import android.support.annotation.Nullable;

import com.bob.retrofit.okhttp.RequestBody;
import com.bob.retrofit.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @Desc 默认转换器
 */
public class BuiltInConverters extends Converter.Factory {

    /**
     * responseBody(响应格式)只支持 OkHttp的ResponseBody格式 以及 Void格式
     */
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type,
            Annotation[] annotations,
            Retrofit retrofit) {
        if (type == ResponseBody.class) {
            return Utils.isAnnotationPresent(annotations, Streaming.class) ?
                    StreamingResponseBodyConverter.INSTANCE :
                    BufferingResponseBodyConverter.INSTANCE;
        }
        if (type == Void.class) {
            return VoidResponseBodyConverter.INSTANCE;
        }
        return null;
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (RequestBody.class.isAssignableFrom(Utils.getRawType(type))) {
            return RequestBodyConverter.INSTANCE;
        }
        return null;
    }

    /************************* 适配器内部类 具体类型转换 *************************/

    /**
     * @Desc RequestBody为RequestBody类型
     */
    static final class RequestBodyConverter implements Converter<RequestBody, RequestBody> {
        static final RequestBodyConverter INSTANCE = new RequestBodyConverter();
        @Override public RequestBody convert(RequestBody value) {
            return value;
        }
    }

    /**
     * @Desc ResponseBody为Void类型
     */
    static final class VoidResponseBodyConverter implements Converter<ResponseBody, Void> {
        static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();
        @Override
        public Void convert(ResponseBody value) throws IOException {
            value.close();
            return null;
        }
    }

    /**
     * @Desc ResponseBody为ResponseBody类型
     */
    static final class BufferingResponseBodyConverter implements Converter<ResponseBody, ResponseBody> {
        static final BufferingResponseBodyConverter INSTANCE = new BufferingResponseBodyConverter();
        @Override
        public ResponseBody convert(ResponseBody value) throws IOException {
            try {
                return Utils.buffer(value);
            } finally {
                value.close();
            }
        }
    }

    /**
     * @Desc ResponseBody为ResponseBody类型
     */
    static final class StreamingResponseBodyConverter implements Converter<ResponseBody, ResponseBody> {
        static final StreamingResponseBodyConverter INSTANCE = new StreamingResponseBodyConverter();
        @Override
        public ResponseBody convert(ResponseBody value) {
            return value;
        }
    }

    /**
     * @Desc Object转换成String类型 其实就是打印出toString方法
     */
    static final class ToStringConverter implements Converter<Object, String> {
        static final ToStringConverter INSTANCE = new ToStringConverter();
        @Override
        public String convert(Object value) throws IOException {
            return value.toString();
        }
    }

}
