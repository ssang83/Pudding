package com.enliple.pudding.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.PointHistoryAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API101;
import com.enliple.pudding.commons.network.vo.BaseAPI;
import com.enliple.pudding.widget.shoptree.PointGuideDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */

public class PointHistoryActivity extends AbsBaseActivity {
    private RelativeLayout buttonBack;
    private AppCompatImageButton buttonPointInfo;

    private AppCompatTextView textViewAvailablePoint;
    private AppCompatTextView textViewAccumulatedAmount;
    private AppCompatTextView textViewExpectedAmount;
    private AppCompatTextView textViewEmpty;

    private RecyclerView recyclerViewPointHistory;
    private ProgressBar progressBar;

    private View viewForAccumulatedAmount;
    private View viewForExpireAmount;

    volatile boolean isEndOfData;
    volatile boolean isLoadMore;

    private PointGuideDialog pointDialog;
    private PointHistoryAdapter mAdapter;

    private View.OnClickListener clickListener = v -> {
        long viewId = v.getId();
        if (viewId == R.id.buttonBack) {
            finish();
        } else if (viewId == R.id.buttonPointInfo) {
            if (pointDialog != null) {
                pointDialog.dialogDismiss();
                pointDialog = null;
            }

            pointDialog = new PointGuideDialog(PointHistoryActivity.this);
            pointDialog.dialogShow(PointHistoryActivity.this);
        } else if (viewId == R.id.layoutForAccumulatedAmount) {
            Intent intent = new Intent(PointHistoryActivity.this, PointSaveActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.layoutForExpireAmount) {
            Intent intent = new Intent(PointHistoryActivity.this, PointExpireActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_point);
        setLayout();

        recyclerViewPointHistory.setHasFixedSize(false);
        recyclerViewPointHistory.setNestedScrollingEnabled(false);

        setEmptyText(getString(R.string.msg_my_shopping_point_history_empty),
                getString(R.string.msg_my_shopping_menu_point));

        isLoadMore = false;

        NetworkBus bus = new NetworkBus(NetworkApi.API101.name());
        EventBus.getDefault().post(bus);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (pointDialog != null) {
            pointDialog.dialogDismiss();
            pointDialog = null;
        }

        super.onDestroy();
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String API101 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API101.toString(), AppPreferences.Companion.getUserId(this), "");

        if (data.arg1.equals(API101)) {
            loadData(data);
        }
    }

    private void setLayout() {
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(clickListener);
        buttonPointInfo = findViewById(R.id.buttonPointInfo);
        buttonPointInfo.setOnClickListener(clickListener);

        viewForAccumulatedAmount = findViewById(R.id.layoutForAccumulatedAmount);
        viewForAccumulatedAmount.setOnClickListener(clickListener);
        viewForExpireAmount = findViewById(R.id.layoutForExpireAmount);
        viewForExpireAmount.setOnClickListener(clickListener);

        textViewAvailablePoint = findViewById(R.id.textViewAvailablePoint);
        textViewAccumulatedAmount = findViewById(R.id.textViewAccumulatedAmount);
        textViewExpectedAmount = findViewById(R.id.textViewExpectedAmount);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        recyclerViewPointHistory = findViewById(R.id.recyclerViewPointHistory);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * 포인트가 존재하지 않을 때 출력되는 문구를 설정
     *
     * @param emptyText
     * @param colorSpannableText
     */
    private void setEmptyText(@NonNull String emptyText, @NonNull String colorSpannableText) {
        final SpannableStringBuilder sp = new SpannableStringBuilder(emptyText);
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.main_color)),
                0, colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewEmpty.setText(sp);
    }

    /**
     * 사용가능 포인트 텍스트를 설정
     *
     * @param point
     */
    private void setAvailablePoint(int point) {
        String pointStr = String.format(getString(R.string.msg_price_format),
                PriceFormatter.Companion.getInstance().getFormattedValue(point < 0 ? 0 : point));

        textViewAvailablePoint.setText(pointStr);
    }

    private void loadData(NetworkBusResponse data) {
        progressBar.setVisibility(View.GONE);
        if (data.arg2.equals("ok")) {
            API101 response = new Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API101.class);

            setAvailablePoint(Integer.valueOf(response.point));
            textViewAccumulatedAmount.setText(String.format(getString(R.string.msg_price_format),
                    PriceFormatter.Companion.getInstance().getFormattedValue(response.save_point)));
            textViewExpectedAmount.setText(String.format(getString(R.string.msg_price_format),
                    PriceFormatter.Companion.getInstance().getFormattedValue(response.expire_point)));

            if (response.data.size() > 0) {
                recyclerViewPointHistory.setVisibility(View.VISIBLE);
                textViewEmpty.setVisibility(View.GONE);

                if (mAdapter == null) {
                    mAdapter = new PointHistoryAdapter(PointHistoryActivity.this);
                    recyclerViewPointHistory.setAdapter(mAdapter);
                }

                mAdapter.addItems(response.data);
            } else {
                recyclerViewPointHistory.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.VISIBLE);
            }
        } else {
            BaseAPI error = new Gson().fromJson(data.arg4, BaseAPI.class);
            Logger.e("error : " + error.toString());
        }
    }
}
