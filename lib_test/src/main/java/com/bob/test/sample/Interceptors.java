package com.bob.test.sample;

import java.io.IOException;
import java.util.Random;

import okio.Buffer;
import okio.ForwardingSink;
import okio.Sink;

public class Interceptors {

    public void run() throws Exception {

        final byte cipher = (byte) (new Random().nextInt(256) - 128);
        System.out.println("Cipher :" + cipher);

        Buffer wire = new Buffer();

        Sink sink = new InterceptingSink(wire) {
            @Override
            protected void intercept(byte[] data, int offset, int length) throws IOException {

            }
        };

    }

    abstract class InterceptingSink extends ForwardingSink {

        private final Buffer.UnsafeCursor cursor = new Buffer.UnsafeCursor();

        public InterceptingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (source.size() < byteCount) {
                throw new IllegalArgumentException("size=" + source.size() + " byteCount=" + byteCount);
            }
            if (byteCount == 0) return;

            source.readUnsafe(cursor);
            try {
                long remaining = byteCount;
                for (int length = cursor.seek(0); remaining > 0 && length > 0; length = cursor.next()) {
                    int toIntercept = (int) Math.min(length, remaining);
                    intercept(cursor.data, cursor.start, toIntercept);

                    remaining -= toIntercept;
                }
            } finally {
                cursor.close();
            }

            super.write(source, byteCount);
        }

        protected abstract void intercept(byte[] data, int offset, int length) throws IOException;
    }


    public static void main(String... args) throws Exception {
        new Interceptors().run();
    }

}
