package com.enliple.pudding.fragment.my;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.enliple.pudding.AbsBaseFragment;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.CRERequestDetailActivity;
import com.enliple.pudding.activity.DeliveryCheckActivity;
import com.enliple.pudding.activity.DeliveryStatusActivity;
import com.enliple.pudding.activity.DeliveryStatusDetailActivity;
import com.enliple.pudding.adapter.my.DeliveryStatusAdapter;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusProductData;
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask;
import com.enliple.pudding.commons.shoptree.response.DeliveryStatusResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class PaymentSuccessFragment extends AbsBaseFragment implements DeliveryStatusAdapter.Listener {

    private static final int RECYCLER_VIEW_ITEM_CACHE_SIZE = 20;
    private static final int REQUEST_DETAIL = 0;
    private static final int REQUEST_CANCEL = 1;

    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private AppCompatTextView textViewEmpty;
    private LinearLayout empty;
    private LinearLayoutManager layoutManager;
    private DeliveryStatusAdapter mDeliveryStatusAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_delivery_status, null, false);
        setLayout(v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(layoutManager);

        setEmptyText(getString(R.string.msg_my_shopping_delivery_status_payment_not_founds),
                getString(R.string.msg_my_shopping_payment_successfully));

        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL) || (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CANCEL) ) {
            loadData();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemClicked(int position) {
        Intent intent = new Intent(getActivity(), DeliveryStatusDetailActivity.class);
        intent.putExtra(DeliveryStatusDetailActivity.INTENT_EXTRA_KEY_STATUS_TYPE, "결제완료");
        intent.putExtra(DeliveryStatusDetailActivity.INTENT_EXTRA_KEY_REG_DATE,
                mDeliveryStatusAdapter.getItem(position).date);
        intent.putExtra(DeliveryStatusDetailActivity.INTENT_EXTRA_KEY_ORDER_NUMBER,
                mDeliveryStatusAdapter.getItem(position).orderNumber);
        intent.putExtra(DeliveryStatusDetailActivity.INTENT_EXTRA_KEY_SHOPTREE_ORDER_NUMBER,
                mDeliveryStatusAdapter.getItem(position).stOrderNumber);
        startActivityForResult(intent, REQUEST_DETAIL);
    }

    @Override
    public void onDeliveryStatusClicked(DeliveryStatusProductData data) {
        Intent intent = new Intent(getActivity(), DeliveryCheckActivity.class);
        intent.putExtra(DeliveryCheckActivity.INTENT_EXTRA_KEY_URL, data.trackingInfo_url);
        startActivity(intent);
    }

    @Override
    public void onCancelPurchaseClicked(DeliveryStatusProductData data) {
        Intent intent = new Intent(getActivity(), CRERequestDetailActivity.class);
        Logger.e("data.price :::::: " + data.price);
        intent.putExtra(CRERequestDetailActivity.INTENT_KEY_REQUEST_TYPE, "cancel");
        intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_NAME, data.title);
        intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_OPTION, data.option);
        intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_PRICE, String.valueOf(data.price));
        intent.putExtra(CRERequestDetailActivity.INTENT_KEY_PRODUCT_KEY, data.itemKey);
        intent.putExtra(CRERequestDetailActivity.INTENT_KEY_DELIVERY_COMPANY, data.trackingInfo_name);

        startActivityForResult(intent, REQUEST_CANCEL);
    }

    private void setLayout(View v) {
        mRecyclerView = v.findViewById(R.id.recyclerViewStatus);
        progressBar = v.findViewById(R.id.progressBar);
        textViewEmpty = v.findViewById(R.id.textViewEmpty);
        empty = v.findViewById(R.id.empty);
    }

    /**
     * 상품이 존재하지 않을 때 출력되는 문구를 설정
     *
     * @param emptyText
     * @param colorSpannableText
     */
    private void setEmptyText(@NonNull String emptyText, @NonNull String colorSpannableText) {
        final SpannableStringBuilder sp = new SpannableStringBuilder(emptyText);
        sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_color)),
                0, colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, colorSpannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewEmpty.setText(sp);
    }

    /**
     * 리스트가 없을 경우 표시되는 안내문구의 표시여부를 설정
     *
     * @param visible
     */
    private void setEmptyViewVisibility(boolean visible) {
        empty.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void loadData() {
        ShopTreeAsyncTask task = new ShopTreeAsyncTask(getActivity());
        task.getDeliveryStatusList("결제완료",
                (result, obj) -> {
                    Logger.d("" + obj.toString());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        final DeliveryStatusResponse responseObj = mapper.readValue(obj.toString(),
                                DeliveryStatusResponse.class);

                        if (responseObj != null && responseObj.getResult().equals("SUCCESS")) {
                            if (responseObj.deliveryType != null) {
                                if (DeliveryStatusActivity.instance != null) {
                                    Logger.e("paymentSuccess :: " + responseObj.deliveryType.paymentSuccess);
                                    Logger.e("productPending :: " + responseObj.deliveryType.productPending);
                                    Logger.e("productDeliverying :: " + responseObj.deliveryType.productDeliverying);
                                    Logger.e("productDelivered :: " + responseObj.deliveryType.productDelivered);

                                    DeliveryStatusActivity.instance.setPaymentSuccessCount(responseObj.deliveryType.paymentSuccess);
                                } else {
                                    Logger.e("delivery status activity instance null");
                                }

                                if (responseObj.orders != null && responseObj.orders.size() > 0) {
                                    setEmptyViewVisibility(false);
                                    mDeliveryStatusAdapter = new DeliveryStatusAdapter(this);
                                    mRecyclerView.setAdapter(mDeliveryStatusAdapter);
                                    mDeliveryStatusAdapter.setItems(responseObj.orders);
                                } else {
                                    setEmptyViewVisibility(true);
                                }
                            } else {
                                DeliveryStatusActivity.instance.setPaymentSuccessCount(0);
                                setEmptyViewVisibility(true);
                            }
                        } else {
                            DeliveryStatusActivity.instance.setPaymentSuccessCount(0);
                            setEmptyViewVisibility(true);
                        }
                    } catch (Exception e) {
                        Logger.e(e.toString());
                        progressBar.setVisibility(View.GONE);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
