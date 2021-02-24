package com.enliple.pudding.commons.chat;

import org.json.JSONObject;

public class Protocol {
    public static String encode(int id, String route, JSONObject msg) {
        JSONObject transferObj = new JSONObject();
        try {
            transferObj.put("id", "" + id);
            transferObj.put("route", route);
            transferObj.put("msg", msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return transferObj.toString();
    }
}
