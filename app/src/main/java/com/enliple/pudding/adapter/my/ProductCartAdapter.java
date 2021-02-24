package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.ProductDetailActivity;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.app.ShopTreeKey;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.shoptree.data.CartData;
import com.enliple.pudding.commons.shoptree.data.CartProductData;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.ProductCartResponse;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.AppAlertDialog;
import com.enliple.pudding.widget.NothingSelectedSpinnerAdapter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joooonho.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kim Joonsung on 2018-09-18.
 */

public class ProductCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0X2001;
    private static final int VIEW_TYPE_FOOTER = 0X3001;
    private static final int NOT_ALL_SAME = -1;
    private static final int ALL_CHECKED = 0;
    private static final int ALL_UNCHECKED = 1;
    private Context mContext;
    private List<CartData> mItems = new ArrayList<>();
    private ProductCartResponse response;
    private Listener mListener;
    private int totalProductCount;
    private String optionCount = "";

    public ProductCartAdapter() {
        setHasStableIds(true);
    }

    public ProductCartAdapter(Context context, Listener mListener) {
        this();
        this.mListener = mListener;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mItems.size()) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return hasStableIds() ? position + 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product_cart_footer, parent, false);
            return new ProductCartFooterViewHolder(v);
        } else if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product_cart_item, parent, false);
            return new ProductCartViewHolder(v);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProductCartFooterViewHolder) {
            ProductCartFooterViewHolder footerViewHolder = (ProductCartFooterViewHolder) holder;
            footerViewHolder.textViewTotalProductPrice.setText(String.format(mContext.getString(R.string.msg_price_format),
                    PriceFormatter.Companion.getInstance().getFormattedValue(response.totalProductPrice)));

            footerViewHolder.textViewTotalDeliveryPrice.setText(String.format(mContext.getString(R.string.msg_price_format),
                    PriceFormatter.Companion.getInstance().getFormattedValue(response.totalDeliveryPrice)));

            footerViewHolder.textViewTotalOrderPrice.setText(String.format(mContext.getString(R.string.msg_price_format),
                    PriceFormatter.Companion.getInstance().getFormattedValue(response.totalOrderPrice)));

            footerViewHolder.buttonAllBuy.setOnClickListener(v -> {
                mListener.buyAll();
            });
            footerViewHolder.buttonChoiceBuy.setOnClickListener(v -> {
                mListener.buyChoice();
            });

        } else if (holder instanceof ProductCartViewHolder) {
            final CartData item = getItem(position);

            ProductCartViewHolder viewHolder = (ProductCartViewHolder) holder;
            viewHolder.layoutProductContainer.removeAllViews();
            if (item.productData != null) {
                for (int i = 0; i < item.productData.size(); i++) {
                    CartProductData data = item.productData.get(i);
                    CartProductHolder productHolder = new CartProductHolder(viewHolder.itemView.getContext(), viewHolder.layoutProductContainer);

                    RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) productHolder.divider.getLayoutParams();

                    if(position == mItems.size() - 1) {
                        if(i == item.productData.size() - 1) {
                            param.leftMargin = 0;
                            param.rightMargin = 0;
                        } else {
                            param.leftMargin = Utils.ConvertDpToPx(mContext, 44);
                            param.rightMargin = Utils.ConvertDpToPx(mContext, 15);
                        }
                    } else {
                        param.leftMargin = Utils.ConvertDpToPx(mContext, 44);
                        param.rightMargin = Utils.ConvertDpToPx(mContext, 15);
                    }

                    productHolder.divider.setLayoutParams(param);

                    ImageLoad.setImage(
                            mContext,
                            productHolder.imageViewThumbnail,
                            data.image,
                            R.drawable.product_no_img,
                            ImageLoad.SCALE_NONE,
                            DiskCacheStrategy.ALL);

                    productHolder.buttonProduct.setSelected(data.isSelected());
                    productHolder.textViewTitle.setText(data.title);
                    productHolder.textViewOption.setText(data.optionName);

                    if(data.displayprice != null) {
                        productHolder.textViewPrice.setText(String.format(mContext.getString(R.string.msg_price_format),
                                PriceFormatter.Companion.getInstance().getFormattedValue(data.displayprice)));
                    } else {
                        productHolder.textViewPrice.setText(String.format(mContext.getString(R.string.msg_price_format),
                                PriceFormatter.Companion.getInstance().getFormattedValue(0)));
                    }

                    String quantity = data.quantity;
                    int iQuantity = 0;
                    if (!TextUtils.isEmpty(quantity) || quantity != null) {
                        try {
                            iQuantity = Integer.valueOf(quantity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Logger.e("iQuantity :: " + iQuantity);
                    if (iQuantity > 0) {
                        productHolder.strSoldOut.setVisibility(View.GONE);
                        productHolder.textViewPrice.setVisibility(View.VISIBLE);
                        productHolder.layoutOption.setVisibility(View.VISIBLE);
                    } else {
                        productHolder.strSoldOut.setVisibility(View.VISIBLE);
                        productHolder.textViewPrice.setVisibility(View.GONE);
                        productHolder.layoutOption.setVisibility(View.GONE);
                    }

                    productHolder.buttonProduct.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            v.setSelected(!v.isSelected());
                            if (v.isSelected()) {
                                data.setSelected(true);

//                                if(mListener != null) {
//                                    mListener.updateSelectCount(getSelectedCount(mItems), totalProductCount, position);
//                                }
                            } else {
                                data.setSelected(false);

//                                if(mListener != null) {
//                                    mListener.updateSelectCount(getSelectedCount(mItems), totalProductCount, position);
//                                }
                            }

//                            int shopCheckStatus = getShopCheckButtonStatus(mItems.get(position).productData);
//                            Logger.e("shopCheckStatus :: " + shopCheckStatus);
//                            if (shopCheckStatus == ALL_CHECKED) {
//                                mItems.get(position).setSelected(true);
//                            } else if (shopCheckStatus == ALL_UNCHECKED) {
//                                mItems.get(position).setSelected(false);
//                            } else {
//                                mItems.get(position).setSelected(false);
//                            }

                            if (mListener != null) {
                                mListener.updateSelectCount(getSelectedCount(mItems), totalProductCount, position, getUnCheckedId(mItems));
                            }
                        }
                    });

                    productHolder.layoutDel.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            AppAlertDialog dialog = new AppAlertDialog(mContext);
                            dialog.setTitle("장바구니");
                            dialog.setMessage("삭제하시겠습니까?");
                            dialog.setLeftButton(mContext.getString(R.string.msg_my_follow_cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.setRightButton(mContext.getString(R.string.msg_my_follow_confirm), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    selectProductDel(data.ct_id);
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    });

                    if(iQuantity > 0) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, getSpinnerData(iQuantity));
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        productHolder.optionSpinner.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_cart_option, mContext));
                        productHolder.optionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Logger.e("onItemSelected");
                                if(parent.getItemAtPosition(position) == null) {
                                    productHolder.optionType.setText(String.valueOf(data.productCount));
                                } else {
                                    optionCount = parent.getItemAtPosition(position).toString();
                                    Logger.d("################ optionCount : " + optionCount);

                                    productHolder.optionType.setVisibility(View.GONE);
                                    ShopTreeAsyncTask task = new ShopTreeAsyncTask(mContext);
                                    task.getCartUpdate(data.ct_id, optionCount,
                                            (result, obj) -> {
                                                Logger.d("" + obj.toString());
                                                try {
                                                    ObjectMapper mapper = new ObjectMapper();
                                                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                                                    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                                                    final ProductCartResponse responseObj = mapper.readValue(obj.toString(), ProductCartResponse.class);

                                                    if (responseObj != null) {
                                                        if (responseObj.getResult().equals("SUCCESS")) {
                                                            if(mListener != null) {
                                                                mListener.cartUpdate();
                                                            }

                                                            new AppToast(mContext).showToastMessage("수량이 변경되었습니다",
                                                                    AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);

/*                                                            if (responseObj.cartList.size() > 0) {
                                                                setFooterData(responseObj);
                                                                mItems.clear();
                                                                mItems.addAll(responseObj.cartList);
                                                                notifyDataSetChanged();

                                                                new AppToast(mContext).showToastMessage("수량이 변경되었습니다",
                                                                        AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                                                            }*/
                                                        } else {
                                                            new AppToast(mContext).showToastMessage(responseObj.getResultMessage(),
                                                                    AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    Logger.e(e.toString());
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                Logger.e("onNothingSelected");
                            }
                        });
                    }

                    productHolder.rootView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent1 = new Intent(mContext, ProductDetailActivity.class);
                            Logger.e("idx val :: " + data.seq);
                            intent1.putExtra("from_live", false);
                            intent1.putExtra("from_store", false);
                            intent1.putExtra("idx", data.seq);
                            intent1.putExtra("name", data.title);
                            intent1.putExtra("price", Utils.ToNumFormat(data.price) + "원");
                            intent1.putExtra("image", data.image);
                            intent1.putExtra("storeName", item.shopName);
                            intent1.putExtra(ShopTreeKey.KEY_PCODE, data.pCode);
                            intent1.putExtra(ShopTreeKey.KEY_SCCODE, item.scCode);
                            mContext.startActivity(intent1);
                        }
                    });

                    productHolder.rootView.setTag(data);
                    viewHolder.layoutProductContainer.addView(productHolder.rootView);
                }
            }
        }
    }

    public void setItems(List<CartData> items, int count) {
        this.totalProductCount = count;

        mItems.clear();
        mItems.addAll(items);

        notifyDataSetChanged();
    }

    public void setFooterData(ProductCartResponse response) {
        this.response = response;
        notifyItemChanged(mItems.size());
    }

    private int getSelectedCount(List<CartData> cartItems) {
        int count = 0;
        for (int i = 0; i < cartItems.size(); i++) {
            for (int j = 0; j < cartItems.get(i).productData.size(); j++) {
                if (cartItems.get(i).productData.get(j).isSelected()) {
                    count++;
                }
            }
        }

        return count;
    }

    private String getUnCheckedId(List<CartData> cartItems) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cartItems.size(); i++) {
            for (int j = 0; j < cartItems.get(i).productData.size(); j++) {
                if (!cartItems.get(i).productData.get(j).isSelected()) {
                    sb.append(cartItems.get(i).productData.get(j).ct_id);
                    sb.append(",");
                }
            }
        }

        return sb.toString();
    }

    public int getProductCount() {
        if (mItems == null) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < mItems.size(); i++) {
            for (int j = 0; j < mItems.get(i).productData.size(); j++) {
                count++;
            }
        }

        return count;
    }

    public int getSelectedProductCount() {
        if (mItems == null) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < mItems.size(); i++) {
            for (int j = 0; j < mItems.get(i).productData.size(); j++) {
                if (mItems.get(i).productData.get(j).isSelected()) {
                    count++;
                }
            }
        }

        return count;
    }

    public JSONObject getSelectedProduct() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < mItems.size(); i++) {
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                for (int j = 0; j < mItems.get(i).productData.size(); j++) {
                    JSONObject rowObject = new JSONObject();
                    if (mItems.get(i).productData.get(j).isSelected()) {
                        String pCode = mItems.get(i).productData.get(j).pCode;
                        String optionKey = mItems.get(i).productData.get(j).optionKey;
                        String ct_id = mItems.get(i).productData.get(j).ct_id;
                        String stream_key = mItems.get(i).productData.get(j).strStreamKey;
                        String vod_type = mItems.get(i).productData.get(j).strVodType;
                        String recommend_mb_id = mItems.get(i).productData.get(j).strRecommendMbId;
                        String optionQty = mItems.get(i).productData.get(j).quantity;
                        boolean addPossible = true;
                        if ( TextUtils.isEmpty(optionQty) )
                            addPossible = false;
                        else {
                           if ( "0".equals(optionQty) || "null".equals(optionQty) )
                               addPossible = false;
                        }
                        Logger.e("optionQty :: " + optionQty);
                        if (TextUtils.isEmpty(pCode) || "null".equals(pCode)) {
                            pCode = "";
                        }

                        if (TextUtils.isEmpty(optionKey) || "null".equals(optionKey)) {
                            optionKey = "";
                        }
                        rowObject.put("pcode", pCode);
                        rowObject.put("key", optionKey);
                        rowObject.put("ct_id", ct_id);
                        rowObject.put("stream_key", stream_key);
                        rowObject.put("vod_type", vod_type);
                        rowObject.put("recommend_mb_id", recommend_mb_id);
                        rowObject.put("cnt", mItems.get(i).productData.get(j).productCount);
                        if ( addPossible ) // 품절 상품은 구매 목록에 포함시키지 않음
                            array.put(rowObject);
                    }
                }

                if (array.length() > 0) {
                    object.put("sc_code", mItems.get(i).scCode);
                    object.put("products", array);

                    jsonArray.put(object);
                }
            }

            jsonObject.put("group", jsonArray);
        } catch (Exception e) {
        }

        Logger.e("######### productList : " + jsonObject.toString());

        return jsonObject;
    }

    public void productSelectAll() {
        Logger.e("productSelectAll");
        if (mItems == null) {
            return;
        }

        for (int i = 0; i < mItems.size(); i++) {
            for (int j = 0; j < mItems.get(i).productData.size(); j++) {
                mItems.get(i).setSelected(true);
                mItems.get(i).productData.get(j).setSelected(true);
            }
        }

        notifyDataSetChanged();
    }

    public String productUnSelectAll() {
        Logger.e("productUnSelectAll");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mItems.size(); i++) {
            for (int j = 0; j < mItems.get(i).productData.size(); j++) {
                mItems.get(i).setSelected(false);
                mItems.get(i).productData.get(j).setSelected(false);

                sb.append(mItems.get(i).productData.get(j).ct_id);
                sb.append(",");
            }
        }

        notifyDataSetChanged();

        return sb.toString();
    }

    public void productDeleteAll() {
        if (mItems == null) {
            return;
        }

        selectProductDel();

        for (int i = 0; i < mItems.size(); i++) {
            mItems.remove(i);
        }

        notifyDataSetChanged();
    }

    public void selectProductDel(String id) {
        if (mItems == null) {
            return;
        }

        ShopTreeAsyncTask task = new ShopTreeAsyncTask(mContext);
        task.cartProductDelete(id,
                (result, obj) -> {
                    Logger.d("" + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final ProductCartResponse responseObj = mapper.readValue(obj.toString(), ProductCartResponse.class);

                        if (responseObj != null) {
                            if (responseObj.getResult().equals("SUCCESS")) {
                                if (responseObj.cartList == null) {
                                    AppPreferences.Companion.setCartCnt(mContext, "0");
                                    if (mListener != null) {
                                        mListener.setCartEmptyView(true);
                                    }
                                } else {
                                    if (responseObj.cartList.size() > 0) {
                                        mItems.clear();
                                        mItems.addAll(responseObj.cartList);
                                        setFooterData(responseObj);

                                        if (mListener != null) {
                                            mListener.checkAll(true);
                                            mListener.updateSelectCount(getProductCount(), getProductCount(), 0, getUnCheckedId(mItems));
                                        }
                                    } else {
                                        if (mListener != null) {
                                            mListener.setCartEmptyView(true);
                                        }
                                    }
                                }

                                if(mListener != null) {
                                    mListener.cartDelete();
                                }

                                notifyDataSetChanged();
                            } else {
                                new AppToast(mContext).showToastMessage(responseObj.getResultMessage(), AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(e.toString());
                    }
                });
    }

    public void selectProductDel() {
        if (mItems == null) {
            return;
        }

        ArrayList<String> ct_id = new ArrayList<>();

        for (int i = 0; i < mItems.size(); i++) {
            for (int j = 0; j < mItems.get(i).productData.size(); j++) {
                if (mItems.get(i).productData.get(j).isSelected()) {
                    ct_id.add(mItems.get(i).productData.get(j).ct_id);
                }
            }
        }

        String[] ctid = new String[ct_id.size()];
        ctid = ct_id.toArray(ctid);
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(mContext);
        task.cartProductMultiDelete(ctid,
                (result, obj) -> {
                    Logger.d("" + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final ProductCartResponse responseObj = mapper.readValue(obj.toString(), ProductCartResponse.class);

                        if (responseObj != null) {
                            if (responseObj.getResult().equals("SUCCESS")) {
                                if (responseObj.cartList == null) {
                                    AppPreferences.Companion.setCartCnt(mContext, "0");
                                    if (mListener != null) {
                                        mListener.setCartEmptyView(true);
                                    }
                                } else {
                                    if (responseObj.cartList.size() > 0) {

                                        mItems.clear();
                                        mItems.addAll(responseObj.cartList);
                                        setFooterData(responseObj);

                                        if (mListener != null) {
                                            mListener.checkAll(true);
                                            mListener.updateSelectCount(getProductCount(), getProductCount(), 0, getUnCheckedId(mItems));
                                        }
                                    } else {
                                        if (mListener != null) {
                                            mListener.setCartEmptyView(true);
                                        }
                                    }
                                }

                                if(mListener != null) {
                                    mListener.cartDelete();
                                }

                                notifyDataSetChanged();
                            } else {
                                new AppToast(mContext).showToastMessage(responseObj.getResultMessage(), AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(e.toString());
                    }
                });
    }

    public CartData getItem(int itemPosition) {
        return itemPosition < mItems.size() ? mItems.get(itemPosition) : null;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private ArrayList<String> getSpinnerData(int optionCnt) {
        ArrayList<String> spinnerData = new ArrayList<>();

        int count = optionCnt + 1;
        for(int i = 1 ; i < count ; i++) {
            spinnerData.add(String.valueOf(i));
        }

        return spinnerData;
    }

    /**
     * Inner View Holder from ProductCartViewHolder
     */
    static class CartProductHolder {
        public RelativeLayout rootView;
        public RelativeLayout buttonProduct;
        public SelectableRoundedImageView imageViewThumbnail;
        public TextView textViewTitle;
        public TextView textViewPrice, strSoldOut;
        public TextView textViewOption, optionType;
        public View divider;
        public RelativeLayout layoutDel, layoutOption;
        public Spinner optionSpinner;
        public CartProductHolder(Context context, ViewGroup root) {
            rootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_product_cart_item, root, false);
            buttonProduct = rootView.findViewById(R.id.buttonProduct);
            imageViewThumbnail = rootView.findViewById(R.id.imageViewThumbnail);
            textViewTitle = rootView.findViewById(R.id.textViewTitle);
            textViewPrice = rootView.findViewById(R.id.textViewPrice);
            strSoldOut = rootView.findViewById(R.id.strSoldOut);
            textViewOption = rootView.findViewById(R.id.textViewOption);
            divider = rootView.findViewById(R.id.divider);
            layoutDel = rootView.findViewById(R.id.layoutDel);
            optionSpinner = rootView.findViewById(R.id.optionSpinner);
            optionType = rootView.findViewById(R.id.optionType);
            layoutOption = rootView.findViewById(R.id.layoutOption);
        }
    }

    static class ProductCartViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layoutProductContainer;

        public ProductCartViewHolder(View itemView) {
            super(itemView);
            layoutProductContainer = itemView.findViewById(R.id.layoutProductContainer);
        }
    }

    static class ProductCartFooterViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTotalProductPrice;
        public TextView textViewTotalDeliveryPrice;
        public TextView textViewTotalOrderPrice;
        public TextView buttonAllBuy;
        public TextView buttonChoiceBuy;

        public ProductCartFooterViewHolder(View itemView) {
            super(itemView);
            textViewTotalProductPrice = itemView.findViewById(R.id.textViewTotalProductPrice);
            textViewTotalDeliveryPrice = itemView.findViewById(R.id.textViewTotalDeliveryPrice);
            textViewTotalOrderPrice = itemView.findViewById(R.id.textViewTotalOrderPrice);
            buttonAllBuy = itemView.findViewById(R.id.buttonAllBuy);
            buttonChoiceBuy = itemView.findViewById(R.id.buttonChoiceBuy);
        }
    }

    /**
     * Event Callbacks
     */
    public interface Listener {
        void updateSelectCount(int selected, int total, int position, String id);

        void setCartEmptyView(boolean visible);

        void checkAll(boolean allCheck);

        void directBuy(CartProductData item);

        void cartUpdate();

        void buyChoice();
        void buyAll();
        void cartDelete();
    }

    private int getShopCheckButtonStatus(List<CartProductData> cartData) {
        int checkCount = 0;
        int unCheckCount = 0;
        for (int i = 0; i < cartData.size(); i++) {
            boolean isSelected = cartData.get(i).isSelected();
            if (isSelected) {
                checkCount++;
            } else {
                unCheckCount++;
            }
        }

        if (cartData.size() == checkCount) {
            return ALL_CHECKED;
        } else if (cartData.size() == unCheckCount) {
            return ALL_UNCHECKED;
        } else {
            return NOT_ALL_SAME;
        }
    }
}