
package com.bob.retrofit.doc.socket;

import java.io.IOException;
import java.net.UnknownHostException;

public class ISocket {

    private static SocketImplFactory factory = null;
    SocketImpl impl;

    public ISocket() {
        setImpl();
    }

    private void setImpl() {
        if (factory != null) {
            impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            impl = new SocketImpl() {};
        }

        if (impl != null) {
            impl.setSocket(this);
        }
    }

    private void checkOldImpl() {
        if (impl == null) {
            return;
        }
    }

    public ISocket(String host, int port) throws IOException {
        this(InetAddress.getAllByName(host), port, null, true);
    }

    private ISocket(InetAddress[] addresses, int port, SocketAddress localAddr, boolean stream) throws IOException {

    }

    public void bind(SocketAddress bindpoint) throws IOException {


    }

}
