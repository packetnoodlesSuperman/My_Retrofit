package com.bob.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
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

    static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
    static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);

    private final String httpMethod;
    private final HttpUrl baseUrl;
    private final String relativeUrl;
    private final Headers headers;
    private final MediaType contentType;
    private final boolean hasBody;
    private final boolean isFormEncoded;
    private final boolean isMultipart;
    private final Converter<ResponseBody, R> responseConverter;
    private final okhttp3.Call.Factory callFactory;

    //这个R 就是Call<R> 或者Observable<R>中的  而returnType 就是Call<R> 或者Observable<R>
    private final CallAdapter<R, T> callAdapter;

    private final ParameterHandler<?> parameterHandlers;

    public ServiceMethod(Builder<R, T> builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.callAdapter = builder.callAdapter;
        this.baseUrl = builder.retrofit.baseUrl();
        this.responseConverter = builder.responseConverter;
        this.httpMethod = builder.httpMethod;
        this.relativeUrl = builder.relativeUrl;
        this.headers = builder.headers;
        this.contentType = builder.contentType;
        this.hasBody = builder.hasBody;
        this.isFormEncoded = builder.isFormEncoded;
        this.isMultipart = builder.isMultipart;
        this.parameterHandlers = builder.parameterHandlers;
    }

    T adapt(Call<R> call) {
        return callAdapter.adapt(call);
    }

    public okhttp3.Call toCall(Object... args) {
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
        ParameterHandler<Object> handlers = (ParameterHandler<Object>) parameterHandlers;
        int argumentCount = args != null ? args.length : 0;

        return callFactory.newCall(requestBuilder.build());
    }

    /**
     * @param <T> 就是后台返回的JS对象 响应对象
     */
    static final class Builder<T, R> {

        final Retrofit retrofit;
        final Method method;
        //方法上的注解
        final Annotation[] methodAnnotations;
        //方法入参参数类型 也就是参数的Class类型
        final Type[] parameterTypes;
        //参数上的注解 一个参数可能多个注解修饰 从0开始起
        final Annotation[][] parameterAnnotationsArray;


        CallAdapter<T, R> callAdapter;
        Type responseType;
        Converter<ResponseBody, T> responseConverter;

        ParameterHandler<?>[] parameterHandlers;

        Builder(Retrofit retrofit, Method method){
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            //getGenericParameterTypes --> 返回方法内入参参数类型 也就是参数的Class类型
            this.parameterTypes = method.getGenericParameterTypes();
            //参数上的注解 一个参数可能多个注解修饰 从0开始起
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }

        //解析方法
        public ServiceMethod build() {
            //call的适配器 将T--> 转换成 R
            callAdapter = createCallAdapter();
            //Call<R> 这里的R就是 ---> responseType
            responseType = callAdapter.responseType();
            //Call<T> 中的T不能只有Response 不要带泛型参数 或者非Response类型
            //Retrofit 自带了转换的功能，可以将服务器返回的数据自动解析为你的Java Bean，所以这个地方你不能使用Response
            if (responseType == Response.class || responseType == okhttp3.Response.class) {
                throw methodError("'" + Utils.getRawType(responseType).getName() +
                        "' is not a valid response body type. Did you mean ResponseBody?);");//不是有效的响应正文类型，您是指ResponseBody
            }
            //转换器
            responseConverter = createResponseConverter();
            //解析方法上的注解
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }
            //parameterAnnotationsArray 参数上的注解 一个参数可能多个注解修饰 从0开始起
            int parameterCount = parameterAnnotationsArray.length;
            parameterHandlers = new ParameterHandler<?>[parameterCount];
            for (int p = 0; p < parameterCount; p++) {
                //parameterTypes 方法入参参数类型 也就是参数的Class类型
                Type parameterType = parameterTypes[p];

                Annotation[] parameterAnnotations = parameterAnnotationsArray[p];
                if (parameterAnnotations == null) {
                    throw parameterError(p, "No Retrofit annotation found.");
                }
                parameterHandlers[p] = parseParameter(p, parameterType, parameterAnnotations);
            }

            return null;
        }

        /**
         * @param p 索引
         * @param parameterType 参数type类型
         * @param annotations 该参数上的注解数组
         */
        private ParameterHandler<?> parseParameter(int p, Type parameterType, Annotation[] annotations) {
            ParameterHandler<?> result = null;
            for (Annotation annotation : annotations) {
                ParameterHandler<?> annotationAction = parseParameterAnnotation(p, parameterType, annotations, annotation);
            }
            return null;
        }

        /**
         * 解析参数注解
         */
        private ParameterHandler<?> parseParameterAnnotation(int p, Type type, Annotation[] annotations, Annotation annotation) {
            if (type == HttpUrl.class || type == String.class || type == URI.class || (type instanceof Class && "android.net.Uri".equals(((Class<?>) type).getName()))) {
                return new ParameterHandler.RelativeUrl();
            } else if (annotation instanceof Path) {

            }
        }

        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            return null;
        }

        /**
         * @return 获取Call的适配器
         */
        private CallAdapter<T, R> createCallAdapter() {
            //拿到返回值Class类型 方法返回的类型
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
                //PARAM_URL_REGEX 代表 命名格式 字母开头 字母数组下划线横线组成的 字符
                Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
                if (queryParamMatcher.find()) {
                    //抛出异常
                    queryParamMatcher.group(1);
                }
            }

            this.relativeUrlParamNames = parsePathParameters(value);
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