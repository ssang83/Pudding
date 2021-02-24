package com.enliple.pudding.widget;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.PurchaseActivity;
import com.enliple.pudding.adapter.DetailOptionAdapter;
import com.enliple.pudding.adapter.ProductTabPagerAdapter;
import com.enliple.pudding.bus.VideoPipBus;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.chat.ChatManager;
import com.enliple.pudding.commons.data.DialogModel;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.ui_compat.PixelUtil;
import com.enliple.pudding.commons.widget.NonSwipeableViewPager;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.commons.widget.tab_layout.WrappedTabLayoutStripUtil;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.fragment.main.ShoppingLiveFragment;
import com.enliple.pudding.model.DetailOptionItem;
import com.enliple.pudding.model.DetailOptionList;
import com.enliple.pudding.model.DetailSubOption;
import com.enliple.pudding.model.ProductDialogTab;
import com.enliple.pudding.model.SellerInfo;
import com.enliple.pudding.model.ShipCondition;
import com.enliple.pudding.model.ShopTreeImageModel;
import com.enliple.pudding.model.ShopTreeModel;
import com.enliple.pudding.model.ShopTreeStoreModel;
import com.enliple.pudding.widget.shoptree.DetailDialog;
import com.google.android.material.tabs.TabLayout;
import com.igaworks.v2.abxExtensionApi.AbxCommerce;
import com.igaworks.v2.core.AdBrixRm;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlayerNewProductDialog extends DialogFragment {

    public static String TAG = "PlayerNewProductDialog";

    private RelativeLayout mainLayer;

    private ProductTabPagerAdapter pagerAdapter;
    private NonSwipeableViewPager pager;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private Context context;
    private ArrayList<DialogModel> items;
    private VideoPipBus bus;
    private boolean isCast;
    private RelativeLayout optionLayer, buttonCart, buttonDone;
    private LinearLayout tabLayer;
    private RecyclerView optionRecyclerView;
    private AppCompatImageView thumbnail;
    private AppCompatTextView shopName, textViewSubject, originalPrice, textViewPrice, totalPrice, totalQuantity;

    private WrappedLinearLayoutManager layoutManager;
    private DetailOptionAdapter adapter;

    private String idx = "";
    private String pCode = "";
    private String scCode = "";
    private String streamKey = "";
    private String vodType = "";
    private String recommendId = "";
    private ShopTreeModel mModel;
    private boolean isLoginUser = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullScreenDialogStyle);
        registerReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_player_new_products, container, false);

        mainLayer = view.findViewById(R.id.mainLayer);
        pager = view.findViewById(R.id.pager);
        toolbar = view.findViewById(R.id.toolbar);
        tabLayout = view.findViewById(R.id.tabLayout);
        optionLayer = view.findViewById(R.id.optionLayer);
        tabLayer = view.findViewById(R.id.tabLayer);
        optionRecyclerView = view.findViewById(R.id.optionRecyclerView);
        thumbnail = view.findViewById(R.id.thumbnail);
        shopName = view.findViewById(R.id.shopName);
        textViewSubject = view.findViewById(R.id.textViewSubject);
        originalPrice = view.findViewById(R.id.originalPrice);
        textViewPrice = view.findViewById(R.id.textViewPrice);
        totalPrice = view.findViewById(R.id.totalPrice);
        totalQuantity = view.findViewById(R.id.totalQuantity);
        buttonCart = view.findViewById(R.id.buttonCart);
        buttonDone = view.findViewById(R.id.buttonDone);

        mainLayer.setOnClickListener(clickListener);
        buttonCart.setOnClickListener(clickListener);
        buttonDone.setOnClickListener(clickListener);

        pager.setSwipeEnable(false);

        Bundle bundle = getArguments();
        items = bundle.getParcelableArrayList("list");
        bus = (VideoPipBus) bundle.getSerializable("bus");
        isCast = bundle.getBoolean("isCast");
        isLoginUser = AppPreferences.Companion.getLoginStatus(getActivity());

        Logger.e("items size :: " + items.size());
        initTab();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e("onDestroyCalled");
        unregisterReceiver();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch ( v.getId() ) {
                case R.id.mainLayer:
                    dismiss();
                    break;
                case R.id.buttonCart:
                    if (isLoginUser) {
                        String sc = scCode;
                        String pcode = pCode;
                        String optionStr = "";

                        ArrayList<DetailOptionItem> selectedOptionArray = adapter.getSelectedList();

                        if (selectedOptionArray != null && selectedOptionArray.size() > 0) {
                            JSONObject obj = new JSONObject();

                            try {
                                obj.put("stream_key", streamKey);
                                obj.put("vod_type", vodType);
                                obj.put("recommend_mb_id", recommendId);
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
                                ShopTreeAsyncTask task = new ShopTreeAsyncTask(getActivity());
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
                                                        EventBus.getDefault().post(new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(getActivity())));
                                                    }
                                                }, 100);
                                                Logger.e("before msg :: " + msg );
                                                if ( TextUtils.isEmpty(msg) )
                                                    msg = getResources().getString(R.string.inserted_cart);
                                                Logger.e("after msg :: " + msg );
                                                new AppToast(getActivity()).showToastMessage(msg,
                                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                                        AppToast.GRAVITY_BOTTOM);

                                                Intent intent = new Intent(ShoppingLiveFragment.SEND_CHAT);
                                                intent.putExtra("GUBUN", ChatManager.GUBUN_CART);
                                                intent.putExtra("NAME", mModel.getName());
                                                getActivity().sendBroadcast(intent);

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
                                                    new DetailDialog(getActivity(), "알림", errMessage, new DetailDialog.CancelRequestDialogListener() {
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
                    }
                    break;
                case R.id.buttonDone:
                    if ( isLoginUser ) {
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
                                    sObject.put("recommend_mb_id", recommendId);
                                    iArray.put(sObject);
                                }
                                obj.put("products", iArray);
                                jArray.put(obj);
                                m_obj.put("group", jArray);

                                optionStr = m_obj.toString();

                                if (TextUtils.isEmpty(optionStr))
                                    return;
                                Intent intent = new Intent(getActivity(), PurchaseActivity.class);
                                intent.putExtra("PRODUCT_BODY", optionStr);
                                intent.putExtra("FROM_CART", false);
                                startActivity(intent);
                            } else {
                                new AppToast(getActivity()).showToastMessage(getResources().getString(R.string.msg_select_option),
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    break;
            }
        }
    };

    private void initTab() {
        if ( pagerAdapter == null ) {
            tabLayout.addTab(tabLayout.newTab().setText("판매상품"));
            tabLayout.addTab(tabLayout.newTab().setText("장바구니"));
        }

        pagerAdapter = new ProductTabPagerAdapter(pager, getChildFragmentManager(), items, bus, isCast);
        pager.setAdapter(pagerAdapter);
//        pagerAdapter.setProductData(items, bus, isCast);
        tabLayout.addOnTabSelectedListener(tabSelectedListener);

        // TabStrip Size 축소
        WrappedTabLayoutStripUtil.wrapTabIndicatorToTitle(tabLayout, PixelUtil.dpToPx(getActivity(), 60), PixelUtil.dpToPx(getActivity(), 60));

        tabLayout.getTabAt(0).select();

//        Fragment fragment = (Fragment) pagerAdapter.instantiateItem(pager, ProductDialogTab.PRODUCT_LIST.ordinal());

        setupTabLayoutFonts();
    }

    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Logger.e("tabSelected");
            setupTabLayoutFonts();
            if ( tab != null )
                pager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            Logger.e("onTabUnselected");
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            Logger.e("onTabReselected");
        }
    };

    public void closeSubPop() {
        if ( optionLayer.getVisibility() == View.VISIBLE ) {
            tabLayer.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    optionLayer.setVisibility(View.GONE);
                    Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
                    optionLayer.startAnimation(slideDown);
                }
            }, 100);
        } else {
            dismiss();
        }
    }

