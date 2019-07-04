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

    final okhttp3.Call.Factory callFactory;

    private final Map<Method, ServiceMethod<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();

    private final List<CallAdapter.Factory> adapterFactories;

    Retrofit(
            okhttp3.Call.Factory callFactory,
            List<CallAdapter.Factory> adapterFactories
    ) {
        this.callFactory = callFactory;
        this.adapterFactories = adapterFactories;
    }


    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        //是否为合法接口
        Utils.validateServiceInterface(service);

        //返回动态生成的代理类 也就是 service 的实例化对象
        return (T) Proxy.newProxyInstance(service.getClassLoader(),
                new Class<?>[]{service},
                new InvocationHandler() {

                    /**
                     * @return 返回的是 Call<R>
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

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

    private ServiceMethod<?, ?> loadServiceMethod(Method method) {
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

    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null, returnType, annotations);
    }

    private CallAdapter<?, ?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType, Annotation[] annotations) {

        //如果skipPast == null 则返回-1  除非集合里面存了null
        int start = adapterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = adapterFactories.size(); i < count; i++) {
            CallAdapter<?, ?> adapter = adapterFactories.get(i).get(returnType, annotations, this);
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
