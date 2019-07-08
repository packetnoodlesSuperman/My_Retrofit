package com.bob.retrofit.okhttp;

import java.util.List;

import okio.BufferedSource;

public class FormBody extends ResponseBody {

    private static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    private final List<String> encodeNames;
    private final List<String> encodeValues;

    FormBody(List<String> encodeNames, List<String> encodeValues) {
        this.encodeNames = encodeNames;
        this.encodeValues = encodeValues;
    }

    @Override
    public MediaType contentType() {
        return null;
    }

    @Override
    public long contentLength() {
        return 0;
    }

    @Override
    public BufferedSource source() {
        return null;
    }
}
