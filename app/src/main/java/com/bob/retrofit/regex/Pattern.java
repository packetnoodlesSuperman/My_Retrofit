package com.bob.retrofit.regex;

import java.util.regex.Matcher;

/**
 * Created by xhb on 2019/7/8.
 *
 * Pattern p = Pattern.compile("a*b"); //Pattern  包装正则表达式
 * Matcher m = p.matcher("aaaaab");     //Matcher 为匹配器 匹配的一些结果
 * boolean b = m.matches();
 *
 *
 * [abc]        匹配a、b、c任意一个字符
 * [a-zA-Z]     匹配a-z或A-Z任意一个字符
 * .            匹配除了换行符外的任意字符
 * \d           匹配数组 等价于 [0-9]
 * \D           匹配任意非数字的字符
 * \w           匹配 等价于 [a-zA-Z_0-9]
 * \W           反之
 * {n}          重复n次
 * {n,}         重复n次多n+次
 * {n, m}       重复n到m次
 */

public class Pattern {

    public static Pattern compile(String regex) { return  null;}

    public static Pattern compile(String regex, int flags) {return  null;}

    public Matcher matcher(CharSequence input) { return null; }

    public int flags() { return /*flags;*/ -1;}

}
