package com.bob.retrofit;

//回调
public interface Callback<T> {

    void onResponse(Call<T> call, Response<T> response);

    void onFailure(Call<T> call, Throwable t);

}
