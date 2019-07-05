package com.bob.retrofit.okhttp;

import java.net.Proxy;

public class Route {

    Address address;
    Proxy proxy;

    public Address address() {
        return null;
    }

    public boolean requiresTunnel() {
        return address.sslSocketFactory != null && proxy.type() == Proxy.Type.HTTP;
    }
}
