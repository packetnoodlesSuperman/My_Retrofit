package com.bob.retrofit.okio;

import android.support.annotation.Nullable;

public final class SegmentPool {

    static final long MAX_SIZE = 64 * 1024;

    static @Nullable Segment next;

    static long byteCount;

    static Segment take() {
        synchronized (SegmentPool.class) {
            if (next != null) {
                Segment result = next;
                next = result.next;
                result.next = null;
                byteCount -= Segment.SIZE;
                return result;
            }
        }
       // Pool is empty. Don't zero-fill while holding a lock.
        return new Segment();
    }

    public static void recycle(Segment s) {

    }
}