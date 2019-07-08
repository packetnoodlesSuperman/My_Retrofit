package com.bob.retrofit.okhttp.connection;

import com.bob.retrofit.okhttp.Call;
import com.bob.retrofit.okhttp.Connection;
import com.bob.retrofit.okhttp.ConnectionSpec;
import com.bob.retrofit.okhttp.EventListener;
import com.bob.retrofit.okhttp.Handshake;
import com.bob.retrofit.okhttp.HttpUrl;
import com.bob.retrofit.okhttp.Protocol;
import com.bob.retrofit.okhttp.Request;
import com.bob.retrofit.okhttp.Route;

import java.net.Proxy;
import java.net.Socket;
import java.util.List;

/**
 * @Desc RealConnection对象意味着我们已经跟服务端有了一条通信链路
 *       有通信链路了，意味着在这个类实现的三次握手
 */
public class RealConnection implements Connection {

    private final Route route;

    public RealConnection(Route route) {
        this.route = route;
    }

    public boolean supportsUrl(HttpUrl url) {
        if (url.port() != route.address().url().port()) {
            return false;
        }


        return false;
    }

    @Override
    public Route route() {
        return null;
    }

    @Override
    public Socket socket() {
        return null;
    }

    @Override
    public Handshake handshake() {
        return null;
    }

    @Override
    public Protocol protocol() {
        return null;
    }

    /**
     * @Desc 建立网络连接
     */
    public void connect(
            int connectTimeout,
            int readTimeout,
            int writeTimeout,
            int pingIntervalMillis,
            boolean connectionRetryEnabled,
            Call call,
            EventListener eventListener
    ) {
        RouteException routeException = null;
        List<ConnectionSpec> connectionSpecs = route.address().connectionSpecd();
        ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);

        if (route.address().sslSocketFactory() == null) {
            if (connectionSpecs.contains(ConnectionSpec.CLEARTEXT))
        }

        while (true) {
            if (route.requiresTunnel()) {
                connectTunnel(connectTimeout, readTimeout, writeTimeout, call, eventListener);

            } else {
                connectSocket();
            }
        }
    }

    private void connectSocket() {
        Proxy proxy = route.proxy();

        rawSocket = proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP
                ? address.socketFactory().createSocket() : new Socket(proxy);

    }

    private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout, Call call, EventListener eventListener) {
        Request tunnelRequest = createTunnelRequest();
    }

    private Request createTunnelRequest() {
        return new Request.Builder()
                .url(route.address().url())
                .header("Host", Util.hostHeader(route.address().url(), true))
                .header("Proxy-Connection", "Keep-Alive") // For HTTP/1.0 proxies like Squid.
                .header("User-Agent", Version.userAgent())
                .build();
    }

}
