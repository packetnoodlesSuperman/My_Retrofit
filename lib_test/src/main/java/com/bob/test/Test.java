package com.bob.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import okio.BufferedSink;
import okio.Okio;

public class Test {

    public static void main(String[] args) {
        File file = new File("file");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




        String s = null;
        try {
            s = Okio.buffer(Okio.source(file))
                    .readByteString()
                    .utf8()
                    .toString();
//                    .readUtf8();
        } catch (IOException e) {

        }

        System.out.print(s);

        try {
            Okio.buffer(Okio.sink(file))
//                .writeString("我是每二条\n", Charset.forName("GBK"))
//                .writeString("我是每二条", Charset.forName("utf-8"))
//                .write("我是汉字字串\n".getBytes())
//                    .writeUtf8("我是汉字字串")
                    .writeUtf8(s+s+s+s)
//                    .writeString("我是汉字字串", Charset.forName("GBK"))
//                    .writeString("abcdefghijkl$#\n", Charset.forName("GBK"))
//                    .writeUtf8("abcdefghijkl$#\n")
//                    .writeString("我是汉字字串\n", Charset.forName("UTF-16"))
//                    .writeString("我是汉字字串\n", Charset.forName("UTF-32"))
//                    .writeString("你好", Charset.forName("utf-8"))
                    .close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
