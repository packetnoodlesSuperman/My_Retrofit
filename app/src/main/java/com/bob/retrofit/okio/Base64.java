package com.bob.retrofit.okio;

public class Base64 {

    private Base64() {}

    //解析base64 字符串
    //Base64是一种用64个字符来表示任意二进制数据的方法 [a-z, A-Z, 0-9, +, -] 64个字符
    //Base64编码会把3字节的二进制数据编码为4字节的文本数据，长度增加33%  一个字符占6bit
    //Base64用\x00字节在末尾补足后，再在编码的末尾加上1个或2个=号，表示补了多少字节，解码的时候，会自动去掉
    public static byte[] decode(String in) {
        int limit = in.length();
        for (; limit > 0; limit--) {
            char c = in.charAt(limit - 1);
            //去掉换行 空字符 已经base64 添加的=号
            if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
                break;
            }
        }

        byte[] out = new byte[(int)(limit * 6L / 8L)];

        return null;
    }

    private static final byte[] MAP = new byte[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     *  https://blog.csdn.net/fengbingchun/article/details/85016088
     * 标准的Base64并不适合直接放在URL里传输，因为URL编码器会把标准Base64中的/和+分别改成了-和_
     */
    private static final byte[] URL_MAP = new byte[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '-', '_'
    };

}