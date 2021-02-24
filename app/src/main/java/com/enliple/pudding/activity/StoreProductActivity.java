package com.enliple.pudding.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.home.ItemAdapter;
import com.enliple.pudding.bus.ZzimStatusBus;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkConst;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API70;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.SecondCategoryItem;
import com.enliple.pudding.model.TempJSON;
import com.enliple.pudding.model.ThirdCategoryItem;
import com.enliple.pudding.model.ThreeCategoryItem;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class StoreProductActivity extends AbsBaseActivity {
    public static final int REQUEST_FOR_LIKE = 10013;
    private static final int REQUEST_NO = 46;
    private boolean mIsVisibleToUser = true;
    private RecyclerView recyclerLiveProduct;
    private AppCompatTextView titleBarTitle;
    private RelativeLayout buttonClose;
    private ItemAdapter adapter;
    private String order = "1";
    private String mainOrder = "";
    private int mCount = 1;
    private boolean isMore = false;
    private String selectedCategory = "";
    private String searchWord = "";
    WrappedLinearLayoutManager layoutManager;
    private boolean isEndOfData = false;

    private String scCode;
    private String storeName;
    private String vodType;
    private boolean fromLive;
    private boolean fromStore;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_product);

        EventBus.getDefault().register(this);

        scCode = getIntent().getStringExtra(ShopTreeKey.KEY_SCCODE);
        fromStore = getIntent().getBooleanExtra("from_store", false);
        storeName = getIntent().getStringExtra("storeName");
        fromLive = getIntent().getBooleanExtra("from_live", false);
        vodType = getIntent().getStringExtra(ShopTreeKey.KEY_VOD_TYPE);
        if ( scCode == null )
            finish();

        adapter = new ItemAdapter(StoreProductActivity.this, fromLive, true, new ItemAdapter.Listener() {
            @Override
            public void bannerClicked(API70.EventItem item) {

            }

            @Override
            public void categoryClicked(String categoryId) {
                selectedCategory = categoryId;
                mCount = 1;
                isMore = false;
                searchWord = "";
                requestList(mCount, selectedCategory, scCode, REQUEST_NO, searchWord, 1);
            }

            @Override
            public void onSortClicked(String od) {
                Logger.e("order :: " + order);
                Logger.e("od :: " + od);
                order = od;
                mCount = 1;
                isMore = false;
                requestList(mCount, selectedCategory, scCode, REQUEST_NO, searchWord, 2);
            }

            @Override
            public void onSort2Clicked(String od) {
                mainOrder = od;
                mCount = 1;
                isMore = false;
                requestList(mCount, selectedCategory, scCode, REQUEST_NO, searchWord, 5);
            }

            @Override
            public void onLikeClick(API70.ProductItem info, boolean status) {
                String idx = info.idx;
                processZzimStatus(idx, info.strType, status);
            }

            @Override
            public void onItemClick(API70.ProductItem info) {
                Logger.e("name :: " + info.title);
                Logger.e("price :: " + info.price);
                Logger.e("image :: " + info.image1);
                Logger.e("storeName :: " + info.sitename);

                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setComponent(new ComponentName("com.enliple.pudding", "com.enliple.pudding.activity.ProductDetailActivity"));
                Logger.e("idx val :: " + info.idx);
                intent1.putExtra("from_live", fromLive);
                intent1.putExtra("from_store", true);
                intent1.putExtra("idx", info.idx);
                intent1.putExtra("name", info.title);
                intent1.putExtra("price", Utils.ToNumFormat((int) Double.parseDouble(info.price)) + "원");
                intent1.putExtra("image", info.image1);
                intent1.putExtra("storeName", info.sitename);
                intent1.putExtra(ShopTreeKey.KEY_PCODE, info.pcode);
                intent1.putExtra(ShopTreeKey.KEY_SCCODE, info.sc_code);
                intent1.putExtra(ShopTreeKey.KEY_VOD_TYPE, vodType);
                startActivityForResult(intent1, REQUEST_FOR_LIKE);
            }
        });
        titleBarTitle = findViewById(R.id.titleBarTitle);
        buttonClose = findViewById(R.id.buttonClose);
        recyclerLiveProduct = findViewById(R.id.recyclerLiveProduct);
        layoutManager = new WrappedLinearLayoutManager(StoreProductActivity.this);
        layoutManager.setOrientation(WrappedLinearLayoutManager.VERTICAL);
        recyclerLiveProduct.setHasFixedSize(false);
        recyclerLiveProduct.setLayoutManager(layoutManager);
        recyclerLiveProduct.setAdapter(adapter);

        recyclerLiveProduct.addOnScrollListener(scrollListener);

        titleBarTitle.setText(storeName);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        NetworkBus bus = new NetworkBus(NetworkApi.API81.name(), "select", scCode);// 전체 카테고리 가져오기
        EventBus.getDefault().post(bus);
        requestList(mCount, selectedCategory, scCode, REQUEST_NO, searchWord, 3);
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            int totalItemCount = adapter.getItemCount() - 1;
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0 ) {
                isMore = true;
                requestList(mCount, selectedCategory, scCode, REQUEST_NO, searchWord, 4);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mIsVisibleToUser = true;
//        NetworkBus bus = new NetworkBus(NetworkApi.API81.name(), "select", scCode);// 전체 카테고리 가져오기
//        EventBus.getDefault().post(bus);
//        requestList(mCount, selectedCategory, scCode, REQUEST_NO, searchWord);
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsVisibleToUser = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_LIKE ) {
            if ( data != null ) {
                if ( resultCode == RESULT_OK ) {
                    setResult(RESULT_OK, data);
                    finish();
                }
            } else {
                Logger.e("data is null");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("mIsVisibleToUser :: " + mIsVisibleToUser);
        String API81 = NetworkHandler.Companion.getInstance(StoreProductActivity.this).getKey(NetworkApi.API81.toString(), "select", scCode);
        String API70 = "GET/products?page";
//        requestList(mCount, selectedCategory, scCode, REQUEST_NO, searchWord, 1);
        API70 = NetworkHandler.Companion.getInstance(StoreProductActivity.this).getProductKey(NetworkApi.API70.toString(), ""+mCount, ""+REQUEST_NO, selectedCategory, scCode, searchWord,  order, mainOrder);
        Logger.e("data.arg1 :: " + data.arg1);
        Logger.e("API81 :: " + API81);
        if ( mIsVisibleToUser ) {
            if (data.arg1.equals(API81))
                setCategory(data);
            else if (data.arg1.equals(API70))
                getProductList(data);
        }

        if ( data.arg1.startsWith("POST/products/") && data.arg1.endsWith("wish") ) {
            String[] arr = data.arg1.split("/");
            String idx = arr[2];
            Logger.e("idx :: " + idx);
            if ( adapter != null ) {
                adapter.likeSuccess(idx);
            }
        }
    }

    private void setCategory(NetworkBusResponse data) {
        if (data.arg2.equals("ok")) {
            String str = DBManager.getInstance(StoreProductActivity.this).get(data.arg1);
            Logger.e("str :: " + str);
//            str = TempJSON.JSON;
            try {
                JSONObject obj = new JSONObject(str);
                JSONArray array = obj.optJSONArray("data");
                ArrayList<ThreeCategoryItem> itemArray = new ArrayList<>();

                // 1차 카테고리 전체를 add
                ThreeCategoryItem firstAll = new ThreeCategoryItem();
                firstAll.setCategoryId("");
                firstAll.setCategoryName("전체");
                firstAll.setSelected(true);
                firstAll.setCategoryImage(NetworkConst.CATEGORY_ALL_IMAGE_API);
                firstAll.setCategoryImageOn(NetworkConst.CATEGORY_ALL_ON_IMAGE_API);
                firstAll.setCategoryImageOff(NetworkConst.CATEGORY_ALL_OFF_IMAGE_API);
                firstAll.setSecondCategory(null);

                itemArray.add(firstAll);

                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.optJSONObject(i);
                        ThreeCategoryItem item = new ThreeCategoryItem();

                        String categoryId = object.optString("categoryId");
                        String categoryName = object.optString("categoryName");
                        String categoryImage = object.optString("categoryImage");
                        String categoryImageOn = object.optString("categoryImageOn");
                        String categoryImageOff = object.optString("categoryImageOff");
                        boolean selected = false;
                        ArrayList<SecondCategoryItem> secondCategoryArray = null;

                        JSONArray secondArray = object.optJSONArray("sub");
                        if (secondArray != null && secondArray.length() > 0) {
                            secondCategoryArray = new ArrayList<>();
                            SecondCategoryItem secondAllItem = new SecondCategoryItem();
                            secondAllItem.setCategoryId(categoryId);
                            secondAllItem.setCategoryName("전체");
                            secondAllItem.setSelected(true);
                            secondAllItem.setThirdCategory(null);

                            secondCategoryArray.add(secondAllItem);

                            for (int j = 0; j < secondArray.length(); j++) {
                                JSONObject secondObject = secondArray.optJSONObject(j);
                                SecondCategoryItem secondItem = new SecondCategoryItem();
                                String s_categoryId = secondObject.optString("categoryId");
                                String s_categoryName = secondObject.optString("categoryName");
                                boolean s_selected = false;

                                ArrayList<ThirdCategoryItem> thirdCategoryArray = null;

                                JSONArray thirdArray = secondObject.optJSONArray("sub");
                                if (thirdArray != null && thirdArray.length() > 0) {
                                    thirdCategoryArray = new ArrayList<>();
                                    ThirdCategoryItem thirdAllItem = new ThirdCategoryItem();
                                    thirdAllItem.setCategoryId(s_categoryId);
                                    thirdAllItem.setCategoryName("전체");
                                    thirdAllItem.setSelected(true);

                                    thirdCategoryArray.add(thirdAllItem);

                                    for (int k = 0; k < thirdArray.length(); k++) {
                                        JSONObject thirdObject = thirdArray.optJSONObject(k);
                                        ThirdCategoryItem thirdItem = new ThirdCategoryItem();
                                        String t_categoryId = thirdObject.optString("categoryId");
                                        String t_categoryName = thirdObject.optString("categoryName");
                                        boolean t_selected = false;

                                        thirdItem.setCategoryId(t_categoryId);
                                        thirdItem.setCategoryName(t_categoryName);
                                        thirdItem.setSelected(t_selected);

                                        thirdCategoryArray.add(thirdItem);
                                    }
                                }
                                secondItem.setCategoryId(s_categoryId);
                                secondItem.setCategoryName(s_categoryName);
                                secondItem.setSelected(s_selected);
                                secondItem.setThirdCategory(thirdCategoryArray);
                                secondCategoryArray.add(secondItem);
                            }
                        }
                        item.setCategoryId(categoryId);
                        item.setCategoryName(categoryName);
                        item.setSelected(selected);
                        item.setCategoryImage(categoryImage);
                        item.setCategoryImageOn(categoryImageOn);
                        item.setCategoryImageOff(categoryImageOff);
                        item.setSecondCategory(secondCategoryArray);
                        itemArray.add(item);
                    }
                }
                adapter.setCategoryItem(itemArray);

                Logger.e("itemArray.size :: " + itemArray.size());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processZzimStatus(String idx, String type, boolean status) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", AppPreferences.Companion.getUserId(StoreProductActivity.this));
            obj.put("type", type);
            obj.put("is_wish" , status == true ? "Y" : "N");

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API126.name(), idx, body);
            EventBus.getDefault().post(bus);

            EventBus.getDefault().post(new ZzimStatusBus(idx, status == true ? "Y" : "N", ""));
        } catch (Exception e) {
            Logger.p(e);
        }
    }

    private void requestList(int count, String cId, String skey, int requestNo, String searchWord, int index) {
        Logger.e(" ******************** ");
        Logger.e("requestList count :: " + count);
        Logger.e("requestList cId :: " + cId);
        Logger.e("requestList skey :: " + skey);
        Logger.e("requestList isMore :: " + isMore);
        Logger.e("requestList order :: " + order);
        Logger.e("requestList index :: " + index);
        NetworkBus bus = new NetworkBus(NetworkApi.API70.name(), count + "", requestNo + "",
                TextUtils.isEmpty(cId) ? "" : cId,
                skey == null ? "" : skey, searchWord,
                order, mainOrder);
        EventBus.getDefault().post(bus);
    }

    private void getProductList(NetworkBusResponse data) {
        if (data.arg2.equals("ok")) {
            String str = DBManager.getInstance(StoreProductActivity.this).get(data.arg1);
            Logger.e("Str :: "  + str);
            API70 response = new Gson().fromJson(str, API70.class);
            Logger.e("totalCount ::::::: " + response.total_cnt);
            isEndOfData = response.goodsList.size() == 0;
            if (response.goodsList.size() > 0)
                mCount++;
            if (isMore) {
                adapter.addItems(response.goodsList);
            } else {
                adapter.setItems(response.goodsList, response.event, response.total_cnt);
            }
        }
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}