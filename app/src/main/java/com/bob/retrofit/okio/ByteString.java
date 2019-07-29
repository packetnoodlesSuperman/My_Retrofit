package com.bob.retrofit.okio;

import android.support.annotation.NonNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 本质是一个持有byte数组和对应utf8编码String的类
 * 缓存utf-8字符串可有效提高utf-8编解码效率，更适用于网络传输
 * 能够让这个类在Byte和String转换上基本没有开销<空间换时间的概念></>
 *
 * transient 关键字  （之前和volatile关键字 可见性  搞混淆了）
 * 简单地说，就是让某些被修饰的成员属性变量不被序列化
 */
public class ByteString implements Serializable, Comparable<ByteString> {

    //digits 数字
    static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    //不可变字节序列
    final byte[] data;
    transient int hashCode;
    transient String utf8;

    //构造函数
    ByteString(byte[] data) {
        this.data = data;
    }

    /**
     * 克隆该ByteString
     */
    public static ByteString of(byte... data) {
        if (data == null) throw new IllegalArgumentException("data == null");
        return new ByteString(data.clone());
    }

    //克隆该ByteString 指定的区域
    public static ByteString of(byte[] data, int offset, int byteCount) { //offset 偏移量 就是从哪里开始
        if (data == null) throw new IllegalArgumentException("data == null");
        Util.checkOffsetAndCount(data.length, offset, byteCount);

        byte[] copy = new byte[byteCount];
        System.arraycopy(data, offset, copy, 0, byteCount);
        return new ByteString(copy);
    }

//    public static ByteString of(ByteBuffer data) {
//        if (data == null) throw new IllegalArgumentException("data == null");
//
//        byte[] copy = new byte[data.remaining()];
//        data.get(copy);
//        return new ByteString(copy);
//    }


    //使用Utf8对s编码 s保存在data中 静态方法
    public static ByteString encodeUtf8(String s) {
        if (s == null) throw new IllegalArgumentException("s == null");

        ByteString byteString = new ByteString(s.getBytes(Util.UTF_8));
        byteString.utf8 = s;
        return byteString;
    }

    //指定编码方式编码 静态方法
    public static ByteString encodeString(String s, Charset charset) {
        if (s == null) throw new IllegalArgumentException("s == null");
        if (charset == null) throw new IllegalArgumentException("charset == null");
        return new ByteString(s.getBytes(charset));
    }


    /**
     * @return 获取data的 utf8 编码方式的字符串
     * data编码还是之前的编码方式
     */
    public String utf8() {
        String result = utf8;
        return result != null ? result : (utf8 = new String(data, Util.UTF_8));
    }

    //成员方法  指定对data的编码方式
    public String string(Charset charset) {
        if (charset == null) throw new IllegalArgumentException("charset == null");
        return new String(data, charset);
    }

    //对data进行 base64编码
    public String base64() {
        return Base64.encode(data);
    }

    //对data进行MD5  摘要算法，也是加密算法的一种，还有另外一种叫法：指纹
    //单向散列算法 消息摘要算法
    //由于其单向运算，具有一定的不可逆性
    public ByteString md5() { return digest("MD5"); }
    public ByteString sha1() { return digest("SHA-1"); }
    public ByteString sha256() { return digest("SHA-256"); }
    public ByteString sha512() { return digest("SHA-512"); }

    private ByteString digest(String algorithm) {
        try {
            //Java自带的加密类MessageDigest类 : MessageDigest类为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法
            //getInstance 创建MessageDigest对象
            //digest 获取摘要（加密），结果是字节数组
            return ByteString.of(MessageDigest.getInstance(algorithm).digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int compareTo(@NonNull ByteString o) {
        return 0;
    }

    /**
     * 读32个字节的对象 到ByteString
     */
    private void readObject(ObjectInputStream in) throws IOException {
        //读取一个32位的整数
        int dataLength = in.readInt();
        ByteString byteString = ByteString.read(in, dataLength);
        try {
            Field field = ByteString.class.getDeclaredField("data");
            field.setAccessible(true);
            field.set(this, byteString.data);
        } catch (NoSuchFieldException e) {
            throw new AssertionError();
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        }
    }

    public static ByteString read(InputStream in, int byteCount) throws IOException {
        if (in == null) throw new IllegalArgumentException("in == null");
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);

        byte[] result = new byte[byteCount];
        for (int offset = 0, read; offset < byteCount; offset += read) {
            read = in.read(result, offset, byteCount - offset);
            if (read == -1) throw new EOFException();
        }
        return new ByteString(result);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }
}
