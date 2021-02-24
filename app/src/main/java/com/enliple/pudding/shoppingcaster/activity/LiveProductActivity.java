package com.enliple.pudding.shoppingcaster.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.MobonWebActivity;
import com.enliple.pudding.activity.ProductDetailActivity;
import com.enliple.pudding.adapter.home.ItemAdapter;
import com.enliple.pudding.adapter.home.PurchaseListAdapter;
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
import com.enliple.pudding.commons.network.vo.API139;
import com.enliple.pudding.commons.network.vo.API151;
import com.enliple.pudding.commons.network.vo.API70;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.SecondCategoryItem;
import com.enliple.pudding.model.ThirdCategoryItem;
import com.enliple.pudding.model.ThreeCategoryItem;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class LiveProductActivity extends AppCompatActivity implements  View.OnClickListener {
    public static final int PRODUCT_DETAIL_REQUEST = 10331;
    public static final int PRODUCT_MOBON_DETAIL_REQUEST = 10332;
    private static final int REQUEST_NO = 20;
    private RelativeLayout buttonBack;
    private RelativeLayout editLayer, shoptitleLayer;
    private AppCompatImageView goTop;
    private EditText editSearch;
    private ImageView shopLogo;
    private AppCompatTextView shopName, topTitle;
    private AppCompatTextView btn_search;
    private RecyclerView recyclerLiveProduct;

    private boolean mIsVisibleToUser = true;
    private boolean fromStore = false;
    private boolean isPromotion = false;
    private String storeName, storeLogo;
    private boolean isSample;
    private String shopKey = "";

    private ItemAdapter adapter;
    private PurchaseListAdapter pAdapter;
    private String order = "1";
    private String mainOrder = "";
    private int mCount = 1;
    private boolean isMore = false;
    private String selectedCategory = "";
    private String searchWord = "";
    WrappedLinearLayoutManager layoutManager;
    private boolean isEndOfData = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_product);
        EventBus.getDefault().register(this);

        Intent intent = getIntent();
        if (intent != null) {
            fromStore = intent.getBooleanExtra("from_store", false);
            isPromotion = intent.getBooleanExtra("from_promotion", false);
            storeLogo = intent.getStringExtra("image");
            storeName = intent.getStringExtra("name");
            isSample = intent.getBooleanExtra("is_sample", false);
            shopKey = intent.getStringExtra("shopKey");
        }

        if ( shopKey == null )
            shopKey = "";
        goTop = findViewById(R.id.goTop);
        btn_search = (AppCompatTextView) findViewById(R.id.btn_search);
        buttonBack = findViewById(R.id.buttonBack);
        editLayer = (RelativeLayout) findViewById(R.id.editLayer);
        shoptitleLayer = (RelativeLayout) findViewById(R.id.shoptitleLayer);
        topTitle = (AppCompatTextView) findViewById(R.id.topTitle);
        editSearch = (EditText) findViewById(R.id.editSearch);
        editSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editSearch.setOnEditorActionListener(actionListener);
        shopLogo = (ImageView) findViewById(R.id.shopLogo);
        shopName = (AppCompatTextView) findViewById(R.id.shopName);

        buttonBack.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        goTop.setOnClickListener(this);


        if (fromStore) {
            topTitle.setVisibility(View.GONE);
            editLayer.setVisibility(View.GONE);
            shoptitleLayer.setVisibility(View.VISIBLE);
            shopName.setText(storeName);
//            Glide.with(LiveProductActivity.this).load(storeLogo).asBitmap().centerCrop().into(new BitmapImageViewTarget(shopLogo) {
//                @Override
//                protected void setResource(Bitmap resource) {
//                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
//                    circularBitmapDrawable.setCircular(true);
//                    shopLogo.setImageDrawable(circularBitmapDrawable);
//                }
//            });
            if (!TextUtils.isEmpty(storeLogo) ) {
                shopLogo.setVisibility(View.VISIBLE);
                RequestOptions options = new RequestOptions();
                options.centerCrop();
                RequestBuilder<Bitmap> drawableRequest = Glide.with(LiveProductActivity.this).setDefaultRequestOptions(options).asBitmap().load(storeLogo);
                drawableRequest.into(new BitmapImageViewTarget(shopLogo) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        shopLogo.setImageDrawable(circularBitmapDrawable);
                    }
                });
            } else {
                shopLogo.setVisibility(View.GONE);
            }
        } else {
            if (isSample) {
                topTitle.setVisibility(View.VISIBLE);
                topTitle.setText("구매내역");
                editLayer.setVisibility(View.GONE);
                shoptitleLayer.setVisibility(View.GONE);
            } else if (isPromotion) {
                topTitle.setVisibility(View.VISIBLE);
                editLayer.setVisibility(View.GONE);
                shoptitleLayer.setVisibility(View.GONE);

                topTitle.setText(getString(R.string.msg_promotion_sample_item));
            } else {
                topTitle.setVisibility(View.GONE);
                editLayer.setVisibility(View.VISIBLE);
                shoptitleLayer.setVisibility(View.GONE);
            }
        }
        if ( isSample ) {
            pAdapter = new PurchaseListAdapter(LiveProductActivity.this, new PurchaseListAdapter.Listener() {
                @Override
                public void onItemClick(API151.Data info) {
                    Logger.e("name :: " + info.title);
                    Logger.e("price :: " + info.price);
                    Logger.e("image :: " + info.image1);
                    Logger.e("storeName :: " + info.sitename);
                    if ("1".equals(info.strType)) {
                        Intent intent1 = new Intent(LiveProductActivity.this, ProductDetailActivity.class);
                        Logger.e("idx val :: " + info.idx);

                        Logger.e("idx val :: " + info.idx);
                        intent1.putExtra("from_live", true);
                        intent1.putExtra("from_store", fromStore);
                        intent1.putExtra("idx", info.idx);
                        intent1.putExtra("name", info.title);
                        intent1.putExtra("price", Utils.ToNumFormat((int) Double.parseDouble(info.price)) + "원");
                        intent1.putExtra("image", info.image1);
                        intent1.putExtra("storeName", info.sitename);
                        intent1.putExtra(ShopTreeKey.KEY_PCODE, info.pcode);
                        intent1.putExtra(ShopTreeKey.KEY_SCCODE, info.sc_code);
                        startActivityForResult(intent1, PRODUCT_DETAIL_REQUEST);
                    } else if ( "2".equals(info.strType) ) {

                    } else if ( "3".equals(info.strType) ) {
                        ShopTreeAsyncTask task = new ShopTreeAsyncTask(LiveProductActivity.this);
                        task.getDRCLink(info.idx, "", "", info.strType, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                            @Override
                            public void onResponse(boolean result, Object obj) {
                                if (result) {
                                    JSONObject object = (JSONObject) obj;
                                    API139 response = new Gson().fromJson(object.toString(), API139.class);
                                    if ("success".equals(response.result)) {
                                        Intent intent1 = new Intent(LiveProductActivity.this, MobonWebActivity.class);
                                        intent1.putExtra("idx", info.idx);
                                        intent1.putExtra("name", info.title);
                                        intent1.putExtra("price", Utils.ToNumFormat((int) Double.parseDouble(info.price)) + "원");
                                        intent1.putExtra("image", info.image1);
                                        intent1.putExtra("storeName", info.sitename);
                                        intent1.putExtra("zzim_status", info.is_wish);
                                        intent1.putExtra(ShopTreeKey.KEY_PCODE, info.pcode);
                                        intent1.putExtra(ShopTreeKey.KEY_SCCODE, info.sc_code);
                                        intent1.putExtra(MobonWebActivity.INTENT_EXTRA_KEY_LINK, response.url);
                                        startActivityForResult(intent1, PRODUCT_MOBON_DETAIL_REQUEST);
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void onLikeClick(API151.Data item, boolean status) {

                }
            });
        } else {
            adapter = new ItemAdapter(LiveProductActivity.this, true, false,  new ItemAdapter.Listener() {
                @Override
                public void bannerClicked(API70.EventItem item) {

                }

                @Override
                public void categoryClicked(String categoryId) {
                    selectedCategory = categoryId;
                    mCount = 1;
                    isMore = false;
                    searchWord = "";
                    requestList(mCount, selectedCategory, shopKey, REQUEST_NO, searchWord);
                }

                @Override
                public void onSortClicked(String od) {
                    Logger.e("order :: " + order);
                    Logger.e("od :: " + od);
                    order = od;
                    mCount = 1;
                    isMore = false;
                    requestList(mCount, selectedCategory, shopKey, REQUEST_NO, searchWord);
                }

                @Override
                public void onSort2Clicked(String od) {
                    Logger.e("mainOrder :: " + mainOrder);
                    Logger.e("od :: " + od);
                    mainOrder = od;
                    mCount = 1;
                    isMore = false;
                    requestList(mCount, selectedCategory, shopKey, REQUEST_NO, searchWord);
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
                    if ("1".equals(info.strType)) {
                        Intent intent1 = new Intent(LiveProductActivity.this, ProductDetailActivity.class);
                        Logger.e("idx val :: " + info.idx);

                        Logger.e("idx val :: " + info.idx);
                        intent1.putExtra("from_live", true);
                        intent1.putExtra("from_store", fromStore);
                        intent1.putExtra("idx", info.idx);
                        intent1.putExtra("name", info.title);
                        intent1.putExtra("price", Utils.ToNumFormat((int) Double.parseDouble(info.price)) + "원");
                        intent1.putExtra("image", info.image1);
                        intent1.putExtra("storeName", info.sitename);
                        intent1.putExtra(ShopTreeKey.KEY_PCODE, info.pcode);
                        intent1.putExtra(ShopTreeKey.KEY_SCCODE, info.sc_code);
                        startActivityForResult(intent1, PRODUCT_DETAIL_REQUEST);
                    } else if ( "2".equals(info.strType) ) {

                    } else if ( "3".equals(info.strType) ) {
                        ShopTreeAsyncTask task = new ShopTreeAsyncTask(LiveProductActivity.this);
                        task.getDRCLink(info.idx, "", "", info.strType, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                            @Override
                            public void onResponse(boolean result, Object obj) {
                                try {
                                    if (result) {
                                        JSONObject object = (JSONObject) obj;
                                        API139 response = new Gson().fromJson(object.toString(), API139.class);
                                        if ("success".equals(response.result)) {
                                            Intent intent1 = new Intent(LiveProductActivity.this, MobonWebActivity.class);
                                            intent1.putExtra("idx", info.idx);
                                            intent1.putExtra("name", info.title);
                                            intent1.putExtra("price", Utils.ToNumFormat((int) Double.parseDouble(info.price)) + "원");
                                            intent1.putExtra("image", info.image1);
                                            intent1.putExtra("storeName", info.sitename);
                                            intent1.putExtra("zzim_status", info.is_wish);
                                            intent1.putExtra(ShopTreeKey.KEY_PCODE, info.pcode);
                                            intent1.putExtra(ShopTreeKey.KEY_SCCODE, info.sc_code);
                                            intent1.putExtra(MobonWebActivity.INTENT_EXTRA_KEY_LINK, response.url);
                                            startActivityForResult(intent1, PRODUCT_MOBON_DETAIL_REQUEST);
                                        }
                                    }
                                } catch (Exception e) {
                                    Logger.p(e);
                                }
                            }
                        });
                    }
                }
            });
        }

        recyclerLiveProduct = findViewById(R.id.recyclerLiveProduct);
        layoutManager = new WrappedLinearLayoutManager(LiveProductActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerLiveProduct.setHasFixedSize(false);
        recyclerLiveProduct.setLayoutManager(layoutManager);

        if ( isSample ) {
            if ( pAdapter != null )
                recyclerLiveProduct.setAdapter(pAdapter);
        } else {
            if ( adapter != null )
                recyclerLiveProduct.setAdapter(adapter);
        }

        if ( isSample ) {
            NetworkBus bus = new NetworkBus(NetworkApi.API151.name());
            EventBus.getDefault().post(bus);
        } else {
            recyclerLiveProduct.addOnScrollListener(scrollListener);
            NetworkBus bus = new NetworkBus(NetworkApi.API81.name(), "select", shopKey);// 전체 카테고리 가져오기
            EventBus.getDefault().post(bus);
            requestList(mCount, selectedCategory, shopKey, REQUEST_NO, searchWord);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsVisibleToUser = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsVisibleToUser = false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonBack) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (v.getId() == R.id.btn_search) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
            String word = editSearch.getText().toString();
            String trimed = word.replaceAll(" ", "");
            if (!TextUtils.isEmpty(trimed)) {
                searchWord = word;
                mCount = 1;
                isMore = false;
                requestList(mCount, selectedCategory, shopKey, REQUEST_NO, searchWord);
            }
        } else if ( v.getId() == R.id.goTop) {
            recyclerLiveProduct.scrollToPosition(0);
        }
    }

    private TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                String word = editSearch.getText().toString();
                String trimed = word.replaceAll(" ", "");
                if (!TextUtils.isEmpty(trimed)) {
                    searchWord = word;
                    mCount = 1;
                    isMore = false;
                    requestList(mCount, selectedCategory, shopKey, REQUEST_NO, searchWord);
                }
            }
            return false;
        }
    };

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if ( isSample ) {

            } else {
                int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                int totalItemCount = adapter.getItemCount() - 1;
                if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData && dy > 0 ) {
                    isMore = true;
                    requestList(mCount, selectedCategory, shopKey, REQUEST_NO, searchWord);
                }

                if ( goTop.getVisibility() == View.GONE ) {
                    if ( lastVisibleItemPosition > 10 )
                        goTop.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onScrollStateChanged (@NonNull RecyclerView recyclerView, int newState ) {
            Logger.e("scroll onScrollStateChanged :: newState :: " + newState);
            int state = newState;
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if ( state == RecyclerView.SCROLL_STATE_IDLE )
                            goTop.setVisibility(View.GONE);
                    }
                }, 1500);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("onActivityResult LiveProductActivity");
        Logger.d("requestCode :: " + requestCode);
        Logger.d("resultCode :: " + resultCode);
        if (requestCode == PRODUCT_DETAIL_REQUEST) {
            if (resultCode == RESULT_OK) {
                String idx = data.getStringExtra("idx");
                String name = data.getStringExtra("name");
                String price = data.getStringExtra("price");
                String image = data.getStringExtra("image");
                String storeName = data.getStringExtra("storeName");
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
                intent.putExtra("strType", "1");
                Logger.e("onActivityResult LiveProductActivity :: " + idx);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if ( requestCode == PRODUCT_MOBON_DETAIL_REQUEST ) {
            if (resultCode == RESULT_OK) {
                String idx = data.getStringExtra("idx");
                String name = data.getStringExtra("name");
                String price = data.getStringExtra("price");
                String image = data.getStringExtra("image");
                String storeName = data.getStringExtra("storeName");
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
                intent.putExtra("strType", "3");
                Logger.e("onActivityResult LiveProductActivity :: " + idx);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        if ( isSample ) {
            String key = "GET/user/" + AppPreferences.Companion.getUserId(LiveProductActivity.this) + "/order";
            if(key.equals(data.arg1)) {
                handleNetworkAPI151(data);
            }
        } else {
            Logger.e("shopKey :: " + shopKey);
            String API81 = NetworkHandler.Companion.getInstance(LiveProductActivity.this).getKey(NetworkApi.API81.toString(), "select", shopKey);
            String API70 = "GET/products?page";
            API70 = NetworkHandler.Companion.getInstance(LiveProductActivity.this).getProductKey(NetworkApi.API70.toString(), ""+mCount, ""+REQUEST_NO, selectedCategory, "", searchWord,  order, mainOrder);
            Logger.e("data.arg1 :: " + data.arg1);
            Logger.e("API81 :: " + API81);
            Logger.e("API70 :: " + API70);

            if ( mIsVisibleToUser ) {
                if (data.arg1.equals(API81))
                    setCategory(data);
                else if (data.arg1.startsWith(API70))
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
    }

    private void handleNetworkAPI151(NetworkBusResponse data) {
        if("ok" == data.arg2) {
            API151 response = new Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API151.class);
            try {
                pAdapter.setItems(response.data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setCategory(NetworkBusResponse data) {
        if (data.arg2.equals("ok")) {
            String str = DBManager.getInstance(LiveProductActivity.this).get(data.arg1);
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
            obj.put("user", AppPreferences.Companion.getUserId(LiveProductActivity.this));
            obj.put("is_wish" , status == true ? "Y" : "N");
            obj.put("type", type);

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API126.name(), idx, body);
            EventBus.getDefault().post(bus);
        } catch (Exception e) {
            Logger.p(e);
        }
    }

    private void requestList(int count, String cId, String skey, int requestNo, String searchWord) {
        Logger.e(" ******************** ");
        Logger.e("requestList count :: " + count);
        Logger.e("requestList cId :: " + cId);
        Logger.e("requestList skey :: " + skey);
        Logger.e("requestList isMore :: " + isMore);
        Logger.e("requestList order :: " + order);
        NetworkBus bus = new NetworkBus(NetworkApi.API70.name(), count + "", requestNo + "",
                TextUtils.isEmpty(cId) ? "" : cId,
                skey == null ? "" : skey, searchWord,
                order, mainOrder);
        EventBus.getDefault().post(bus);
    }

    private void getProductList(NetworkBusResponse data) {
        if (data.arg2.equals("ok")) {
            if ( adapter == null )
                return;
            String str = DBManager.getInstance(LiveProductActivity.this).get(data.arg1);
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
}
