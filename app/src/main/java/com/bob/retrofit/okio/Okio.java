package com.bob.retrofit.okio;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;



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
    //获取 有buffer<缓存区>的包装流  RealBufferedSource和RealBufferedSink 内部都有一个buffer
    public static BufferedSource buffer(Source source) {
        return new RealBufferedSource(source);
    }
    public static BufferedSink buffer(Sink sink) {
        return new RealBufferedSink(sink);
    }

    /**
     * 输出流 封装 写
     * 创建一个Sink 用于封装OutputStream流
     */
    public static Sink sink(OutputStream out) {
        return sink(out, new Timeout());
    }
    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (out == null) throw new IllegalArgumentException("out == null");
        if (timeout == null) throw new IllegalArgumentException("timeout == null");

        /**
         * 对Sink的认识
         * 写的功能 通过Buffer缓存到的数据 委托给OutputStream写
         * 刷新和关闭 也是OutputStream的事  谁让写的事是OutputStream做的呢！！
         */
        return new Sink() {
            /**
             * 调用写的方法的时候 会将缓存区的buffer传递过来
             */
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                //checkOffsetAndCount(source.size, 0, byteCount); 边界检查
                while (byteCount > 0) {
                    timeout.throwIfReached();
                    //获取buffer的内部双链表的 头部
                    Segment head = source.head;
                    //limit代表可以写的地方， post代表可以读的地方
                    //limit - pos 就是Segment已经写上的数据的区域
                    int toCopy = (int) Math.min(byteCount, head.limit - head.pos);

                    //真正的输出流在写 可能是FileOutputStream  获取其他Java的输出流实现类
                    out.write(head.data, head.pos, toCopy);

                    //接下来你对Segment进行回收， 已经buffer的双链表进行调整
                    head.pos += toCopy;     //这个Segment被你读过了 读过的数据被OutputStream写走了
                    byteCount -= toCopy;
                    source.size -= toCopy;

                    if (head.pos == head.limit) {
                        //buffer的头索引 指向下一个
                        source.head = head.pop();
                        SegmentPool.recycle(head);
                    }
                }
            }

            @Override
            public Timeout timeout() { return timeout; }
            @Override
            public void flush() throws IOException { out.flush(); }
            @Override
            public void close() throws IOException { out.close(); }
        };
    }


    //输入流封装
    public static Source source(InputStream in) { return source(in, new Timeout()); }
    private static Source source(final InputStream in, final Timeout timeout) {
        if (in == null) throw new IllegalArgumentException("in == null");
        if (timeout == null) throw new IllegalArgumentException("timeout == null");

        return new Source() {

            /**
             * 读流的操作  将数据写在buffer缓存区里面
             * 写到buffer缓存区里面  就是读到了内存里
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
                    if (isAndroidGetsocknameError(e)) { throw new IOException(e); }
                    throw e;
                }
            }

            @Override
            public Timeout timeout() {
                return timeout;
            }
            @Override
            public void close() throws IOException { }
        };
    }


    /****************************** 其他 ******************************/
    public static Sink sink(Socket socket) throws IOException {
        if (socket == null) throw new IllegalArgumentException("socket == null");
        if (socket.getOutputStream() == null) throw new IOException("socket's output stream == null");

        AsyncTimeout timeout = timeout(socket);
        Sink sink = sink(socket.getOutputStream(), timeout);
        return timeout.sink(sink);
    }

    private static AsyncTimeout timeout(final Socket socket) {
        return new AsyncTimeout() {
            @Override
            protected IOException newTimeoutException(@Nullable IOException cause) {
                InterruptedIOException ioe = new SocketTimeoutException("timeout");
                if (cause != null) {
                    ioe.initCause(cause);
                }
                return ioe;
            }

            @Override
            protected void timedOut() {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    static boolean isAndroidGetsocknameError(AssertionError e) {
        return e.getCause() != null && e.getMessage() != null
                && e.getMessage().contains("getsockname failed");
    }

}