package com.bob.retrofit.okio;

import com.bob.retrofit.Body;

public final class Segment {

    static final int SIZE = 8192;

    static final int SHARE_MINIMUM = 1024;

    Segment next;

    Segment prev;
    //代表该片段是否共享
    boolean shared;
    //代表自己是否可以操作本片段（与shared互斥）
    boolean owner;
    //pos代表开始可以读的字节序号
    int limit;
    //limit代表可以写的字节序号
    int pos;

    final byte[] data;

    //默认的构造
    Segment() {
        this.data = new byte[SIZE];
        this.owner = true;
        this.shared = false;
    }

    Segment(byte[] data, int pos, int limit, boolean shared, boolean owner) {
        this.data = data;
        this.pos = pos;
        this.limit = limit;
        this.shared = shared;
        this.owner = owner;
    }

    Segment sharedCopy() {
        shared = true;
        return new Segment(data, pos, limit, true, false);
    }

    Segment unsharedCopy() {
        return new Segment(data.clone(), pos, limit, false, true);
    }

    public Segment pop() {
        Segment result = next != this ? next : null;
        prev.next = next;
        next.prev = prev;
        next = null;
        prev = null;
        return result;
    }

    public Segment push(Segment segment){
        segment.prev = this;
        segment.next = next;
        next.prev = segment;
        next = segment;
        return segment;
    }

    /**
     * public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);
     * src 源数据
     * srcPos 源数组中的起始位置
     * dest 目标数组
     * destPos 目标数组的起始位置
     * length 要复制数组元素的数量
     *
     * 该方法是用了 native 关键字，调用的为 C++ 编写的底层函数，可见其为 JDK 中的底层函数
     */
    //往sink部分写入this（Segment）长度为byteCount的数据
    public void writeTo(Segment sink, int byteCount) {
        if (!sink.owner) throw new IllegalArgumentException();

        if (sink.limit + byteCount > SIZE) {
            if (sink.shared) throw new IllegalArgumentException();
            if (sink.limit + byteCount - sink.pos > SIZE) throw new IllegalArgumentException();
            //先将sink原本存储的数据往前移，pos设为1
            System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
            sink.limit -= sink.pos;
            sink.pos = 0;
        }
        //将this（Segment）数据写入sink中，从limit开始
        System.arraycopy(data, pos, sink.data, sink.limit, byteCount);
        sink.limit += byteCount;
        pos += byteCount;
    }

}
