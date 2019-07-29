package com.bob.test;

import java.nio.charset.Charset;

public final class Utf8 {

    private Utf8() {}

    public static long size(String string) {
        return size(string, 0, string.length());
    }
    public static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    public static void main(String[] args) {
        //　char 在java中是2个字节。java采用unicode，2个字节（16位）来表示一个字符
        // https://maimode.iteye.com/blog/1341354
        Character p = '春'; //\u6625  cmd->输入native2ascii->回车->输入中文->回车
        Character o = '龳';

        // 这里有一个中文 \ud87e\udc09 这个中文需要四个字节 就不能用char表示
        // https://www.qqxiuzi.cn/zh/hanzi-unicode-bianma.php?zfj=jrkz

//        char o = '\u6625';
        //获取系统默认编码
        System.out.println(System.getProperty("file.encoding"));
        //获取系统默认的字符编码
        System.out.println(Charset.defaultCharset());
        System.out.println();
//        byte[] unicodes = charToByte(o);
//        byte[] unicodes = "龳春".getBytes(Charset.forName("unicode"));
        byte[] unicodes = "龳春".getBytes(Charset.forName("utf8"));
        for (byte b : unicodes) {
            //-2,1表示这代表是unicode格式（代表ff,fe)  //UTF-32 与 UTF-16 一样有大尾序和小尾序之别，编码前会放置 U+0000FEFF 或 U+0000FFFE 以区分
            //-97 -77 代表unicode的字符位置
            //1110 0001 1100 0111 unicode  1110 0000 1100 0110  -> 1001 1111 1011 1001 (反码 到 补码 到 原码)
            //1001 0111 1100 0010 1100 0111  -23 -66 -77 utf-8  1001 0110 1100 0001 1100 0110  -> 1110 1001 1011 1110 10 11 1001 (反码 到 补码 到 原码)
            //                                                  -> 1001 11 1110 11 1001 utf-8转unicode 去掉前缀
            //内存读的都是补码
            System.out.println("unicodes ---> " + b) ;
        }
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
