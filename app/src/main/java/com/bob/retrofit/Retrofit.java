package com.bob.retrofit;

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

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

/**
 * @Desc 举个栗子
 *  public interface PersonalProtocol {
 *       @FormUrlEncoded
 *       @POST("user/personalListInfo")
 *       Call<Response<PersonalInfo>> getPersonalListInfo(@Field("cur_page") int page);
 *  }
 *
 *  Retrofit retrofit = new Retrofit.Builder().baseUrl("www.xxxx.com/").build();
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
    final okhttp3.Call.Factory callFactory;
    //url的封装 协议-域名-端口-路径-键值对
    final HttpUrl baseUrl;
    //
    final List<Converter.Factory> converterFactories;
    //适配转换器
    final List<CallAdapter.Factory> callAdapterFactories;

    final Executor callbackExecutor;
    //判断是否是提前解析这个接口
    final boolean validateEagerly;

    Retrofit(
            okhttp3.Call.Factory callFactory,
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
     * @param returnType 返回Class类型
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

    public <T> Converter<ResponseBody, T> responseBodyConverter(Type responseType, Annotation[] annotations) {
        return nextResponseBodyConverter(null, responseType, annotations);
    }

    private <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory skipPast, Type responseType, Annotation[] annotations) {

        return null;
    }

    public okhttp3.Call.Factory callFactory() {
        return callFactory;
    }

    //内部类 配置
    public static final class Builder {

        private okhttp3.Call.Factory callFactory;

        private Platform platform;
        private List<CallAdapter.Factory> adapterFactories = new ArrayList<>();

        Builder() {
            this(Platform.get());
        }

        Builder(Platform platform) {
            this.platform = platform;
        }

        public Builder baseUrl(String baseUrl) {
            return null;
        }


        public Retrofit build() {

            okhttp3.Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            Executor callbackExecutor = null;
            if (callbackExecutor == null) {
                callbackExecutor = platform.defaultCallbackExecutor();
            }

            List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories);
            adapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));
            return new Retrofit(callFactory, adapterFactories);
        }

    }

}
