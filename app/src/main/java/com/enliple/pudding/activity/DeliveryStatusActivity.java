package com.enliple.pudding.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.fragment.my.PaymentSuccessFragment;
import com.enliple.pudding.fragment.my.ProductDeliveredFragment;
import com.enliple.pudding.fragment.my.ProductDeliveringFragment;
import com.enliple.pudding.fragment.my.ProductPendingFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class DeliveryStatusActivity extends AbsBaseActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener {
    // 선언 포지션 변경금지! (변경시 ordinal 함수 Index 꼬임)
    public enum DeliveryTab {
        PAYMENT_SUCCESS,                            // 결제완료
        PRODUCT_PENDING,                            // 상품준비중
        PRODUCT_DELIVERING,                         // 배송중
        PRODUCT_DELIVERED                           // 배송완료
    }

    public static final String PAYMENT_COMPLETED = "결제완료";
    public static final String PRODUCT_READY = "상품준비중";
    public static final String PRODUCT_SENDING = "배송중";
    public static final String DELIVERY_FINISHED = "배송완료";
    public static final String PURCHASE_COMPLETED = "구매 확정";

    private ViewPager mViewPager;

    private View layoutPaymentSuccess;                  // 결재완료 버튼
    private View layoutProductPending;                  // 상품준비중 버튼
    private View layoutProductDelivering;               // 배송중 버튼
    private View layoutProductDelivered;                // 배송완료 버튼

    private AppCompatTextView textViewPaymentSuccessCnt;         // 결재완료 건수
    private AppCompatTextView textViewProductPendingCnt;         // 상품준비중 건수
    private AppCompatTextView textViewProductDeliveringCnt;      // 배송중 건수
    private AppCompatTextView textViewProductDeliveredCnt;       // 배송완료 건수
    private RelativeLayout buttonBack;

    private DeliveryPagerAdapter mPagerAdapter;
    public static DeliveryStatusActivity instance;
    private DeliveryTab currentTab;
    private int count_total, count_pay, ready_pay, delivery_pay, complete_pay;
    private String status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_delivery_status);
        setLayout();

        Intent intent = getIntent();
        status = intent.getStringExtra("STATUS");

        mPagerAdapter = new DeliveryPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        releaseCategorySelected();
        layoutPaymentSuccess.setSelected(true);

        enableViewPagerChangeListener();

        layoutPaymentSuccess.setOnClickListener(this);
        buttonBack.setOnClickListener(this);
        layoutProductPending.setOnClickListener(this);
        layoutProductDelivering.setOnClickListener(this);
        layoutProductDelivered.setOnClickListener(this);

        mViewPager.setOffscreenPageLimit(3);
