package com.bob.test;

import java.nio.charset.Charset;

public final class Utf8 {

    private Utf8() {}

    public static long size(String string) {
        return size(string, 0, string.length());
    }

    public static void main(String[] args) {
        //　char 在java中是2个字节。java采用unicode，2个字节（16位）来表示一个字符
        // https://maimode.iteye.com/blog/1341354
        char c = '你';
        byte r = (byte ) c;
        System.out.println("r="+r);

        System.out.println("你".toCharArray());
        System.out.println("你".toCharArray().length);
        byte[] bytes = "你".getBytes(Charset.forName("gbk"));
        for (byte b : bytes) {
            System.out.println(b);
        }
          //0b 1b6d3586
        //
        System.out.println(size("你"));
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
         * UTF-8 是一种可变编码格式，长度从一个字节到四个字节
         * UTF-8使用一至六个字节为每个字符编码
         *      （尽管如此，2003年11月UTF-8被RFC 3629重新规范，只能使用原来Unicode定义的区域，U+0000到U+10FFFF，也就是说最多四个字节）：
         *
         * 所以最多四个字节   10ffff
         *
         * 一个字节 0x0000 - 0x007f
         * 两个字节 0x0080 - 0x07ff
         * 三个字节 0x0800 - 0xffff
         *
         * 0x00-0x7F:表示US-ASCII字符,共128个码位，占用1个字节；
         * 0x80-0x7FF:第一个字节由110开始，接着单字节由10开始,共1920个码位,占用2个字节；
         * 0x800-0xD7FF,0xE000-0xFFFF:第一个字节由1110开始，接着的字节由10开始;占用3个字节；
         * 0x10000-0x10FFFF:第一个字节由11110开始，接着的字节由10开始,占用4个字节。
         *
         * 是辅助平面U+D800到U+DFFF之间的码位是永久保留的，不会映射到任何Unicode字符。
         */
        long result = 0;
        for (int i = beginIndex; i < endIndex; ) {
            int c = string.charAt(i);

            if (c < 0x80) { // 0111 0000  一个字节 == 128+64 + 32 = 224
                result++;
                i++;
            } else if (c < 0x800) { // 0000 0111 0000 0000
                result += 2;
                i++;
            } else if  (c < 0xd800 || c > 0xdfff) { //1101 0111 0000 0000  -- 1101 1111 1111 1111
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
