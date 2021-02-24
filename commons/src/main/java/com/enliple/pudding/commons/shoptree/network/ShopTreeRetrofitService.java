package com.enliple.pudding.commons.shoptree.network;

import android.content.Context;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;

public class ShopTreeRetrofitService extends ShopTreeBaseRetrofit {
    public static CommonAPI api(Context context) {
        return (CommonAPI) retrofit(context, CommonAPI.class);
    }

    public interface CommonAPI {

        @GET
        Call<ResponseBody> CommonGetAPI(@retrofit2.http.Url String arg, @Header("Cookie") String cookie);

        @GET
        Call<ResponseBody> CommonGetAPI(@retrofit2.http.Url String arg, @QueryMap Map<String, String> params, @Header("Cookie") String cookie);

        @GET
        Call<ResponseBody> CommonGetAPI(@retrofit2.http.Url String arg, @QueryMap Map<String, String> params);

        @GET
        Call<ResponseBody> CommonGetAPI(@retrofit2.http.Url String arg);


        @POST
        Call<ResponseBody> CommonPostAPI(@retrofit2.http.Url String arg);

        @POST
        Call<ResponseBody> CommonPostAPI(@retrofit2.http.Url String arg, @Header("Cookie") String cookie);

        @FormUrlEncoded
        @POST
        Call<ResponseBody> CommonPostAPI(@retrofit2.http.Url String arg, @FieldMap Map<String, String> params);

        @FormUrlEncoded
        @POST
        Call<ResponseBody> CommonPostAPI(@retrofit2.http.Url String arg, @FieldMap Map<String, String> params, @Header("Cookie") String cookie);

        // put 추가
        @PUT
        Call<ResponseBody> CommonPutAPI(@retrofit2.http.Url String arg);

        @PUT
        Call<ResponseBody> CommonPutAPI(@retrofit2.http.Url String arg, @FieldMap Map<String, String> param);

        @PUT
        Call<ResponseBody> CommonPutAPI1(@retrofit2.http.Url String arg, @Body RequestBody body);

        @FormUrlEncoded
        @PUT
        Call<ResponseBody> CommonPutAPI(@retrofit2.http.Url String arg, @FieldMap Map<String, String> params, @Header("Cookie") String cookie);

        @DELETE
        Call<ResponseBody> CommonDeleteAPI(@retrofit2.http.Url String arg);

        @DELETE
        Call<ResponseBody> CommonDeleteAPI(@retrofit2.http.Url String arg, @QueryMap Map<String, String> param);

        @DELETE
        Call<ResponseBody> CommonDeleteAPI1(@retrofit2.http.Url String arg, @Body RequestBody body);

        @FormUrlEncoded
        @DELETE
        Call<ResponseBody> CommonDeleteAPI(@retrofit2.http.Url String arg, @QueryMap Map<String, String> params, @Header("Cookie") String cookie);

        @POST
        Call<ResponseBody> CommonPostAPI1(@retrofit2.http.Url String arg, @Body RequestBody body);
    }


}
