package com.bob.retrofit.okhttp.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.bob.retrofit.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.internal.Util;
import okio.BufferedSink;

public class DiskLruCache implements Cloneable, Flushable {

    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_TEMP = "journal.tmp";
    static final String JOURNAL_FILE_BACKUP = "journal.bkp";
    static final String MAGIC = "libcore.io.DiskLruCache";
    static final String VERSION_1 = "1";
    static final long ANY_SEQUENCE_NUMBER = -1;
    static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    private static final String REMOVE = "REMOVE";
    private static final String READ = "READ";

    final FileSystem fileSystem;
    final File directory;
    private final File journalFile;
    private final File journalFileTmp;
    private final File journalFileBackup;
    private final int appVersion;
    private long maxSize;
    final int valueCount;
    private long size = 0;
    BufferedSink journalWriter;
    final LinkedHashMap<String, DiskLruCache.Entry> lruEntries = new LinkedHashMap<>(0, 0.75f, true);
    int redundantOpCount;
    boolean hasJournalErrors;
    //是否初始化， 只能初始化一次
    boolean initialized;
    boolean closed;
    boolean mostRecentTrimFailed;
    boolean mostRecentRebuildFailed;
    private long nextSequenceNumber = 0;
    private final Executor executor;

    /**
     * @Desc 构造函数
     */
    DiskLruCache(FileSystem fileSystem, File directory, int appVersion, int valueCount, long maxSize, Executor executor) {
        this.fileSystem = fileSystem;
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, JOURNAL_FILE);
        this.journalFileTmp = new File(directory, JOURNAL_FILE_TEMP);
        this.journalFileBackup = new File(directory, JOURNAL_FILE_BACKUP);
        this.valueCount = valueCount;
        this.maxSize = maxSize;
        this.executor = executor;
    }


    /**
     * @Desc 创建
     */
    public static DiskLruCache create(FileSystem fileSystem, File directory, int appVersion, int valueCount, long maxSize) {
        //maxSize 存储空间大小
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        //保存的个数
        if (valueCount <= 0) {
            throw new IllegalArgumentException("valueCount <= 0");
        }
        //创建线程池  不理解可以自己看Executor的module详解
        Executor executor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), Util.threadFactory("OkHttp DiskLruCache", true));

        //将缓存文件存放目录，软件版本号，一个key对应缓存文件数目，缓存文件上限传入构造器
        return new DiskLruCache(fileSystem, directory, appVersion, valueCount, maxSize, executor);
    }

    public Editor edit(String key) {
        return edit(key, ANY_SEQUENCE_NUMBER);
    }

    private Editor edit(String key, long expectedSequenceNumber) {
        initialize();
        checkNoClosed();
        Entry entry = lruEntries.get(key);


        return null;
    }

    //加锁的方法 初始化
    private synchronized void initialize() {
        //检测一个线程是否拥有锁
        assert Thread.holdsLock(this);
        if (initialized) {
            return;
        }

        if (fileSystem.exists(journalFileBackup)) {
            if (fileSystem.exists(journalFile)) {
                fileSystem.delete(journalFileBackup);
            } else {
                fileSystem.rename(journalFileBackup, journalFile);
            }
        }

        if (fileSystem.exists(journalFile)) {
            try {
                readJournal();
                processJournal();
                initialized = true;
                return;
            } catch (IOException e) { }

            try {
                delete();
            } finally {
                closed = false;
            }
        }

        rebuildJournal();
        initialized = true;
    }

    @Override
    public void flush() throws IOException {

    }

    /****************************** 内部类 ******************************/
    private final class Entry {
        final String key;
        final long[] lengths;
        final File[] cleanFiles;
        final File[] dirtyFiles;
        boolean readable;
        Editor currentEditor;
        long sequenceNumber;
    }

    public final class Editor {

    }


    /**
     * @Desc 对DiskLruCache的使用
     */
    public static void main(String[] args) {
        Context context = null;
        Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        File dir = new File(Environment.getExternalStorageDirectory(), "shot");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            //开启
            DiskLruCache diskLruCache = DiskLruCache.create(null, dir, 1, 1, 100 * 1024 * 1024);
            //当前时间作为名称
            DiskLruCache.Editor editor = diskLruCache.edit(Long.toString(System.currentTimeMillis()));
            //获取一个输出流
            BufferedOutputStream outputStream = new BufferedOutputStream(editor.newOutputStream(0));
            //Bitmap压缩，
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            //IO操作完毕
            editor.commit();
            //确保都写入日志
            diskLruCache.flush();
            //回收资源
            diskLruCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}