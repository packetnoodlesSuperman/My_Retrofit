package com.bob.retrofit;

import java.io.IOException;
import java.lang.reflect.Array;

/**
 * Created by xhb on 2019/7/8.
 */

public abstract class ParameterHandler<T> {

    abstract void apply(RequestBuilder builder, T value) throws IOException;

    final ParameterHandler<Iterable<T>> iterable(){
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
}
