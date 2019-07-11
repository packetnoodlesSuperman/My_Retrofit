package com.bob.retrofit.okhttp.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * @Desc 实现类SYSTEM.所以可以FileSystem看成okhttp中文件系统对okio的桥接管理类
 */
public interface FileSystem {

    FileSystem SYSTEM = new FileSystem() {
        @Override
        public Source source(File file) throws FileNotFoundException {
            return Okio.source(file);
        }

        @Override
        public Sink sink(File file) throws FileNotFoundException {
            try {
                return Okio.sink(file);
            } catch (FileNotFoundException e) {
                file.getParentFile().mkdirs();
                return Okio.sink(file);
            }
        }

        //获取File的Sink,拼接用的(用于写)
        @Override
        public Sink appendingSink(File file) throws FileNotFoundException {
            try {
                return Okio.appendingSink(file);
            } catch (FileNotFoundException e) {
                file.getParentFile().mkdirs();
                return Okio.appendingSink(file);
            }
        }

        @Override
        public void delete(File file) throws IOException {
            if (!file.delete() && file.exists()) {
                throw new IOException("failed to delete " + file);
            }
        }

        @Override
        public boolean exists(File file) {
            return file.exists();
        }

        @Override
        public long size(File file) {
            return file.length();
        }

        //文件改名
        @Override
        public void rename(File from, File to) throws IOException {
            delete(to);
            if (!from.renameTo(to)) {
                throw new IOException("failed to rename " + from + " to " + to);
            }
        }

        //删除文件夹
        @Override
        public void deleteContents(File directory) throws IOException {
            File[] files = directory.listFiles();
            if (files == null) {
                throw new IOException("not a readable directory: " + directory);
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteContents(file);
                }
                if (!file.delete()) {
                    throw new IOException("failed to delete " + file);
                }
            }
        }
    };

    Source source(File file) throws FileNotFoundException;

    Sink sink(File file) throws FileNotFoundException;

    Sink appendingSink(File file) throws FileNotFoundException;

    void delete(File file) throws IOException;

    boolean exists(File file);

    long size(File file);

    void rename(File from, File to) throws IOException;

    //删除文件夹
    void deleteContents(File directory) throws IOException;
}