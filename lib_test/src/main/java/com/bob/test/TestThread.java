package com.bob.test;

public class TestThread {
    public static void main(String[] args) {
        final Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (TestThread.class) {
                    try {
                        Thread.sleep(5000L);
                        TestThread.class.wait(1000L);
                    } catch (InterruptedException ignore) {
                        //释放资源或者做其他的事情
                    }
                    System.out.println("aaaaaaaaaaaaaaa");
                }
            }
        });
        thread1.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (TestThread.class) {
                    System.out.println("bbbbbbbbbbb");
                    TestThread.class.notify();
                    //或者thread1.interrupt();
                }
            }
        }).start();
    }
}