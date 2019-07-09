package com.bob.retrofit;

import android.support.annotation.Nullable;

import com.bob.retrofit.okhttp.HttpUrl;
import com.bob.retrofit.okhttp.OkHttpClient;
import com.bob.retrofit.okhttp.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @Desc 举个栗子
 *  public interface PersonalProtocol {
 *       @FormUrlEncoded
 *       @POST("user/personalListInfo")
 *       Call<Response<PersonalInfo>> getPersonalListInfo(@Field("cur_page") int page);
 *  }
 *
 *  Retrofit retrofit = new Retrofit.Builder().baseUrl("www.xxxx.com/").build();  //不设置client会提供一个默认的
 *  PersonalProtocol personalProtocol = retrofit.create(PersonalProtocol.class);
 *  Call<Response<PersonalInfo>> call = personalProtocol.getPersonalListInfo(12);
 *  call.enqueue(new Callback<Response<PersonalInfo>>() {
 *      @Override
 *      public void onResponse(Call<Response<PersonalInfo>> call, Response<Response<PersonalInfo>> response) {
 *          //数据请求成功
 *      }
 *      @Override
 *      public void onFailure(Call<Response<PersonalInfo>> call, Throwable t) {
 *          //数据请求失败
 *      }
 *  });
 */
public final class Retrofit {

    //一个方法 对应的方法头上的注解解析  缓存
    private final Map<Method, ServiceMethod<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();
    //OkHttpClient实现了Call.Factory接口
    final com.bob.retrofit.okhttp.Call.Factory callFactory;
    //根Url
    final HttpUrl baseUrl;
    //转换器
    final List<Converter.Factory> converterFactories;
    //适配转换器
    final List<CallAdapter.Factory> callAdapterFactories;
    /**{@link ExecutorCallAdapterFactory} 参照这个类 在Android—Platform中**/
    final Executor callbackExecutor;
    //判断是否是提前解析这个接口类的所有方法
    final boolean validateEagerly;

