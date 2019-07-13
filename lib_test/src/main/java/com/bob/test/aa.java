package com.bob.test;

import java.io.File;
import java.io.IOException;

import okio.Okio;

public class aa {

    public static void main(String[] args) {
        String s = "任命任命任命任命";
        System.out.print(s);
        try {
            Okio.buffer(Okio.sink(new File("file")))
                    .writeUtf8(s)
                    .close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
