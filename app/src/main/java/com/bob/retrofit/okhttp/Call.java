package com.bob.retrofit.okhttp;

import java.io.IOException;

public interface Call extends Cloneable {

    Request request();

    Response execute() throws IOException;

    void enqueue(Callback responseCallback);

    void cancel();

    boolean isExecuted();

    boolean isCanceled();

    Call clone();

    public interface Factory {

        Call newCall(Request request);

    }

}
