package com.bob.retrofit.okio;

import java.util.concurrent.TimeUnit;

/**
 * Created by xhb on 2019/7/13.
 */

public class AsyncTimeout extends Timeout {

    private static final int TIMEOUT_WRITE_SIZE = 64*1024; //1024byte = 1kb  64kb
    // 60秒 一分钟
    private static final long IDEL_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);  //秒转毫秒
    //60秒转到 纳秒
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDEL_TIMEOUT_MILLIS); //毫秒转纳秒


    static AsyncTimeout head;

    private boolean inQueue;

    private AsyncTimeout next;

    private long timeoutAt;

    protected void timeOut() {}

    public final void enter() {
        if (inQueue) {
            throw new IllegalStateException("Unbalanced enter/exit");
        }

        long timeoutNanos = timeoutNanos();

        boolean hasDeadline = hasDeadline();

        if (timeoutNanos == 0 && !hasDeadline) {
            return;
        }

        inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }

    private void scheduleTimeout(AsyncTimeout asyncTimeout, long timeoutNanos, boolean hasDeadline) {
        if (head == null) {
            head = new AsyncTimeout();
            new Watchdog().start();
        }
    }

    public Sink sink(Sink sink) {
        return new Sin;
    }


    private static final class Watchdog extends Thread {

        Watchdog() {
            super("OKio Watchdog");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    AsyncTimeout timeout;
                    synchronized (AsyncTimeout.class) {
                        timeout = awaitTimeout();

                        if (timeout == null) {
                            continue;
                        }

                        if (timeout == head) {
                            head = null;
                            return;
                        }
                    }
                    timeout.timeOut();
                } catch (InterruptedException ignored) {}
            }
        }

    }


    static AsyncTimeout awaitTimeout() throws InterruptedException {
        AsyncTimeout node = head.next;
        if (node == null) {
            long startNanos = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_NANOS);

            return head.next == null && (System.nanoTime() - startNanos) >= IDEL_TIMEOUT_MILLIS ?
                    head : null;
        }


        return null;

    }

}
