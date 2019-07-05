package com.bob.retrofit.okhttp;

import android.support.annotation.Nullable;

import java.util.List;

import javax.net.ssl.SSLSocketFactory;

public final class Address {

    final @Nullable SSLSocketFactory sslSocketFactory;

    public Address(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public HttpUrl url() {
        return null;
    }

    public List<ConnectionSpec> connectionSpecd() {
        return null;
    }
}