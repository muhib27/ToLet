package com.to.let.bd.utils.retrofit;

import com.to.let.bd.model.google_place.GooglePlace;
import com.to.let.bd.utils.Urls;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConstants {
    private static OkHttpClient client;

    private static OkHttpClient getHttpClient() {
        if (client != null) {
            return client;
        }
        client = new OkHttpClient.Builder()
                .readTimeout(retrofitReadTimeOut, TimeUnit.SECONDS)
                .connectTimeout(retrofitConnectionTimeOut, TimeUnit.SECONDS)
                .build();
        return client;
    }

    //in seconds
    private static final long retrofitReadTimeOut = 60;
    private static final long retrofitConnectionTimeOut = 60;

    private static Retrofit retrofit;

    private static Retrofit getRetrofitInstance() {
        if (retrofit != null) {
            return retrofit;
        }
        retrofit = new Retrofit.Builder()
                .client(getHttpClient())
                .baseUrl(Urls.getGoogleApiBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    private static ApiList getApiList() {
        return getRetrofitInstance().create(ApiList.class);
    }

    public static RequestBody getRequestBody(String jsonString) {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
    }

    public static Call<GooglePlace> getGooglePlaces(String location, String type) {
        return getApiList().getPlaces(Urls.getGooglePlaceNearBy(), Urls.getGoogleApiKey(), location, 1000, type);
    }
}
