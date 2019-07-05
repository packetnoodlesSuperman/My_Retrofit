package com.bob.retrofit.doc.socket;

import java.net.UnknownHostException;

public interface InetAddressImpl {

    InetAddress[] lookupAllHostAddr(String hostname, int netId) throws UnknownHostException;

}
