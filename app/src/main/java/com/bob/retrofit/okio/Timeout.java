package com.bob.retrofit.okio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

public class Timeout {

    public static final Timeout NONE = new Timeout() {
        @Override public Timeout timeout(long timeout, TimeUnit unit) {
            return this;
        }
        @Override public Timeout deadlineNanoTime(long deadlineNanoTime) {
            return this;
        }
        @Override public void throwIfReached() throws IOException { }
    };

    private boolean hasDeadline;
    private long deadlineNanoTime;
    private long timeoutNanos;

    public Timeout() {}

    public Timeout timeout(long timeout, TimeUnit unit) {
        if (timeout < 0) throw new IllegalArgumentException("timeout < 0: " + timeout);
        if (unit == null) throw new IllegalArgumentException("unit == null");

        //unit为单位， 转换为纳秒
        this.timeoutNanos = unit.toNanos(timeout);
        return this;
    }

    public long timeoutNanos() {
        return timeoutNanos;
    }

    public boolean hasDeadline() {
        return hasDeadline;
    }

    public long deadlineNanoTime() {
        if (!hasDeadline) throw new IllegalStateException("No deadline");
        return deadlineNanoTime;
    }

    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        this.hasDeadline = true;
        this.deadlineNanoTime = deadlineNanoTime;
        return this;
    }

    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            throw new InterruptedIOException("thread interrupted");
        }

        if (hasDeadline && deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }

}
