package com.bob.retrofit.okhttp;

import java.io.IOException;

public abstract class NamedRunnable implements Runnable {

    protected final String name;

    public NamedRunnable(String format, Object... args) {
        this.name = "args";
//                Util.format(format, args);
    }

    @Override public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    protected abstract void execute() throws IOException;
}
