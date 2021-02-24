package com.enliple.pudding.commons.shoptree.network;

import com.enliple.pudding.commons.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ShopTreeAsyncApi extends ShopTreeCommonApi {

    CallbackObjectResponse callback;

    public ShopTreeAsyncApi(String _url, Map<String, String> params, RequestBody bd, CallbackObjectResponse _response) {
        url = _url;
        param = params;
        callback = _response;
        body = bd;
        try {
            listenerRetrofit = new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if (response != null && response.isSuccessful() && response.body() != null) {
                        try {
                            String str = response.body().string();
                            Logger.w("str :: " + str);
                            if (str.startsWith("{"))
                                callback.onResponse(new JSONObject(str));
                            else if (str.startsWith("[")) {
                                callback.onResponse(new JSONArray(str).getJSONObject(0));
                            } else
                                callback.onResponse(str);
                        } catch (IOException e) {
                            System.out.println("error => " + e.getMessage());
                            callback.onError("error::" + e.getMessage());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            System.out.println("error => " + e.getMessage());
                            callback.onError("error::" + e.getMessage());
                        }
                    } else if (response != null && response.errorBody() != null) {
                        try {
                            String str = response.errorBody().string();
                            Logger.w("str :: " + str);
                            if (str.startsWith("{"))
                                callback.onResponse(new JSONObject(str));
                            else if (str.startsWith("[")) {
                                callback.onResponse(new JSONArray(str).getJSONObject(0));
                            } else
                                callback.onResponse(str);
                        } catch (IOException e) {
                            System.out.println("error => " + e.getMessage());
                            callback.onError("error::" + e.getMessage());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            System.out.println("error => " + e.getMessage());
                            callback.onError("error::" + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    if (callback != null)
                        callback.onError("error::" + t.getMessage());

                    System.out.println("error => " + t.getMessage());
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("error::" + e.getMessage());
        }
    }
}