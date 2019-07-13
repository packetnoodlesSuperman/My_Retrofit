package com.bob.retrofit.okio;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

//输出流  写
public interface Sink extends Closeable, Flushable {

    void write(Buffer source, long byteCount) throws IOException;

    Timeout timeout();

    @Override void flush() throws IOException;

    @Override void close() throws IOException;

}
