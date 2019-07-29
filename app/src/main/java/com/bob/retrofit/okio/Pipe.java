package com.bob.retrofit.okio;

import android.graphics.ImageDecoder;

import java.io.IOException;

public class Pipe {

    final long maxBufferSize;
    final Buffer buffer = new Buffer();

    private final Source source = new PipeSource();
    boolean sourceClosed;

    public Pipe(int maxBufferSize) {
        if (maxBufferSize < 1L) {
            throw new IllegalArgumentException("maxBufferSize < 1: " + maxBufferSize);
        }
        this.maxBufferSize = maxBufferSize;
    }
    public Source source() {
        return source;
    }


    final class PipeSource implements Source {

        final Timeout timeout = new Timeout();

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            synchronized (buffer) {
                if (sourceClosed) throw new IllegalStateException("closed");

                while (buffer.size() == 0) {
                    if (sinkClosed) return -1L;
                    timeout.waitUntilNotified(buffer); // Wait until the sink fills the buffer.
                }

                long result = buffer.read(sink, byteCount);
                buffer.notifyAll(); // Notify the sink that it can resume writing.
                return result;
            }
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }

        @Override
        public void close() throws IOException {
            synchronized (buffer) {
                sourceClosed = true;
                buffer.notifyAll(); // Notify the sink that no more bytes are desired.
            }
        }
    }
}
