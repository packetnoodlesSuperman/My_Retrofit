package com.bob.retrofit.okhttp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import okio.BufferedSource;

public abstract class ResponseBody implements Closeable{

    private Reader reader;

    public abstract MediaType contentType();

    public abstract long contentLength();

    public final InputStream byteStream() {
        return source().inputStream();
    }

    public abstract BufferedSource source();

    @Override public void close() {
        Util.closeQuietly(source());
    }


    /**
     * @Desc 创建ResponseBody
     */
    public static ResponseBody create(
            final MediaType contentType,
            final long contentLength,
            final BufferedSource content
    ) {
        if (content == null) {
            throw new NullPointerException("source == null");
        }
        return new ResponseBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public BufferedSource source() {
                return content;
            }
        };
    }

}
