package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

/**
 * @param <R> 就是后台返回的JS对象 响应对象
 * @param <T>
 */
public final class ServiceMethod<R, T> {

    private final String httpMethod;
    private final HttpUrl baseUrl;
    private final String relativeUrl;
    private final Headers headers;
    private final MediaType contentType;
    private final boolean hasBody;
    private final boolean isFormEncoded;
    private final boolean isMultipart;

    private final okhttp3.Call.Factory callFactory;

    //这个R 就是Call<R> 或者Observable<R>中的  而returnType 就是Call<R> 或者Observable<R>
    private final CallAdapter<R, T> callAdapter;

    public ServiceMethod(Builder<R, T> builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.callAdapter = builder.callAdapter;

        this.httpMethod = builder.httpMethod;
    }

    T adapt(Call<R> call) {
        return callAdapter.adapt(call);
    }

    public okhttp3.Call toCall(Object[] args) {
        RequestBuilder requestBuilder = new RequestBuilder(
                httpMethod,
                baseUrl,
                relativeUrl,
                headers,
                contentType,
                hasBody,
                isFormEncoded,
                isMultipart
        );
        return callFactory.newCall(requestBuilder.build());
    }

    /**
     * @param <T> 就是后台返回的JS对象 响应对象
     */
    static final class Builder<T, R> {

        final Retrofit retrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        //参数类型 也就是参数的Class类型
        final Type[] parameterTypes;
        //参数上的注解 一个参数可能多个注解修饰 从0开始起
        final Annotation[][] parameterAnnotationsArray;


        CallAdapter<T, R> callAdapter;
        Type responseType;
        Converter<ResponseBody, T> responseConverter;

        Builder(Retrofit retrofit, Method method){
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            //getGenericParameterTypes --> 返回参数类型 也就是参数的Class类型
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }

        //解析方法
        public ServiceMethod build() {
            //
            callAdapter = createCallAdapter();
            //Call<R> 这里的R就是 ---> responseType
            responseType = callAdapter.responseType();

            if (responseType == Response.class || responseType == okhttp3.Response.class) {
                throw methodError("", null);
            }

            //
            responseConverter = createResponseConverter();

            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            return null;
        }

        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            return null;
        }

        private CallAdapter<T, R> createCallAdapter() {
            //拿到返回值Class类型
            Type returnType = method.getGenericReturnType();
            //获取方法上的所有注解
            Annotation[] annotations = method.getAnnotations();
            return (CallAdapter<T, R>)retrofit.callAdapter(returnType, annotations);
        }

        private Converter<ResponseBody, T> createResponseConverter() {
            Annotation[] annotations = method.getAnnotations();
            return retrofit.responseBodyConverter(responseType, annotations);
        }


        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof GET) {
                parseHttpMethodAndPath(
                        "GET",
                        ((GET)annotation).value(),
                        false
                );
            }
        }

        static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
        static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

        private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {


            int question = value.indexOf("?");
            if (question != -1 && question < value.length() -1) {
                String queryParams = value.substring(question + 1);
                Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
                if (queryParamMatcher.find()) {
                    //抛出异常

                }
            }

            parsePathParameters(value);
        }

        private Set<String> parsePathParameters(String path) {
            Matcher m = PARAM_URL_REGEX.matcher(path);
            Set<String> patterns = new LinkedHashSet<>();
            while (m.find()) {
                patterns.add(m.group(1));
            }
            return patterns;
        }

    }

}
