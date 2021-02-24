package com.enliple.pudding.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.RecentProductsAdapter;
import com.enliple.pudding.bus.ZzimStatusBus;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API139;
import com.enliple.pudding.commons.network.vo.API55;
import com.enliple.pudding.commons.network.vo.BaseAPI;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.AppAlertDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RecentProductsActivity extends AbsBaseActivity {
    private int REQUEST_ZZIM_STATUS = 42014;
    private RelativeLayout buttonBack, toplayer;
    private RelativeLayout btnCheckAll, btnDelete, twoBtnLayer, btnCancel, btnEdit;
    private AppCompatImageView imgAllCheck, topBtn;
    private AppCompatTextView tvAllCount, tvSelected, tvSelectedCount, slash, strAll;
    private RecyclerView list;
    private View empty, layoutEmpty;

    private RecentProductsAdapter adapter;
    private WrappedLinearLayoutManager layoutManager;
    private API55.ProductItem clickedItem;
    private boolean isCheckAll = false;
    private int totalCount = 0;
    private int selectedCount = 0;
    private int pageCount = 1;
    private boolean isReload = false;
    private boolean isEndOfData = false;
    private AppAlertDialog dialog;
    private API55.ProductItem zzimItem = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recentproduct);
        registerReceiver();
        initView();

        EventBus.getDefault().register(this);

        NetworkBus bus = new NetworkBus(NetworkApi.API55.name(), String.valueOf(pageCount));
        EventBus.getDefault().post(bus);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void  onMessageEvent(ZzimStatusBus bus) {
        String idx = bus.getProductId();
        adapter.changeZzim(idx);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String api55 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API55.toString(), AppPreferences.Companion.getUserId(this), "");

        String API109 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API109.toString(), AppPreferences.Companion.getUserId(this), "");
        String zzimId = "";
        if ( zzimItem != null ) zzimId = zzimItem.idx;
        String api126 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API126.toString(),  zzimId , "");

        if (data.arg1.startsWith(api55)) {
            if("ok".equals(data.arg2)) {
                API55 response = new Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API55.class);

//                tvAllCount.setText("전체(" + response.nTotalCount + ")");
//                strAll.setText("전체(");
//                slash.setVisibility(View.GONE);
//                tvSelectedCount.setVisibility(View.GONE);
//                tvSelectedCount.setText(selectedCount + "");
//                tvAllCount.setText(response.nTotalCount + "개");

                isEndOfData = response.data.size() == 0;
                pageCount = response.pageCount;

                if (response.nTotalCount > 0) {
                    totalCount = response.nTotalCount;
                    setEmptyViewVisible(false);

                    if(!isReload) {
                        strAll.setText("전체(");
                        slash.setVisibility(View.GONE);
                        tvSelectedCount.setVisibility(View.GONE);
                        tvSelectedCount.setText(selectedCount + "");
                        tvAllCount.setText(response.nTotalCount + "개");
                        adapter.setItems(response.data);
                    } else {
                        if ( adapter.getCheckStatus() ) {
                            List<API55.ProductItem> dataArray = response.data;

                            if ( dataArray != null && dataArray.size() > 0 ) {
                                for ( int i = 0 ; i < dataArray.size() ; i ++ ) {
                                    API55.ProductItem item = dataArray.get(i);
                                    item.isShow = true;
                                    if ( isCheckAll ) {
                                        item.isSelect = true;
                                    }

                                    dataArray.set(i, item);
                                }
                                if ( isCheckAll ) {
                                    selectedCount = selectedCount + dataArray.size();
                                    tvSelectedCount.setText(selectedCount + "");
                                }
                                adapter.addItems(dataArray);
                            }
                        } else {
                            if ( isCheckAll ) {
                                List<API55.ProductItem> dataArray = response.data;

                                if ( dataArray != null && dataArray.size() > 0 ) {
                                    for ( int i = 0 ; i < dataArray.size() ; i ++ ) {
                                        API55.ProductItem item = dataArray.get(i);
                                        item.isSelect = true;
                                        dataArray.set(i, item);
                                    }
                                    selectedCount = selectedCount + dataArray.size();
                                    tvSelectedCount.setText(selectedCount + "");
                                    adapter.addItems(dataArray);
                                }
                            } else {
                                adapter.addItems(response.data);
                            }

                        }


//                        if ( adapter.getCheckStatus() ) {
//                            List<API55.Data> dataArray = response.data;
//                            for ( int i = 0 ; i < dataArray.size() ; i ++ ) {
//                                API55.Data dt = dataArray.get(i);
//                                List<API55.Data.ProductItem> temp_products = dt.products;
//                                for ( int j = 0 ; j < dt.products.size() ; j ++ ) {
//                                    API55.Data.ProductItem dt_product = dt.products.get(j);
//                                    dt_product.isShow = true;
//                                    temp_products.set(j, dt_product);
//                                }
//                                dt.products = temp_products;
//                                dataArray.set(i, dt);
//                            }
//                            adapter.addItems(dataArray);
//                        } else {
//                            adapter.addItems(response.data);
//                        }
                    }
                } else {
                    if ( !isReload )
                        setEmptyViewVisible(true);
                }
            }
        } else if (data.arg1.startsWith(API109)) {
            if ("ok".equals(data.arg2)) {
                dialog.dismiss();
                adapter.deleteChecked();
                selectedCount = 0;
            } else {
                BaseAPI error = new Gson().fromJson(data.arg4, BaseAPI.class);
                Logger.e("error : " + error);
            }
        } else if(data.arg1.equals(api126)) {
            if("ok".equals(data.arg2)) {
                adapter.changeZzim(zzimId);
            } else {
                try {
                    JSONObject response = new JSONObject(data.arg4);
                    new AppToast(this).showToastMessage(response.getString("message"),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM);
                } catch (Exception e) {
                    Logger.p(e);
                }
            }
        }
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(RecentProductsActivity.this).registerReceiver(receiver, new IntentFilter("selected"));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(RecentProductsActivity.this).unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent it) {
            boolean in = it.getBooleanExtra("increase", false);

            if (in) {
                selectedCount++;
            } else {
                selectedCount--;
            }

            tvSelected.setText(selectedCount + "개 선택");
            slash.setVisibility(View.VISIBLE);
            tvSelectedCount.setVisibility(View.VISIBLE);
            tvSelectedCount.setText(selectedCount + "");
            tvAllCount.setText(totalCount + "개");
            if (selectedCount == totalCount) {
                isCheckAll = true;
                imgAllCheck.setBackgroundResource(R.drawable.check_on);
                adapter.setCheck(isCheckAll);
            } else if (selectedCount == 0) {
                isCheckAll = false;
                imgAllCheck.setBackgroundResource(R.drawable.cart_check_off);
                adapter.setCheck(isCheckAll);
            } else {
                imgAllCheck.setBackgroundResource(R.drawable.cart_check_off);
            }
        }
    };

    RecentProductsAdapter.Listener callback = new RecentProductsAdapter.Listener() {
        @Override
        public void onItemClicked(API55.ProductItem item) {
            clickedItem = item;
            Logger.e("click item.strType :: " + item.strType);
            if ( "2".equals(item.strType) || "3".equals(item.strType) ) {
                ShopTreeAsyncTask task = new ShopTreeAsyncTask(RecentProductsActivity.this);
                task.getDRCLink(item.idx, "", "", item.strType, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                    @Override
                    public void onResponse(boolean result, Object obj) {
                        try {
                            if (result) {
                                JSONObject object = (JSONObject) obj;
                                API139 response = new Gson().fromJson(object.toString(), API139.class);
                                if ("success".equals(response.result)) {
                                    Logger.e("response.url :: " + response.url);
                                    Intent intent = new Intent(RecentProductsActivity.this, LinkWebViewActivity.class);
                                    intent.putExtra("LINK", response.url);
                                    intent.putExtra("IDX", item.idx);
                                    intent.putExtra("IS_WISH", item.is_wish);
                                    intent.putExtra("TYPE", item.strType);
                                    intent.putExtra("TITLE", item.title);
                                    intent.putExtra("ITEM_LINK", true);
                                    startActivityForResult(intent, REQUEST_ZZIM_STATUS);
                                }
                            }
                        } catch (Exception e) {
                            Logger.p(e);
                        }
                    }
                });
            } else if ( "1".equals(item.strType) ) {
                Intent intent = new Intent(RecentProductsActivity.this, ProductDetailActivity.class);
                Logger.e("item.it_id :: " + item.idx);
                Logger.e("item.pcode :: " + item.pcode);
                Logger.e("item.sc_code :: " + item.sc_code);
                intent.putExtra(ShopTreeKey.KEY_IDX, item.idx);
                intent.putExtra(ShopTreeKey.KEY_PCODE, item.pcode);
                intent.putExtra(ShopTreeKey.KEY_SCCODE, item.sc_code);
                intent.putExtra(ShopTreeKey.KEY_STREAM_KEY, "");
                intent.putExtra(ShopTreeKey.KEY_VOD_TYPE, "");

                startActivityForResult(intent, REQUEST_ZZIM_STATUS);
            }
        }

        @Override
        public void setDeletedCount(int deletedCount) {
            totalCount = totalCount - deletedCount;
            selectedCount = 0;
            slash.setVisibility(View.VISIBLE);
            tvSelectedCount.setVisibility(View.VISIBLE);
            tvSelectedCount.setText(selectedCount + "");
            tvAllCount.setText(totalCount + "개");
            tvSelected.setText("0개 선택");
            imgAllCheck.setBackgroundResource(R.drawable.cart_check_off);
            if ( totalCount <= 0 )
                setEmptyViewVisible(true);
        }

        @Override
        public void onZzimClicked(API55.ProductItem item) {
            zzimItem = item;
            boolean status = false;
            if ( "N".equals(item.is_wish) )
                status = true;
            processZzimStatus(status, item.idx, item.strType);
        }
    };

    private void initView() {
        empty = findViewById(R.id.empty);
        btnCheckAll = (RelativeLayout) findViewById(R.id.btnCheckAll);
        btnDelete = (RelativeLayout) findViewById(R.id.btnDelete);
        twoBtnLayer = (RelativeLayout) findViewById(R.id.twoBtnLayer);
        btnCancel = (RelativeLayout) findViewById(R.id.btnCancel);
        btnEdit = (RelativeLayout) findViewById(R.id.btnEdit);
        imgAllCheck = (AppCompatImageView) findViewById(R.id.imgAllCheck);
        tvAllCount = (AppCompatTextView) findViewById(R.id.tvAllCount);
        tvSelectedCount = (AppCompatTextView) findViewById(R.id.tvSelectedCount);
        slash = (AppCompatTextView) findViewById(R.id.slash);
        tvSelected = (AppCompatTextView) findViewById(R.id.tvSelected);
        list = (RecyclerView) findViewById(R.id.list);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        toplayer = findViewById(R.id.toplayer);
        strAll = findViewById(R.id.strAll);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(clickListener);
        topBtn = findViewById(R.id.topBtn);
        topBtn.setOnClickListener(clickListener);

        adapter = new RecentProductsAdapter(RecentProductsActivity.this, callback);
        layoutManager = new WrappedLinearLayoutManager(RecentProductsActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        list.addOnScrollListener(scrollListener);
        list.setHasFixedSize(false);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(layoutManager);

        list.setAdapter(adapter);

        btnCheckAll.setOnClickListener(clickListener);
        btnDelete.setOnClickListener(clickListener);
        btnCancel.setOnClickListener(clickListener);
        btnEdit.setOnClickListener(clickListener);
    }

    private void toggleAllCheck() {
        isCheckAll = !isCheckAll;
        if (isCheckAll) {
            imgAllCheck.setBackgroundResource(R.drawable.check_on);
        } else {
            imgAllCheck.setBackgroundResource(R.drawable.cart_check_off);
        }
        adapter.setCheck(isCheckAll);

        if (isCheckAll) {
            tvSelected.setText(totalCount + "개 선택");
            selectedCount = adapter.getItemCount();
            strAll.setText("전체선택(");
            slash.setVisibility(View.VISIBLE);
            tvSelectedCount.setVisibility(View.VISIBLE);
            tvSelectedCount.setText(selectedCount + "");
            tvAllCount.setText(totalCount + "개");
        } else {
            selectedCount = 0;
            tvSelected.setText("0개 선택");
            strAll.setText("전체선택(");
            slash.setVisibility(View.VISIBLE);
            tvSelectedCount.setVisibility(View.VISIBLE);
            tvSelectedCount.setText(selectedCount + "");
            tvAllCount.setText(totalCount + "개");
        }
    }

    private void setEmptyViewVisible(boolean visible) {
        if (visible) {
            layoutEmpty.setVisibility(View.VISIBLE);
            toplayer.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            toplayer.setVisibility(View.VISIBLE);
            list.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ProductDetail로 넘어간 이후 해당 상품을 찜 했을 경우 처리를 위해
        if ( requestCode == REQUEST_ZZIM_STATUS ) {
            if ( data != null ) {
                boolean selected = data.getBooleanExtra("LIKE_STATUS", false);
                adapter.forcedZzim(clickedItem.idx, selected);
            }
        }
    }
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            int totalItemCount = adapter.getItemCount() - 1;
            int visibleCount = layoutManager.findFirstVisibleItemPosition();

            if ( topBtn.getVisibility() == View.GONE) {
                if ( visibleCount > 8 )
                    topBtn.setVisibility(View.VISIBLE);
            }

            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                isReload = true;
                ++pageCount;

                NetworkBus bus = new NetworkBus(NetworkApi.API55.name(), String.valueOf(pageCount));
                EventBus.getDefault().post(bus);
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if ( newState == RecyclerView.SCROLL_STATE_IDLE ) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE)
                            topBtn.setVisibility(View.GONE);
                    }
                }, 1500);
            }
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonBack :
                    onBackPressed();
                    break;
                case R.id.btnCheckAll:
                    toggleAllCheck();
                    break;
                case R.id.btnEdit:
                    twoBtnLayer.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.GONE);
                    btnCheckAll.setVisibility(View.VISIBLE);
                    empty.setVisibility(View.GONE);
                    adapter.setCheckVisible(View.VISIBLE);
                    strAll.setText("전체선택(");
                    slash.setVisibility(View.VISIBLE);
                    tvSelectedCount.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnCancel:
                    twoBtnLayer.setVisibility(View.GONE);
                    btnEdit.setVisibility(View.VISIBLE);
                    btnCheckAll.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                    adapter.setCheckVisible(View.GONE);
                    strAll.setText("전체(");
                    slash.setVisibility(View.GONE);
                    tvSelectedCount.setVisibility(View.GONE);
                    break;
                case R.id.btnDelete:
                    if ( adapter.getSelectedCount() > 0 ) {
                        dialog = new AppAlertDialog(RecentProductsActivity.this);
                        dialog.setTitle("최근 본 상품");
                        dialog.setMessage("선택한 상품을 삭제할까요?");
                        dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setRightButton(getString(R.string.msg_my_qna_del), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Logger.e("########### product id : " + adapter.getProductId());
                                NetworkBus bus = new NetworkBus(NetworkApi.API109.name(), adapter.getProductId());
                                EventBus.getDefault().post(bus);
                            }
                        });

                        dialog.show();
                    }
                    break;

                case R.id.topBtn:
                    list.scrollToPosition(0);
                    break;
            }
        }
    };

    private void processZzimStatus(boolean status, String idx, String type) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", AppPreferences.Companion.getUserId(RecentProductsActivity.this));
            obj.put("is_wish" , status == true ? "Y" : "N");
            obj.put("type", type);

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API126.name(), idx, body);
            EventBus.getDefault().post(bus);
        } catch (Exception e) {
            Logger.p(e);
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
