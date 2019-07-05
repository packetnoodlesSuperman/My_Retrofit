package com.bob.retrofit.okhttp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;

public final class Dispatcher {

    private final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;


    synchronized void enqueue(RealCall.AsyncCall call) {
        if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            readyAsyncCalls.add(call);
        }
    }

    private int runningCallsForHost(RealCall.AsyncCall call) {
        int result = 0;
        for (RealCall.AsyncCall c : runningAsyncCalls) {
            if (c.get().forWebSocket) {
                continue;
            }
            if (c.host().equals(call.host())) {
                result++;
            }
        }
        return result;
    }

    public synchronized ExecutorService executorService() {
        return null;
    }

}
