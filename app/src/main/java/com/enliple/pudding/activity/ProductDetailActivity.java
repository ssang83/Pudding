package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.DetailOptionAdapter;
import com.enliple.pudding.bus.SoftKeyboardBus;
import com.enliple.pudding.bus.ZzimStatusBus;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.chat.ChatManager;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API21;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.fragment.main.ShoppingLiveFragment;
import com.enliple.pudding.fragment.shoptree.ProductQnAFragment;
import com.enliple.pudding.fragment.shoptree.ReviewFragment;
import com.enliple.pudding.fragment.shoptree.SalerInfoFragment;
import com.enliple.pudding.keyboard.KeyboardHeightProvider;
import com.enliple.pudding.model.DetailOptionItem;
import com.enliple.pudding.model.DetailOptionList;
import com.enliple.pudding.model.DetailSubOption;
import com.enliple.pudding.model.SellerInfo;
import com.enliple.pudding.model.ShipCondition;
import com.enliple.pudding.model.ShopTreeImageModel;
import com.enliple.pudding.model.ShopTreeModel;
import com.enliple.pudding.model.ShopTreeStoreModel;
import com.enliple.pudding.widget.AppAlertDialog;
import com.enliple.pudding.widget.SingleButtonDialog;
import com.enliple.pudding.widget.shoptree.DetailDialog;
import com.google.gson.Gson;
import com.igaworks.v2.abxExtensionApi.AbxCommerce;
import com.igaworks.v2.core.AdBrixRm;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ProductDetailActivity extends AbsBaseActivity {
    public static final int REQUEST_STORE_TO_DETAIL = 0xBD02;
    public static final int REST_HEIGHT = 174; // TITLE + TAB + BUTTON HEIGHT + status bar (24dp)

    private static final int TAB_PRODUCT_INFO = 0;
    private static final int TAB_PRODUCT_REVIEW = 1;
    private static final int TAB_QNA = 2;
    private static final int TAB_SALER_INFO = 3;

    private static final String SHOP_TREE_PRODUCT_URL = "https://shop-tree.com/products/";

    private static final int ACTIVITY_REQUEST_CODE_LIVE = 0x001;

    private ShopTreeModel mModel;
    private SellerInfo mCellerInfo = null;

    private Bundle mBundle;
    private boolean isHandlerRunning = false;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private ReviewFragment reviewFragment;
    private ProductQnAFragment productQnAFragment;
    private SalerInfoFragment salerInfoFragment;

    private ScrollView scrollView;
    private AppCompatImageView go_top, imageViewLogo, main_image;
    private WebView dummy;
    private FrameLayout qa_layoutForSubFragment, celler_layoutForSubFragment, review_layoutForSubFragment;
    private String pCode, scCode, mDescription, name, image, price, storeName;
    private View viewForRealTab, viewForHiddenTab, productLine, productHiddenLine, reviewLine, reviewHiddenLine, qaLine, qaHiddenLine, salerInfoLine, salerInfoHiddenLine;
    private AppCompatTextView top_base;
    private LinearLayout viewForProductInfo, mTwoBtnLayer, mBroadcastBtnLayer, btnGoShop;
    private RelativeLayout productInfoLayer, productReviewLayer, qaLayer, salerInfoLayer, productInfoHiddenLayer, qaHiddenLayer, salerInfoHiddenLayer,
            productReviewHiddenLayer, buttonCasting, buttonDone, buttonCart, buttonZzim, optionLayer, close_layer;
    private AppCompatTextView textViewTitle, textViewDeliveryFee, productRealTab, reviewRealTab, qnaRealTab, salerRealTab, productHiddneTab, reviewHiddenTab,
            qnaHiddenTab, salerHiddenTab, textViewDeliveryGuide, imageViewNewIcon, imageViewBestIcon, textViewShopName, textViewPrice, textViewDiscountPrice, textViewPoint, percentage,
            textViewPointGuide, totalQuantity, totalPrice;
    private AppCompatButton goBroadcast;
    private RelativeLayout buttonBack, frLayer;
    private AppCompatImageView imageViewZzim;
    private RecyclerView optionRecyclerView;

    private boolean isFromLive = false;
    private boolean isFromStore = false;
    private String idx = "";
    private String mScCode, mPCode, streamKey, vodType, recommend_id;
    private boolean isLoginUser = false;
    public static int productLayerHeight = 0;
    private int viewHeight = 0;
    public boolean isShown = false;
    private KeyboardHeightProvider mKeyboardHeightProvider;
    private int scrollPosition = 0;

    private WrappedLinearLayoutManager layoutManager;
    private DetailOptionAdapter adapter;
    private int position = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        EventBus.getDefault().register(this);
        mKeyboardHeightProvider = new KeyboardHeightProvider(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mKeyboardHeightProvider.start();
            }
        }, 500);

        int screenHeight = AppPreferences.Companion.getScreenHeight(ProductDetailActivity.this);
        int rest = Utils.ConvertDpToPx(ProductDetailActivity.this, REST_HEIGHT);
        viewHeight = screenHeight - rest;
        Intent intent = getIntent();
        if (intent != null) {
            idx = intent.getStringExtra("idx");
            Logger.e("getIdx detail :: " + idx);
            isFromLive = intent.getBooleanExtra("from_live", false);
            isFromStore = intent.getBooleanExtra("from_store", false);
            pCode = intent.getStringExtra(ShopTreeKey.KEY_PCODE);
            scCode = intent.getStringExtra(ShopTreeKey.KEY_SCCODE);
            Logger.d("idx :: " + idx);
            Logger.d("pCode :: " + pCode);
            Logger.d("scCode :: " + scCode);
            streamKey = intent.getStringExtra(ShopTreeKey.KEY_STREAM_KEY);
            vodType = intent.getStringExtra(ShopTreeKey.KEY_VOD_TYPE);
            recommend_id = intent.getStringExtra(ShopTreeKey.KEY_RECOMMEND_ID);
            Logger.e("vodType :: " + vodType);
            Logger.e("streamKey :: " + streamKey);
            name = intent.getStringExtra("name");
            image = intent.getStringExtra("image");
            price = intent.getStringExtra("price");
            storeName = intent.getStringExtra("storeName");
        } else {
            finish();
        }
        initViews();

        try {
            AdBrixRm.CommerceProductModel productModel1 = new AdBrixRm.CommerceProductModel()
                    .setProductID(idx)
                    .setProductName(name);

            AbxCommerce.productView(productModel1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus ) {
        super.onWindowFocusChanged(hasFocus);
        int[] l = new int[2];
        frLayer.getLocationOnScreen(l);
        int y = l[1];
        int bot = y + frLayer.getHeight();
        scrollPosition = bot;
    }


    @Override
    public void onResume() {
        super.onResume();
        isLoginUser = AppPreferences.Companion.getLoginStatus(ProductDetailActivity.this);
        isShown = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isShown = false;
    }

    @Override
    public void onBackPressed() {
        Logger.e("onBackPressed");
        Intent intent = getIntent();
        intent.putExtra("idx", idx);
        intent.putExtra("LIKE_STATUS", imageViewZzim.isSelected());
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close();
        }
        AppToast.Companion.cancelAllToast();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SoftKeyboardBus bus) {
        Logger.e("SoftKeyboardBus height: " + bus.height);
        if ( isShown ) {
            boolean isKeyboardShow = false;
            if ( bus.height > 100 ) {
                Logger.e("scrollPosition :: " + scrollPosition);
                if ( qa_layoutForSubFragment.getVisibility() == View.GONE )
                    scrollView.scrollTo(0, scrollPosition);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String api126 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API126.toString(), idx, "");
        String api21 = NetworkHandler.Companion.getInstance(this)
                .getKey(NetworkApi.API21.toString(), AppPreferences.Companion.getUserId(this), "");

        if(data.arg1.equals(api126)) {
            if("ok".equals(data.arg2)) {
                if ( imageViewZzim.isSelected() )
                    imageViewZzim.setBackgroundResource(R.drawable.like_off_ico);
                else
                    imageViewZzim.setBackgroundResource(R.drawable.like_on_ico);
                imageViewZzim.setSelected(!imageViewZzim.isSelected());
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
        } else if(data.arg1.equals(api21)) {
            API21 response = new Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API21.class);
            if (Integer.valueOf(response.cartCount) > 0) {
                AppPreferences.Companion.setCartCnt(ProductDetailActivity.this, response.cartCount);
            }
        }
    }

    private void processZzimStatus(boolean status) {
        if ( isLoginUser ) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("user", AppPreferences.Companion.getUserId(this));
                obj.put("is_wish" , status == true ? "Y" : "N");
                obj.put("type", "1");

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
                NetworkBus bus = new NetworkBus(NetworkApi.API126.name(), idx, body);
                EventBus.getDefault().post(bus);

                EventBus.getDefault().post(new ZzimStatusBus(idx, status == true ? "Y" : "N", ""));
            } catch (Exception e) {
                Logger.p(e);
            }
        } else {
            goLogin();
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long viewId = v.getId();
            if (viewId == R.id.productInfoLayer || viewId == R.id.productInfoLayer1) {
                Logger.e("PRODUCT CLICKED");
                setTabUI(TAB_PRODUCT_INFO);
                dummy.setVisibility(View.VISIBLE);
                Logger.e("dummy visible");
                review_layoutForSubFragment.setVisibility(View.GONE);
                qa_layoutForSubFragment.setVisibility(View.GONE);
                celler_layoutForSubFragment.setVisibility(View.GONE);
                scrollToView(viewForRealTab, scrollView, 0);
            } else if (viewId == R.id.productReviewLayer || viewId == R.id.productReviewLayer1) {
                Logger.e("REVIEW CLICKED");
                Logger.e("screenHeight :: " + AppPreferences.Companion.getScreenHeight(ProductDetailActivity.this));
                Logger.e("productHeight :: " + productLayerHeight);
                review_layoutForSubFragment.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if ( review_layoutForSubFragment.getHeight() > 0 ) {
                            Logger.e("qa_layoutForSubFragment height :: " + review_layoutForSubFragment.getHeight());
                            Logger.e("viewHeight :: " + viewHeight);
                            int diff = viewHeight - review_layoutForSubFragment.getHeight();
                            Logger.e("diff :: " + diff);
                            if ( diff > 0 ) {
                                reviewFragment.setEmptyHeight(diff);
                            }
                            scrollToView(viewForRealTab, scrollView, 0);
                            review_layoutForSubFragment.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
                setTabUI(TAB_PRODUCT_REVIEW);
                dummy.setVisibility(View.GONE);
                Logger.e("dummy gone");
                review_layoutForSubFragment.setVisibility(View.VISIBLE);
                qa_layoutForSubFragment.setVisibility(View.GONE);
                celler_layoutForSubFragment.setVisibility(View.GONE);
            } else if (viewId == R.id.qaLayer || viewId == R.id.qaLayer1) {
                Logger.e("QA CLICKED");

                Logger.e("screenHeight :: " + AppPreferences.Companion.getScreenHeight(ProductDetailActivity.this));
                Logger.e("productHeight :: " + productLayerHeight);
                qa_layoutForSubFragment.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if ( qa_layoutForSubFragment.getHeight() > 0 ) {
                            Logger.e("qa_layoutForSubFragment height :: " + qa_layoutForSubFragment.getHeight());
                            Logger.e("viewHeight :: " + viewHeight);
                            int diff = viewHeight - qa_layoutForSubFragment.getHeight();
                            Logger.e("diff :: " + diff);
                            if ( diff > 0 ) {
                                productQnAFragment.setEmptyHeight(diff);
                            }
                            scrollToView(viewForRealTab, scrollView, 0);
                            qa_layoutForSubFragment.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
                setTabUI(TAB_QNA);
                dummy.setVisibility(View.GONE);
                Logger.e("dummy gone");
                review_layoutForSubFragment.setVisibility(View.GONE);
                qa_layoutForSubFragment.setVisibility(View.VISIBLE);
                celler_layoutForSubFragment.setVisibility(View.GONE);
            } else if (viewId == R.id.salerInfoLayer || viewId == R.id.salerInfoLayer1) {
//                Logger.e("CELLER CLICKED");
                setTabUI(TAB_SALER_INFO);
                dummy.setVisibility(View.GONE);
                Logger.e("dummy gone");
                review_layoutForSubFragment.setVisibility(View.GONE);
                qa_layoutForSubFragment.setVisibility(View.GONE);
                celler_layoutForSubFragment.setVisibility(View.VISIBLE);
                scrollToView(viewForRealTab, scrollView, 0);
            } else if (viewId == R.id.buttonDone) {

                /**
                 if (isLoginUser) {
                 if ( mFOptionLayer.getVisibility() == View.VISIBLE ) {
                 String optionStr = "";
                 try {
                 String sc = scCode;
                 String pcode = pCode;

                 ArrayList<PurchaseShopTreeModel> modelArray = null;
                 if (mHasOption) {
                 modelArray = mOptionAdapter.getItems();
                 } else {
                 PurchaseShopTreeModel p_model = new PurchaseShopTreeModel();
                 p_model.setOptionKey("");
                 p_model.setOptionQuantity(mNQuantity);
                 modelArray = new ArrayList<PurchaseShopTreeModel>();
                 modelArray.add(p_model);
                 }

                 if (modelArray.size() <= 0) {
                 //                                Toast.makeText(ProductDetailActivity.this, getResources().getString(R.string.msg_select_option), Toast.LENGTH_SHORT).show();
                 new AppToast(ProductDetailActivity.this).showToastMessage(getResources().getString(R.string.msg_select_option),
                 AppToast.DURATION_MILLISECONDS_DEFAULT,
                 AppToast.GRAVITY_BOTTOM);
                 return;
                 }

                 JSONObject m_obj = new JSONObject();

                 JSONArray jArray = new JSONArray();
                 JSONObject obj = new JSONObject();
                 obj.put("sc_code", sc);
                 JSONArray iArray = new JSONArray();
                 for (int i = 0; i < modelArray.size(); i++) {
                 JSONObject sObject = new JSONObject();
                 sObject.put("pcode", pcode);
                 sObject.put("key", modelArray.get(i).getOptionKey());
                 sObject.put("cnt", modelArray.get(i).getOptionQuantity());
                 sObject.put("stream_key", streamKey);
                 sObject.put("vod_type", vodType);
                 sObject.put("ct_id", "");
                 sObject.put("recommend_mb_id", recommend_id);
                 iArray.put(sObject);
                 }
                 obj.put("products", iArray);
                 jArray.put(obj);
                 m_obj.put("group", jArray);

                 optionStr = m_obj.toString();

                 } catch (Exception e) {
                 e.printStackTrace();
                 return;
                 }

                 if (TextUtils.isEmpty(optionStr))
                 return;
                 Intent intent = new Intent(ProductDetailActivity.this, PurchaseActivity.class);
                 intent.putExtra("PRODUCT_BODY", optionStr);
                 intent.putExtra("FROM_CART", false);
                 startActivity(intent);
                 } else {
                 mFOptionLayer.setVisibility(View.VISIBLE);
                 }
                 } else {
                 goLogin();
                 }
                 **/
                if (isLoginUser) {
                    if ( optionLayer.getVisibility() == View.VISIBLE ) {
                        String optionStr = "";
                        try {
                            String sc = scCode;
                            String pcode = pCode;

                            ArrayList<DetailOptionItem> selectedOptionArray = adapter.getSelectedList();
                            if (selectedOptionArray != null && selectedOptionArray.size() > 0) {
                                JSONObject m_obj = new JSONObject();

                                JSONArray jArray = new JSONArray();
                                JSONObject obj = new JSONObject();
                                obj.put("sc_code", sc);
                                JSONArray iArray = new JSONArray();
                                for (int i = 0; i < selectedOptionArray.size(); i++) {
                                    JSONObject sObject = new JSONObject();
                                    sObject.put("pcode", pcode);
                                    sObject.put("key", selectedOptionArray.get(i).getKey());
                                    sObject.put("cnt", selectedOptionArray.get(i).getSelectedQuantity());
                                    sObject.put("stream_key", streamKey);
                                    sObject.put("vod_type", vodType);
                                    sObject.put("ct_id", "");
                                    sObject.put("recommend_mb_id", recommend_id);
                                    iArray.put(sObject);
                                }
                                obj.put("products", iArray);
                                jArray.put(obj);
                                m_obj.put("group", jArray);

                                optionStr = m_obj.toString();

                                if (TextUtils.isEmpty(optionStr))
                                    return;
                                Intent intent = new Intent(ProductDetailActivity.this, PurchaseActivity.class);
                                intent.putExtra("PRODUCT_BODY", optionStr);
                                intent.putExtra("FROM_CART", false);
                                startActivity(intent);
                            } else {
                                new AppToast(ProductDetailActivity.this).showToastMessage(getResources().getString(R.string.msg_select_option),
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        optionLayer.setVisibility(View.VISIBLE);
                    }
                } else {
                    goLogin();
                }
            } else if (viewId == R.id.goBroadcast) {
                if (isLoginUser) {
                    Intent intent = new Intent();
                    intent.putExtra("from_store", isFromStore);
                    intent.putExtra("from_live", isFromLive);
                    intent.putExtra("idx", idx);
                    intent.putExtra("name", name);
                    intent.putExtra("image", image);
                    intent.putExtra("price", price);
                    intent.putExtra("storeName", storeName);
                    intent.putExtra(ShopTreeKey.KEY_PCODE, pCode);
                    intent.putExtra(ShopTreeKey.KEY_SCCODE, scCode);
                    intent.putExtra("LIKE_STATUS", imageViewZzim.isSelected());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    goLogin();
                }
            } else if (viewId == R.id.go_top) {
                scrollView.smoothScrollTo(0, 0);
                go_top.setVisibility(View.GONE);
            } else if (viewId == R.id.buttonBack) {
                Intent intent = new Intent();
                intent.putExtra("idx", idx);
                intent.putExtra("LIKE_STATUS", imageViewZzim.isSelected());
                setResult(RESULT_CANCELED, intent);
                finish();
            } else if (viewId == R.id.btnGoShop ) {
                Intent intent = new Intent(ProductDetailActivity.this, StoreProductActivity.class);
                intent.putExtra("from_live", isFromLive);
                intent.putExtra("from_store", isFromStore);
                intent.putExtra("storeName", textViewShopName.getText().toString());
                intent.putExtra(ShopTreeKey.KEY_SCCODE, scCode);
                intent.putExtra(ShopTreeKey.KEY_VOD_TYPE, vodType);
                startActivityForResult(intent, REQUEST_STORE_TO_DETAIL);

            } else if(viewId == R.id.buttonZzim) {
                processZzimStatus(!imageViewZzim.isSelected());
            } else if(viewId == R.id.buttonCasting) {
                if(AppPreferences.Companion.getLoginStatus(ProductDetailActivity.this)) {
                    Intent intent = new Intent(ProductDetailActivity.this, BroadcastSettingActivity.class);
                    intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.Companion.getUserId(ProductDetailActivity.this));
                    intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "");
                    intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_PRODUCT);
                    intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.Companion.getUserId(ProductDetailActivity.this));
                    intent.putExtra("from_store", isFromStore);
                    intent.putExtra("idx", idx);
                    intent.putExtra("name", name);
                    intent.putExtra("image", image);
                    intent.putExtra("price", price);
                    intent.putExtra("storeName", storeName);
                    intent.putExtra(ShopTreeKey.KEY_PCODE, pCode);
                    intent.putExtra(ShopTreeKey.KEY_SCCODE, scCode);

                    startActivity(intent);
                } else {
                    startActivityForResult(new Intent(ProductDetailActivity.this, LoginActivity.class), ACTIVITY_REQUEST_CODE_LIVE);
                }
            } else if (viewId == R.id.close_layer) {
                if  (optionLayer.getVisibility() == View.VISIBLE ) {
                    optionLayer.setVisibility(View.GONE);
                }
            }
        }
    };

    private void initViews() {
        optionLayer = findViewById(R.id.optionLayer);
        close_layer = findViewById(R.id.close_layer);
        totalQuantity = findViewById(R.id.totalQuantity);
        totalPrice = findViewById(R.id.totalPrice);
        optionRecyclerView = findViewById(R.id.optionRecyclerView);
        frLayer = findViewById(R.id.frLayer);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        go_top = (AppCompatImageView) findViewById(R.id.go_top);
        top_base = (AppCompatTextView) findViewById(R.id.top_base);
        viewForProductInfo = (LinearLayout) findViewById(R.id.layoutProductInfo);
        mTwoBtnLayer = (LinearLayout) findViewById(R.id.two_btn_layer);
        mBroadcastBtnLayer = (LinearLayout) findViewById(R.id.go_broadcast_btn_layer);
        textViewTitle = (AppCompatTextView) findViewById(R.id.textViewTitle);
        textViewDeliveryFee = (AppCompatTextView) findViewById(R.id.textViewDeliveryFee);
        textViewDeliveryGuide = (AppCompatTextView) findViewById(R.id.textViewDeliveryGuide);
        imageViewNewIcon = (AppCompatTextView) findViewById(R.id.imageViewNewIcon);
        imageViewBestIcon = (AppCompatTextView) findViewById(R.id.imageViewBestIcon);
        textViewShopName = (AppCompatTextView) findViewById(R.id.textViewShopName);
        textViewPrice = (AppCompatTextView) findViewById(R.id.textViewPrice);
        textViewDiscountPrice = (AppCompatTextView) findViewById(R.id.textViewDiscountPrice);
        textViewPoint = (AppCompatTextView) findViewById(R.id.textViewPoint);
        textViewPointGuide = (AppCompatTextView) findViewById(R.id.textViewPointGuide);
        percentage = findViewById(R.id.percentage);
        imageViewLogo = (AppCompatImageView) findViewById(R.id.imageViewLogo);
        main_image = (AppCompatImageView) findViewById(R.id.main_image);
        dummy = (WebView) findViewById(R.id.dummy);
        qa_layoutForSubFragment = (FrameLayout) findViewById(R.id.qa_layoutForSubFragment);
        celler_layoutForSubFragment = (FrameLayout) findViewById(R.id.celler_layoutForSubFragment);
        review_layoutForSubFragment = (FrameLayout) findViewById(R.id.review_layoutForSubFragment);
        buttonDone = findViewById(R.id.buttonDone);
        buttonCart = findViewById(R.id.buttonCart);
        goBroadcast = (AppCompatButton) findViewById(R.id.goBroadcast);
        buttonBack = findViewById(R.id.buttonBack);
        btnGoShop = findViewById(R.id.btnGoShop);
        viewForHiddenTab = findViewById(R.id.layoutForHiddenTab);
        viewForRealTab = findViewById(R.id.layoutForRealTab);
        productInfoLayer = (RelativeLayout) viewForRealTab.findViewById(R.id.productInfoLayer);
        productReviewLayer = (RelativeLayout) viewForRealTab.findViewById(R.id.productReviewLayer);
        qaLayer = (RelativeLayout) viewForRealTab.findViewById(R.id.qaLayer);
        salerInfoLayer = (RelativeLayout) viewForRealTab.findViewById(R.id.salerInfoLayer);
        productInfoHiddenLayer = (RelativeLayout) viewForHiddenTab.findViewById(R.id.productInfoLayer1);
        productReviewHiddenLayer = (RelativeLayout) viewForHiddenTab.findViewById(R.id.productReviewLayer1);
        qaHiddenLayer = (RelativeLayout) viewForHiddenTab.findViewById(R.id.qaLayer1);
        salerInfoHiddenLayer = (RelativeLayout) viewForHiddenTab.findViewById(R.id.salerInfoLayer1);
        productRealTab = (AppCompatTextView) viewForRealTab.findViewById(R.id.textViewProductInfo);
        reviewRealTab = (AppCompatTextView) viewForRealTab.findViewById(R.id.textViewProductReview);
        qnaRealTab = (AppCompatTextView) viewForRealTab.findViewById(R.id.textViewQnA);
        salerRealTab = (AppCompatTextView) viewForRealTab.findViewById(R.id.textViewSalerInfo);
        productHiddneTab = (AppCompatTextView) viewForHiddenTab.findViewById(R.id.textViewProductInfo1);
        reviewHiddenTab = (AppCompatTextView) viewForHiddenTab.findViewById(R.id.textViewProductReview1);
        qnaHiddenTab = (AppCompatTextView) viewForHiddenTab.findViewById(R.id.textViewQnA1);
        salerHiddenTab = (AppCompatTextView) viewForHiddenTab.findViewById(R.id.textViewSalerInfo1);
        productLine = viewForRealTab.findViewById(R.id.product_line);
        reviewLine = viewForRealTab.findViewById(R.id.review_line);
        qaLine = viewForRealTab.findViewById(R.id.qa_line);
        salerInfoLine = viewForRealTab.findViewById(R.id.salerInfo_line);
        productHiddenLine = viewForHiddenTab.findViewById(R.id.product_line1);
        reviewHiddenLine = viewForHiddenTab.findViewById(R.id.review_line1);
        qaHiddenLine = viewForHiddenTab.findViewById(R.id.qa_line1);
        salerInfoHiddenLine = viewForHiddenTab.findViewById(R.id.salerInfo_line1);
        buttonZzim = findViewById(R.id.buttonZzim);
        buttonCasting = findViewById(R.id.buttonCasting);
        imageViewZzim = findViewById(R.id.imageViewZzim);

        buttonDone.setOnClickListener(mClickListener);
        buttonCart.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Logger.e("single click cart");
                if (isLoginUser) {
                    if ( optionLayer.getVisibility() == View.VISIBLE ) {
                        if (mModel == null)
                            return;

                        String sc = scCode;
                        String pcode = pCode;
                        String optionStr = "";

                        ArrayList<DetailOptionItem> selectedOptionArray = adapter.getSelectedList();

                        if (selectedOptionArray != null && selectedOptionArray.size() > 0) {
                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("stream_key", streamKey);
                                obj.put("vod_type", vodType);
                                obj.put("recommend_mb_id", recommend_id);
                                obj.put("sc_code", sc);
                                obj.put("pcode", pcode);
                                obj.put("pid", idx);
                                JSONArray jArray = new JSONArray();
                                for (int i = 0; i < selectedOptionArray.size(); i++) {
                                    JSONObject sObject = new JSONObject();
                                    Logger.e("selected optionkey :: " + selectedOptionArray.get(i).getOptionkey());
                                    sObject.put("key", selectedOptionArray.get(i).getKey());
                                    sObject.put("cnt", selectedOptionArray.get(i).getSelectedQuantity());
                                    jArray.put(sObject);
                                }
                                obj.put("items", jArray);
                                optionStr = obj.toString();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (!TextUtils.isEmpty(optionStr)) {
                                ShopTreeAsyncTask task = new ShopTreeAsyncTask(ProductDetailActivity.this);
                                task.insertCart(optionStr, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
                                    @Override
                                    public void onResponse(boolean rt, Object obj) {
                                        try {
                                            JSONObject object = (JSONObject) obj;
                                            String result = object.optString(ShopTreeKey.KEY_RESULT).toLowerCase();
                                            String msg = object.optString("message");
                                            if ("true".equals(result)) {
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        EventBus.getDefault().post(new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(ProductDetailActivity.this)));
                                                    }
                                                }, 100);
                                                Logger.e("before msg :: " + msg );
                                                if ( TextUtils.isEmpty(msg) )
                                                    msg = getResources().getString(R.string.inserted_cart);
                                                Logger.e("after msg :: " + msg );
//                                                new AppToast(ProductDetailActivity.this).showToastMessage(msg,
//                                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
//                                                        AppToast.GRAVITY_BOTTOM);
                                                AppAlertDialog dialog = new AppAlertDialog(ProductDetailActivity.this);
                                                dialog.setTitle("장바구니");
                                                dialog.setMessage("장바구니에 추가했습니다. 장바구니로 이동하시겠습니까?");
                                                dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                dialog.setRightButton(getString(R.string.msg_my_follow_confirm), new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        startActivity(new Intent(ProductDetailActivity.this, ProductCartActivity.class));
                                                        dialog.dismiss();
                                                    }
                                                });
                                                dialog.show();

                                                Intent intent = new Intent(ShoppingLiveFragment.SEND_CHAT);
                                                intent.putExtra("GUBUN", ChatManager.GUBUN_CART);
                                                intent.putExtra("NAME", mModel.getName());
                                                sendBroadcast(intent);

                                                try {
                                                    AdBrixRm.CommerceProductModel productModel = new AdBrixRm.CommerceProductModel()
                                                            .setProductName(mModel.getName());

                                                    ArrayList proList = new ArrayList();
                                                    proList.add(productModel);

                                                    AbxCommerce.addToCart(proList);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            } else {
                                                String errMessage = object.optString("resultMessage");
//                                            Toast.makeText(ProductDetailActivity.this, errMessage, Toast.LENGTH_SHORT).show();
                                                if (errMessage != null && !TextUtils.isEmpty(errMessage)) {
                                                    new DetailDialog(ProductDetailActivity.this, "알림", errMessage, new DetailDialog.CancelRequestDialogListener() {
                                                        @Override
                                                        public void onDismiss() {

                                                        }
                                                    }).show();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        optionLayer.setVisibility(View.VISIBLE);
                    }
                } else {
                    goLogin();
                }
            }
        });
        goBroadcast.setOnClickListener(mClickListener);
        go_top.setOnClickListener(mClickListener);
        buttonBack.setOnClickListener(mClickListener);
        btnGoShop.setOnClickListener(mClickListener);
        productInfoLayer.setOnClickListener(mClickListener);
        productInfoHiddenLayer.setOnClickListener(mClickListener);
        productReviewLayer.setOnClickListener(mClickListener);
        productReviewHiddenLayer.setOnClickListener(mClickListener);
        qaLayer.setOnClickListener(mClickListener);
        qaHiddenLayer.setOnClickListener(mClickListener);
        salerInfoLayer.setOnClickListener(mClickListener);
        salerInfoHiddenLayer.setOnClickListener(mClickListener);
        buttonZzim.setOnClickListener(mClickListener);
        buttonCasting.setOnClickListener(mClickListener);
        close_layer.setOnClickListener(mClickListener);

        optionLayer.setVisibility(View.GONE);

        viewForProductInfo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if ( viewForProductInfo.getHeight() > 0 ) {
                    productLayerHeight = viewForProductInfo.getHeight();
                    viewForProductInfo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        if ( isFromStore ) {
            btnGoShop.setVisibility(View.GONE);
        } else {
            btnGoShop.setVisibility(View.VISIBLE);
        }

        // 스크롤 이동 시 키보드 내리게 하기
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int downY = 0;
                int yPos = 0;
                if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
                    downY = scrollView.getScrollY();
                } else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
                    yPos = scrollView.getScrollY();
                    if ( Math.abs(downY - yPos) > 200 ) {
                        if ( productQnAFragment != null )
                            productQnAFragment.hideKeyboard();
                    }
                }
                return false;
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                // 해당 뷰가 화면에 보여지고 있는지 없는지 에 따른 동작부
                if (isViewVisible(viewForProductInfo)) {
                    if (viewForHiddenTab.getVisibility() == View.VISIBLE) {
                        viewForHiddenTab.setVisibility(View.GONE);
                    }
                } else {
                    if (viewForHiddenTab.getVisibility() == View.GONE) {
                        viewForHiddenTab.setVisibility(View.VISIBLE);
                    }
                }

                if (isViewVisible(top_base)) {
                    go_top.setVisibility(View.GONE);
                } else {
                    go_top.setVisibility(View.VISIBLE);
                    if ( !isHandlerRunning ) {
                        isHandlerRunning = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                go_top.setVisibility(View.GONE);
                                isHandlerRunning = false;
                            }
                        }, 2000);
                    }
                }
            }
        });

        if (isFromLive) {
            mBroadcastBtnLayer.setVisibility(View.VISIBLE);
            mTwoBtnLayer.setVisibility(View.GONE);
        } else {
            mBroadcastBtnLayer.setVisibility(View.GONE);
            mTwoBtnLayer.setVisibility(View.VISIBLE);
            if ( !TextUtils.isEmpty(vodType) ) {
                buttonCasting.setVisibility(View.GONE);
                mTwoBtnLayer.setWeightSum(3.6f);
                LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) buttonCart.getLayoutParams();
                lParams.weight = 1.5f;
                buttonCart.setLayoutParams(lParams);
                lParams = (LinearLayout.LayoutParams) buttonDone.getLayoutParams();
                lParams.weight = 2.1f;
                buttonDone.setLayoutParams(lParams);
            }
        }
        setTabUI(TAB_PRODUCT_INFO);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 100);
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

    /**
     * 특정 view로 스크롤뷰 이동
     *
     * @param view
     * @param scrollView
     * @param count
     */
    private void scrollToView(View view, final ScrollView scrollView, int count) {
        Logger.d("scrollToView");
        if (view != null && view != scrollView) {
            count += view.getTop();
            scrollToView((View) view.getParent(), scrollView, count);
        } else if (scrollView != null) {
            final int finalCount = count;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, finalCount);
                }
            }, 1);
        }
    }

    public void setTabUI(int position) {
        if (position == TAB_PRODUCT_INFO) {
            productLine.setVisibility(View.VISIBLE);
            productHiddenLine.setVisibility(View.VISIBLE);
            reviewLine.setVisibility(View.GONE);
            reviewHiddenLine.setVisibility(View.GONE);
            qaLine.setVisibility(View.GONE);
            qaHiddenLine.setVisibility(View.GONE);
            salerInfoLine.setVisibility(View.GONE);
            salerInfoHiddenLine.setVisibility(View.GONE);

            productRealTab.setTextColor(Color.parseColor("#9f56f2"));
            productHiddneTab.setTextColor(Color.parseColor("#9f56f2"));

            reviewRealTab.setTextColor(Color.parseColor("#202c37"));
            reviewHiddenTab.setTextColor(Color.parseColor("#202c37"));

            qnaRealTab.setTextColor(Color.parseColor("#202c37"));
            qnaHiddenTab.setTextColor(Color.parseColor("#202c37"));

            salerRealTab.setTextColor(Color.parseColor("#202c37"));
            salerHiddenTab.setTextColor(Color.parseColor("#202c37"));

        } else if (position == TAB_PRODUCT_REVIEW) {
            productLine.setVisibility(View.GONE);
            productHiddenLine.setVisibility(View.GONE);
            reviewLine.setVisibility(View.VISIBLE);
            qaLine.setVisibility(View.GONE);
            reviewHiddenLine.setVisibility(View.VISIBLE);
            qaHiddenLine.setVisibility(View.GONE);
            salerInfoLine.setVisibility(View.GONE);
            salerInfoHiddenLine.setVisibility(View.GONE);

            productRealTab.setTextColor(Color.parseColor("#202c37"));
            productHiddneTab.setTextColor(Color.parseColor("#202c37"));

            reviewRealTab.setTextColor(Color.parseColor("#9f56f2"));
            reviewHiddenTab.setTextColor(Color.parseColor("#9f56f2"));

            qnaRealTab.setTextColor(Color.parseColor("#202c37"));
            qnaHiddenTab.setTextColor(Color.parseColor("#202c37"));

            salerRealTab.setTextColor(Color.parseColor("#202c37"));
            salerHiddenTab.setTextColor(Color.parseColor("#202c37"));
        } else if (position == TAB_QNA) {
            productLine.setVisibility(View.GONE);
            productHiddenLine.setVisibility(View.GONE);
            reviewLine.setVisibility(View.GONE);
            reviewHiddenLine.setVisibility(View.GONE);
            qaLine.setVisibility(View.VISIBLE);
            qaHiddenLine.setVisibility(View.VISIBLE);
            salerInfoLine.setVisibility(View.GONE);
            salerInfoHiddenLine.setVisibility(View.GONE);

            productRealTab.setTextColor(Color.parseColor("#202c37"));
            productHiddneTab.setTextColor(Color.parseColor("#202c37"));

            reviewRealTab.setTextColor(Color.parseColor("#202c37"));
            reviewHiddenTab.setTextColor(Color.parseColor("#202c37"));

            qnaRealTab.setTextColor(Color.parseColor("#9f56f2"));
            qnaHiddenTab.setTextColor(Color.parseColor("#9f56f2"));

            salerRealTab.setTextColor(Color.parseColor("#202c37"));
            salerHiddenTab.setTextColor(Color.parseColor("#202c37"));
        } else {
            productLine.setVisibility(View.GONE);
            productHiddenLine.setVisibility(View.GONE);
            reviewLine.setVisibility(View.GONE);
            reviewHiddenLine.setVisibility(View.GONE);
            qaLine.setVisibility(View.GONE);
            qaHiddenLine.setVisibility(View.GONE);
            salerInfoLine.setVisibility(View.VISIBLE);
            salerInfoHiddenLine.setVisibility(View.VISIBLE);

            productRealTab.setTextColor(Color.parseColor("#202c37"));
            productHiddneTab.setTextColor(Color.parseColor("#202c37"));

            reviewRealTab.setTextColor(Color.parseColor("#202c37"));
            reviewHiddenTab.setTextColor(Color.parseColor("#202c37"));

            qnaRealTab.setTextColor(Color.parseColor("#202c37"));
            qnaHiddenTab.setTextColor(Color.parseColor("#202c37"));

            salerRealTab.setTextColor(Color.parseColor("#9f56f2"));
            salerHiddenTab.setTextColor(Color.parseColor("#9f56f2"));
        }
    }


    private void setData(ShopTreeModel model, ArrayList<DetailOptionList> mainOptionList, ArrayList<DetailOptionItem> fullOptionList) {
        Logger.e("setData");
        if (model == null) {
//            Toast.makeText(ProductDetailActivity.this, getResources().getString(R.string.msg_incorrect_product_info), Toast.LENGTH_SHORT).show();
            new AppToast(ProductDetailActivity.this).showToastMessage(getResources().getString(R.string.msg_incorrect_product_info),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM);
            return;
        }

        String shopName = model.getStoreModel().getName();
        String shopLogo = model.getStoreModel().getShopTreeImageModel().getUrl();
        String productName = model.getName();
        String feeType = model.getShipCondition().getFeeType();
        double d_origin_price = model.getOriginPrice();
        double d_price = model.getPrice();
        double shipFee = model.getShipCondition().getShipFee();
        double feeRange = model.getShipCondition().getFeeRange();
        double d_save_point = model.getSavePoint();
        double d_save_rate = model.getSaveRate();
        Logger.e("d_save_point :: " + d_save_point);
        String s_save_point = Utils.ToNumFormat((int) d_save_point) + "원";

        String s_save_rate = "(" + Utils.ToNumFormat((int) d_save_rate) + "% 적립)";

        String originPrice = Utils.ToNumFormat((int) d_origin_price) + "원";
        String price = Utils.ToNumFormat((int) d_price) + "원";

        String sale = "";
        if ( d_origin_price != d_price ) {
            try {
                if ( d_origin_price <= 0 ) {
                    sale = "";
                } else {
                    double div = d_origin_price - d_price;
                    Logger.e("div :: " + div);
                    double s_rt = div / d_origin_price;
                    Logger.e("s_rt :: " + s_rt);
                    int sale_percentage = (int)(s_rt * 100);
                    Logger.e("sale_percentage :: " + sale_percentage);
                    if ( sale_percentage <= 0 )
                        sale = "";
                    else
                        sale = sale_percentage + "%";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(sale) ) {
            percentage.setVisibility(View.VISIBLE);
            percentage.setText(sale);
        } else {
            percentage.setVisibility(View.GONE);
        }

        if ("무료배송".equals(feeType)) {
            textViewDeliveryFee.setText("무료배송");
            textViewDeliveryGuide.setVisibility(View.INVISIBLE);
        } else {
            String s_fee = Utils.ToNumFormat((int) shipFee) + "원";
            textViewDeliveryFee.setText(s_fee);
            if (feeRange > 0) {
                textViewDeliveryGuide.setVisibility(View.VISIBLE);
                String s_range = Utils.ToNumFormat((int) feeRange);
                String str_feerange = "(" + s_range + "원 이상 구매시 무료 배송)";
                textViewDeliveryGuide.setText(str_feerange);
            } else {
                textViewDeliveryGuide.setVisibility(View.INVISIBLE);
            }

        }
        textViewShopName.setText(shopName);
        textViewTitle.setText(productName);

        textViewPrice.setText(price);
        if ( originPrice.equals(price) ) {
            textViewDiscountPrice.setVisibility(View.GONE);
        } else {
            textViewDiscountPrice.setVisibility(View.VISIBLE);
            textViewDiscountPrice.setText(originPrice);
            textViewDiscountPrice.setPaintFlags(textViewPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        textViewPoint.setText(s_save_point);
        textViewPointGuide.setText(s_save_rate);
        Logger.e("shopLogo :: " + shopLogo);
        ImageLoad.setImage(ProductDetailActivity.this, imageViewLogo, shopLogo, R.drawable.ic_go_shop_pink, ImageLoad.SCALE_NONE, null);

        Glide.with(ProductDetailActivity.this)
                .asBitmap()
                .load(mModel.getMainImage().getUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Logger.d("image onResourceReady :: ");
                        main_image.setImageBitmap(resource);
                        main_image.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                });

        if ( mainOptionList != null && mainOptionList.size() > 0 && fullOptionList != null && fullOptionList.size() > 0 ) {
            // 옵션 있으므로 adapter 형태로
            Logger.e("메인 옵션 있음");
        } else {
            // 옵션 없으므로 일반 논 옵션 형태로
            Logger.e("메인 옵션 없음");
            boolean bSoldout = false;
            if ( model.getQuantity() > 0 )
                bSoldout = false;
            else
                bSoldout = true;

            fullOptionList = new ArrayList<>();

            DetailOptionItem item = new DetailOptionItem();
            item.setKey("");
            item.setName("");
            item.setPrice(d_price);
            item.setQuantity(model.getQuantity());
            item.setSelectedQuantity(1);
            item.setOptionkey("");
            item.setbSoldout(bSoldout);

            fullOptionList.add(item);
        }

        adapter = new DetailOptionAdapter(ProductDetailActivity.this, productName, new DetailOptionAdapter.Listener() {
            @Override
            public void setTotalValue(String tq, String tp) {
                totalQuantity.setText(tq);
                totalPrice.setText(tp);
            }
        });
        Logger.e("*********************************///////////////*****************************");

        layoutManager = new WrappedLinearLayoutManager(ProductDetailActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        optionRecyclerView.setHasFixedSize(false);
        optionRecyclerView.setLayoutManager(layoutManager);
        optionRecyclerView.setAdapter(adapter);
        adapter.setOptionLists(mainOptionList, fullOptionList);

        reviewFragment = new ReviewFragment();
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mBundle = new Bundle();
        mBundle.putString("idx", idx);
        reviewFragment.setArguments(mBundle);
        mFragmentTransaction.replace(R.id.review_layoutForSubFragment, reviewFragment);
        mFragmentTransaction.commit();

        productQnAFragment = new ProductQnAFragment();
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mBundle = new Bundle();
        mBundle.putString("idx", idx);
        productQnAFragment.setArguments(mBundle);
        mFragmentTransaction.replace(R.id.qa_layoutForSubFragment, productQnAFragment);
        mFragmentTransaction.commit();

        salerInfoFragment = new SalerInfoFragment();
        mBundle = new Bundle();
        mBundle.putSerializable("CELLER_INFO", mCellerInfo);
        salerInfoFragment.setArguments(mBundle);
        mFragmentTransaction.replace(R.id.celler_layoutForSubFragment, salerInfoFragment);
        mFragmentTransaction.commit();

    }

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getDetail(idx, new ShopTreeAsyncTask.OnDefaultObjectCallbackListener() {
            @Override
            public void onResponse(boolean rt, Object obj) {
                if (obj != null) {
                    try {
                        JSONObject object = (JSONObject) obj;
                        String result = object.optString(ShopTreeKey.KEY_RESULT).toLowerCase();

                        if ("success".equals(result)) {
                            Logger.e("result success");

                            String title = object.optString("title");
                            String shop_logo = object.optString("shop_logo");
                            String image1Url = object.optString("image1");
                            String pcode = object.optString("pcode");
                            String sc_code = object.optString("sc_code");
                            if ( "null".equals(pcode) || TextUtils.isEmpty(pCode) )
                                pCode = pcode;
                            scCode = sc_code;
                            int image_width = object.optInt("image_width");
                            int image_height = object.optInt("image_height");
                            String shop_name = object.optString("shop_name");
                            String url = object.optString("url");
                            String site_addr = object.optString("site_addr");
                            String site_phone = object.optString("site_phone");
                            String companyName = object.optString("companyName");
                            String ownerName = object.optString("ownerName");
                            String email = object.optString("email");
                            String businessCode = object.optString("businessCode");
                            String salesCode = object.optString("salesCode");
                            double shipfee = object.optDouble("shipfee");
                            double feerange = object.optDouble("feerange");
                            String feetype = object.optString("feetype");
                            String description = object.optString("description");
                            int quantity = object.optInt("quantity");
                            String key = object.optString("key");
                            String bundled = object.optString("bundled");
                            double price = object.optDouble("price");
                            double orgprice = object.optDouble("orgprice");
                            String category = object.optString("category");
                            String regdate = object.optString("regdate");
                            String new_flag = object.optString("new_flag");
                            String best_flag = object.optString("best_flag");
                            double save_point = object.optDouble("save_point");
                            double save_rate = object.optDouble("pointRate");

                            ArrayList<DetailOptionList> mainOptionArray = new ArrayList<>();
                            ArrayList<DetailOptionItem> detailOptionItemArray = new ArrayList<>();

                            JSONArray optionListArray = object.optJSONArray("option_list");
                            if (optionListArray != null && optionListArray.length() > 0) {
                                // 옵션이 있는 경우
                                Logger.e("옵션 있음");
                                for (int i = 0; i < optionListArray.length(); i++) {
                                    JSONObject optionObject = optionListArray.getJSONObject(i);
                                    DetailOptionList optionList = new DetailOptionList();
                                    optionList.setKey(optionObject.optString("key"));
                                    optionList.setName(optionObject.optString("name"));
                                    JSONArray subOption = optionObject.optJSONArray("subOption");

                                    if ( subOption != null && subOption.length() > 0 ) {
                                        Logger.e("subOption 있음");
                                        ArrayList<DetailSubOption> subOptionArray = new ArrayList<>();
                                        for ( int j = 0 ; j < subOption.length() ; j ++ ) {
                                            DetailSubOption detailSubOption = new DetailSubOption();
                                            JSONObject subOptionObject = subOption.optJSONObject(j);
                                            detailSubOption.setOptionkey(subOptionObject.optString("optionkey"));
                                            detailSubOption.setOptionname(subOptionObject.optString("optionname"));
                                            subOptionArray.add(detailSubOption);
                                        }
                                        optionList.setSubOption(subOptionArray);
                                    } else {
                                        Logger.e("subOption 없음");
                                    }
                                    mainOptionArray.add(optionList);
                                }
                                // 메인 옵션 설정 끝
                            } else {
                                Logger.e("option list null");
                            }

                            JSONArray optionListItemArray = object.optJSONArray("option_item_list");
                            if (optionListItemArray != null && optionListItemArray.length() > 0) {
                                // option list item 있음
                                for (int i = 0; i < optionListItemArray.length(); i++) {
                                    DetailOptionItem detailOptionItem = new DetailOptionItem();
                                    JSONObject optionItemObject = optionListItemArray.optJSONObject(i);

                                    double tPrice = optionItemObject.optDouble("price") + price;

                                    detailOptionItem.setKey(optionItemObject.optString("key"));
                                    detailOptionItem.setName(optionItemObject.optString("name"));
                                    detailOptionItem.setPrice(tPrice);
                                    detailOptionItem.setQuantity(optionItemObject.optInt("quantity"));
                                    detailOptionItem.setOptionkey(optionItemObject.optString("optionkey"));
                                    detailOptionItem.setbSoldout(optionItemObject.optBoolean("bSoldout"));

                                    detailOptionItemArray.add(detailOptionItem);
                                }
                            } else {
                                // option list item 없음
                            }



                            textViewTitle.setText(title);

                            if(object.optString("is_wish").equals("Y")) {
                                imageViewZzim.setSelected(true);
                                imageViewZzim.setBackgroundResource(R.drawable.like_on_ico);
                            } else {
                                imageViewZzim.setSelected(false);
                                imageViewZzim.setBackgroundResource(R.drawable.like_off_ico);
                            }

                            ShopTreeStoreModel store_model = new ShopTreeStoreModel();

                            ShopTreeImageModel image = new ShopTreeImageModel();
                            image.setUrl(shop_logo);

                            ShopTreeImageModel image1 = new ShopTreeImageModel();
                            image1.setUrl(image1Url);
                            image1.setWidth(image_width);
                            image1.setHeight(image_height);

                            store_model.setName(shop_name);
                            store_model.setShopTreeImageModel(image);
                            store_model.setSiteUrl(url);
                            store_model.setSiteAddr(site_addr);
                            store_model.setSitePhone(site_phone);
                            store_model.setCompanyName(companyName);
                            store_model.setOwnerName(ownerName);
                            store_model.setEMail(email);
                            store_model.setBusinessCode(businessCode);
                            store_model.setSalesCode(salesCode);

                            ShipCondition sc = new ShipCondition();
                            sc.setShipFee(shipfee);
                            sc.setFeeType(feetype);
                            sc.setFeeRange(feerange);

                            String d_info = object.optString("deliveryInfo");
                            String w_info = object.optString("warrantyInfo");
                            String e_info = object.optString("exchangeInfo");
                            String r_info = object.optString("refundInfo");

                            if (TextUtils.isEmpty(d_info)) {
                                d_info = getResources().getString(R.string.msg_reference_detail);
                            } else if ("null".equals(d_info)) {
                                d_info = getResources().getString(R.string.msg_reference_detail);
                            }

                            if (TextUtils.isEmpty(w_info)) {
                                w_info = getResources().getString(R.string.msg_reference_detail);
                            } else if ("null".equals(w_info)) {
                                w_info = getResources().getString(R.string.msg_reference_detail);
                            }

                            if (TextUtils.isEmpty(e_info)) {
                                e_info = getResources().getString(R.string.msg_reference_detail);
                            } else if ("null".equals(e_info)) {
                                e_info = getResources().getString(R.string.msg_reference_detail);
                            }

                            if (TextUtils.isEmpty(r_info)) {
                                r_info = getResources().getString(R.string.msg_reference_detail);
                            } else if ("null".equals(r_info)) {
                                r_info = getResources().getString(R.string.msg_reference_detail);
                            }

                            SellerInfo c_info = new SellerInfo();
                            c_info.setAddress(site_addr);
                            c_info.setBusinessCode(businessCode);
                            c_info.setDeliveryInfo(d_info);
                            c_info.setEMail(email);
                            c_info.setExchangeInfo(e_info);
                            c_info.setIcon(image.getUrl());
                            c_info.setName(companyName);
                            c_info.setPhone(site_phone);
                            c_info.setRefundInfo(r_info);
                            c_info.setSalesCode(salesCode);
                            c_info.setWarrantyInfo(w_info);

                            ShopTreeModel model = new ShopTreeModel();
                            model.setDescription(description);
                            model.setQuantity(quantity);
                            model.setKey(key);
                            model.setName(title);
                            model.setStoreModel(store_model);
                            model.setBundled(bundled);
                            model.setPrice(price);
                            model.setOriginPrice(orgprice);
                            model.setCategory(category);
                            model.setShipCondition(sc);
                            model.setRegDate(regdate);
                            model.setNew(new_flag);
                            model.setBest(best_flag);
                            model.setDeliveryInfo(d_info);
                            model.setWarrantyInfo(w_info);
                            model.setRefundInfo(r_info);
                            model.setExchangeInfo(e_info);
                            model.setSavePoint(save_point);
                            model.setSaveRate(save_rate);
                            model.setMainImage(image1);

                            mDescription = description;
                            mCellerInfo = c_info;
                            String loadUrl = SHOP_TREE_PRODUCT_URL + pCode;
                            Logger.e("loadUrl :: " + loadUrl);
                            Logger.e("dummy visibility :: " + dummy.getVisibility());
                            Logger.e("review_layoutForSubFragment visibility :: " + review_layoutForSubFragment.getVisibility());
                            Logger.e("qa_layoutForSubFragment visibility :: " + qa_layoutForSubFragment.getVisibility());
                            Logger.e("celler_layoutForSubFragment visibility :: " + celler_layoutForSubFragment.getVisibility());
                            dummy.getSettings().setLoadWithOverviewMode(true);
                            dummy.getSettings().setUseWideViewPort(true);
                            dummy.getSettings().setJavaScriptEnabled(true);
                            dummy.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                            dummy.getSettings().setTextSize(WebSettings.TextSize.LARGEST);
                            Logger.e("pCode :: " + pCode);
                            dummy.loadUrl(loadUrl);


                            dummy.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    Logger.e("onPageFinished :: url :: " + url + " width :: " + dummy.getWidth() + "dummy height :: " + dummy.getHeight());

                                }

                                @Override
                                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                                    Logger.e("onReceivedError :: error :: " + error.toString());
                                }

                                @Override
                                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                                    Logger.e("onReceivedHttpError :: error :: " + errorResponse.toString());
                                }

                                @Override
                                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                                    Logger.e("onReceivedSslError :: error :: " + error.toString());
                                }
                            });

                            mModel = model;
                            Logger.e("mainOption size :: " + mainOptionArray.size());
                            setData(mModel, mainOptionArray, detailOptionItemArray);
                            Logger.d("image :: " + mModel.getMainImage().getUrl());
                        } else {
                            Logger.e("result not success");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    Logger.e("response null");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("onActivityResult ShopTreeProductActivity");
        Logger.d("requestCode :: " + requestCode);
        Logger.d("resultCode :: " + resultCode);
        if(requestCode == ACTIVITY_REQUEST_CODE_LIVE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(ProductDetailActivity.this, BroadcastSettingActivity.class);
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.Companion.getUserId(ProductDetailActivity.this));
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "");
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_LIVE);
            intent.putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.Companion.getUserId(ProductDetailActivity.this));

            startActivity(intent);
        } else if ( requestCode == REQUEST_STORE_TO_DETAIL && resultCode == Activity.RESULT_OK ) {
            if (isLoginUser) {
                Intent intent = new Intent();
                intent.putExtra("from_store", data.getBooleanExtra("from_store", false));
                intent.putExtra("from_live", data.getBooleanExtra("from_live", false));
                intent.putExtra("idx", data.getStringExtra("idx"));
                intent.putExtra("name", data.getStringExtra("name"));
                intent.putExtra("image", data.getStringExtra("image"));
                intent.putExtra("price", data.getStringExtra("price"));
                intent.putExtra("storeName", data.getStringExtra("storeName"));
                intent.putExtra(ShopTreeKey.KEY_PCODE, data.getStringExtra(ShopTreeKey.KEY_PCODE));
                intent.putExtra(ShopTreeKey.KEY_SCCODE, data.getStringExtra(ShopTreeKey.KEY_SCCODE));
                intent.putExtra("LIKE_STATUS", data.getBooleanExtra("LIKE_STATUS", false));
                setResult(RESULT_OK, intent);
                finish();
            } else {
                goLogin();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void goLogin() {
        startActivity(new Intent(ProductDetailActivity.this, LoginActivity.class));
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}