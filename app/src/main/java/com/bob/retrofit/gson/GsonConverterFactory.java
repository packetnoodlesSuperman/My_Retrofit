package com.bob.retrofit.gson;

import android.support.annotation.Nullable;

import com.bob.retrofit.Converter;
import com.bob.retrofit.Retrofit;
import com.bob.retrofit.okhttp.RequestBody;
import com.bob.retrofit.okhttp.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


/**
 * @POST
 * @Multipart
 * Call<ResponseBody>method1(@Part("p") RequestBody body);
 *
 * @POST
 * Call<Void>method2(@Body RequestBody body);
 * ----------------------------------------------------------------------------
 * @POST
 * @Multipart
 *  Call<String>method1(@Part("p") Map<String,String> body);
 *
 * @POST
 * Call<List<User>>method2(@Body User body);
 *
 * 最上面没有添加自定义converter时可以做的
 * 下面是是添加了GsonConverter后可以做的
 *
 * 可以发现仅仅使用默认的converter的话会在使用上存在巨大限制。
 * 1. 没有添加自定义converter
 *     --> 使用@Part,@PartMap,@Body标记的参数类型就只能是RequestBody
 *     --> 对于方法的返回结果来说 方法放回结果的泛型参数只支持ResponseBody与Void
 * 2. 添加自定义converter
 *     --> 参数和方法返回结果类型 可以使用不同的bean
 */
public class GsonConverterFactory extends Converter.Factory {

    public static GsonConverterFactory create() {
        return create(new Gson());
    }

    private static GsonConverterFactory create(Gson gson) {
        return new GsonConverterFactory(gson);
    }

    private final Gson gson;

    public GsonConverterFactory(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonResponseBodyConverter<>(gson, adapter);
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter<>(gson, adapter);
    }
}
