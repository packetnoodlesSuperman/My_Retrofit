package com.bob.retrofit.okio;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

public class GzipSink implements Sink {

    private final BufferedSink sink;

    private final Deflater deflater;

    private final DeflaterSink deflaterSink;

    private boolean closed;

    private final CRC32 crc = new CRC32();

    /**
     * Deflater, Inflater两个工具类
     * 用Deflater与Inflater实现字节的压缩与解压
     *
     * Deflater 压缩工具
     */
    public GzipSink(Sink sink) {
        if(sink == null) throw new IllegalArgumentException("sink == null");

        this.deflater = new Deflater(DEFAULT_COMPRESSION, true);

        //创建了一个新的bufferSink
        this.sink = Okio.buffer(sink);
        this.deflaterSink = new DeflaterSink(this.sink, deflater);

        writeHeader();
    }

    private void writeHeader() {
        Buffer buffer = this.sink.buffer();
        buffer.writeShort(0x1f8b);  // Two-byte Gzip ID.
        buffer.writeByte(0x08);     // 8 == Deflate compression method.
        buffer.writeByte(0x00);     // No flags.
        buffer.writeInt(0x00);      // No modification time.
        buffer.writeByte(0x00);     // No extra flags.
        buffer.writeByte(0x00);     // No OS.
    }

    @Override
    public void write(Buffer source, long byteCount) {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (byteCount == 0) return;

        updateCrc(source, byteCount);
        deflaterSink.write(source, byteCount);
    }

    /**
     * 这个方法首先将链表的数据通过CRC32方法生成一个唯一值，好在读取的时候进行效验，
     * 既验证数据有没有被篡改，篡改了就没法读取了并抛异常
     *
     * 通俗的讲CRC就是目前应用最广泛的一种文件完整性的校验算法
     */
    private void updateCrc(Buffer buffer, long byteCount) throws IOException  {
        for (Segment head = buffer.head; byteCount > 0; head = head.next) {
            int segmentLength = (int) Math.min(byteCount, head.limit - head.pos);

            //进行CRC32数据完整性验证
            crc.update(head.data, head.pos, segmentLength);
            byteCount -= segmentLength;
        }
    }
}
