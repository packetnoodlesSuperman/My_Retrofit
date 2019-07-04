package com.bob.retrofit;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public final class Utils {

    public static <T> void validateServiceInterface(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        //不能继承
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
        }
    }

    public static Class<?> getRawType(Type type) {

        return null;
    }

    static Type getCallResponseType(Type returnType) {

        return getParameterUpperBound(0, (ParameterizedType) returnType);
    }

    /**
     * @param index
     * @param type ParameterizedType表示参数化类型
     */
    static Type getParameterUpperBound(int index, ParameterizedType type) {
        //获取泛型中的实际类型，可能会存在多个泛型，例如Map<K,V>,所以会返回Type[]数组
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException(
                    "Index " + index + " not in range [0," + types.length + ") for " + type);
        }

        Type paramType = types[index];
        //当需要描述的类型是泛型类，而且泛型类中的泛型被定义为(? extends xxx)或者(? super xxx)这种类型，
        // 比如List<? extends TestReflect>，这个类型首先将由ParameterizedType实现，
        // 当调用ParameterizedType的getActualTypeArguments()方法后得到的Type就由WildcardType实现
        if (paramType instanceof WildcardType) {

            //WildcardType接口有getUpperBounds()方法，得到的是类型的上边界的Type数组，实际上就是类型的直接父类，也就是extends后面的类型
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }
}
