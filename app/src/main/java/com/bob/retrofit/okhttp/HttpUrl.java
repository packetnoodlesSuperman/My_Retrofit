package com.bob.retrofit.okhttp;

import java.util.ArrayList;
import java.util.List;

import okio.Buffer;

public final class HttpUrl {
    //十六进制
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    final String url;
    final String scheme;    //协议 如：http
    final String host;      //域名
    final int port;         //端口号
    final List<String> pathSegments;
    final List<String> queryNamesAndValues;
    final String username;
    final String password;
    final String fragment;

    HttpUrl(Builder builder) {
        this.url = builder.toString();
        this.scheme = builder.scheme;
        this.host = builder.host;
        this.port = builder.effectivePort();
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public static final class Builder {
        String scheme;
        String host;
        int port = -1;
        List<String> encodedQueryNamesAndValues;

        public Builder scheme(String scheme) {
            if (scheme == null) {
                throw new NullPointerException();
            } else if (scheme.equalsIgnoreCase("http")) {
                this.scheme = "http";
            } else if (scheme.equalsIgnoreCase("https")) {
                this.scheme = "https";
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        public Builder host(String host) {
            if (host == null) {
                throw new NullPointerException();
            }
            String encoded = canonicalizeHost(host, 0, host.length());
            if (encoded == null) {
                throw new IllegalArgumentException("unexpected host: " + host);
            }
            this.host = encoded;
            return this;
        }

        private String canonicalizeHost(String input, int pos, int limit) {
            String percentDecoded = percentDecode(input, pos, limit, false);
            return null;
        }

        private String percentDecode(String input, int pos, int limit, boolean plusIsSpace) {
            for (int i = pos; i < limit; i++){
                char c = input.charAt(i);
                if (c == '%' || (c == '+' && plusIsSpace)) {
                    Buffer out = new Buffer();
                    out.writeUtf8(input, pos, i);
                }
            }
            return null;
        }

        public int effectivePort() {
            return port != -1 ? port : defaultPort(scheme);
        }

        public Builder addEncodedQueryParameter(String encodedName, String encodeValue) {
            if (encodedName == null) {
                throw new NullPointerException();
            }
            if (encodedQueryNamesAndValues == null) {
                encodedQueryNamesAndValues = new ArrayList<>();
            }
            encodedQueryNamesAndValues.add();

        }

        public static int defaultPort(String scheme) {
            if (scheme.equals("http")) {
                return 80;
            } else if (scheme.equals("https")) {
                return 443;
            } else {
                return -1;
            }
        }
    }

}
