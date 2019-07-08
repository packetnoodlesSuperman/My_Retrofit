package com.bob.retrofit.okhttp;

import android.support.annotation.Nullable;

import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public final class Address {

    final HttpUrl url;
    final Dns dns;
    final SocketFactory socketFactory;


    final @Nullable SSLSocketFactory sslSocketFactory;

    public Address(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        this.url = new HttpUrl.Builder()
                .scheme(sslSocketFactory != null ? "https" : "http")
                .host(uriHost)
                .port(uriPort)
                .build();




    }

    public HttpUrl url() {
        return null;
    }

    public List<ConnectionSpec> connectionSpecd() {
        return null;
    }
}