package com.bob.retrofit.okio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class Okio {
    //私有构造
    private Okio() {}


    /**
     * BufferedSource source = Okio.buffer(Okio.source(in));
     * String s = source.readUtf8();  //以UTF-8读
     * System.out.println(s);     //打印
     * source.close();
     */
    /************* 获取buffer *************/
    public static BufferedSource buffer(Source source) {
        return new RealBufferedSource(source);
    }
    public static BufferedSink buffer(Sink sink) {
        return new RealBufferedSink(sink);
    }

    /**
     * 输出流 封装
     */
    public static Sink sink(OutputStream out) {
        return sink(out, new Timeout());
    }
    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (out == null) throw new IllegalArgumentException("out == null");
        if (timeout == null) throw new IllegalArgumentException("timeou == null");

        return new Sink() {

            @Override
            public void write(Buffer source, long byteCount) throws IOException {

            }

            @Override
            public Timeout timeout() {
                return null;
            }

            @Override
            public void flush() throws IOException {

            }

            @Override
            public void close() throws IOException {

            }
        };
    }



    //输入流封装
    public static Source source(InputStream in) {
        return source(in, new Timeout());
    }
    private static Source source(final InputStream in, final Timeout timeout) {
        if (in == null) throw new IllegalArgumentException("in == null");
        if (timeout == null) throw new IllegalArgumentException("timeout == null");

        return new Source() {

            /**
             * @Desc 读流的操作
             */
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                //读多少字节  byteCount读的字节数量
                if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                if (byteCount == 0) return 0;
                try {
                    //线程是否被中断， 是否超时
                    timeout.throwIfReached();

                    //拿到一个可写的序列空间 //该sink 就是buffer 缓冲区 对Segment的包装
                    Segment tail = sink.writableSegment(1);
                    //tail.limit 为Segment可以写的位置
                    int maxToCopay = (int) Math.min(byteCount, Segment.SIZE - tail.limit);
                    //maxToCopay 表示该可写的Segment的最大可以写的字节数量
                    //通过输入流 将字节写到Segment中 返回写了多少个字节数量
                    int bytesRead = in.read(tail.data, tail.limit, maxToCopay);
                    if (bytesRead == -1) return -1;

                    tail.limit += bytesRead;
                    sink.size += bytesRead;

                    return bytesRead;
                } catch (AssertionError e) {
                    if (isAndroidGetsocknameError(e)) {
                        throw new IOException(e);
                    }
                    throw e;
                }
            }

            @Override
            public Timeout timeout() {
                return null;
            }

            @Override
            public void close() throws IOException {

            }
        };
    }




    static boolean isAndroidGetsocknameError(AssertionError e) {
        return e.getCause() != null && e.getMessage() != null
                && e.getMessage().contains("getsockname failed");
    }

}
