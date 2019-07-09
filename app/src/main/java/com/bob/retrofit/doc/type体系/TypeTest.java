package com.bob.retrofit.doc.type体系;

import android.support.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class TypeTest {

    @RequiresApi(api = 28)
    public static void main(String[] args) throws NoSuchMethodException {
        Person person = new Person();
        Method eat = person.getClass().getMethod("eat");
        Type eatReturnType = eat.getGenericReturnType();

        System.out.println("eat__ReturnType; " + eatReturnType.getClass().getSimpleName());
        if (eatReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) eatReturnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type actualTypeArgument = actualTypeArguments[0];
            System.out.println("actualTypeArgument; " + actualTypeArgument.getClass().getSimpleName());

            if (actualTypeArgument instanceof WildcardType) {
                Type[] upperBounds = ((WildcardType) actualTypeArgument).getUpperBounds();
                System.out.println("upperBounds; " + upperBounds[0].getTypeName());
            }
        }

        Method name = person.getClass().getMethod("name");
        Type nameReturnType = name.getGenericReturnType();
        System.out.println("name___ReturnType; " + nameReturnType.getClass().getSimpleName());

        Method age = person.getClass().getMethod("age");
        Type ageReturnType = age.getGenericReturnType();
        System.out.println("age___ReturnType; " + ageReturnType.getClass().getSimpleName());

    }

    //打印结果
    //eat__ReturnType; ParameterizedTypeImpl
    //actualTypeArgument; WildcardTypeImpl
    //upperBounds; java.lang.String
    //name___ReturnType; TypeVariableImpl
    //age___ReturnType; GenericArrayTypeImpl

    static class Person {

        public Call<String> eat(){ return null; }

        public <T> T name() { return null; }

        public <T> T[] age() { return null; }
    }

    static interface Call<T>{}

}
