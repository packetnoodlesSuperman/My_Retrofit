package com.bob.retrofit.okhttp;

import java.io.IOException;

public interface Authenticator {

    Authenticator NONE = new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            return null;
        }
    };

    Request authenticate(Route route, Response response) throws IOException;

}
