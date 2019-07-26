package com.bob.retrofit.okio;

public final class Utf8 {

    private Utf8() {}

    public static long size(String string) {
        return size(string, 0, string.length());
    }

    public static void main() {

    }

    public static long size(String string, int beginIndex, int endIndex) {
        if (string == null) throw new IllegalArgumentException("string == null");
        if (beginIndex < 0) throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        }

        //上面都是判断

        /**
         * UTF-8 是一种变长字节编码方式
         *
         * 如果只有一个字节则其最高二进制位为0
         * 如果是多字节，其第一个字节从最高位开始，连续的二进制位值为1的个数决定了其编码的位数，其余各字节均以10开头
         */
        long result = 0;
        for (int i = beginIndex; i < endIndex; ) {
            int c = string.charAt(i);

            if (c < 0x80) { // 0111 0000  一个字节
                result++;
                i++;
            } else if (c < 0x800) { // 0000 0111 0000 0000
                result += 2;
                i++;
            } else if  (c < 0xd800 || c > 0xdfff) { // 0000 0111 0000 0000  -- 0000 1111 1111 1111
                result += 3;
                i++;
            } else {
                result += 4;
                i += 2;
            }
        }
        return result;
    }

}
