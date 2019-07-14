package com.bob.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

public class aa {

    public static void main(String[] args){


//        try {
//            final BufferedSink file = Okio.buffer(Okio.sink(new File("file")));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        BufferedSource bufferedSource = null;
        try {
            File file = new File("file");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            Buffer buffer = new Buffer();
            long read = Okio.buffer(Okio.source(file))
                    .read(buffer, 10000);



            bufferedSource = Okio.buffer(Okio.source(file));

            read(bufferedSource);


//            final BufferedSource finalBufferedSource2 = bufferedSource;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
////                write(file);
//                    read(finalBufferedSource2);
//                }
//            }, "thread---001").start();
//
//
//            final BufferedSource finalBufferedSource = bufferedSource;
//
//
//            final BufferedSource finalBufferedSource1 = bufferedSource;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
////                write(file);
//
//                    read(finalBufferedSource1);
//                }
//            }, "thread--002").start();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

//    public static void write(BufferedSink sink) {
//        String s = "";
//        System.out.print(s);
//        try {
//
//                    sink
//                            .writeUtf8(s)
//                            .writeUtf8("\n")
//                            .writeUtf8(s)
//                            .writeUtf8("\n")
//                            .writeUtf8(s)
//                            .writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//                            .writeUtf8(s).writeUtf8("\n")
//
//                    .close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public static void read(BufferedSource bufferedSource) {
        try {
            long fileTemp = bufferedSource.readAll(
                    Okio.sink(new File("fileTemp"))
            );
            System.out.print("" + Thread.currentThread() + fileTemp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
