package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * Created by xhb on 2019/7/7.
 */

public class BuiltInConverters extends Converter.Factory {


    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type,
            Annotation[] annotations,
            Retrofit retrofit) {
        if (type == ResponseBody.class) {
            return Utils.isAnnotationPresent(annotations, Streaming.class) ?
                    StreamingResponseBodyConveter.INSTANCE :

        }

    }

    static final class BufferingResponseBodyConverter implements Converter<ResponseBody, ResponseBody>

}
