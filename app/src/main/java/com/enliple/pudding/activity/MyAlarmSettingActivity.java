package com.enliple.pudding.activity;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.AlarmSettingAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API65;
import com.enliple.pudding.commons.network.vo.BaseAPI;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MyAlarmSettingActivity extends AbsBaseActivity implements AlarmSettingAdapter.AdapterListener {

    private RelativeLayout buttonBack;
    private RelativeLayout btnMessageAlarm, btnEventAlarm, btnAllLive;
    private ImageView imgMessageAlarm, imgEventAlarm, imgAllLive;
    private RecyclerView list;

    private AlarmSettingAdapter adapter;
    private WrappedLinearLayoutManager linearLayoutManager;
    private String liveAlarm = "N";
    private String messageAlarm = "N";
    private String eventAlarm = "N";
//    private String followAlarm = "N";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_alarm_setting);
        EventBus.getDefault().register(this);

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);
        btnMessageAlarm = findViewById(R.id.btnMessageAlarm);
        btnEventAlarm = findViewById(R.id.btnEventAlarm);
        btnAllLive = findViewById(R.id.btnAllLive);
        imgMessageAlarm = findViewById(R.id.imgMessageAlarm);
        imgEventAlarm = findViewById(R.id.imgEventAlarm);
        imgAllLive = findViewById(R.id.imgAllLive);
        list = findViewById(R.id.list);

        linearLayoutManager = new WrappedLinearLayoutManager(MyAlarmSettingActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setHasFixedSize(false);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(linearLayoutManager);

        adapter = new AlarmSettingAdapter(MyAlarmSettingActivity.this);
        adapter.setListener(this);
        list.setAdapter(adapter);

        buttonBack.setOnClickListener(clickListener);
        btnMessageAlarm.setOnClickListener(clickListener);
        btnEventAlarm.setOnClickListener(clickListener);
        btnAllLive.setOnClickListener(clickListener);

        NetworkBus bus = new NetworkBus(NetworkApi.API65.name());
        EventBus.getDefault().post(bus);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String API65 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API65.toString(), AppPreferences.Companion.getUserId(this), "");

        String API64 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API65.toString(), AppPreferences.Companion.getUserId(this), "");

        if (data.arg1.equals(API65)) {
            handleNetworkAPI65(data);
        } else if (data.arg1.equals(API64)) {
            handleNetworkAPI64(data);
        }
    }

    @Override
    public void onAlarmSet(API65.AlarmItem.FollowAlarmItem item, String followYN) {
//        followAlarm = followYN;
//        setAlarm("follow", item);
        setEachLiveAlarm(followYN, item);
    }

    private void handleNetworkAPI65(NetworkBusResponse data) {
        if ("ok".equals(data.arg2)) {
            String str = DBManager.getInstance(this).get(data.arg1);
            API65 response = new Gson().fromJson(str, API65.class);

            if (response.data.live_YN.equals("Y")) {
                imgAllLive.setBackgroundResource(R.drawable.switch_on);
                list.setVisibility(View.VISIBLE);
            } else {
                imgAllLive.setBackgroundResource(R.drawable.switch_off);
                list.setVisibility(View.GONE);
            }

            if (response.data.message_YN.equals("Y")) {
                imgMessageAlarm.setBackgroundResource(R.drawable.switch_on);
            } else {
                imgMessageAlarm.setBackgroundResource(R.drawable.switch_off);
            }

            if (response.data.event_YN.equals("Y")) {
                imgEventAlarm.setBackgroundResource(R.drawable.switch_on);
            } else {
                imgEventAlarm.setBackgroundResource(R.drawable.switch_off);
            }

            liveAlarm = response.data.live_YN;
            eventAlarm = response.data.event_YN;
            messageAlarm = response.data.message_YN;

            adapter.setItems(response.data.follow_alarm);
        } else {
            BaseAPI errorResult = new Gson().fromJson(data.arg4, BaseAPI.class);
            Logger.e("error : " + errorResult.toString());
        }
    }

    private void handleNetworkAPI64(NetworkBusResponse data) {
        if ("fail".equals(data.arg2)) {
            BaseAPI errorResult = new Gson().fromJson(data.arg4, BaseAPI.class);
            Logger.e("error : " + errorResult.toString());

            new AppToast(this).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonBack:
                    finish();
                    break;
                case R.id.btnMessageAlarm:
                    toggleMessageAlarm();
                    if (messageAlarm.equals("Y")) {
                        imgMessageAlarm.setBackgroundResource(R.drawable.switch_on);
                    } else {
                        imgMessageAlarm.setBackgroundResource(R.drawable.switch_off);
                    }

                    setAlarm("message", null);
                    break;
                case R.id.btnEventAlarm:
                    toggleEventAlarm();
                    if (eventAlarm.equals("Y")) {
                        imgEventAlarm.setBackgroundResource(R.drawable.switch_on);
                    } else {
                        imgEventAlarm.setBackgroundResource(R.drawable.switch_off);
                    }

                    setAlarm("event", null);
                    break;
                case R.id.btnAllLive:
                    toggleAllSelect();
                    if (liveAlarm.equals("Y")) {
                        imgAllLive.setBackgroundResource(R.drawable.switch_on);
                        list.setVisibility(View.VISIBLE);
                    } else {
                        imgAllLive.setBackgroundResource(R.drawable.switch_off);
                        list.setVisibility(View.GONE);
                    }

                    setAlarm("live", null);
                    break;
            }
        }
    };

    private void setEachLiveAlarm(String value, API65.AlarmItem.FollowAlarmItem item) {
        try {
            JSONObject requestObj = new JSONObject();

            requestObj.put("type", "live");
            requestObj.put("val", value);
            requestObj.put("user", AppPreferences.Companion.getUserId(this));
            requestObj.put("follow_user", item.mb_id);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API64.name(), body);
            EventBus.getDefault().post(bus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAlarm(String type, API65.AlarmItem.FollowAlarmItem item) {
        try {
            JSONObject requestObj = new JSONObject();

            if (type.equals("message")) {
                requestObj.put("type", type);
                requestObj.put("val", messageAlarm);
                requestObj.put("user", AppPreferences.Companion.getUserId(this));
            } else if (type.equals("event")) {
                requestObj.put("type", type);
                requestObj.put("val", eventAlarm);
                requestObj.put("user", AppPreferences.Companion.getUserId(this));
            } else if (type.equals("live")) {
                requestObj.put("type", type);
                requestObj.put("val", liveAlarm);
                requestObj.put("user", AppPreferences.Companion.getUserId(this));
            } else {
//                requestObj.put("type", type);
//                requestObj.put("val", followAlarm);
//                requestObj.put("user", AppPreferences.Companion.getUserId(this));
//                requestObj.put("follow_user", item.mb_id);
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API64.name(), body);
            EventBus.getDefault().post(bus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    private void toggleAllSelect() {
        if (liveAlarm.equals("Y")) {
            liveAlarm = "N";
        } else {
            liveAlarm = "Y";
        }
    }

    private void toggleMessageAlarm() {
        if (messageAlarm.equals("Y")) {
            messageAlarm = "N";
        } else {
            messageAlarm = "Y";
        }
    }

    private void toggleEventAlarm() {
        if (eventAlarm.equals("Y")) {
            eventAlarm = "N";
        } else {
            eventAlarm = "Y";
        }
    }
}
