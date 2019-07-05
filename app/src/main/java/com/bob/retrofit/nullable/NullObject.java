package com.bob.retrofit.nullable;

public class NullObject implements DependencyBase {
    @Override
    public void Operation() {
        //do nothing
    }

    @Override
    public boolean isNull() {
        return true;
    }
}
