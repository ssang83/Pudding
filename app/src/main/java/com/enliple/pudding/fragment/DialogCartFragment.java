package com.enliple.pudding.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.activity.PurchaseActivity;
import com.enliple.pudding.adapter.my.ProductCartAdapter;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DialogCartFragment extends Fragment implements ProductCartAdapter.Listener {
    private static final int RECYCLER_VIEW_ITEM_CACHE_SIZE = 20;

    private RecyclerView recyclerViewCart;
    private View layoutEmpty;
    private TextView textViewEmpty;
    private TextView textViewCheckCount, textViewAllCount;
    private RelativeLayout buttonChecBoxAll;
    private ProgressBar progressBar;
    private View viewForSelectAll;
    private View divider;
    private ProductCartAdapter mAdapter;
    private List<CartData> cartData;

    private int productCnt;
    private boolean mIsFirst = false;
    private boolean isReload = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_dialog_cart, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayout(view);
        loadCartList("");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void cartDelete() {
        EventBus.getDefault().post(new NetworkBus(NetworkApi.API21.name(), AppPreferences.Companion.getUserId(getActivity())));
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
            new AppToast(getActivity()).showToastMessage("상품을 선택해 주세요",
                    AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
            return;
        }

        try {
            JSONArray arr = body.optJSONArray("group");
            if ( arr != null && arr.length() > 0 ) {

            } else {
                new AppToast(getActivity()).showToastMessage("품절 상품은 구매할 수 없습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getActivity(), PurchaseActivity.class);
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
                new AppToast(getActivity()).showToastMessage("상품을 선택해 주세요",
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                return;
            }

            try {
                JSONArray arr = body.optJSONArray("group");
                if ( arr != null && arr.length() > 0 ) {

                } else {
                    new AppToast(getActivity()).showToastMessage("품절 상품은 구매할 수 없습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            Intent intent = new Intent(getActivity(), PurchaseActivity.class);
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
                new AppToast(getActivity()).showToastMessage("상품을 선택해 주세요",
                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                return;
            }

            try {
                JSONArray arr = body.optJSONArray("group");
                if ( arr != null && arr.length() > 0 ) {

                } else {
                    new AppToast(getActivity()).showToastMessage("품절 상품은 구매할 수 없습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getActivity(), PurchaseActivity.class);
            intent.putExtra("PRODUCT_BODY", body.toString());
            intent.putExtra("FROM_CART", true);
            startActivity(intent);
        }
    }

    private void setLayout(View view) {
        buttonChecBoxAll = view.findViewById(R.id.buttonChecBoxAll);
        buttonChecBoxAll.setOnClickListener(clickListener);

        RelativeLayout buttonTrash = view.findViewById(R.id.buttonTrash);
        buttonTrash.setOnClickListener(clickListener);

        recyclerViewCart = view.findViewById(R.id.recyclerViewCartList);
        recyclerViewCart.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE);
        recyclerViewCart.setHasFixedSize(false);

        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        viewForSelectAll = view.findViewById(R.id.layoutForSelectAll);
        divider = view.findViewById(R.id.divider);

        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        textViewCheckCount = view.findViewById(R.id.textViewCheckCount);
        textViewAllCount = view.findViewById(R.id.textViewAllCount);

        progressBar = view.findViewById(R.id.progressBar);
    }

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

        ShopTreeAsyncTask task = new ShopTreeAsyncTask(getActivity());
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
                            mAdapter = new ProductCartAdapter(getActivity(), this);
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
        if (viewId == R.id.buttonChecBoxAll) {
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
            AppAlertDialog dialog = new AppAlertDialog(getActivity());
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
