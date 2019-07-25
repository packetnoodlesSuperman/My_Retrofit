package com.bob.retrofit.okio;

import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * AsyncTimeout的超时判断是异步的
 * AsyncTimeout 自身 采用了双向链表（Queue）的结构排序，内部使用了一个线程来处理链表，判断是否超时
 * Okio 主要在对 Socket 读写时使用到 AsyncTimout 类
 */
public class AsyncTimeout extends Timeout {

    private static final int TIMEOUT_WRITE_SIZE = 64*1024;

    //60秒 转 毫秒
    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    //60秒 转 纳秒
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);

    static AsyncTimeout head;
    private AsyncTimeout next;

    private boolean inQueue;

    protected void timedOut() { }

    //标记 在未来的那个时间点是  超过该时间点 就算超时
    private long timeoutAt;

    public final void enter() {
        if (inQueue) throw new IllegalStateException("Unbalanced enter/exit");

        //多少时间后 就超时  比如 1000ms  就是一秒后就是超时
        long timeoutNanos = timeoutNanos();
        //表示设置死亡时间了吗
        boolean hasDeadline = hasDeadline();

        if (timeoutNanos == 0 && !hasDeadline) {
            //代表该条件就已经超时了
            return;
        }

        inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }


    private static synchronized void scheduleTimeout(AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
        if (head == null) {
            //创建头的时候 看门狗也要开启
            head = new AsyncTimeout();
            new Watchdog().start();
        }

        long now = System.nanoTime();
        if (timeoutNanos != 0 && hasDeadline) {
            //设置了死亡时间 并且设置了超时时间
            node.timeoutAt = now+Math.min(timeoutNanos, node.deadlineNanoTime() - now);
        } else if (timeoutNanos != 0) {
            //设置了超时时间 但是没有设置死亡时间
            node.timeoutAt = now + timeoutNanos;
        } else if (hasDeadline) {
            //设置了死亡时间 但是没有设置超时时间
            node.timeoutAt = node.deadlineNanoTime();
        } else {
            //都没有设置
            throw new AssertionError();
        }

        //多少毫秒后 将触发超时
        long remainingNanos = node.remainingNanos(now);

        //加入该链表
        for (AsyncTimeout prev = head; true; prev = prev.next) {
            //遍历该链表 从头遍历
            if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {

                node.next = prev.next;
                prev.next = node;
                if (prev == head) {
                    AsyncTimeout.class.notify();
                }
                break;
            }
        }

    }

    private static final class Watchdog extends Thread {

        Watchdog() {
            super("okio Watchdog");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    AsyncTimeout timedOut;

                    synchronized (AsyncTimeout.class) {
                        timedOut = awaitTimeout();

                        if (timedOut == null) continue;

                        if (timedOut == head) {
                            head = null;
                            return;
                        }

                        timedOut.timedOut();
                    }

                }catch (InterruptedException ignored) { }
            }
        }
    }

    static @Nullable AsyncTimeout awaitTimeout() throws InterruptedException {
        AsyncTimeout node = head.next;

        return null;
    }

    private long remainingNanos(long now) {
        return timeoutAt - now;
    }

}
