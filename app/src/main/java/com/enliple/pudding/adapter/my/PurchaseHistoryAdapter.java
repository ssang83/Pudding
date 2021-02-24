package com.enliple.pudding.adapter.my;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.DeliveryCheckActivity;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryData;
import com.enliple.pudding.commons.shoptree.data.PurchaseHistoryItems;
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

public class PurchaseHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String PRODUCT_DELIVERING = "배송중";
    private static final String PRODUCT_DELIVERED = "배송 완료";
    private static final String PURCHASE_CONFIRM = "구매 확정";
    private static final String PAYMENT_SUCCESS = "결제 완료";
    private static final String PRODUCT_READY = "상품 준비중";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private List<PurchaseHistoryData> mItems = new ArrayList<>();
    private Listener mListener;
    private Context mContext;

    public interface Listener {
        void onItemClicked(PurchaseHistoryItems data);

        void exchangeRequest(PurchaseHistoryItems data);

        void refundRequest(PurchaseHistoryItems data);

        void cancelRequest(PurchaseHistoryItems data);

        void productReview(PurchaseHistoryItems data, String date);

        void productDetailReview(PurchaseHistoryItems data, String date);
    }

    public PurchaseHistoryAdapter() {
        setHasStableIds(true);
    }

    public PurchaseHistoryAdapter(Context context) {
        this();
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_purchase_history, parent, false);
            return new PurchaseHistoryViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.purchase_history_footer, parent, false);
            return new PurchaseHistoryFooterHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == TYPE_ITEM) {
            onBindItem((PurchaseHistoryViewHolder) holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == mItems.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<PurchaseHistoryData> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void addItems(List<PurchaseHistoryData> items) {
        int currentItemCount = mItems.size();
        int newItemCount = items != null ? items.size() : 0;
        mItems.addAll(items);
        notifyItemRangeInserted(currentItemCount, newItemCount);
    }

    private void onBindItem(PurchaseHistoryViewHolder holder, int position) {
        PurchaseHistoryData item = getItem(position);

        String[] array = item.date.split(" ");
        String time = array[0];
        String[] aTime = time.split("-");
        String fTime = aTime[0] + "년 " + aTime[1] + "월 " + aTime[2] + "일";
        holder.textViewDate.setText(fTime);

        if(position == mItems.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.layoutProductContainer.removeAllViews();
        if (item.data != null) {
            for (int i = 0; i < item.data.size(); i++) {
                PurchaseHistoryItems product = item.data.get(i);
                PurchaseHistoryProductHolder productHolder = new PurchaseHistoryProductHolder(holder.itemView.getContext(), holder.layoutProductContainer);
                productHolder.textViewTitle.setText(product.title);
                productHolder.textViewOption.setText(product.option);
                String st = product.status.replaceAll(" ", "");
                productHolder.textViewStatus.setText(st);
                productHolder.textViewShopName.setText(product.storeNname);
                double price = product.price;
                String sPrice = Utils.ToNumFormat(price) + "원";
                productHolder.price.setText(sPrice);
                ImageLoad.setImage(mContext, productHolder.imageViewThumbnail, product.image, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);
                String productStatus = product.status;
                Logger.e("productStatus :: " + productStatus);
                if (productStatus.equals(PRODUCT_DELIVERED)) { // 배송완료, 한줄리뷰
                    productHolder.buttonDeliveryCheck.setVisibility(View.VISIBLE);
                    productHolder.layoutForButtons.setVisibility(View.VISIBLE);
                    productHolder.buttonDetailReview.setVisibility(View.GONE);
                    productHolder.buttonCancel.setVisibility(View.GONE);
                } else if (productStatus.equals(PURCHASE_CONFIRM)) { //구매확정, 포토리뷰
                    productHolder.buttonDeliveryCheck.setVisibility(View.GONE);
                    productHolder.layoutForButtons.setVisibility(View.GONE);
                    productHolder.buttonDetailReview.setVisibility(View.VISIBLE);
                    productHolder.buttonCancel.setVisibility(View.GONE);
                } else if (productStatus.equals(PAYMENT_SUCCESS)) {
                    productHolder.buttonDeliveryCheck.setVisibility(View.GONE);
                    productHolder.layoutForButtons.setVisibility(View.GONE);
                    productHolder.buttonDetailReview.setVisibility(View.GONE);
                    productHolder.buttonCancel.setVisibility(View.VISIBLE);
                } else if (productStatus.equals(PRODUCT_DELIVERING)) {
                    productHolder.buttonDeliveryCheck.setVisibility(View.VISIBLE);
                    productHolder.layoutForButtons.setVisibility(View.GONE);
                    productHolder.buttonDetailReview.setVisibility(View.GONE);
                    productHolder.buttonCancel.setVisibility(View.GONE);
                } else if(productStatus.equals(PRODUCT_READY)) {
                    productHolder.buttonDeliveryCheck.setVisibility(View.GONE);
                    productHolder.layoutForButtons.setVisibility(View.GONE);
                    productHolder.buttonDetailReview.setVisibility(View.GONE);
                    productHolder.buttonCancel.setVisibility(View.VISIBLE);
                } else {
                    productHolder.buttonDeliveryCheck.setVisibility(View.GONE);
                    productHolder.layoutForButtons.setVisibility(View.GONE);
                    productHolder.buttonDetailReview.setVisibility(View.GONE);
                    productHolder.buttonCancel.setVisibility(View.GONE);
                }


                holder.layoutProductContainer.addView(productHolder.rootView);

                productHolder.buttonDeliveryCheck.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(mContext, DeliveryCheckActivity.class);
                        intent.putExtra(DeliveryCheckActivity.INTENT_EXTRA_KEY_URL, product.trackingInfoUrl);
                        mContext.startActivity(intent);
                    }
                });

                productHolder.buttonReivew.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.productReview(product, item.date);
                        }
                    }
                });

                productHolder.buttonRefund.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.refundRequest(product);
                        }
                    }
                });

                productHolder.buttonExchange.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.exchangeRequest(product);
                        }
                    }
                });

                productHolder.buttonCancel.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.cancelRequest(product);
                        }
                    }
                });

                productHolder.buttonDetailReview.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.productDetailReview(product, item.date);
                        }
                    }
                });

                productHolder.clickLayer.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.onItemClicked(product);
                        }
                    }
                });
