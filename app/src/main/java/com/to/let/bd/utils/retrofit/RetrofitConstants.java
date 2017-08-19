package com.to.let.bd.utils.retrofit;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.to.let.bd.model.google_place.GooglePlace;
import com.to.let.bd.utils.Urls;

import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConstants {
    private static OkHttpClient getHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(retrofitReadTimeOut, TimeUnit.SECONDS)
                .connectTimeout(retrofitConnectionTimeOut, TimeUnit.SECONDS)
                .build();
    }

    //in seconds
    private static final long retrofitReadTimeOut = 60;
    private static final long retrofitConnectionTimeOut = 60;

    private static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .client(getHttpClient())
                .baseUrl(Urls.getGoogleApiBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiList getApiList() {
        return getRetrofitInstance().create(ApiList.class);
    }

    public static RequestBody getRequestBody(String jsonString) {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
    }

    public static Call<GooglePlace> getGooglePlaces(String location, String type) {
        return getApiList().getPlaces(Urls.getGooglePlaceNearBy(), Urls.getGoogleApiKey(), location, 1000, type);
    }
}
