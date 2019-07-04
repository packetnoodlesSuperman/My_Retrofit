package com.bob.retrofit;

public interface Callback<T> {

    void onResponse(Call<T> call, Response<T> response);

    void onFailture(Call<T> call, Throwable t);

}
