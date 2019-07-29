package com.bob.retrofit.okio;

import java.nio.charset.Charset;

public final class Util {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static short reverseBytesShort(short s) {
        int i = s & 0xffff;
        int resersed = (i & 0xff00) >>> 8 | (i & 0xff) << 8;
        return (short)resersed;
    }

    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0 || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("size=%s offset=%s byteCount=%s", size, offset, byteCount));
        }
    }

}
