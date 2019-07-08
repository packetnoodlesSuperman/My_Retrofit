package com.bob.retrofit.okhttp;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class Route {

    final Address address;
    final Proxy proxy;
    final InetSocketAddress inetSocketAddress;

    public Route(Address address, Proxy proxy, InetSocketAddress inetSocketAddress) {
        if (address == null) {
            throw new NullPointerException();
        }
        if (proxy == null) {
            throw new NullPointerException();
        }
        if (inetSocketAddress == null) {
            throw new NullPointerException();
        }
        this.address = address;
        this.proxy = proxy;
        this.inetSocketAddress = inetSocketAddress;
    }

    public Address address() { return address; }
    public Proxy proxy() {
        return proxy;
    }

    /**
     * https://blog.csdn.net/chunqiuwei/article/details/74936885
     * 表示是否需要一个隧道
     * 如果是ssl 并且代理的类型是HTTP 就表示和代理直接是一个隧道
     * 否则就是一个socket
     *
     * 特别的： HTTP代理指的是使用代理服务器使网络用户访问外部网站。
     *          代理服务器是介于浏览器和Web服务器之间的一台服务器，
     *         是建立在超文本传输协议上的网络浏览方式，
     *         作用是可以防伪部分对协议进行了限制的局域网。
     *         它通常绑定在代理服务器的80、3128、8080等端口上
     * HTTP协议即超文本传输协议
     */
    public boolean requiresTunnel() {
        return address.sslSocketFactory != null && proxy.type() == Proxy.Type.HTTP;
    }

    /**
     * @return 重写HashCode
     */
    @Override public int hashCode() {
        int result = 17;
        result = 31 * result + address.hashCode();
        result = 31 * result + proxy.hashCode();
        result = 31 * result + inetSocketAddress.hashCode();
        return result;
    }
}
