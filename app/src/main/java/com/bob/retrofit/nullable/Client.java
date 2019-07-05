package com.bob.retrofit.nullable;

//https://www.jianshu.com/p/b9bf43ba75fe
public class Client {

    public void test(DependencyBase dependencyBase){
        Factory.get(dependencyBase).Operation();
    }

}