    Retrofit(
            com.bob.retrofit.okhttp.Call.Factory callFactory,
            HttpUrl baseUrl,
            List<Converter.Factory> converterFactories,
            List<CallAdapter.Factory> callAdapterFactories,
            Executor callbackExecutor,
            boolean validateEagerly
    ) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
        this.converterFactories = converterFactories;
        this.callAdapterFactories = callAdapterFactories;
        this.callbackExecutor = callbackExecutor;
        this.validateEagerly = validateEagerly;
    }

    /**
     * @Desc 精华部分
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        //是否为合法接口
        Utils.validateServiceInterface(service);

        if (validateEagerly) {
            eagerlyValidateMethods(service);
        }

        //返回动态生成的代理类 也就是 service 的实例化对象
        return (T) Proxy.newProxyInstance(service.getClassLoader(),
                new Class<?>[]{service},
                new InvocationHandler() {

                    //平台
                    private final Platform platform = Platform.get();

                    /**
                     * @return 返回的是 Call<R>
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        //如果是父类方法
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        //这个只有Java8平台才会走此方法  android平台固定返回false
                        if (platform.isDefaultMethod(method)) {
                            return platform.invokeDefaultMethod(method, service, proxy, args);
                        }

                        //Object，任何类的父类  （T，在传入的时候就已经限定了参数的类型）
                        //T，泛型参数    <声明>一个泛型类或泛型方法
                        //?，类型通配符  <使用>一个泛型类或泛型方法
                        //使用也只能这样 ---> 有界通配符<? extends XXX>，<? super XXX>
                        ServiceMethod<Object, Object> serviceMethod = (ServiceMethod<Object, Object>) loadServiceMethod(method);
                        OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);
                        return serviceMethod.adapt(okHttpCall);
                    }
                });
    }

    //提前解析这个类中的所有方法
    private <T> void eagerlyValidateMethods(Class<T> service) {
        Platform platform = Platform.get();
        for (Method method : service.getDeclaredMethods()) {
            if (!platform.isDefaultMethod(method)) {
                loadServiceMethod(method);
            }
        }
    }

    //解析方法
    private ServiceMethod<?, ?> loadServiceMethod(Method method) {
        //获取缓存
        ServiceMethod<?, ?> serviceMethod = serviceMethodCache.get(method);
        if (serviceMethod != null) {
            return serviceMethod;
        }

        //去解析这个Method成一个MethodService
        synchronized (serviceMethodCache) {
            serviceMethod = serviceMethodCache.get(method);
            if (serviceMethod == null) {
                serviceMethod = new ServiceMethod.Builder<>(this, method).build();
                serviceMethodCache.put(method, serviceMethod);
            }
        }
        return serviceMethod;
    }

    /**
     * @param returnType 方法返回的Type类型
     * @param annotations 方法上的所有注解
     */
    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null, returnType, annotations);
    }
    private CallAdapter<?, ?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType, Annotation[] annotations) {
        //如果skipPast == null 则返回-1  除非集合里面存了null
        int start = callAdapterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            //一个一个尝试 拿到对应的适配器
            CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations, this);
            if (adapter != null) {
                return adapter;
            }
        }
        //否则抛异常
        return null;
    }

    /**
     * @param responseType Call<T> 或者 Obserable<T> 中T的type类型
     * @param annotations method上的注解
     */
    public <T> Converter<ResponseBody, T> responseBodyConverter(Type responseType, Annotation[] annotations) {
        return nextResponseBodyConverter(null, responseType, annotations);
    }
    private <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory skipPast, Type responseType, Annotation[] annotations) {

        return null;
    }


    public com.bob.retrofit.okhttp.Call.Factory callFactory() {
        return callFactory;
    }

    /************************** 内部类 配置 **************************/
    public static final class Builder {
        //平台
        private Platform platform;
        //手动传入自定义的 OKhttpCilent
        private com.bob.retrofit.okhttp.Call.Factory callFactory;
        //根url
        private HttpUrl baseUrl;
        //转换器工厂 集合
        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        //适配器工厂 集合  响应转换成 Call<T> 或者 Observable<T>  （序列化与反序列化 socket有读写的流）
        private List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        //线程池 这个干吗的呢？ TODO
        private @Nullable Executor callbackExecutor;
        //判断是否是提前解析这个接口类的所有方法
        private boolean validateEagerly;

        public Builder() { this(Platform.get()); }

        Builder(Platform platform) { this.platform = platform; }
        Builder(Retrofit retrofit) {
            platform = Platform.get();
            callFactory = retrofit.callFactory;
            baseUrl = retrofit.baseUrl;
            converterFactories.addAll(retrofit.converterFactories);
            converterFactories.remove(0);
            callAdapterFactories.addAll(retrofit.callAdapterFactories);
            callAdapterFactories.remove(callAdapterFactories.size() - 1);
            callbackExecutor = retrofit.callbackExecutor;
            validateEagerly = retrofit.validateEagerly;
        }

        public Builder baseUrl(String baseUrl) {
            Utils.checkNotNull(baseUrl, "baseUrl == null");
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            return baseUrl(httpUrl);
        }

        public Builder baseUrl(HttpUrl baseUrl) {
            Utils.checkNotNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * @param client 需要设置OkHttpClient
         */
        public Builder client(OkHttpClient client) {
            return callFactory(Utils.checkNotNull(client, "client == null"));
        }

        public Builder callFactory(com.bob.retrofit.okhttp.Call.Factory factory) {
            this.callFactory = Utils.checkNotNull(factory, "factory == null");
            return this;
        }

        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }

        public Retrofit build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            com.bob.retrofit.okhttp.Call.Factory callFactory = this.callFactory;
            //如果client是空 则设置一个默认的
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            //获取一个默认的adapterFactory
            /**Android平台是{@link ExecutorCallAdapterFactory} Java8平台是{@link DefaultCallAdapterFactory}**/
            Executor callbackExecutor = null;
            if (callbackExecutor == null) {
                callbackExecutor = platform.defaultCallbackExecutor();
            }
            List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.callAdapterFactories);
            /**是{@link ExecutorCallAdapterFactory}还是{@link DefaultCallAdapterFactory} 全由callbackExecutor==null判断**/
            adapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));

            //转换器 第一个位置先加上BuiltInConverters()
            List<Converter.Factory> converterFactories = new ArrayList<>(1 + this.converterFactories.size());
            converterFactories.add(new BuiltInConverters());
            converterFactories.addAll(this.converterFactories);

            //创建Retrofit
            return new Retrofit(callFactory,              //client
                                baseUrl,                  //根url
                                converterFactories,     //转换器
                                adapterFactories,         //适配器
                                callbackExecutor,         //线程池
                                validateEagerly);       //提前解析开关
        }
    }
}