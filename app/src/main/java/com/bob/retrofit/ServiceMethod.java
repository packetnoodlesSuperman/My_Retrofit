package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;

/**
 * @param <R> 就是后台返回的JS对象 响应对象
 * @param <T>
 */
public final class ServiceMethod<R, T> {

    private final okhttp3.Call.Factory callFactory;

    private final CallAdapter<R, T> callAdapter;

    public ServiceMethod(Builder<R, T> builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.callAdapter = builder.callAdapter;
    }

    T adapt(Call<R> call) {
        return callAdapter.adapt(call);
    }

    public okhttp3.Call toCall(Object[] args) {
        RequestBuilder requestBuilder = new RequestBuilder();

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

        public ServiceMethod build() {
            callAdapter = createCallAdapter();
            responseType = callAdapter.responseType();

            responseConverter = createResponseConverter();

            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            return null;
        }

        private CallAdapter<T, R> createCallAdapter() {
            Type returnType = method.getGenericReturnType();
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
