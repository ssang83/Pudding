package com.enliple.pudding.adapter.my;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.DeliveryCheckActivity;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryDetailItems;
import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryDetailProductData;
import com.enliple.pudding.widget.shoptree.DeliveryBasicInfoDialog;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */

public class PurchaseHistoryDetailAdapter extends RecyclerView.Adapter<PurchaseHistoryDetailAdapter.PurchaseHistoryDetailViewHolder> {

    private static final String PAYMENT_SUCCESS = "결제 완료";
    private static final String PRODUCT_PENDING = "상품 준비중";
    private static final String PRODUCT_DELIVERING = "배송중";
    private static final String PRODUCT_DELIVERED = "배송 완료";
    private static final String REFUND_COMPLETE = "환불 완료";
    private static final String CANCEL_COMPLETE = "취소 완료";
    private static final String DEPOSIT_CANCEL = "입금 대기";
    private static final String COMPLETE = "구매 확정";

    private Context mContext;
    private List<PurchaseHistoryDetailItems> mItems = new ArrayList<>();
    private Listener mListener;

    public PurchaseHistoryDetailAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public PurchaseHistoryDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_purchase_history_detail, parent, false);

        return new PurchaseHistoryDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PurchaseHistoryDetailViewHolder holder, int position) {
        bindPurchaseHistoryDetailViewHolder(holder, position);
    }

    private void bindPurchaseHistoryDetailViewHolder(PurchaseHistoryDetailViewHolder holder, int position) {
        PurchaseHistoryDetailItems item = mItems.get(position);
        String shopName = item.shopName;

        holder.layoutProductContainer.removeAllViews();
        if (item.productData != null) {
            for (int i = 0; i < item.productData.size(); i++) {
                PurchaseHistoryDetailProductData productData = item.productData.get(i);
                PruchaseHistoryDetailProductHodler productHolder =
                        new PruchaseHistoryDetailProductHodler(holder.itemView.getContext(), holder.layoutProductContainer);

                if (productData.status.equals(PRODUCT_DELIVERED)) {
                    productHolder.viewForButtons.setVisibility(View.VISIBLE);
                } else {
                    productHolder.viewForButtons.setVisibility(View.GONE);
                }

                productHolder.textViewStatus.setText(productData.status);

                if (productData.status.equals(PAYMENT_SUCCESS) ||
                        productData.status.equals(DEPOSIT_CANCEL)) {
                    productHolder.buttonCancel.setText("주문취소");
                } else if(productData.status.equals(PRODUCT_PENDING)) {
                    productHolder.buttonCancel.setText("주문취소");
                } else if (productData.status.equals(COMPLETE)) {
                    if (productData.review != null) {
                        if (productData.review.is_type.equals("2")) {
                            productHolder.buttonCancel.setVisibility(View.GONE);
                        } else {
                            productHolder.buttonCancel.setText("꼼꼼리뷰 작성");
                        }
                    } else {
                        productHolder.buttonCancel.setVisibility(View.GONE);
                    }
                } else {
                    productHolder.buttonCancel.setVisibility(View.GONE);
                }

                if (productData.status.equals(PRODUCT_DELIVERING) ||
                        productData.status.equals(PRODUCT_DELIVERED) || productData.status.equals(COMPLETE)) {
                    productHolder.buttonDeliveryCheck.setVisibility(View.VISIBLE);
                } else {
                    productHolder.buttonDeliveryCheck.setVisibility(View.GONE);
                }

                if (productData.status.equals(CANCEL_COMPLETE) ||
                        productData.status.equals(REFUND_COMPLETE)) {
                    SpannableString ss = new SpannableString(productData.title);
                    ss.setSpan(new StrikethroughSpan(), 0, productData.title.length(), 0);
                    productHolder.textViewTitle.setText(ss);

                    SpannableString ss1 = new SpannableString(productData.option);
                    ss1.setSpan(new StrikethroughSpan(), 0, productData.option.length(), 0);
                    productHolder.textViewOption.setText(ss1);
                } else {
                    productHolder.textViewTitle.setText(productData.title);
                    productHolder.textViewOption.setText(productData.option);
                }

                ImageLoad.setImage(mContext, productHolder.imageViewThumbnail, productData.image, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);

                productHolder.buttonCancel.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            if (productData.review == null) {
                                mListener.cancelRequest(((PurchaseHistoryDetailProductData) productHolder.rootView.getTag()));
                            } else {
                                mListener.productDetailReview(((PurchaseHistoryDetailProductData) productHolder.rootView.getTag()), shopName);
                            }
                        }
                    }
                });

                productHolder.buttonExchange.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.exchangeRequest(((PurchaseHistoryDetailProductData) productHolder.rootView.getTag()));
                        }
                    }
                });

                productHolder.buttonRefund.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.refundRequest(((PurchaseHistoryDetailProductData) productHolder.rootView.getTag()));
                        }
                    }
                });

                productHolder.buttonReview.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.productReview(((PurchaseHistoryDetailProductData) productHolder.rootView.getTag()), shopName);
                        }
                    }
                });

                productHolder.buttonDeliveryCheck.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        DeliveryBasicInfoDialog deliveryBasicInfoDialog = new DeliveryBasicInfoDialog(mContext,
                                "배송조회", productData.trackingInfoName, productData.trackingInfoCode, productData.trackingInfoUrl);
                        deliveryBasicInfoDialog.show();
