package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.RelativeLayout;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.CustomerAllAdapter;
import com.enliple.pudding.adapter.my.FAQCategoryAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API21;
import com.enliple.pudding.commons.network.vo.API38;
import com.enliple.pudding.commons.network.vo.API41;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

public class CustomerCenterAllActivity extends AbsBaseActivity {
    private static final int REQUEST_ONE_BY_ONE = 29131;
    private static final int NUMBER_COLUMNS = 3;
    private static final String FAQLIST_TYPE_CATEGORY = "category";
    private static final String FAQLIST_TYPE_ALL = "all";
    private static final int REQUEST_GO_CART = 50222;

    private RelativeLayout buttonBack;
    private AppCompatImageButton buttonMessage, buttonCart;
    private AppCompatTextView textViewMessageBadge, textViewCartBadge;
    private RelativeLayout btnSearchQuestion;
    private RecyclerView list, recycler;
    private CustomerAllAdapter adapter;
    private FAQCategoryAdapter faqAdapter;
    private WrappedLinearLayoutManager layoutManager;
    private GridLayoutManager gridLayoutManager;
    private String firstCategoryId = "";
    private String selectedCategoryType = "";
    private int cartCnt, messageCnt;
    private boolean isLoadMore;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_center_all);

        initViews();
    }

    private void initViews() {
        list = findViewById(R.id.list);
        recycler = findViewById(R.id.recycler);

        buttonBack = findViewById(R.id.buttonBack);
        buttonMessage = findViewById(R.id.buttonMessage);
        buttonCart = findViewById(R.id.buttonCart);
        textViewMessageBadge = findViewById(R.id.textViewMessageBadge);
        textViewCartBadge = findViewById(R.id.textViewCartBadge);
        btnSearchQuestion = findViewById(R.id.btnSearchQuestion);

        buttonBack.setOnClickListener(clickListener);
        buttonMessage.setOnClickListener(clickListener);
        buttonCart.setOnClickListener(clickListener);
        btnSearchQuestion.setOnClickListener(clickListener);

        gridLayoutManager = new GridLayoutManager(CustomerCenterAllActivity.this, NUMBER_COLUMNS);
        recycler.setLayoutManager(gridLayoutManager);
        faqAdapter = new FAQCategoryAdapter(CustomerCenterAllActivity.this);
        recycler.setAdapter(faqAdapter);
        faqAdapter.setOnItemClickListener(new FAQCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, API38.FAQCategoryData data) {
                selectedCategoryType = FAQLIST_TYPE_CATEGORY;
                firstCategoryId = data.id;
                Logger.e("firstCategory :: " + firstCategoryId);
                NetworkBus bus = new NetworkBus(NetworkApi.API41.name(), selectedCategoryType, firstCategoryId);
                EventBus.getDefault().post(bus);
            }
        });
        adapter = new CustomerAllAdapter(CustomerCenterAllActivity.this);
        layoutManager = new WrappedLinearLayoutManager(CustomerCenterAllActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setHasFixedSize(false);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
//        adapter.setItems(getData(makeJSON()));

        EventBus.getDefault().register(this);

        NetworkBus bus = new NetworkBus(NetworkApi.API38.name());
        EventBus.getDefault().post(bus);

        bus = new NetworkBus(NetworkApi.API40.name(), AppPreferences.Companion.getUserId(CustomerCenterAllActivity.this));
        EventBus.getDefault().post(bus);

        bus = new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(this));
        EventBus.getDefault().post(bus);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GO_CART) {
                int cartCnt = data.getIntExtra("CART_CNT", 0);
                setCartBadgeCount(cartCnt);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("onMessageEvent faq category data.arg1 " + data.arg1);
        String categoryKey = "GET/support/faqcategory";
        String faqlistKey = "GET/support/faqlist?type=" + selectedCategoryType + "&keyword=" + firstCategoryId;
        String key = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API21.toString(), AppPreferences.Companion.getUserId(this), "");
        if (data.arg1.equals(categoryKey)) {
            String str = DBManager.getInstance(this).get(categoryKey);
            API38 response = new Gson().fromJson(str, API38.class);

            if ("success".equals(response.result)) {
                faqAdapter.setItems(response.data);
//                selectedCategoryType = FAQLIST_TYPE_CATEGORY;
//                firstCategoryId = response.data.get(0).id;
                selectedCategoryType = "all";
                firstCategoryId = "";
                NetworkBus bus = new NetworkBus(NetworkApi.API41.name(), selectedCategoryType, firstCategoryId);
                EventBus.getDefault().post(bus);
            }
        } else if (data.arg1.equals(faqlistKey)) {
            String str = DBManager.getInstance(this).get(faqlistKey);
            API41 response = new Gson().fromJson(str, API41.class);
            if (adapter != null) {
                adapter.setItems(response.data);
            }
        } else if (data.arg1.equals(key)) {
            String str = DBManager.getInstance(this).get(data.arg1);
            API21 response = new Gson().fromJson(str, API21.class);
            try {
                messageCnt = Integer.valueOf(response.newMessage);
                cartCnt = Integer.valueOf(response.cartCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setSmsBadgeCount(messageCnt);
            setCartBadgeCount(cartCnt);
        }
    }

    private void setSmsBadgeCount(int count) {
        textViewMessageBadge.setText("" + count);
        if (count > 0) {
//            textViewMessageBadge.setVisibility(View.VISIBLE);
        } else {
            textViewMessageBadge.setVisibility(View.GONE);
        }
    }

    /**
     * 장바구니 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     *
     * @param count
     */
    private void setCartBadgeCount(int count) {
        textViewCartBadge.setText("" + count);
        if (count > 0) {
//            textViewCartBadge.setVisibility(View.VISIBLE);
        } else {
            textViewCartBadge.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.buttonBack:
                    finish();
                    break;

                case R.id.buttonMessage:
                    startActivity(new Intent(getApplicationContext(), MessageActivity.class));
                    break;

                case R.id.buttonCart:
                    Intent intent1 = new Intent(getApplicationContext(), ProductCartActivity.class);
                    startActivityForResult(intent1, REQUEST_GO_CART);
                    break;

                case R.id.btnSearchQuestion:
                    Intent intent3 = new Intent(getApplicationContext(), CustomerCenterSearchActivity.class);
                    intent3.putExtra("MESSAGE_CNT", messageCnt);
                    intent3.putExtra("CART_CNT", cartCnt);
                    startActivity(intent3);
                    break;
            }
        }
    };

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