//    public void closeSubPop() {
//        if ( pagerAdapter != null )
//            pagerAdapter.closeSubPop(tabLayout.getSelectedTabPosition());
//    }

    private void setupTabLayoutFonts() {
        int tabPosition = tabLayout.getSelectedTabPosition();
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabCnt = vg.getChildCount();
        for (int i = 0 ; i < tabCnt ; i ++ ) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(i);
            int tabChildCnt = vgTab.getChildCount();
            for (int j = 0 ; j < tabChildCnt ; j ++ ) {
                View tabViewChild = vgTab.getChildAt(j);
                if (tabViewChild instanceof AppCompatTextView) {
                    if (i == tabPosition) {
                        ((AppCompatTextView)tabViewChild).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/notosanskr_medium.otf"));
                        ((AppCompatTextView)tabViewChild).setTextSize(14);
                    } else {
                        ((AppCompatTextView)tabViewChild).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/notosanskr_regular.otf"));
                        ((AppCompatTextView)tabViewChild).setTextSize(14);
                    }
                }
            }
        }
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter("DIALOG_CLICKED"));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent it) {
            String action = it.getAction();
            if ( "DIALOG_CLICKED".equals(action) ) {
                if ( optionLayer.getVisibility() != View.VISIBLE) {
                    idx = it.getStringExtra("idx");
                    pCode = it.getStringExtra("pCode");
                    scCode = it.getStringExtra("scCode");
                    streamKey = it.getStringExtra("streamKey");
                    vodType = it.getStringExtra("vodType");
                    recommendId = it.getStringExtra("recommendId");
                    Logger.e("idx :: " + idx);
                    Logger.e("pCode :: " + pCode);
                    Logger.e("scCode :: " + scCode);
                    Logger.e("streamKey :: " + streamKey);
                    Logger.e("vodType :: " + vodType);
                    Logger.e("recommendId :: " + recommendId);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadData();
                        }
                    }, 100);


