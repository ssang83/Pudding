package com.enliple.pudding.commons.chat;

import org.json.JSONObject;

/**
 * Callback function of server response.
 */
public interface DataCallBack {
    void responseData(JSONObject message);
}