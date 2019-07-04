package com.bob.retrofit.proxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class MyClassLoader extends ClassLoader {

    private File dir;

    public MyClassLoader(String path) {
        this.dir = new File(path);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        File clazzFile = new File(dir, name+".class");
        //加载到内存就是 将流信息读到jvm中
        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(clazzFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return defineClass("com.bob.retrofit.proxy."+name,
                    baos.toByteArray(), 0, baos.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.findClass(name);
    }
}
