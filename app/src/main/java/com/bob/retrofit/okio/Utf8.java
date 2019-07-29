package com.bob.retrofit.okio;

/**
 * unicode 的了解可以看 test/UTF8 文件
 *
 * UTF-8 是 Unicode 的实现方式之一  https://blog.csdn.net/qq_40829288/article/details/91865214
 *
 *  JAVAC是以系统默认编码读入源文件，然后按UNICODE进行编码的 (具体说是UTF-16编码)
 *  在JAVA运行的时候，JAVA也是采用UNICODE编码的，并且默认输入和输出的都是操作系统的默认编码
 *
 *  也就是说在new String(bytes[,encode])中，系统认为输入的是编码为encode的字节流
 *  它还是要从这个encode转换成Unicode，也就是说有bytes-->encode字符-->Unicode字符的转换
 *  而在String.getBytes([encode])中，系统要做一个Unicode字符-->encode字符-->bytes的转换。
 *
 *  我们通常说的Unicode是一个字符集，在这个字符集中每个字符都有对应的唯一十六进制值。
 *  Unicdoe字符集包含了全球所有的字符，所以它的体积较为庞大，如此便分为了17个平面。
 *  17个平面中第一个平面为基本平面（BMP）,剩下的16个为辅助平面（SMP）。
 *  基本平面的字符对应十六进制值的区域为0x0000~0xFFFF，辅助平面中的字符对应十六进制值的区域为0x010000~0x10FFFF
 *
 *  根据Unicode定义，总共有1,114,112个代码点，编号从0x0到0x10FFFF 大概110多万个字符
 *  Unicode 只是一个符号集，它只规定了符号的二进制代码，却没有规定这个二进制代码应该如何存储。
 *
 *
 *  我的理解
 *  1. java默认使用的是unicode编码 比如char 就是两个字节  而unicode的BMP（基本多文种平面）
 *      使用的是16bit 两个字节表示 65536个编码，其中有近39000种已被定义完成，而中国字就占了21000种
 *  2.
 */
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
         * UTF-8 是一种变长字节编码方式 http://www.ruanyifeng.com/blog/2007/10/ascii_unicode_and_utf-8.html
         *
         * 如果只有一个字节则其最高二进制位为0
         * 如果是多字节，其第一个字节从最高位开始，连续的二进制位值为1的个数决定了其编码的位数，其余各字节均以10开头
         * 如三个字节  则为 1110 xxxx 10 xxxxxx 10 xxxxxx
         *
         * UTF-16实质是一种重新编码计算的方式，是依附于Unicode字符集的。以Unicode字符集为参考基础，
         * 对其中的字符所对应的十六进制值进行重新计算获取一个新的十六进制值。如果我们对Unicode字符集中的所有字符都进行了UTF-16编码，
         * 那获得的值组合起来就可以说是一个UTF-16字符集了。
         */
        long result = 0;
        for (int i = beginIndex; i < endIndex; ) {
            int c = string.charAt(i);

            /**
             * Unicode十六进制(高位0都去掉)          Unicode二进制(高位0都去掉)            UTF-8二进制(其中的 x 由前面的Unicode二进制值来决定)
             *  0x0 ~ 0x7F                              0 ~ 1111111                                    0xxxxxxx
             *  0x80 ~ 0x7FF                        10000000 ~ 111 11111111                        110xxxxx 10xxxxxx
             *  0x800 ~ 0xFFFF                      1000 00000000 ~ 11111111 11111111           1110xxxx 10xxxxxx 10xxxxxx
             *  0x10000 ~ 0x10FFFF           1 00000000 00000000 ~ 10000 11111111 11111111     11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
             */
            if (c < 0x80) {         //0000 0000 -- 0000 007F (0-127)  一个字节
                //对于单字节的符号，字节的第一位设为0，后面7位为这个符号的 Unicode 码。因此对于英语字母，UTF-8 编码和 ASCII 码是相同的。
                result++;
                i++;
            } else if (c < 0x800) { // 0000 0080 -- 0000 07FF (128-2047) 两个字节
                //第一个字节由110开始，接着单字节由10开始,共1920个码位,占用2个字节；
                //110 00000 10 000000 - 110 11111 10 111111
                result += 2;
                i++;
            } else if  (c < 0xd800 || c > 0xdfff) { // 00x800 -- 0xD7FF + 0xE000 -- 0xFFFF 三个字节
                // 1101 0111 0000 0000 - 1101 1111 1111 1111

                //研究一下UTF-8三字字节表示的范围吧
                //1110<前缀 表示三个字节 第n+1=4 必须为零> 0000  10<后面的字节用10表示> 000000 10 000000
                //可得出最小表示二进制 去掉前缀 0000 000000 000000 但是用三个字节表示0确实意义不大 所以 800表示最小
                // 0000 0100 0000 0000  -> 0x800      1101 0111 0000 0000 --> 0xd800 转化UTF-8  为  1110 1101 1001 1100 1000 0000
                result += 3;
                i++;
            } else {    //0001 0000 -- 0010 FFFF (65536-1050623)    四个字节
                result += 4;
                i += 2;
            }
        }
        return result;
    }

}