//                        Intent intent = new Intent(mContext, DeliveryCheckActivity.class);
//                        intent.putExtra(DeliveryCheckActivity.INTENT_EXTRA_KEY_URL, productData.trackingInfoUrl);
//                        mContext.startActivity(intent);
                    }
                });

                productHolder.rootView.setTag(productData);
                holder.layoutProductContainer.addView(productHolder.rootView);
            }
        }
    }

    public void setItems(List<PurchaseHistoryDetailItems> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        }

        mItems.clear();
        mItems.addAll(items);

        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    static class PruchaseHistoryDetailProductHodler {

        public LinearLayout rootView;
        public AppCompatTextView textViewTitle;
        public AppCompatTextView textViewStatus;
        public AppCompatTextView textViewOption;
        public AppCompatImageView imageViewThumbnail;
        public AppCompatButton buttonRefund;
        public AppCompatButton buttonExchange;
        public AppCompatButton buttonCancel;
        public View buttonDeliveryCheck;
        public AppCompatButton buttonReview;
        public View viewForButtons;

        public PruchaseHistoryDetailProductHodler(Context context, ViewGroup root) {
            rootView = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.layout_purchase_deliverd_product_item, root, false);
            textViewTitle = rootView.findViewById(R.id.textViewTitle);
            textViewStatus = rootView.findViewById(R.id.textViewStatus);
            textViewOption = rootView.findViewById(R.id.textViewOption);
            imageViewThumbnail = rootView.findViewById(R.id.imageViewThumbnail);
            buttonRefund = rootView.findViewById(R.id.buttonRefund);
            buttonExchange = rootView.findViewById(R.id.buttonExchange);
            buttonCancel = rootView.findViewById(R.id.buttonCancel);
            buttonDeliveryCheck = rootView.findViewById(R.id.buttonDeliveryCheck);
            buttonReview = rootView.findViewById(R.id.buttonReview);
            viewForButtons = rootView.findViewById(R.id.layoutForButtons);
        }
    }

    static class PurchaseHistoryDetailViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layoutProductContainer;

        public PurchaseHistoryDetailViewHolder(View itemView) {
            super(itemView);
            layoutProductContainer = itemView.findViewById(R.id.layoutProductContainer);
        }
    }

    /**
     * Event Callbacks
     */
    public interface Listener {
        void exchangeRequest(PurchaseHistoryDetailProductData data);

        void refundRequest(PurchaseHistoryDetailProductData data);

        void cancelRequest(PurchaseHistoryDetailProductData data);

        void productReview(PurchaseHistoryDetailProductData data, String shopName);

        void productDetailReview(PurchaseHistoryDetailProductData data, String shopName);
    }
}
