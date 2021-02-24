package com.enliple.pudding.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.my.ProductCartAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.shoptree.data.CartData;
import com.enliple.pudding.commons.shoptree.data.CartProductData;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.ProductCartResponse;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.AppAlertDialog;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by Kim Joonsung on 2018-09-18.
 */

public class ProductCartActivity extends AbsBaseActivity implements ProductCartAdapter.Listener {

    private static final int RECYCLER_VIEW_ITEM_CACHE_SIZE = 20;

    private RelativeLayout buttonBack;
    private RecyclerView recyclerViewCart;
    private View layoutEmpty;
    private TextView textViewEmpty;
    private TextView textViewCheckCount, textViewAllCount;
    private RelativeLayout buttonChecBoxAll;
    private ProgressBar progressBar;
    private View viewForSelectAll;
    private View divider;
    private View layoutButton;
    private View layoutGuide;

    private ProductCartAdapter mAdapter;
    private List<CartData> cartData;

    private int productCnt;
    private boolean mIsFirst = false;
    private boolean isReload = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        //ButterKnife.bind(this);

        setLayout();
        loadCartList("");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if (mAdapter != null) {
            intent.putExtra("CART_CNT", mAdapter.getProductCount());
        } else {
            intent.putExtra("CART_CNT", 0);
        }

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void cartDelete() {
        EventBus.getDefault().post(new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(this)));
    }

    @Override
    public void updateSelectCount(int selected, int total, int position, String id) {
        Logger.d("@@@@@@@@@@@@@@@@@@ id : " + id);
        textViewCheckCount.setText(String.valueOf(selected));

        if (selected == total) {
            buttonChecBoxAll.setSelected(true);
        } else {
            buttonChecBoxAll.setSelected(false);
        }

        loadCartList(id);
    }

    @Override
    public void setCartEmptyView(boolean visible) {
        setEmptyViewVisibility(visible);
    }

    @Override
    public void checkAll(boolean allCheck) {
        Logger.e("ProductCartActivity checkAll allCheck :: " + allCheck);
        if (allCheck) {
            buttonChecBoxAll.setSelected(true);
        }
    }

    @Override
    public void directBuy(CartProductData item) {
        JSONObject body = mAdapter.getSelectedProduct();
        Logger.e("optionStr :: " + body);
        if (mAdapter.getSelectedProductCount() == 0) {
            new AppToast(this).showToastMessage("상품을 선택해 주세요",
                    AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
            return;
        }

        try {
            JSONArray arr = body.optJSONArray("group");
            if ( arr != null && arr.length() > 0 ) {

            } else {
                new AppToast(this).showToastMessage("품절 상품은 구매할 수 없습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(ProductCartActivity.this, PurchaseActivity.class);
        intent.putExtra("PRODUCT_BODY", body.toString());
        intent.putExtra("FROM_CART", true);
        startActivity(intent);
    }

    @Override
    public void cartUpdate() {
        loadCartList("");
    }

    @Override
    public void buyChoice() {
        if (mAdapter != null) {
            JSONObject body = mAdapter.getSelectedProduct();
            Logger.e("optionStr :: " + body);
            if (mAdapter.getSelectedProductCount() == 0) {
                new AppToast(this).showToastMessage("상품을 선택해 주세요",
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                return;
            }

            try {
                JSONArray arr = body.optJSONArray("group");
                if ( arr != null && arr.length() > 0 ) {

                } else {
                    new AppToast(this).showToastMessage("품절 상품은 구매할 수 없습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            Intent intent = new Intent(ProductCartActivity.this, PurchaseActivity.class);
            intent.putExtra("PRODUCT_BODY", body.toString());
            intent.putExtra("FROM_CART", true);
            startActivity(intent);
        }
    }

    @Override
    public void buyAll() {
        if (mAdapter != null) {
            mAdapter.productSelectAll();
            buttonChecBoxAll.setSelected(true);
            textViewCheckCount.setText(String.valueOf(mAdapter.getProductCount()));
            JSONObject body = mAdapter.getSelectedProduct();
            Logger.e("optionStr :: " + body);
            if (mAdapter.getSelectedProductCount() == 0 ) {
                new AppToast(this).showToastMessage("상품을 선택해 주세요",
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                return;
            }

            try {
                JSONArray arr = body.optJSONArray("group");
                if ( arr != null && arr.length() > 0 ) {

                } else {
                    new AppToast(this).showToastMessage("품절 상품은 구매할 수 없습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(ProductCartActivity.this, PurchaseActivity.class);
            intent.putExtra("PRODUCT_BODY", body.toString());
            intent.putExtra("FROM_CART", true);
            startActivity(intent);
        }
    }

    private void setLayout() {
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(clickListener);
        buttonChecBoxAll = findViewById(R.id.buttonChecBoxAll);
        buttonChecBoxAll.setOnClickListener(clickListener);

        RelativeLayout buttonTrash = findViewById(R.id.buttonTrash);
        buttonTrash.setOnClickListener(clickListener);

        recyclerViewCart = findViewById(R.id.recyclerViewCartList);
        recyclerViewCart.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE);
        recyclerViewCart.setHasFixedSize(false);

        layoutEmpty = findViewById(R.id.layoutEmpty);
        viewForSelectAll = findViewById(R.id.layoutForSelectAll);
        divider = findViewById(R.id.divider);
        layoutButton = findViewById(R.id.layoutButton);
        layoutGuide = findViewById(R.id.layoutGuide);

        textViewEmpty = findViewById(R.id.textViewEmpty);
        textViewCheckCount = findViewById(R.id.textViewCheckCount);
        textViewAllCount = findViewById(R.id.textViewAllCount);

        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * 리스트가 없을 경우 표시되는 안내문구의 표시여부를 설정
     *
     * @param visible
     */
    private void setEmptyViewVisibility(boolean visible) {
        layoutEmpty.setVisibility(visible ? View.VISIBLE : View.GONE);
        recyclerViewCart.setVisibility(visible ? View.GONE : View.VISIBLE);
        viewForSelectAll.setVisibility(visible ? View.GONE : View.VISIBLE);
        divider.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private void setCheckProductCount(List<CartData> cartData) {
        for (int i = 0; i < cartData.size(); i++) {
            int count = cartData.get(i).productData.size();
            if (count > 0) {
                productCnt += count;
            }
        }
        textViewCheckCount.setText(String.valueOf(productCnt));
        textViewAllCount.setText(String.valueOf(productCnt));
    }

    private void loadCartList(String it_id) {
        Logger.e("@@@@@@@@@@@@@ it_id : " + it_id);
        if(TextUtils.isEmpty(it_id)) {
            isReload = true;
        } else {
            isReload = false;
        }

        ShopTreeAsyncTask task = new ShopTreeAsyncTask(this);
        task.getCartList(it_id, (result, obj) -> {
            Logger.d("response :: " + obj.toString());
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                final ProductCartResponse responseObj = mapper.readValue(obj.toString(), ProductCartResponse.class);
                Logger.e("responseObj result :: " + responseObj.getResult());
                if (responseObj != null && responseObj.getResult().equals("SUCCESS")) {
                    if (responseObj.cartList != null && responseObj.cartList.size() > 0) {
                        Logger.e("responseObj cartlist size :: " + responseObj.cartList.size());

                        setEmptyViewVisibility(false);
                        if(!mIsFirst) {
                            mIsFirst = true; // 최초 한번만 설정
                            buttonChecBoxAll.setSelected(true);
                            setCheckProductCount(responseObj.cartList);
                        }

                        cartData = responseObj.cartList;
                        if (mAdapter == null) {
                            mAdapter = new ProductCartAdapter(this, this);
                            recyclerViewCart.setAdapter(mAdapter);
                        }

                        if(isReload) {
                            mAdapter.setItems(cartData, productCnt);
                            mAdapter.setFooterData(responseObj);
                        } else {
                            mAdapter.setFooterData(responseObj);
                        }
                    } else {
                        setEmptyViewVisibility(true);
                    }
                } else {
                    setEmptyViewVisibility(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
            } finally {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private View.OnClickListener clickListener = v -> {
        long viewId = v.getId();
        if (viewId == R.id.buttonBack) {
            onBackPressed();
        } else if (viewId == R.id.buttonChecBoxAll) {
            if (mAdapter != null) {
                Logger.e("ProductCartActivity buttonCheckBoxAll Clicked");
                v.setSelected(!v.isSelected());
                if (v.isSelected()) {
                    Logger.e("ProductCartActivity buttonCheckBoxAll selected");
                    mAdapter.productSelectAll();
                    textViewCheckCount.setText(String.valueOf(mAdapter.getProductCount()));

                    loadCartList("");
                } else {
                    Logger.e("ProductCartActivity buttonCheckBoxAll unselected");
                    loadCartList(mAdapter.productUnSelectAll());
                    textViewCheckCount.setText("0");
                }
            }
        }else if (viewId == R.id.buttonTrash) {
            AppAlertDialog dialog = new AppAlertDialog(this);
            dialog.setTitle("장바구니");
            dialog.setMessage("삭제하시겠습니까?");
            dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setRightButton(getString(R.string.msg_my_follow_confirm), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdapter != null) {
                        if (buttonChecBoxAll.isSelected()) {
                            mAdapter.productDeleteAll();
                            setEmptyViewVisibility(true);
                        } else {
                            mAdapter.selectProductDel();
                        }
                    }
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    };
}