package com.enliple.pudding.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.PuddingApplication;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkConst;
import com.enliple.pudding.commons.network.NetworkDBKey;
import com.enliple.pudding.commons.network.vo.API51;
import com.enliple.pudding.commons.network.vo.API52;
import com.enliple.pudding.fragment.my.CookiePurchaseListFragment;
import com.enliple.pudding.fragment.my.CookieReceivedListFragment;
import com.enliple.pudding.fragment.my.CookieSendListFragment;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;


public class CookieManagementActivity extends AbsBaseActivity {
    private static final int TAB_PURCHASE_LIST = 0;
    private static final int TAB_SEND_LIST = 1;
    private static final int TAB_RECEIVED_LIST = 2;
    private static final int ACTIVITY_REQUEST_AUTH = 6973;
    private RelativeLayout buttonBack;
    private AppCompatTextView totalCount, send_cookie_no, expired_cookie_no, btnBuyCookie, txtPurchaseList, txtSendList, txtReceivedList, purchaseListLine, sendListLine, receivedListLine, hiddenTxtPurchaseList, hiddenTxtSendList, hiddenTxtReceivedList, hiddenPurchaseListLine, hiddenSendListLine, hiddenReceivedListLine, textViewEmpty;
    private RelativeLayout tabPurchaseList, tabSendList, tabReceivedList, hiddenTabPurchaseList, hiddenTabSendList, hiddenTabReceivedList;
    private LinearLayout top, tabLayer, hiddenTabLayer;
    private ScrollView scrollView;
    private FrameLayout container;
    private CookiePurchaseListFragment cookiePurchaseListFragment;
    private CookieSendListFragment cookieSendListFragment;
    private CookieReceivedListFragment cookieReceivedListFragment;
    private List<API51.Data> buyList;
    private List<API51.Data> receivedList;
    private List<API51.Data> sendList;
    private int tabPosition = -1;
    private String userId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookie_management);
        Logger.e("CookieManagementActivity onCreate");
        EventBus.getDefault().register(CookieManagementActivity.this);

        userId = AppPreferences.Companion.getUserId(CookieManagementActivity.this);
        NetworkBus bus = new NetworkBus(NetworkApi.API52.name(), userId);
        EventBus.getDefault().post(bus);

        initViews();
    }

    public void initViews() {
        container = findViewById(R.id.container);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        scrollView = findViewById(R.id.scrollView);
        top = findViewById(R.id.top);
        tabLayer = findViewById(R.id.tabLayer);
        hiddenTabLayer = findViewById(R.id.hiddenTabLayer);
        buttonBack = findViewById(R.id.buttonBack);
        totalCount = findViewById(R.id.totalCount);
        send_cookie_no = findViewById(R.id.send_cookie_no);
        expired_cookie_no = findViewById(R.id.expired_cookie_no);
        btnBuyCookie = findViewById(R.id.btnBuyCookie);
        txtPurchaseList = findViewById(R.id.txtPurchaseList);
        txtSendList = findViewById(R.id.txtSendList);
        txtReceivedList = findViewById(R.id.txtReceivedList);
        hiddenTxtPurchaseList = findViewById(R.id.hiddenTxtPurchaseList);
        hiddenTxtSendList = findViewById(R.id.hiddenTxtSendList);
        hiddenTxtReceivedList = findViewById(R.id.hiddenTxtReceivedList);
        tabPurchaseList = findViewById(R.id.tabPurchaseList);
        tabSendList = findViewById(R.id.tabSendList);
        tabReceivedList = findViewById(R.id.tabReceivedList);
        hiddenTabPurchaseList = findViewById(R.id.hiddenTabPurchaseList);
        hiddenTabSendList = findViewById(R.id.hiddenTabSendList);
        hiddenTabReceivedList = findViewById(R.id.hiddenTabReceivedList);
        purchaseListLine = findViewById(R.id.purchaseListLine);
        sendListLine = findViewById(R.id.sendListLine);
        receivedListLine = findViewById(R.id.receivedListLine);
        hiddenPurchaseListLine = findViewById(R.id.hiddenPurchaseListLine);
        hiddenSendListLine = findViewById(R.id.hiddenSendListLine);
        hiddenReceivedListLine = findViewById(R.id.hiddenReceivedListLine);

        setLineWidth(txtPurchaseList, purchaseListLine, 5);
        setLineWidth(txtSendList, sendListLine, 5);
        setLineWidth(txtReceivedList, receivedListLine, 6);
        setLineWidth(hiddenTxtPurchaseList, hiddenPurchaseListLine, 5);
        setLineWidth(hiddenTxtSendList, hiddenSendListLine, 5);
        setLineWidth(hiddenTxtReceivedList, hiddenReceivedListLine, 6);

        buttonBack.setOnClickListener(clickListener);
        btnBuyCookie.setOnClickListener(clickListener);
        tabPurchaseList.setOnClickListener(clickListener);
        tabSendList.setOnClickListener(clickListener);
        tabReceivedList.setOnClickListener(clickListener);
        hiddenTabPurchaseList.setOnClickListener(clickListener);
        hiddenTabSendList.setOnClickListener(clickListener);
        hiddenTabReceivedList.setOnClickListener(clickListener);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                // 해당 뷰가 화면에 보여지고 있는지 없는지 에 따른 동작부
                if (isViewVisible(top)) {
                    if (hiddenTabLayer.getVisibility() == View.VISIBLE) {
                        hiddenTabLayer.setVisibility(View.GONE);
                    }
                } else {
                    if (hiddenTabLayer.getVisibility() == View.GONE) {
                        hiddenTabLayer.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        tabChanged(TAB_PURCHASE_LIST);
    }

    private void setLineWidth(AppCompatTextView textView, AppCompatTextView line, int length) {
        Rect realSize = new Rect();
        textView.getPaint().getTextBounds(textView.getText().toString(), 0, length, realSize);
        int textWidth = realSize.width();
        ViewGroup.LayoutParams params = line.getLayoutParams();
        params.width = textWidth;
        params.height = params.height;
        line.setLayoutParams(params);
        line.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private void tabChanged(int position) {
        Logger.e("CookieManagementActivity tabChanged");
        if (tabPosition == position)
            return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        tabPosition = position;
        if (position == TAB_PURCHASE_LIST) {
            tabUIChange(true, txtPurchaseList, purchaseListLine);
            tabUIChange(false, txtSendList, sendListLine);
            tabUIChange(false, txtReceivedList, receivedListLine);
            tabUIChange(true, hiddenTxtPurchaseList, hiddenPurchaseListLine);
            tabUIChange(false, hiddenTxtSendList, hiddenSendListLine);
            tabUIChange(false, hiddenTxtReceivedList, hiddenReceivedListLine);
            cookiePurchaseListFragment = new CookiePurchaseListFragment();
            transaction.replace(R.id.container, cookiePurchaseListFragment);
            NetworkBus bus = new NetworkBus(NetworkApi.API51.name(), userId, "buy");
            EventBus.getDefault().post(bus);
        } else if (position == TAB_SEND_LIST) {
            tabUIChange(false, txtPurchaseList, purchaseListLine);
            tabUIChange(true, txtSendList, sendListLine);
            tabUIChange(false, txtReceivedList, receivedListLine);
            tabUIChange(false, hiddenTxtPurchaseList, hiddenPurchaseListLine);
            tabUIChange(true, hiddenTxtSendList, hiddenSendListLine);
            tabUIChange(false, hiddenTxtReceivedList, hiddenReceivedListLine);
            cookieSendListFragment = new CookieSendListFragment();
            transaction.replace(R.id.container, cookieSendListFragment);
            NetworkBus bus = new NetworkBus(NetworkApi.API51.name(), userId, "give");
            EventBus.getDefault().post(bus);
        } else if (position == TAB_RECEIVED_LIST) {
            tabUIChange(false, txtPurchaseList, purchaseListLine);
            tabUIChange(false, txtSendList, sendListLine);
            tabUIChange(true, txtReceivedList, receivedListLine);
            tabUIChange(false, hiddenTxtPurchaseList, hiddenPurchaseListLine);
            tabUIChange(false, hiddenTxtSendList, hiddenSendListLine);
            tabUIChange(true, hiddenTxtReceivedList, hiddenReceivedListLine);
            cookieReceivedListFragment = new CookieReceivedListFragment();
            transaction.replace(R.id.container, cookieReceivedListFragment);
            NetworkBus bus = new NetworkBus(NetworkApi.API51.name(), userId, "receive");
            EventBus.getDefault().post(bus);
        }
        transaction.commit();
    }

    private void tabUIChange(boolean isSelected, AppCompatTextView tv, AppCompatTextView line) {
        if (isSelected) {
            line.setVisibility(View.VISIBLE);
            tv.setTextColor(0xFF192028);
            Typeface typeface = ResourcesCompat.getFont(CookieManagementActivity.this, R.font.notosanskr_medium);
            tv.setTypeface(typeface);
        } else {
            line.setVisibility(View.GONE);
            tv.setTextColor(0xFF8192A5);
            Typeface typeface = ResourcesCompat.getFont(CookieManagementActivity.this, R.font.notosanskr_regular);
            tv.setTypeface(typeface);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonBack:
                    finish();
                    break;

                case R.id.btnBuyCookie:
                    String url = NetworkConst.COOKIE_PAYMENT_API + AppPreferences.Companion.getUserId(CookieManagementActivity.this);
                    Intent intent = new Intent(CookieManagementActivity.this, LinkWebViewActivity.class);
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setComponent(new ComponentName("com.enliple.pudding", "com.enliple.pudding.activity.LinkWebViewActivity"));
                    intent.putExtra("LINK", url);
                    intent.putExtra("TITLE", "젤리 구매");
                    intent.putExtra("BUY_MODE", true);
                    startActivity(intent);
//                    if ( "N".equals(PuddingApplication.getApplication().Companion.getMLoginUserData().isAuth) ) {
//                        Intent it = new Intent(CookieManagementActivity.this, PGAuthActivity.class);
//                        it.putExtra(PGAuthActivity.INTENT_EXTRA_KEY_CALL_MODE, PGAuthActivity.INTENT_EXTRA_VALUE_MODE_IDENTIFICATION);
//                        startActivityForResult(it, ACTIVITY_REQUEST_AUTH);
//                    } else {
//                        String url = NetworkConst.COOKIE_PAYMENT_API + AppPreferences.Companion.getUserId(CookieManagementActivity.this);
//                        Intent intent = new Intent(CookieManagementActivity.this, LinkWebViewActivity.class);
////                    Intent intent = new Intent(Intent.ACTION_VIEW);
////                    intent.setComponent(new ComponentName("com.enliple.pudding", "com.enliple.pudding.activity.LinkWebViewActivity"));
//                        intent.putExtra("LINK", url);
//                        intent.putExtra("TITLE", "젤리 구매");
//                        intent.putExtra("BUY_MODE", true);
//                        startActivity(intent);
//                    }
                    break;

                case R.id.tabPurchaseList:
                case R.id.hiddenTabPurchaseList:
                    if (tabPosition != TAB_PURCHASE_LIST) {
                        tabChanged(TAB_PURCHASE_LIST);
                    }
                    break;

                case R.id.tabSendList:
                case R.id.hiddenTabSendList:
                    if (tabPosition != TAB_SEND_LIST) {
                        tabChanged(TAB_SEND_LIST);
                    }
                    break;

                case R.id.tabReceivedList:
                case R.id.hiddenTabReceivedList:
                    if (tabPosition != TAB_RECEIVED_LIST) {
                        tabChanged(TAB_RECEIVED_LIST);
                    }
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("CookiePurchaseListFragment data.arg1 :: " + data.arg1);
        Logger.e("CookiePurchaseListFragment data.arg2 :: " + data.arg2);
        String buyKey = NetworkDBKey.INSTANCE.getAPI51Key(CookieManagementActivity.this, "buy");
        String receivedKey = NetworkDBKey.INSTANCE.getAPI51Key(CookieManagementActivity.this, "receive");
        String sendKey = NetworkDBKey.INSTANCE.getAPI51Key(CookieManagementActivity.this, "give");
        String cookieInfoKey = NetworkDBKey.INSTANCE.getAPI52Key(CookieManagementActivity.this);
        if (data.arg1.equals(buyKey)) {
            String str = DBManager.getInstance(CookieManagementActivity.this).get(data.arg1);
            API51 response = new Gson().fromJson(str, API51.class);
            if (response != null) {
                Logger.e("response.toString :: " + response.toString());
                List<API51.Data> datum = response.data;
                if (datum != null && datum.size() > 0) {
                    textViewEmpty.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    if (cookiePurchaseListFragment != null) {
                        cookiePurchaseListFragment.loadData(datum);
                    }
                } else {
                    container.setVisibility(View.GONE);
                    textViewEmpty.setVisibility(View.VISIBLE);
                    textViewEmpty.setText("젤리 구매내역이 없습니다.");
                }
            }
        } else if (data.arg1.equals(receivedKey)) {
            String str = DBManager.getInstance(CookieManagementActivity.this).get(data.arg1);
            API51 response = new Gson().fromJson(str, API51.class);
            if (response != null) {
                Logger.e("response.toString :: " + response.toString());
                List<API51.Data> datum = response.data;
                if (datum != null && datum.size() > 0) {
                    textViewEmpty.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    if (cookieReceivedListFragment != null) {
                        cookieReceivedListFragment.loadData(datum);
                    }
                } else {
                    container.setVisibility(View.GONE);
                    textViewEmpty.setVisibility(View.VISIBLE);
                    textViewEmpty.setText("선물받은 젤리내역이 없습니다.");
                }
            }
        } else if (data.arg1.equals(sendKey)) {
            String str = DBManager.getInstance(CookieManagementActivity.this).get(data.arg1);
            API51 response = new Gson().fromJson(str, API51.class);
            if (response != null) {
                Logger.e("response.toString :: " + response.toString());
                List<API51.Data> datum = response.data;
                if (datum != null && datum.size() > 0) {
                    textViewEmpty.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    if (cookieSendListFragment != null) {
                        cookieSendListFragment.loadData(datum);
                    }
                } else {
                    container.setVisibility(View.GONE);
                    textViewEmpty.setVisibility(View.VISIBLE);
                    textViewEmpty.setText("선물한 푸딩내역이 없습니다.");
                }
            }
        } else if (data.arg1.equals(cookieInfoKey)) {
            String str = DBManager.getInstance(CookieManagementActivity.this).get(data.arg1);
            API52 response = new Gson().fromJson(str, API52.class);
            if (response != null) {
                if (response.data != null) {
                    API52.Data info = response.data;
                    if (info != null) {
                        int cookie = 0;
                        int give_cookie = 0;
                        int expire_cookie = 0;
                        String sCookie = "";
                        String sGiveCookie = "";
                        String sExpireCookie = "";
                        try {
                            cookie = info.cookie;
                            sCookie = Utils.ToNumFormat(cookie) + "개";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            give_cookie = info.give_cookie;
                            sGiveCookie = Utils.ToNumFormat(give_cookie) + "개";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            expire_cookie = info.expire_cookie;
                            sExpireCookie = Utils.ToNumFormat(expire_cookie) + "개";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        totalCount.setText(sCookie);
                        send_cookie_no.setText(sGiveCookie);
                        expired_cookie_no.setText(sExpireCookie);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(CookieManagementActivity.this)) {
            EventBus.getDefault().unregister(CookieManagementActivity.this);
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    /**
     * 해당 VIEW가 스크롤뷰 내에서 보여지고 있는지 없는지 여부
     *
     * @param view
     * @return
     */
    private boolean isViewVisible(View view) {
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (view.getLocalVisibleRect(scrollBounds)) {
            return true;
        } else
            return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == ACTIVITY_REQUEST_AUTH ) {
            if ( resultCode == RESULT_OK ) {
                String url = NetworkConst.COOKIE_PAYMENT_API + AppPreferences.Companion.getUserId(CookieManagementActivity.this);
                Intent intent = new Intent(CookieManagementActivity.this, LinkWebViewActivity.class);
                intent.putExtra("LINK", url);
                intent.putExtra("TITLE", "젤리 구매");
                intent.putExtra("BUY_MODE", true);
                startActivity(intent);
            }
        }
    }
}
