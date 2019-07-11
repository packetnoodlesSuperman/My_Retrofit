package com.bob.retrofit;

import java.io.IOException;
import java.lang.reflect.Array;

/**
 * Created by xhb on 2019/7/8.
 * ParameterHandler 是一个抽象泛型类，子类实现对其所有的注解进行解析
 * .@Field @FieldMap  @Body 等等
 *
 * ParameterHandler有三个方法 apply iterable array
 */

public abstract class ParameterHandler<T> {

    // 这个就子类实现注解解析地方。本质就是各个对应的注解值value注入到builder中...
    abstract void apply(RequestBuilder builder, T value) throws IOException;

    // 迭代器 解析
    final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>() {
            @Override
            void apply(RequestBuilder builder, Iterable<T> values) throws IOException {
                if (values == null) {
                    return;
                }

                for (T value : values) {
                    ParameterHandler.this.apply(builder, value);
                }
            }
        };
    }

    // 数组 解析
    final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>() {

            @Override
            void apply(RequestBuilder builder, Object values) throws IOException {
                if (values == null) {
                    return;
                }

                for (int i = 0, size = Array.getLength(values); i < size; i++) {
                    ParameterHandler.this.apply(builder, (T) Array.get(values, i));
                }
            }
        };
    }


    /**************************** 内部类 子类 ****************************/

    //Url的 路径 path
    static final class RelativeUrl extends ParameterHandler<Object> {
        @Override
        void apply(RequestBuilder builder, Object value) throws IOException {
            builder.setRelativeUrl(value);
        }
    }

    //GET上的查询键值对 encoded 表示是否编码
    static final class Query<T> extends ParameterHandler<T> {

        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        public Query(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = name;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if (value == null) {
                return;
            }

            String queryValue = valueConverter.convert(value);
            if (queryValue == null) {
                return;
            }

            builder.addQueryParam(name, queryValue, encoded);
        }
    }

}
