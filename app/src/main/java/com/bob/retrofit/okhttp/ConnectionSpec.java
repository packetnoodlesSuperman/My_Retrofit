package com.bob.retrofit.okhttp;

/**
 * @Desc  用于描述传输HTTP流量的socket连接的配置
 *        对于https请求，这些配置主要包括协商安全连接时要使用的TLS版本号和密码套件，是否支持TLS扩展等；
 *        对于http请求则几乎不包含什么信息。
 */
public final class ConnectionSpec {

    public static final ConnectionSpec CLEARTEXT = new Builder(false).build();

    public static final class Builder {

        //安全传输层协议
        boolean tls;

        Builder(boolean tls) {
            this.tls = tls;
        }
    }

}
