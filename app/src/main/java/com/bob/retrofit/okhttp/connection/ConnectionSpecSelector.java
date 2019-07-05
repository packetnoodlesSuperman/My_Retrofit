package com.bob.retrofit.okhttp.connection;

import com.bob.retrofit.okhttp.ConnectionSpec;
import java.util.List;

public final class ConnectionSpecSelector {

    private final List<ConnectionSpec> connectionSpecs;
    private int nextModeIndex;

    public ConnectionSpecSelector(List<ConnectionSpec> connectionSpecs) {
        this.nextModeIndex = 0;
        this.connectionSpecs = connectionSpecs;
    }

}
