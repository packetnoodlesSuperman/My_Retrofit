package com.bob.retrofit.okio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * Timeout类 使用了2种策略来处理是否应该中断等待，
 *      一种是 任务处理的时间，
 *      另一种是设定 任务时间的截止点，
 *
 *      这两种策略也可以同时存在，判断时会取最近的临界点时间
 */
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
    //使用deadline 来 保证所有的任务执行应该在deadlineTime之前执行完成，否则抛出异常
    private long deadlineNanoTime;
    //等待任务执行完成的最长时间，如果设置为0，相当于会无限等待直到任务执行完成
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

    /**  设置截止时间，内部通过调用deadlineNanoTime. */
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