//                    Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
//                    slideUp.setAnimationListener(new Animation.AnimationListener() {
//                        @Override
//                        public void onAnimationStart(Animation animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animation animation) {
//                            Logger.e("animation end");
//                            tabLayer.setVisibility(View.GONE);
//                        }
//
//                        @Override
//                        public void onAnimationRepeat(Animation animation) {
//
//                        }
//                    });
//                    optionLayer.setVisibility(View.VISIBLE);
//                    optionLayer.startAnimation(slideUp);
                }
            }
        }
    };

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(getActivity());
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


                            ShopTreeStoreModel store_model = new ShopTreeStoreModel();
                            ShopTreeImageModel image = new ShopTreeImageModel();
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
    private void setData(ShopTreeModel model, ArrayList<DetailOptionList> mainOptionList, ArrayList<DetailOptionItem> fullOptionList) {
        Logger.e("setData");
        if (model == null) {
//            Toast.makeText(ProductDetailActivity.this, getResources().getString(R.string.msg_incorrect_product_info), Toast.LENGTH_SHORT).show();
            new AppToast(getActivity()).showToastMessage(getResources().getString(R.string.msg_incorrect_product_info),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM);
            return;
        }

        String sName = model.getStoreModel().getName();
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

        String oPrice = Utils.ToNumFormat((int) d_origin_price) + "원";
        String nPrice = Utils.ToNumFormat((int) d_price) + "원";

        originalPrice.setText(oPrice);
        textViewPrice.setText(nPrice);
        shopName.setText(sName);
        textViewSubject.setText(productName);

        Glide.with(getActivity())
                .asBitmap()
                .load(mModel.getMainImage().getUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Logger.d("image onResourceReady :: ");
                        thumbnail.setImageBitmap(resource);
                        thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
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

        adapter = new DetailOptionAdapter(getActivity(), productName, new DetailOptionAdapter.Listener() {
            @Override
            public void setTotalValue(String tq, String tp) {
                totalQuantity.setText(tq);
                totalPrice.setText(tp);
            }
        });
        Logger.e("*********************************///////////////*****************************");

        layoutManager = new WrappedLinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        optionRecyclerView.setHasFixedSize(false);
        optionRecyclerView.setLayoutManager(layoutManager);
        optionRecyclerView.setAdapter(adapter);
        adapter.setOptionLists(mainOptionList, fullOptionList);

        // 데이터 전부 세팅이 끝난 후 뷰 변경
        Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Logger.e("animation end");
                tabLayer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        optionLayer.setVisibility(View.VISIBLE);
        optionLayer.startAnimation(slideUp);
    }
}
