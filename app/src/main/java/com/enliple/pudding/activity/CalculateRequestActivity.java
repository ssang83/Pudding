package com.enliple.pudding.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.CalculateTabPagerAdapter;
import com.enliple.pudding.bus.SoftKeyboardBus;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.vo.API155;
import com.enliple.pudding.commons.ui_compat.PixelUtil;
import com.enliple.pudding.commons.widget.NonSwipeableViewPager;
import com.enliple.pudding.commons.widget.tab_layout.WrappedTabLayoutStripUtil;
import com.enliple.pudding.keyboard.KeyboardHeightProvider;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

public class CalculateRequestActivity extends AbsBaseActivity {
    private CalculateTabPagerAdapter pagerAdapter;
    private NonSwipeableViewPager viewPagerContainer;
    private TabLayout tabLayoutMy;
    private LinearLayout top;
    private RelativeLayout buttonBack;
    private String userId;
    private String userCookie = "0";
    private String userPoint = "0";

    private KeyboardHeightProvider mKeyboardHeightProvider;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calc_request);

        EventBus.getDefault().register(this);

        NetworkBus bus = new NetworkBus(NetworkApi.API155.name());
        EventBus.getDefault().post(bus);

        mKeyboardHeightProvider = new KeyboardHeightProvider(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mKeyboardHeightProvider.start();
            }
        }, 500);
        userId = AppPreferences.Companion.getUserId(CalculateRequestActivity.this);
        viewPagerContainer = findViewById(R.id.viewPagerContainer);
        tabLayoutMy = findViewById(R.id.tabLayoutMy);
        top = findViewById(R.id.top);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SoftKeyboardBus bus) {
        Logger.e("SoftKeyboardBus height: " + bus.height);
        if ( bus.height > 100 ) {
            top.animate().x(0).y(-400).setDuration(200).start(); // LinearLayout top의 위치를 y축으로 -500정도 위로 올린다.
        } else {
            top.animate().x(0).y(Utils.ConvertDpToPx(CalculateRequestActivity.this, 57)).setDuration(200).start(); // LinearLayout top의 원래 위치로 되돌린다.
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String key = "GET/user/" + AppPreferences.Companion.getUserId(CalculateRequestActivity.this) + "/exchange";
        Logger.e("key :: " + key);
        Logger.e("Data.arg1 :: " + data.arg1);
        if ( data.arg1.equals(key) ) {
            if ("ok" == data.arg2) {
                String str = DBManager.getInstance(CalculateRequestActivity.this).get(data.arg1);
                Logger.e("Str :: " + str);
                API155 response = new Gson().fromJson(str, API155.class);

                API155.ExchangeData dt = response.data;

//                userCookie = response.;
//                userPoint = response.userPoint;
//                userCookie = 32140 + "";
//                userPoint = 233210 + "";
                initTab(userId, dt);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close();
        }
    }

    private void initTab(String userId, API155.ExchangeData data) {
        if ( pagerAdapter == null ) {
            tabLayoutMy.addTab(tabLayoutMy.newTab().setText(getString(R.string.exchange_jelly)));
            tabLayoutMy.addTab(tabLayoutMy.newTab().setText(getString(R.string.exchange_point)));
        }
        pagerAdapter = new CalculateTabPagerAdapter(viewPagerContainer, getSupportFragmentManager(), userId, data);
        tabLayoutMy.addOnTabSelectedListener(tabSelectedListener);

        // TabStrip Size 축소
        WrappedTabLayoutStripUtil.wrapTabIndicatorToTitle(tabLayoutMy,
                PixelUtil.dpToPx(CalculateRequestActivity.this, 60),
        PixelUtil.dpToPx(CalculateRequestActivity.this, 60));

        tabLayoutMy.getTabAt(0).select();

        setupTabLayoutFonts();
    }


    private void setupTabLayoutFonts() {
        int tabPosition = tabLayoutMy.getSelectedTabPosition();
        ViewGroup vg = (ViewGroup) tabLayoutMy.getChildAt(0);
        int tabCnt = vg.getChildCount();
        for (int i = 0 ; i < tabCnt ; i ++ ) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(i);
            int tabChildCnt = vgTab.getChildCount();
            for (int j = 0 ; j < tabChildCnt ; j ++ ) {
                View tabViewChild = vgTab.getChildAt(j);
                if (tabViewChild instanceof AppCompatTextView) {
                    if (i == tabPosition) {
                        ((AppCompatTextView)tabViewChild).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/notosanskr_medium.otf"));
                        ((AppCompatTextView)tabViewChild).setTextSize(14);
                    } else {
                        ((AppCompatTextView)tabViewChild).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/notosanskr_regular.otf"));
                        ((AppCompatTextView)tabViewChild).setTextSize(14);
                    }
                }
            }
        }
    }

    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            setupTabLayoutFonts();
            if ( tab != null ) {
                viewPagerContainer.setCurrentItem(tab.getPosition());
                tabLayoutMy.getTabAt(tab.getPosition()).select();
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
