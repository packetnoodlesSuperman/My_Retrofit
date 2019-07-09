package com.bob.retrofit;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * @Desc 区分平台
 */
public class Platform {
    //获取相应的平台
    private static final Platform PLATFORM = findPlatform();
    static Platform get() { return PLATFORM; }

    /**
     * @return 获取相应的平台
     */
    private static Platform findPlatform() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                return new Android();
            }
        } catch (ClassNotFoundException ignored) { }

        try {
            Class.forName("java.util.Optional");
            return new Java8();
        } catch (ClassNotFoundException ignored) { }

        return new Platform();
    }

    //为Retrofit的callbackExecutor 提供默认的线程 由不同平台重写该方法
    //Android平台使用的是Handler的主Looper 主Looper比绑定了主线程
    Executor defaultCallbackExecutor() {
        return null;
    }

    CallAdapter.Factory defaultCallAdapterFactory(Executor callbackExecutor) {
        if (callbackExecutor != null) {
            return new ExecutorCallAdapterFactory(callbackExecutor);
        }
        return DefaultCallAdapterFactory.INSTANCE;
    }

    public boolean isDefaultMethod(Method method) {
        return false;
    }

    public Object invokeDefaultMethod(Method method, Class<?> service, Object proxy, Object[] args) throws Throwable {
        throw new UnsupportedOperationException();
    }

    //////////////////////////////////////////// Android平台 //////////////////////////////////////////////////////////////////////////
    /**
     * @Desc Android 平台
     */
    static class Android extends Platform {
        @Override
        Executor defaultCallbackExecutor() {
            return new MainThreadExecutor();
        }

        @Override
        public CallAdapter.Factory defaultCallAdapterFactory(Executor callbackExecutor) {
            if (callbackExecutor == null) throw new AssertionError();
            return new ExecutorCallAdapterFactory(callbackExecutor);
        }

        //主线程 为Retrofit的callback回调 提供默认的线程， 运行在主线程上
        static class MainThreadExecutor implements Executor {
            private final Handler handler = new Handler(Looper.getMainLooper());
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        }
    }

    //////////////////////////////////////////// Java8平台 //////////////////////////////////////////////////////////////////////////
    public static final class Java8 extends Platform {

    }
}