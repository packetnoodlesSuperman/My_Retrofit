package com.bob.retrofit.okhttp;

public abstract class EventListener {

    public void callStart(Call call) { }

    public interface Factory {

        EventListener create(Call call);

    }

}
