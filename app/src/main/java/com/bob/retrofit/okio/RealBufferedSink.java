package com.bob.retrofit.okio;

import java.io.IOException;

public final class RealBufferedSink implements BufferedSink{

    public final Buffer buffer = new Buffer();

    public final Sink sink;

    public RealBufferedSink(Sink sink) {
        this.sink = sink;
    }

    @Override
    public void write() {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }
}
