package com.bob.retrofit.okhttp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public interface Dns {

    Dns SYSTEM = new Dns() {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            if (hostname == null) {
                throw new UnknownHostException();
            }

            try {
                return Arrays.asList(InetAddress.getAllByName(hostname));
            } catch (NullPointerException e) {
                UnknownHostException unknownHostException = new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
                //initCause 将异常包装起来 给cause赋值 Throwable cause
                unknownHostException.initCause(e);
                throw unknownHostException;
            }
        }
    };

    List<InetAddress> lookup(String hostname) throws UnknownHostException;

}