//                for (int j = 0; j < item.data.get(i).productData.size(); j++) {
//                    PurchaseHistoryItems items = item.data.get(i);
//                    PurchaseHistoryProductData data = items.productData.get(j);
//                    PurchaseHistoryProductHolder productHolder =
//                            new PurchaseHistoryProductHolder(holder.itemView.getContext(), holder.layoutProductContainer);
//
//                    productHolder.textViewTitle.setText(data.title);
//                    productHolder.textViewOption.setText(data.option);
//                    productHolder.textViewStatus.setText(data.status);
//                    if (data.status.equals(PRODUCT_DELIVERING) ||
//                            data.status.equals(PRODUCT_DELIVERED)) {
//                        productHolder.buttonDeliveryCheck.setVisibility(View.VISIBLE);
//                        productHolder.buttonReivew.setVisibility(View.VISIBLE);
//
//                        String tName = data.trackingInfoName;
//                        if (!TextUtils.isEmpty(tName)) {
//                            StringBuilder sb = new StringBuilder(data.trackingInfoName);
//                            sb.append(" ");
//                            sb.append(data.trackingInfoCode);
//                        }
//                    } else {
//                        productHolder.buttonDeliveryCheck.setVisibility(View.GONE);
//                        productHolder.buttonReivew.setVisibility(View.GONE);
//                    }
//
//                    if (item.data.get(i).productData.size() >= 2) {
//                        if (j == item.data.get(i).productData.size() - 1) {
//                            productHolder.divider.setVisibility(View.GONE);
//                        } else {
//                            productHolder.divider.setVisibility(View.VISIBLE);
//                        }
//                    } else {
//                        productHolder.divider.setVisibility(View.GONE);
//                    }
//
////                    Glide.with(mContext)
////                            .load(data.image)
////                            .asBitmap()
////                            .priority(Priority.HIGH)
////                            .diskCacheStrategy(DiskCacheStrategy.ALL)
////                            .into(productHolder.imageViewThumbnail);
//                    ImageLoad.setImage(mContext, productHolder.imageViewThumbnail, data.image, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);
//                    holder.layoutProductContainer.addView(productHolder.rootView);
//
//                    productHolder.buttonDeliveryCheck.setOnClickListener(new OnSingleClickListener() {
//                        @Override
//                        public void onSingleClick(View v) {
//                            Intent intent = new Intent(mContext, DeliveryCheckActivity.class);
//                            intent.putExtra(DeliveryCheckActivity.INTENT_EXTRA_KEY_URL, data.trackingInfoUrl);
//                            mContext.startActivity(intent);
//                        }
//                    });
//
//                    productHolder.buttonReivew.setOnClickListener(new OnSingleClickListener() {
//                        @Override
//                        public void onSingleClick(View v) {
//                            if (mListener != null) {
//                                mListener.onReviewClicked(data, item.orderNo);
//                            }
//                        }
//                    });
//                }
            }
        }
    }

    public PurchaseHistoryData getItem(int itemPosition) {
        return itemPosition < mItems.size() ? mItems.get(itemPosition) : null;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    static class PurchaseHistoryProductHolder {

        public LinearLayout rootView, layoutForButtons, clickLayer;
        public AppCompatImageView imageViewThumbnail;
        public AppCompatTextView textViewTitle;
        public AppCompatTextView textViewStatus;
        public AppCompatTextView textViewOption, textViewShopName;
        public AppCompatTextView price;
        public RelativeLayout buttonDeliveryCheck;
        public AppCompatTextView buttonReivew, buttonExchange, buttonRefund, buttonDetailReview, buttonCancel;

        public PurchaseHistoryProductHolder(Context context, ViewGroup root) {
            rootView = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.layout_purchase_history_item, root, false);

            clickLayer = rootView.findViewById(R.id.clickLayer);
            imageViewThumbnail = rootView.findViewById(R.id.imageViewThumbnail);
            textViewTitle = rootView.findViewById(R.id.textViewTitle);
            price = rootView.findViewById(R.id.price);
            textViewStatus = rootView.findViewById(R.id.textViewStatus);
            textViewOption = rootView.findViewById(R.id.textViewOption);
            buttonDeliveryCheck = rootView.findViewById(R.id.buttonDeliveryCheck);
            buttonReivew = rootView.findViewById(R.id.buttonReview);
            buttonExchange = rootView.findViewById(R.id.buttonExchange);
            buttonRefund = rootView.findViewById(R.id.buttonRefund);
            buttonDetailReview = rootView.findViewById(R.id.buttonDetailReview);
            buttonCancel = rootView.findViewById(R.id.buttonCancel);
            layoutForButtons = rootView.findViewById(R.id.layoutForButtons);
            textViewShopName = rootView.findViewById(R.id.textViewShopName);
        }
    }

    /**
     * RecyclerView Main ViewHolder Class
     */
    static class PurchaseHistoryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layoutProductContainer;
        public AppCompatTextView textViewDate;
        public AppCompatTextView textViewOrderNumber;
        public View viewForDetail;
        public View divider;

        public PurchaseHistoryViewHolder(View itemView) {
            super(itemView);
            layoutProductContainer = itemView.findViewById(R.id.layoutProductContainer);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewOrderNumber = itemView.findViewById(R.id.textViewOrderNumber);
            viewForDetail = itemView.findViewById(R.id.layoutForDetail);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    static class PurchaseHistoryFooterHolder extends RecyclerView.ViewHolder {
        public PurchaseHistoryFooterHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * Event Callbacks
     */
//    public interface Listener {
//        void onItemClicked(int position);
//
//        void onReviewClicked(PurchaseHistoryProductData item, String orderNo);
//    }
}
