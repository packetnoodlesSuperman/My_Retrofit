package com.bob.retrofit.okhttp;

import java.net.Socket;

public interface Connection {

    Route route();

    Socket socket();

    Handshake handshake();

    Protocol protocol();

}
