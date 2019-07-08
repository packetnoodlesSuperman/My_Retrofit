package com.bob.retrofit;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

public final class RequestBuilder {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String PATH_SEGMENT_ALWAYS_ENCODE_SET = "\"<>^`{}\\?#";

    private final String method;
    //OkHttp的baseUrl
    private final HttpUrl baseUrl;
    //HttpUrl 的相对路径 比如GET、POST 注解后面的url路径
    private String relativeUrl;

    private HttpUrl.Builder urlBuilder;

    private final Request.Builder requestBuilder;

    private MediaType contentType;

    private final boolean hasBody;

    private MultipartBody.Builder multipartBuilder;

    private FormBody.Builder formBuilder;

    private RequestBody body;

    RequestBuilder(
            String method,
            HttpUrl baseUrl,
            String relativeUrl,
            Headers headers,
            MediaType contentType,
            boolean hasBody,
            boolean isFormEncoded,
            boolean isMultipart
    ) {
        this.method = method;
        this.baseUrl = baseUrl;
        this.relativeUrl = relativeUrl;
        this.requestBuilder = new Request.Builder();
        this.contentType = contentType;
        this.hasBody = hasBody;

        if (headers != null) {
            requestBuilder.headers(headers);
        }

        if (isFormEncoded) {
            formBuilder = new FormBody.Builder();
        } else if (isMultipart) {
            multipartBuilder = new MultipartBody.Builder();
            multipartBuilder.setType(MultipartBody.FORM);
        }
    }


    Request build() {
        HttpUrl url;
        HttpUrl.Builder urlBuilder = this.urlBuilder;

        if (urlBuilder != null) {
            url = urlBuilder.build();
        } else {

            url = baseUrl.resolve(relativeUrl);
            if (url == null) {
                throw new IllegalArgumentException("Malformed URL. Base: " + baseUrl + ", Relative: " + relativeUrl);
            }
        }

        RequestBody body = this.body;
        if (body == null) {
            if (formBuilder != null) {
                body = formBuilder.build();
            } else if (multipartBuilder != null) {
                body = multipartBuilder.build();
            } else if (hasBody) {
                body = RequestBody.create(null, new byte[0]);
            }
        }

        MediaType contentType = this.contentType;
        if (contentType != null) {
            if (body != null) {
                body = new ContentTypeOverridingRequestBody(body, contentType);
            } else {
                requestBuilder.addHeader("Content-Type", contentType.toString());
            }
        }

        return requestBuilder
                .url(url)
                .method(method, body)
                .build();
    }

    private static class ContentTypeOverridingRequestBody extends RequestBody {

        private final RequestBody delegate;
        private final MediaType

        public ContentTypeOverridingRequestBody(RequestBody body, MediaType contentType) {

        }

        @Override
        public MediaType contentType() {
            return null;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {

        }
    }
}
