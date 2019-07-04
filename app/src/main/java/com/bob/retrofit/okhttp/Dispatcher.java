package com.bob.retrofit.okhttp;

import java.util.concurrent.ExecutorService;

public class Dispatcher {

    synchronized void enqueue(RealCall.AsyncCall call) {
        executorService().execute(call);
    }

    public synchronized ExecutorService executorService() {
        return null;
    }

}
