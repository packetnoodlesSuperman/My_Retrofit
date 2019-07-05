package com.bob.retrofit.nullable;

public class Dependency implements DependencyBase, Nullable {
    @Override
    public void Operation() {

    }

    @Override
    public boolean isNull() {
        return false;
    }
}
