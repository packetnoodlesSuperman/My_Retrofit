package com.bob.retrofit.okio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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

            //将数据写到buffer里面去
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                while (byteCount > 0) {
                    timeout.throwIfReached();

                    //buffer都有一个Segment链表
                    Segment head = source.head;
                    //Sink接口的目的,就是将存在于内存Buffer对象中的数据,write到输出流中
                    //那么复制多少呢？
                    int toCopy = (int) Math.min(byteCount, head.limit - head.pos);
                    //将Segment可以写的数据 放到OutputStream流中， 然后可以写到文件上
                    //toCopy表示步长
                    out.write(head.data, head.pos, toCopy);

                    head.pos += toCopy;

                    //byteCount只是一个基本类型 值不能传递的呀？
                    byteCount -= toCopy;
                    source.size -= toCopy;

                    if (head.pos == head.limit) {
                        //Segment断开与前驱和后继的联系
                        source.head = head.pop();
                        //SegmentPool 回收Segment 放进池子里
                        SegmentPool.recycle(head);
                    }
                }
            }

            @Override
            public Timeout timeout() {
                return timeout;
            }

            @Override
            public void flush() throws IOException {
                //其实就是OutputStream的委托类
                out.flush();
            }

            @Override
            public void close() throws IOException {
                //其实就是OutputStream的委托类
                out.close();
            }
        };
    }


    //socket
    public static Sink sink(Socket socket) throws IOException {
        if (socket == null) throw new NullPointerException("socket == null");

        AsyncTimeout timeout = timeout(socket);
        Sink sink = sink(socket.getOutputStream(), timeout);
        return timeout.sink(sink);
    }

    private static AsyncTimeout timeout(Socket socket) {
        return new AsyncTimeout(){


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
                    int maxToCopy = (int) Math.min(byteCount, Segment.SIZE - tail.limit);
                    //maxToCopy 表示该可写的Segment的最大可以写的字节数量
                    //通过输入流 将字节写到Segment中 返回写了多少个字节数量
                    //从limit开始可以写， maxToCopy 表示步长
                    int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
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
                in.close();
            }
        };
    }

    public static Source source(File file) throws FileNotFoundException {
        return source(new FileInputStream(file));
    }







    static boolean isAndroidGetsocknameError(AssertionError e) {
        return e.getCause() != null && e.getMessage() != null
                && e.getMessage().contains("getsockname failed");
    }

}
