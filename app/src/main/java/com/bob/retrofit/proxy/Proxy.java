package com.bob.retrofit.proxy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Proxy implements Serializable {

    public static Object newProxyInstance(
            ClassLoader classLoader,
            Class<?>[] interfaceNames,
            InvocationHandler handler
    ) {
        //第一步 用字符创拼接一个$Proxy0.java文件
        StringBuilder proxyClass = step1(interfaceNames[0]);

        //第二步 保存文件
        step2(proxyClass);

        //第三步 编译java文件
        step3();

        //第四步 将编译的class加载进内存
        Object o = step4(handler);
        return o;
    }


    protected InvocationHandler h;

    static String newLine = "\r\n";

    static String fileDir = "D:\\AndroidProject\\retrofit\\app\\src\\main\\java\\com\\bob\\retrofit\\proxy";

    public Proxy(InvocationHandler h) {
        this.h = h;
    }


    private static StringBuilder step1(Class<?> interfaceName) {
        return new StringBuilder()
                .append("package com.bob.retrofit.proxy;").append(newLine)
                .append("import java.lang.reflect.Method;").append(newLine)
                .append("import com.bob.retrofit.proxy.Proxy;").append(newLine)
                .append("import com.bob.retrofit.proxy.InvocationHandler;").append(newLine)
                .append(String.format("public final class $Proxy0 extends %s implements %s {", "Proxy", interfaceName.getName())).append(newLine)
                .append("   public $Proxy0(InvocationHandler var1) {" ).append(newLine)
                .append("       super(var1);" ).append(newLine)
                .append("   }" ).append(newLine)
                .append(getMethod(interfaceName))
                .append("}");

    }

    private static StringBuilder getMethod(Class<?> interfaceName) {
        Method[] methods = interfaceName.getMethods();
        StringBuilder builder = new StringBuilder();
        for (Method method : methods) {
            builder.append(String.format("   public final void %s() {", method.getName())).append(newLine)
                    .append("       try {").append(newLine)
                    .append(String.format("           Method md = %s.class.getMethod(\"%s\", new Class[]{});", interfaceName.getName(), method.getName())).append(newLine)
                    .append("           this.h.invoke(this, md, (Object[])null);").append(newLine)
                    .append("       } catch (Exception e) {").append(newLine)
                    .append("           e.printStackTrace();").append(newLine)
                    .append("       } catch (Throwable throwable) {").append(newLine)
                    .append("           throwable.printStackTrace();").append(newLine)
                    .append("       }").append(newLine)
                    .append("   }").append(newLine);
        }

        return builder;
    }

    private static void step2(StringBuilder proxyClass) {
        File file = new File(fileDir, "./$Proxy0.java");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(proxyClass.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    // StandardJavaFileManager standardJavaFileManager = javaCompiler.getStandardFileManager(null, null, null);
    // Iterable it = standardJavaFileManager.getJavaFileObjects(fileName)
    // JavaCompiler.CompilationTask task = javaCompiler.getTask(null, standardJavaFileManager, null, null, null, it)
    // task.call();
    // standardJavaFileManager.close();
    private static void step3() {
        try {
            //第一行
            Class<?> toolProviderClass = Class.forName("javax.tools.ToolProvider");
            Method getSystemJavaCompiler = toolProviderClass.getMethod("getSystemJavaCompiler");
            Object javaCompiler = getSystemJavaCompiler.invoke(null);
            //第二行
            Method task = null;
            Object standardJavaFileManager = null;
            for (Method method : javaCompiler.getClass().getDeclaredMethods()) {
                if ("getStandardFileManager".equals(method.getName()) && method.getParameterTypes().length == 3 && standardJavaFileManager == null) {
                    standardJavaFileManager = method.invoke(javaCompiler, null, null, null);
                }
                if ("getTask".equals(method.getName()) && task == null) {
                    task = method;
                }
            }
            //第三行
            Method getJavaFileObjects = standardJavaFileManager.getClass().getMethod("getJavaFileObjects", String[].class);
            Object it = getJavaFileObjects.invoke(standardJavaFileManager, (Object)new String[]{fileDir + "\\$Proxy0.java"});
            //第四行
            Object compilationTask = task.invoke(javaCompiler, null, standardJavaFileManager, null, null, null, it);
            //第五行
            Method call = compilationTask.getClass().getDeclaredMethod("call");
            call.invoke(compilationTask);
            Method close = standardJavaFileManager.getClass().getMethod("close");
            close.invoke(standardJavaFileManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object step4(InvocationHandler h) {
        MyClassLoader classLoader = new MyClassLoader(fileDir);
        try {
            Class<?> $Proxy0Clazz = classLoader.findClass("$Proxy0");
            Constructor<?> constructor = $Proxy0Clazz.getConstructor(InvocationHandler.class);
            Object o = constructor.newInstance(h);
            return o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
