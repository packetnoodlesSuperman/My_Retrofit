package com.bob.retrofit;

import com.bob.retrofit.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by xhb on 2019/7/7.
 */

public class BuiltInConverters extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type,
            Annotation[] annotations,
            Retrofit retrofit) {
        if (type == ResponseBody.class) {
            return Utils.isAnnotationPresent(annotations, Streaming.class) ?
                    StreamingResponseBodyConveter.INSTANCE :
                    BufferingResponseBodyConverter.INSTANCE;
        }
        if (type == Void.class) {
            return VoidResponseBodyConverter.INSTANCE;
        }
        return null;

    }

    static final class VoidResponseBodyConverter implements Converter<ResponseBody, Void> {
        static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();
        @Override
        public Void convert(ResponseBody value) throws IOException {
            value.close();
            return null;
        }
    }

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
