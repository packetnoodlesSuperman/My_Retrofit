package com.bob.retrofit.okio;

import java.io.Closeable;
import java.io.IOException;

//输入流  读
public interface Source extends Closeable {

    long read(Buffer sink, long byteCount) throws IOException;

    Timeout timeout();

    @Override void close() throws IOException;

}
