package com.to.let.bd.utils.retrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallBack<T> implements Callback<T> {
    @Override
    public void onFailure(Call call, Throwable t) {

    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
//        if(){
//            onSuccess(response);
//        }
    }

    abstract void onSuccess(Response<T> response);

    abstract void onFailure();
}
