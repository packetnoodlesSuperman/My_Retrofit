package com.bob.retrofit.okio;

import java.io.IOException;

public final class RealBufferedSource implements BufferedSource {

    public final Buffer buffer = new Buffer();

    public final Source source;

    public RealBufferedSource(Source source) {
        this.source = source;
    }

    public String readUtf8() throws IOException {
        //全部写到该 buffer的 segment链表中
        buffer.writeAll(source);
        return buffer.readUtf8();
    }



    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        return 0;
    }

    @Override
    public Timeout timeout() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
