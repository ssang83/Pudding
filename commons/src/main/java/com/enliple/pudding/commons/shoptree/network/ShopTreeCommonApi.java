package com.enliple.pudding.commons.shoptree.network;

import android.content.Context;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;

public class ShopTreeCommonApi {
    public String url;
    public Callback<ResponseBody> listenerRetrofit;
    public Map<String, String> param;
    public Map<String, String> mHeader;
    public String mHeaderStr;
    public RequestBody body;
    public void execute(Context context) {
        if (context == null)
            return;
        if (param != null && param.size() > 0)
            ShopTreeRetrofitService.api(context).CommonGetAPI(url, param).enqueue(listenerRetrofit);
        else
            ShopTreeRetrofitService.api(context).CommonGetAPI(url).enqueue(listenerRetrofit);
    }

    public void postExecute(Context context) {
        if (context == null)
            return;

        if (param != null && param.size() > 0) {
            ShopTreeRetrofitService.api(context).CommonPostAPI(url, param).enqueue(listenerRetrofit);
        } else if(body != null) {
            ShopTreeRetrofitService.api(context).CommonPostAPI1(url, body).enqueue(listenerRetrofit);
        } else {
            ShopTreeRetrofitService.api(context).CommonPostAPI(url).enqueue(listenerRetrofit);
        }
    }

    public void putExecute(Context context) {
        if (param != null && param.size() > 0) {
            ShopTreeRetrofitService.api(context).CommonPutAPI(url, param).enqueue(listenerRetrofit);
        } else if(body != null) {
            ShopTreeRetrofitService.api(context).CommonPutAPI1(url, body).enqueue(listenerRetrofit);
        } else {
            ShopTreeRetrofitService.api(context).CommonPutAPI(url).enqueue(listenerRetrofit);
        }
    }

    public void deleteExecute(Context context) {
        if (param != null && param.size() > 0) {
            ShopTreeRetrofitService.api(context).CommonDeleteAPI(url, param).enqueue(listenerRetrofit);
        } else if(body != null) {
            ShopTreeRetrofitService.api(context).CommonDeleteAPI1(url, body).enqueue(listenerRetrofit);
        } else {
            ShopTreeRetrofitService.api(context).CommonDeleteAPI(url).enqueue(listenerRetrofit);
        }
    }

    public interface CallbackStringResponse {
        void onResponse(String result);

        void onError(String error);
    }


    public interface CallbackObjectResponse {
        void onResponse(Object result);

        void onError(String error);
    }


}