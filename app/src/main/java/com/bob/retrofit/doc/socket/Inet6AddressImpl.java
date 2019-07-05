package com.bob.retrofit.doc.socket;

import java.net.UnknownHostException;

public class Inet6AddressImpl implements InetAddressImpl {
    @Override
    public InetAddress[] lookupAllHostAddr(String host, int netId) throws UnknownHostException {
        if (host == null || host.isEmpty()) {
            return loopbackAddresses();
        }

        return new InetAddress[0];
    }

    private InetAddress[] loopbackAddresses() {
        return new InetAddress[0];
    }
}
