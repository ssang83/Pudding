package com.enliple.pudding.shoppingcaster.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API69;
import com.enliple.pudding.commons.network.vo.BaseAPI;
import com.enliple.pudding.R;
import com.enliple.pudding.shoppingcaster.adapter.LiveStoreAdapter;
import com.enliple.pudding.shoppingcaster.data.LiveStoreItem;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class LiveStoreActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int GO_LIVE_PRODUCT_REQUEST = 10332;
    private static final int NUMBER_COLUMNS = 3;

    private RecyclerView recyclerView;
    private ImageButton buttonBack;
    private AppCompatButton btn_search;
    private EditText editSearch;
    private String testJSONStr;
    private LiveStoreAdapter liveStoreAdapter;
    private GridLayoutManager gridLayoutManager;
    private String search = "";

    private ArrayList<LiveStoreItem> storeArray = new ArrayList<LiveStoreItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_store);
        EventBus.getDefault().register(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerLiveStore);
        editSearch = (EditText) findViewById(R.id.editSearch);
        buttonBack = findViewById(R.id.buttonBack);
        btn_search = (AppCompatButton) findViewById(R.id.btn_search);

        buttonBack.setOnClickListener(this);
        btn_search.setOnClickListener(this);

        gridLayoutManager = new GridLayoutManager(LiveStoreActivity.this, NUMBER_COLUMNS);

        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(gridLayoutManager);

        liveStoreAdapter = new LiveStoreAdapter(LiveStoreActivity.this);
        recyclerView.setAdapter(liveStoreAdapter);

//        String jwt = AppPreferences.Companion.getJWT(LiveStoreActivity.this);
//        RetrofitNetworkProvider.Companion.createRetrofitInstance(
//                LiveStoreActivity.this,
//                AppPreferences.Companion.getServerAddress(LiveStoreActivity.this),
//                LiveStoreService.class)
//                .getLiveStore(jwt, "")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        response -> {
//                            Logger.e("product response :: " + response.toString());
//                            liveStoreAdapter.setItems(response.getData());
//                        },
//                        error -> {
//                            Logger.e("product response error :: " + error.getMessage());
//                        });


        liveStoreAdapter.setItemClick(new LiveStoreAdapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                API69.ShopItem info = liveStoreAdapter.getItemByPosition(position);
                String image = info.strImageUrl;
                String name = info.strShopName;
                String shopKey = info.shopKey;
                Intent intent = new Intent(LiveStoreActivity.this, LiveProductActivity.class);
                intent.putExtra("from_store", true);
                intent.putExtra("image", image);
                intent.putExtra("name", name);
                intent.putExtra("shopKey", shopKey);
                startActivityForResult(intent, GO_LIVE_PRODUCT_REQUEST);

            }
        });

        NetworkBus bus = new NetworkBus(NetworkApi.API69.name(), search);
        EventBus.getDefault().post(bus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonBack) {
            finish();
        } else if (v.getId() == R.id.btn_search) {
            search = editSearch.getText().toString();

            NetworkBus bus = new NetworkBus(NetworkApi.API69.name(), search);
            EventBus.getDefault().post(bus);

//            String jwt = AppPreferences.Companion.getJWT(LiveStoreActivity.this);
//            RetrofitNetworkProvider.Companion.createRetrofitInstance(
//                    LiveStoreActivity.this,
//                    AppPreferences.Companion.getServerAddress(LiveStoreActivity.this),
//                    LiveStoreService.class)
//                    .getLiveStore(jwt, editSearch.getText().toString())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            response -> {
//                                Logger.e("product response :: " + response.toString());
//                                liveStoreAdapter.setItems(response.getData());
//                            },
//                            error -> {
//                                Logger.e("product response error :: " + error.getMessage());
//                            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("onActivityResult LiveStoreActivity");
        Logger.d("requestCode :: " + requestCode);
        Logger.d("resultCode :: " + resultCode);
        if (requestCode == GO_LIVE_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                String price = data.getStringExtra("price");
                String image = data.getStringExtra("image");
                String storeName = data.getStringExtra("storeName");
                String idx = data.getStringExtra("idx");
                String pcode = data.getStringExtra(ShopTreeKey.KEY_PCODE);
                String sccode = data.getStringExtra(ShopTreeKey.KEY_SCCODE);

                Intent intent = new Intent();
                intent.putExtra("idx", idx);
                intent.putExtra("name", name);
                intent.putExtra("price", price);
                intent.putExtra("image", image);
                intent.putExtra("storeName", storeName);
                intent.putExtra("pcode", pcode);
                intent.putExtra("sccode", sccode);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String API69 = "";
        try {
            API69 = NetworkHandler.Companion.getInstance(this)
                    .getKey(NetworkApi.API69.toString(),
                            TextUtils.isEmpty(search) ? "" : URLEncoder.encode(search, "UTF-8"), "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (data.arg1.equals(API69)) {
            getShopList(data);
        }
    }

    private void getShopList(NetworkBusResponse data) {
        if (data.arg2.equals("ok")) {
            String str = DBManager.getInstance(this).get(data.arg1);
            API69 response = new Gson().fromJson(str, API69.class);

            if (response.nTotalCount > 0) {
                liveStoreAdapter.setItems(response.data);
            }
        } else {
            BaseAPI errorResult = new Gson().fromJson(data.arg4, BaseAPI.class);
            Logger.e("error : $errorResult");
        }
    }
}
