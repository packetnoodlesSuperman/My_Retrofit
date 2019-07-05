package com.bob.retrofit.nullable;

public class Factory {

    public static DependencyBase get(Nullable dependencyBase){

        if(dependencyBase ==null){
            return new NullObject();
        }
        return new Dependency();
    }

}