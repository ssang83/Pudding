package com.enliple.pudding.activity;

import android.app.Activity;
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
import com.enliple.pudding.adapter.my.CREAdapter;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API21;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.CREResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Kim Joonsung on 2018-09-21.
 */

public class CREActivity extends AbsBaseActivity implements View.OnClickListener, CREAdapter.CREAdapterListener {

    private static final int RECYCLER_VIEW_ITEM_CACHE_SIZE = 20;
    private static final int REQUEST_GO_CART = 50222;

    private RelativeLayout buttonBack;
    private RecyclerView recyclerViewCRE;
    private View layoutEmpty;
    private AppCompatTextView textViewEmpty;
    private ProgressBar progressBar;

    private CREAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cre);
        setLayout();

        recyclerViewCRE.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE);
        recyclerViewCRE.setHasFixedSize(false);

        buttonBack.setOnClickListener(this);

        textViewEmpty.setText(getString(R.string.msg_my_shopping_menu_cre) + "상품이 없습니다.");
        setEmptyText(getString(R.string.msg_cre_empty), "취소·교환·반품");

        loadData();
    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        long viewId = v.getId();

        if (viewId == R.id.buttonBack) {
            onBackPressed();
        }
    }

    @Override
    public void onDetailClcik(String idx) {
        Intent intent = new Intent(CREActivity.this, CREDetailActivity.class);
        intent.putExtra(CREDetailActivity.INTENT_KEY_ITEM_KEY, idx);
        startActivity(intent);
    }

    /**
     * 상품이 존재하지 않을 때 출력되는 문구를 설정
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

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getCREList(
                (result, obj) -> {
                    Logger.d("" + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final CREResponse responseObj = mapper.readValue(obj.toString(),
                                CREResponse.class);

                        if (responseObj != null && responseObj.getResult().equals("SUCCESS")) {
                            if (responseObj.creData != null && responseObj.creData.size() > 0) {
                                recyclerViewCRE.setVisibility(View.VISIBLE);
                                layoutEmpty.setVisibility(View.GONE);

                                if (mAdapter == null) {
                                    mAdapter = new CREAdapter(CREActivity.this);
                                    mAdapter.setAdapterListener(this);
                                    recyclerViewCRE.setAdapter(mAdapter);

                                    mAdapter.addItems(responseObj.creData);
                                }

                            } else {
                                recyclerViewCRE.setVisibility(View.GONE);
                                layoutEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void setLayout() {
        buttonBack = findViewById(R.id.buttonBack);
        recyclerViewCRE = findViewById(R.id.recyclerViewCRE);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }
}
