package com.bob.retrofit.regex;

/**
 * Created by xhb on 2019/7/8.
 */

public interface Matcher {

    boolean matches(); //匹配结果

    Pattern pattern(); //返回正则表达式的包装类

    String group(); //其实调用的就是group(0)

    /**
     * Pattern.compile("W(or)(ld)")
     * world 就是整个串 就是group() 也是group(0)
     * or 就是group(1)
     * ld 就是group(2)
     */
    String group(int group); //group(0) 就是整个串
                             //group(1) 就是第一个括号包的表达式

    int groupCount();

    boolean lookingAt();

    Matcher region(int start, int end);

    boolean find(); //相当于迭代器的 hasNext(), 判断后面是否有匹配的正则的字符


}
