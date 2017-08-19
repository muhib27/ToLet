package com.to.let.bd.utils.retrofit;

import com.google.android.gms.maps.model.LatLng;
import com.to.let.bd.model.google_place.GooglePlace;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiList {
    @GET
    Call<GooglePlace> getPlaces(@Url String url,
                                @Query("key") String apiKey,
                                @Query("location") String location,
                                @Query("radius") long radius,
                                @Query("type") String type);


//    @POST
//    Call<ApiResponseList<BookStoreBook>> getBookStoreBooks(@Url String url,
//                                                           @Body RequestBody entity,
//                                                           @Header("version") String version,
//                                                           @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseList<MyLibraryBook>> getMyLibraryBooks(@Url String url,
//                                                           @Body RequestBody entity,
//                                                           @Header("version") String version,
//                                                           @Header("token") String token,
//                                                           @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseList<MyLibraryBook>> getMyLibraryBooks(@Url String url,
//                                                           @Body RequestBody entity,
//                                                           @Header("version") String version,
//                                                           @Header("clientAgent") String clientAgent);

//    @GET
//    Call<ApiResponseList<Category>> getCategories(@Url String url,
//                                                  @Header("version") String version,
//                                                  @Header("clientAgent") String clientAgent);
//
//    @GET
//    Call<ApiResponseList<Publisher>> getPublishers(@Url String url,
//                                                   @Header("version") String version,
//                                                   @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseList<Feedback>> getBookReviews(@Url String url,
//                                                   @Body RequestBody entity,
//                                                   @Header("version") String version,
//                                                   @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<BookDetails> getBookDetails(@Url String url,
//                                     @Body RequestBody entity,
//                                     @Header("version") String version,
//                                     @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<BookSuggestion> getBookSuggestion(@Url String url,
//                                           @Body RequestBody entity,
//                                           @Header("version") String version,
//                                           @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseObject<UserResponse>> getUserNormalLogin(@Url String url,
//                                                             @Body RequestBody entity,
//                                                             @Header("version") String version,
//                                                             @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseObject<UserResponse>> getUserSocialLogin(@Url String url,
//                                                             @Body RequestBody entity,
//                                                             @Header("version") String version,
//                                                             @Header("clientAgent") String clientAgent,
//                                                             @Header("socialLoginType") String socialLoginType);
//
//    @POST
//    Call<ApiResponseObject<Reviews>> getSubmitReviews(@Url String url,
//                                                      @Body RequestBody entity,
//                                                      @Header("version") String version,
//                                                      @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseObject<PurchaseBook>> getPurchaseFreeBook(@Url String url,
//                                                              @Body RequestBody entity,
//                                                              @Header("version") String version,
//                                                              @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseObject<PurchaseBook>> getPurchaseBookBkash(@Url String url,
//                                                               @Body RequestBody entity,
//                                                               @Header("version") String version,
//                                                               @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ApiResponseObject<PurchaseBook>> getPurchaseBookRobi(@Url String url,
//                                                              @Body RequestBody entity,
//                                                              @Header("version") String version,
//                                                              @Header("clientAgent") String clientAgent);
//
//    @GET("/api/bookStore/HomePageDetails")
//    Call<ApiResponseList<JsonArray>> getHome(@Header("version") String version,
//                                             @Header("token") String token,
//                                             @Header("clientAgent") String clientAgent);
//
//    @GET("/api/bookStore/HomePageDetails")
//    Call<JsonObject> getHomeTest(@Header("version") String version,
//                                 @Header("token") String token,
//                                 @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<JsonPrimitive> getForgotPassword(@Url String url,
//                                          @Body RequestBody entity,
//                                          @Header("version") String version,
//                                          @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<JsonObject> getCommonResponse(@Url String url,
//                                       @Body RequestBody entity,
//                                       @Header("version") String version,
//                                       @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<JsonObject> getCommonResponse(@Url String url,
//                                       @Body RequestBody entity,
//                                       @Header("version") String version,
//                                       @Header("token") String token,
//                                       @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ResponseBody> getResponseBody(@Url String url,
//                                       @Body RequestBody entity,
//                                       @Header("version") String version,
//                                       @Header("clientAgent") String clientAgent);
//
//    @POST
//    Call<ResponseBody> getResponseBody(@Url String url,
//                                       @Body RequestBody entity,
//                                       @Header("version") String version,
//                                       @Header("token") String token,
//                                       @Header("clientAgent") String clientAgent);
//
//    @POST("/api/download/getContent")
//    Call<ResponseBody> getDownloadBook(@Body RequestBody entity,
//                                       @Header("version") String version,
//                                       @Header("clientAgent") String clientAgent);
//
//    @GET("/listparams")
//    Call<ResponseBody> getRobiMobileNumber();
////    @POST
////    public Call<JsonObject> getCommonResponse(@Url String url,
////                                              @Body RequestBody entity,
////                                              @Header("version") String version,
////                                              @Header("clientAgent") String clientAgent,
////                                              @Header("socialLoginType") String socialLoginType);
}
