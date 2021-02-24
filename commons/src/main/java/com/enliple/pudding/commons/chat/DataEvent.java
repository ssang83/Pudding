package com.enliple.pudding.commons.chat;

import org.json.JSONObject;

import java.util.EventObject;

/**
 * Data event of broadcast message.
 */
public class DataEvent extends EventObject {

    private JSONObject message;

    public JSONObject getMessage() {
        return message;
    }

    public void setMessage(JSONObject message) {
        this.message = message;
    }

    public DataEvent(Object source, JSONObject message) {
        super(source);
        this.message = message;
    }

}
