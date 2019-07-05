package com.bob.retrofit.doc.socket;

import java.io.Serializable;
import java.net.UnknownHostException;

public class InetAddress implements Serializable {

    static final int NETID_UNSET = 0;

    static final InetAddressImpl impl = new Inet6AddressImpl();

    public static InetAddress[] getAllByName(String host) throws UnknownHostException {
        return impl.lookupAllHostAddr(host, NETID_UNSET).clone();
    }
}
