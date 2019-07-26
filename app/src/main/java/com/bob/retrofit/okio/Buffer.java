package com.bob.retrofit.okio;

import android.support.annotation.NonNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public final class Buffer implements BufferedSource, BufferedSink, Cloneable {

    private static final byte[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public Buffer() {}

    //buffer对SegmentPool的使用 buffer的真实存储空间就是 segment
    Segment head;
    long size;

    public long size() {return size;}
    public Buffer buffer() {return this; }

    Segment writableSegment(int minimumCapacity) {
        if (head == null) {
            head = SegmentPool.take();
            return head.next = head.prev = head;
        }
        Segment tail = head.prev;
        return tail;
    }

    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                writeByte((byte)b);
            }

            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                Buffer.this.write(b, off, len);
            }
            @Override public void flush() { }
            @Override public void close() { }
        };
    }

    private Buffer write(byte[] b, int off, int len) {
        int limit = off + len;
        while (off < limit) {
            Segment tail = writableSegment(1);
            int toCopy = Math.min(limit = off, Segment.SIZE - tail.limit);
            System.arraycopy(b, off, tail.data, tail.limit, toCopy);

            off += toCopy;
            tail.limit += toCopy;
        }
        size += len;
        return this;
    }

    private Buffer writeByte(byte b) {
        Segment segment = writableSegment(1);
        segment.data[segment.limit++] = (byte)b;
        size+=1;
        return this;
    }

    /**
     * 只写 int类型的 后面两个字节  因为writeShort
     */
    public Buffer writeShort(int s) {
        //需要两个字节
        Segment tail = writableSegment(2);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((s >>> 8) & 0xff);
        data[limit++] = (byte)  (s        & 0xff);
        tail.limit = limit;
        size += 2;
        return this;
    }

    public Buffer writeShortLe(int s) {
        return writeShort(Util.reverseBytesShort((short) s));
    }

    public long writeAll(Source source) throws IOException {
        if (source == null) throw new IllegalArgumentException("source == null");

        long totalBytesRead = 0;
        for (long readCount; (readCount = source.read(this, Segment.SIZE)) != -1; ) {
            totalBytesRead += readCount;
        }
        return totalBytesRead;
    }




    public String readUtf8() {
        try {
            return readString(size, Util.UTF_8);
        }catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    public String readString(long byteCount, Charset charset) throws EOFException {
        Segment s = head;
        if (s.pos + byteCount > s.limit) {
            return new String(readByteArray(byteCount), charset);
        }
        return null;
    }

    private byte[] readByteArray(long byteCount) throws EOFException {
        if (byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        }
        byte[] result = new byte[(int) byteCount];
        readFully(result);
        return result;
    }

    public void readFully(byte[] sink) throws EOFException {
        int offset = 0;
        while (offset < sink.length) {
            int read = read(sink, offset, sink.length - offset);
            if (read == -1) throw new EOFException();
            offset += read;
        }
    }

    public int read(byte[] sink, int offset, int byteCount) {

        Segment s = head;
        if (s == null) return -1;
        int toCopy = Math.min(byteCount, s.limit - s.pos);
        System.arraycopy(s.data, s.pos, sink, offset, toCopy);

        s.pos += toCopy;
        size -= toCopy;

        if (s.pos == s.limit) {
            head = s.pop();
            SegmentPool.recycle(s);
        }

        return toCopy;
    }
    @Override
    public long read(Buffer sink, long byteCount) throws IOException {

        sink.write(this, byteCount);
        return 0;
    }

    @Override
    public Timeout timeout() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        while (byteCount > 0) {

            if (byteCount < (source.head.limit - source.head.pos)) {

            }

        }
    }

    @Override
    public void flush() throws IOException {

    }



}