//        mViewPager.setCurrentItem(0);

        if (PAYMENT_COMPLETED.equals(status)) {
            releaseCategorySelected();
            layoutPaymentSuccess.setSelected(true);
            currentTab = DeliveryTab.PAYMENT_SUCCESS;

            if (mViewPager.getCurrentItem() != DeliveryTab.PAYMENT_SUCCESS.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PAYMENT_SUCCESS.ordinal());
                enableViewPagerChangeListener();
            }
        } else if (PRODUCT_READY.equals(status)) {
            releaseCategorySelected();
            layoutProductPending.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_PENDING;

            if (mViewPager.getCurrentItem() != DeliveryTab.PRODUCT_PENDING.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PRODUCT_PENDING.ordinal());
                enableViewPagerChangeListener();
            }
        } else if (PRODUCT_SENDING.equals(status)) {
            releaseCategorySelected();
            layoutProductDelivering.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_DELIVERING;

            if (mViewPager.getCurrentItem() != DeliveryTab.PRODUCT_DELIVERING.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PRODUCT_DELIVERING.ordinal());
                enableViewPagerChangeListener();
            }
        } else if (DELIVERY_FINISHED.equals(status)) {
            releaseCategorySelected();
            layoutProductDelivered.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_DELIVERED;

            if (mViewPager.getCurrentItem() != DeliveryTab.PRODUCT_DELIVERED.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PRODUCT_DELIVERED.ordinal());
                enableViewPagerChangeListener();
            }
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();
        if (viewId == R.id.buttonBack) {
            onBackPressed();
        } else if (viewId == R.id.layoutPaymentSuccess) {
            ShopTreeAsyncTask task = new ShopTreeAsyncTask(DeliveryStatusActivity.this);
            task.getDeliveryStatusList(PAYMENT_COMPLETED, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                @Override
                public void onResponse(boolean result, Object obj) {
                    try {
                        if (result) {
                            if (obj != null) {
                                JSONObject object = (JSONObject) obj;
                                if (object != null) {
                                    String rt = object.optString("result").toLowerCase();
                                    if ("success".equals(rt)) {

                                    } else {
                                        String errorMessage = object.optString("resultMessage");
                                        if (!TextUtils.isEmpty(errorMessage)) {
                                            Toast.makeText(DeliveryStatusActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            releaseCategorySelected();
            layoutPaymentSuccess.setSelected(true);
            currentTab = DeliveryTab.PAYMENT_SUCCESS;

            if (mViewPager.getCurrentItem() != DeliveryTab.PAYMENT_SUCCESS.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PAYMENT_SUCCESS.ordinal());
                enableViewPagerChangeListener();
            }
        } else if (viewId == R.id.layoutProductPending) {
            releaseCategorySelected();
            layoutProductPending.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_PENDING;

            if (mViewPager.getCurrentItem() != DeliveryTab.PRODUCT_PENDING.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PRODUCT_PENDING.ordinal());
                enableViewPagerChangeListener();
            }
        } else if (viewId == R.id.layoutProductDelivering) {
            releaseCategorySelected();
            layoutProductDelivering.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_DELIVERING;

            if (mViewPager.getCurrentItem() != DeliveryTab.PRODUCT_DELIVERING.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PRODUCT_DELIVERING.ordinal());
                enableViewPagerChangeListener();
            }
        } else if (viewId == R.id.layoutProductDelivered) {
            releaseCategorySelected();
            layoutProductDelivered.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_DELIVERED;

            if (mViewPager.getCurrentItem() != DeliveryTab.PRODUCT_DELIVERED.ordinal()) {
                disableViewpagerChangeListener();
                mViewPager.setCurrentItem(DeliveryTab.PRODUCT_DELIVERED.ordinal());
                enableViewPagerChangeListener();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        releaseCategorySelected();

        if (position == DeliveryTab.PAYMENT_SUCCESS.ordinal()) {
            layoutPaymentSuccess.setSelected(true);
            currentTab = DeliveryTab.PAYMENT_SUCCESS;
        } else if (position == DeliveryTab.PRODUCT_PENDING.ordinal()) {
            layoutProductPending.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_PENDING;
        } else if (position == DeliveryTab.PRODUCT_DELIVERING.ordinal()) {
            layoutProductDelivering.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_DELIVERING;
        } else if (position == DeliveryTab.PRODUCT_DELIVERED.ordinal()) {
            layoutProductDelivered.setSelected(true);
            currentTab = DeliveryTab.PRODUCT_DELIVERED;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setLayout() {
        buttonBack = findViewById(R.id.buttonBack);

        textViewPaymentSuccessCnt = findViewById(R.id.textViewPaymentSuccessCnt);
        textViewProductPendingCnt = findViewById(R.id.textViewProductPendingCnt);
        textViewProductDeliveringCnt = findViewById(R.id.textViewProductDeliveringCnt);
        textViewProductDeliveredCnt = findViewById(R.id.textViewProductDeliveredCnt);

        layoutPaymentSuccess = findViewById(R.id.layoutPaymentSuccess);
        layoutProductPending = findViewById(R.id.layoutProductPending);
        layoutProductDelivering = findViewById(R.id.layoutProductDelivering);
        layoutProductDelivered = findViewById(R.id.layoutProductDelivered);

        mViewPager = findViewById(R.id.viewPagerStatus);
    }

    /**
     * 결제완료 총 건수를 지정
     *
     * @param count
     */
    public void setPaymentSuccessCount(int count) {
        if (count > 0) {
            textViewPaymentSuccessCnt.setText(String.valueOf(count));
        } else {
            textViewPaymentSuccessCnt.setText("0");
        }
    }

    /**
     * 상품준비중 총 건수를 지정
     *
     * @param count
     */
    public void setProductPendingCount(int count) {
        if (count > 0) {
            textViewProductPendingCnt.setText(String.valueOf(count));
        } else {
            textViewProductPendingCnt.setText("0");
        }
    }

    /**
     * 배송중 총 건수를 지정
     *
     * @param count
     */
    public void setProductDeliveringCount(int count) {
        if (count > 0) {
            textViewProductDeliveringCnt.setText(String.valueOf(count));
        } else {
            textViewProductDeliveringCnt.setText("0");
        }
    }

    /**
     * 배송완료 총 건수를 지정
     *
     * @param count
     */
    public void setProductDeliveredCnt(int count) {
        if (count > 0) {
            textViewProductDeliveredCnt.setText(String.valueOf(count));
        } else {
            textViewProductDeliveredCnt.setText("0");
        }
    }

    /**
     * 조회 카테고리의 Selected 상태를 모두 초기화
     */
    private void releaseCategorySelected() {
        layoutPaymentSuccess.setSelected(false);
        layoutProductPending.setSelected(false);
        layoutProductDelivering.setSelected(false);
        layoutProductDelivered.setSelected(false);
    }

    /**
     * ViewPageChangeListener 의 활성화 처리
     */
    private void enableViewPagerChangeListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mViewPager.addOnPageChangeListener(this);
        } else {
            mViewPager.setOnPageChangeListener(this);
        }
    }

    /**
     * ViewPageChangeListener의 비활성화 처리
     */
    private void disableViewpagerChangeListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mViewPager.removeOnPageChangeListener(this);
        } else {
            mViewPager.setOnPageChangeListener(null);
        }
    }


    private class DeliveryPagerAdapter extends FragmentPagerAdapter {

        private FragmentManager fm;
        private Fragment[] mFragments;

        public DeliveryPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;

            Context ctx = DeliveryStatusActivity.this;

            mFragments = new Fragment[4];
            mFragments[0] = PaymentSuccessFragment.instantiate(ctx, PaymentSuccessFragment.class.getName());
            mFragments[1] = ProductPendingFragment.instantiate(ctx, ProductPendingFragment.class.getName());
            mFragments[2] = ProductDeliveringFragment.instantiate(ctx, ProductDeliveringFragment.class.getName());
            mFragments[3] = ProductDeliveredFragment.instantiate(ctx, ProductDeliveredFragment.class.getName());
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

    }
}
