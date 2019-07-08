package com.bob.retrofit.proxy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bob.retrofit.R;

import java.io.FileOutputStream;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static void main(String[] args) {
        Person person = (Person)java.lang.reflect.Proxy.newProxyInstance(Person.class.getClassLoader(),
                new Class[]{Person.class},
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.print("reflect-proxy + " + proxy.getClass().hashCode() +" \n");
                        System.out.print("reflect-洗手！！！！ \n");
                        System.out.print(String.format("reflect-方法名字叫---> %s \n", method.toString()));

                        //这里proxy可以替换 Person的是实现类
                        //Object invoke = method.invoke(proxy, args);
                        System.out.print("reflect-擦嘴！！！！\n");
                        return null;
                    }
                });


        //第二个参数为代理类实现的接口
//        Person person = (Person) Proxy.newProxyInstance(Person.class.getClassLoader(), new Class[]{Person.class},
//                new InvocationHandler() {
//                    @Override
//                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                        System.out.print("proxy + " + proxy.getClass().hashCode() +" \n");
//                        System.out.print("洗手！！！！ \n");
//                        System.out.print(String.format("方法名字叫---> %s \n", method.toString()));
////                        Object invoke = method.invoke(proxy, args);
//                        System.out.print("擦嘴！！！！\n");
//                        return method.invoke(proxy, args);
//                    }
//                });

        // person 与 proxy 是同一个对象吗
        System.out.print("person + " + person.getClass().hashCode() +" \n");
        person.eat();

        saveProxyClassFile();
    }

    private static void saveProxyClassFile() {
        try {
            Class<?> proxyGenerator = Class.forName("sun.misc.ProxyGenerator");
            System.out.print(String.format("类名字叫---> %s \n", proxyGenerator.toString()));
            Method[] methods = proxyGenerator.getMethods();
            for (Method method : methods) {
                if ("generateProxyClass".equals(method.getName()) && method.getParameterTypes().length == 2) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (Class clazz : parameterTypes) {
                        System.out.print(String.format("parameterTypes---> %s \n", clazz.getName()));
                    }
                    byte[] proxy = (byte[]) method.invoke(null,"$Proxy0", new Class[]{Person.class});
                    saveFile(proxy);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(String.format("Exception---> %s \n", e.toString()));
        }
    }

    private static void saveFile(byte[] proxy) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("$Proxy0_system.class");
            fileOutputStream.write(proxy);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
