package com.enliple.pudding.commons.chat;

import com.enliple.pudding.commons.BuildConfig;
import com.enliple.pudding.commons.chat.socket.IOAcknowledge;
import com.enliple.pudding.commons.chat.socket.IOCallback;
import com.enliple.pudding.commons.chat.socket.SocketIO;
import com.enliple.pudding.commons.chat.socket.SocketIOException;
import com.enliple.pudding.commons.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketClient {
    private final static String HTTP_HEADER = "http://";
    private final static String HTTPS_HEADER = "https://";
    private final static String JSON_ARRAY_STARTER = "[";

    private int reqId;
    private SocketIO mSocketIO;
    private Map<Integer, DataCallBack> mCallBackMap = new HashMap<>();
    private Map<String, List<DataListener>> mListenerMap = new HashMap<>();

    public SocketClient(String url, int port) {
        StringBuffer buff = new StringBuffer();

        if(BuildConfig.IS_DEV) {
            if (!url.contains(HTTP_HEADER)) {
                buff.append(HTTP_HEADER);
            }
        } else {
            if (!url.contains(HTTPS_HEADER)) {
                buff.append(HTTPS_HEADER);
            }
        }

        buff.append(url);
        buff.append(":");
        buff.append(port);

        try {
            mSocketIO = new SocketIO(buff.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException("please check your url format.");
        }
    }

    /**
     */
    public void connect() {
        mSocketIO.connect(new IOCallback() {
            public void onConnect() {
                Logger.i("onConnect");

                emit("onConnect", null);
            }

            public void onMessage(JSONObject json, IOAcknowledge ack) {
                Logger.w("onMessage json:" + json.toString());
            }

            // get messages from the server side
            public void onMessage(String data, IOAcknowledge ack) {
                Logger.i("onMessage:" + data);

                if (data.indexOf(JSON_ARRAY_STARTER) == 0) {
                    processMessageBatch(data);
                } else {
                    processMessage(data);
                }
            }

            public void onError(SocketIOException e) {
                Logger.w("onError");

                emit("onError", null);
                //mSocketIO = null;

                Logger.p(e);
            }

            public void onDisconnect() {
                Logger.i("onDisconnect");

                emit("onDisconnect", null);
                //mSocketIO = null;
            }

            public void on(String event, IOAcknowledge ack, Object... args) {
                Logger.w("on:" + event);
            }
        });
    }

    /**
     */
    private void sendMessage(int reqId, String route, JSONObject msg) {
        if (mSocketIO != null) {
            mSocketIO.send(Protocol.encode(reqId, route, msg));
        }
    }

    /**
     */
    public void send(Object... args) {
        if (args.length < 2 || args.length > 3) {
            throw new RuntimeException("the send arguments is error.");
        }

        // first argument must be string
        if (!(args[0] instanceof String)) {
            throw new RuntimeException("the route of send is error.");
        }

        String route = args[0].toString();
        JSONObject msg = null;
        DataCallBack cb = null;

        if (args.length == 2) {
            if (args[1] instanceof JSONObject) {
                msg = (JSONObject) args[1];
            } else if (args[1] instanceof DataCallBack) {
                cb = (DataCallBack) args[1];
            }
        } else {
            msg = (JSONObject) args[1];
            cb = (DataCallBack) args[2];
        }

        msg = filter(msg);
        reqId++;
        mCallBackMap.put(reqId, cb);
        sendMessage(reqId, route, msg);
    }

    /**
     */
    public void inform(String route, JSONObject msg) {
        send(route, msg);
    }

    /**
     * Add timestamp to message.
     *
     * @param msg
     * @return msg
     */
    private JSONObject filter(JSONObject msg) {
        if (msg == null) {
            msg = new JSONObject();
        }

        long date = System.currentTimeMillis();
        try {
            msg.put("timestamp", date);
        } catch (JSONException e) {
            Logger.p(e);
        }
        return msg;
    }

    /**
     * 채팅서버와 연결해제
     */
    public void disconnect() {
        Logger.c("disconnect");

        try {
            //mListenerMap.clear();

            if (mSocketIO != null && mSocketIO.isConnected()) {
                mSocketIO.disconnect();
            }
        } catch (Exception e) {
            Logger.p(e);
        }
    }

    /**
     * 채팅서버로 부터 받은 메세지를 처리
     *
     * @param msg
     */
    private void processMessage(String msg) {
        int id;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(msg);
            // send message
            if (jsonObject.has("id")) {
                id = jsonObject.getInt("id");
                DataCallBack cb = mCallBackMap.get(id);
                cb.responseData(jsonObject.getJSONObject("body"));
                mCallBackMap.remove(id);
            } else {
                emit(jsonObject.getString("route"), jsonObject);
            }
        } catch (JSONException e) {
            Logger.p(e);
        }
    }

    /**
     * 메세지를 일괄처리
     *
     * @param msg
     */
    private void processMessageBatch(String msg) {
        Logger.e("processMessageBatch msg :: " + msg);
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(msg);
            for (int i = 0; i < jsonArray.length(); i++) {
                processMessage(jsonArray.getJSONObject(i).toString());
            }
        } catch (JSONException e) {
            Logger.p(e);
        }
    }

    /**
     * Add event listener and wait for broadcast message.
     * 채팅 연결간 발생되는 Broadcast Message 를 수신하는 DataListener 를 지정
     */
    public void on(String route, DataListener listener) {
        //Logger.d("on:" + route);
        List<DataListener> list = mListenerMap.get(route);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(listener);
        mListenerMap.put(route, list);
    }

    /**
     * Touch off the event and call mListenerMap corresponding route.
     *
     * @param route
     * @param message
     * @return true if call success, false if there is no mListenerMap for this
     * route.
     */
    private void emit(String route, JSONObject message) {
        List<DataListener> list = mListenerMap.get(route);
        if (list == null) {
            Logger.w("emit:" + route);
            Logger.w("there is no mListenerMap.");
            return;
        }

        for (DataListener listener : list) {
            DataEvent event = new DataEvent(this, message);
            listener.receiveData(event);
        }
    }
}