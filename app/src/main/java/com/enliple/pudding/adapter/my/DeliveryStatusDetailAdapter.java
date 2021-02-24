package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.CREActivity;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusDetailItems;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusDetailProductData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class DeliveryStatusDetailAdapter extends RecyclerView.Adapter<DeliveryStatusDetailAdapter.DeliveryStatusDetailViewHolder> {
    public static final String PAYMENT_SUCCESS = "결제완료";
    public static final String PRODUCT_PENDING = "상품준비중";
    public static final String PRODUCT_DELIVERING = "배송중";
    public static final String PRODUCT_DELIVERED = "배송완료";
    public static final String PRODUCT_CONFIRM = "구매확정";
    public static final String PRODUCT_COMPLETE_REFUND = "환불완료";

    private Context mContext;
    private List<DeliveryStatusDetailItems> mItems = new ArrayList<>();
    private Listener mListener;

    public DeliveryStatusDetailAdapter(Context mContext) {
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
    public DeliveryStatusDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_delivery_status_detail, parent, false);

        return new DeliveryStatusDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DeliveryStatusDetailViewHolder holder, int position) {
        bindDeliveryStatusDetailViewHolder(holder, position);
    }

    private void bindDeliveryStatusDetailViewHolder(DeliveryStatusDetailViewHolder holder, final int position) {
        DeliveryStatusDetailItems item = mItems.get(position);
        String shopName = item.shopName;
//        holder.textViewShopName.setText(item.shopName);

        holder.layoutProductContainer.removeAllViews();
        if (item.productData != null) {
            for (int i = 0; i < item.productData.size(); i++) {
                final DeliveryStatusDetailProductData productData = item.productData.get(i);
                DeliveryStatusDetailProductHolder productHolder =
                        new DeliveryStatusDetailProductHolder(holder.itemView.getContext(), holder.layoutProductContainer);

//                if(productData.status.equals(PRODUCT_DELIVERED)) {
//                    productHolder.viewForButtons.setVisibility(View.VISIBLE);
//                } else {
//                    productHolder.viewForButtons.setVisibility(View.GONE);
//                }
//

//
//                if(productData.status.equals(PAYMENT_SUCCESS)) {
//                    productHolder.buttonConfirm.setText(mContext.getString(R.string.msg_my_shopping_order_detail_order_cancel));
//                } else if(productData.status.equals(PRODUCT_DELIVERING)) {
//                    productHolder.buttonConfirm.setText(mContext.getString(R.string.msg_my_shopping_order_detail_delivery));
//                } else if(productData.status.equals(PRODUCT_DELIVERED)) {
//                    productHolder.buttonConfirm.setText(mContext.getString(R.string.msg_my_shopping_order_detail_confirm));
//                } else {
//                    productHolder.buttonConfirm.setVisibility(View.GONE);
//                }
                String st = productData.status.replaceAll(" ", "");
                productHolder.textViewStatus.setText(st);
                productHolder.textViewPrice.setText(Utils.ToNumFormat(productData.price) + "원");
                if (PAYMENT_SUCCESS.equals(productData.status.replaceAll(" ", "")) || PRODUCT_PENDING.equals(productData.status.replaceAll(" ", ""))) {
                    productHolder.viewForButtons.setVisibility(View.GONE);
                    productHolder.buttonConfirm.setVisibility(View.VISIBLE);
                    productHolder.buttonBasicInfo.setVisibility(View.GONE);
                    productHolder.buttonConfirm.setText(mContext.getString(R.string.msg_my_shopping_order_detail_order_cancel));
                } else if (PRODUCT_DELIVERING.equals(productData.status.replaceAll(" ", ""))) {
                    productHolder.viewForButtons.setVisibility(View.GONE);
                    productHolder.buttonConfirm.setVisibility(View.GONE);
                    productHolder.buttonBasicInfo.setVisibility(View.VISIBLE);
                    productHolder.buttonConfirm.setText(mContext.getString(R.string.msg_my_shopping_delivery_status));
                } else if (PRODUCT_DELIVERED.equals(productData.status.replaceAll(" ", ""))) {
                    productHolder.viewForButtons.setVisibility(View.VISIBLE);
                    productHolder.buttonConfirm.setVisibility(View.GONE);
                    productHolder.buttonBasicInfo.setVisibility(View.VISIBLE);
//                    productHolder.buttonConfirm.setText(mContext.getString(R.string.msg_title_after_received_review));
                } else if (PRODUCT_CONFIRM.equals(productData.status.replaceAll(" ", ""))) {
                    productHolder.viewForButtons.setVisibility(View.GONE);
                    productHolder.buttonConfirm.setVisibility(View.VISIBLE);
                    productHolder.buttonBasicInfo.setVisibility(View.GONE);
                    productHolder.buttonConfirm.setText(mContext.getString(R.string.msg_title_detail_review));
                } else {
                    productHolder.viewForButtons.setVisibility(View.GONE);
                    productHolder.buttonConfirm.setVisibility(View.GONE);
                    productHolder.buttonBasicInfo.setVisibility(View.GONE);
                }

                productHolder.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( !PAYMENT_SUCCESS.equals(productData.status.replaceAll(" ", ""))
                                && !PRODUCT_PENDING.equals(productData.status.replaceAll(" ", ""))
                                && !PRODUCT_DELIVERING.equals(productData.status.replaceAll(" ", ""))
                                && !PRODUCT_DELIVERED.equals(productData.status.replaceAll(" ", ""))
                                && !PRODUCT_CONFIRM.equals(productData.status.replaceAll(" ", ""))) {
                            mContext.startActivity(new Intent(mContext, CREActivity.class));
                        }
                    }
                });

                productHolder.textViewTitle.setText(productData.title);
                productHolder.textViewOption.setText(productData.option);
                productHolder.textViewShopName.setText(item.shopName);

//                Glide.with(mContext)
//                        .load(productData.image)
//                        .asBitmap()
//                        .priority(Priority.HIGH)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(productHolder.imageViewThumbnail);
                ImageLoad.setImage(mContext, productHolder.imageViewThumbnail, productData.image, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);
                productHolder.buttonBasicInfo.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.showBasicInformation(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()));
                        }
                    }
                });

                productHolder.buttonConfirm.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        String text = productHolder.buttonConfirm.getText().toString();
                        if (text.equals(mContext.getString(R.string.msg_title_after_received_review)) || text.equals(mContext.getString(R.string.msg_title_detail_review))) {
                            if (mListener != null) {
                                mListener.confirmRequest(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()), shopName);
                            }
                        } else if (text.equals(mContext.getString(R.string.msg_my_shopping_delivery_status))) {
                            if (mListener != null) {
                                mListener.showBasicInformation(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()));
                            }
                        } else if (text.equals(mContext.getString(R.string.msg_my_shopping_order_detail_order_cancel))) {
                            if (mListener != null) {
                                mListener.cancelRequest(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()));
                            }
                        } else {
                            if (mListener != null) {
                                mListener.showBasicInformation(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()));
                            }
                        }
                    }
                });

                productHolder.buttonExchange.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.exchangeRequest(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()));
                        }
                    }
                });

                productHolder.buttonRefund.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.refundRequest(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()));
                        }
                    }
                });

                productHolder.writeReview.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (mListener != null) {
                            mListener.confirmRequest(((DeliveryStatusDetailProductData) productHolder.rootView.getTag()), shopName);
                        }
                    }
                });

                productHolder.rootView.setTag(productData);
                holder.layoutProductContainer.addView(productHolder.rootView);

                if ( position == mItems.size() - 1 ) {
                    productHolder.bot.setVisibility(View.VISIBLE);
                } else {
                    productHolder.bot.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setItems(List<DeliveryStatusDetailItems> items) {
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

    static class DeliveryStatusDetailProductHolder {

        public LinearLayout rootView;
        public AppCompatTextView textViewTitle;
        public AppCompatTextView textViewPrice;
        public AppCompatTextView textViewStatus;
        public AppCompatTextView textViewOption;
        public AppCompatTextView textViewShopName;
        public AppCompatImageView imageViewThumbnail;
        public AppCompatImageView imageViewType;
        public AppCompatTextView buttonBasicInfo;
        public AppCompatTextView buttonRefund;
        public AppCompatTextView writeReview;
        public AppCompatTextView buttonExchange;
        public AppCompatTextView buttonConfirm;
        public View viewForButtons, bot;

        public DeliveryStatusDetailProductHolder(Context context, ViewGroup root) {
            rootView = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.layout_delivered_product_item, root, false);
            textViewTitle = rootView.findViewById(R.id.textViewTitle);
            textViewPrice = rootView.findViewById(R.id.textViewPrice);
            textViewStatus = rootView.findViewById(R.id.textViewStatus);
            textViewOption = rootView.findViewById(R.id.textViewOption);
            imageViewThumbnail = rootView.findViewById(R.id.imageViewThumbnail);
            buttonBasicInfo = rootView.findViewById(R.id.buttonBasicInfo);
            buttonRefund = rootView.findViewById(R.id.buttonRefund);
            writeReview = rootView.findViewById(R.id.writeReview);
            buttonExchange = rootView.findViewById(R.id.buttonExchange);
            buttonConfirm = rootView.findViewById(R.id.buttonConfirm);
            viewForButtons = rootView.findViewById(R.id.layoutForButtons);
            bot = rootView.findViewById(R.id.bot);
            imageViewType = rootView.findViewById(R.id.imageViewType);
            textViewShopName = rootView.findViewById(R.id.textViewShopName);
        }
    }

    static class DeliveryStatusDetailViewHolder extends RecyclerView.ViewHolder {
//        public AppCompatTextView textViewShopName;
        public LinearLayout layoutProductContainer;

        public DeliveryStatusDetailViewHolder(View itemView) {
            super(itemView);
            layoutProductContainer = itemView.findViewById(R.id.layoutProductContainer);
//            textViewShopName = itemView.findViewById(R.id.textViewShopName);
        }
    }

    /**
     * Event Callbacks
     */
    public interface Listener {
        void showBasicInformation(DeliveryStatusDetailProductData data);

        void exchangeRequest(DeliveryStatusDetailProductData data);

        void refundRequest(DeliveryStatusDetailProductData data);

        void confirmRequest(DeliveryStatusDetailProductData data, String shopName);

        void cancelRequest(DeliveryStatusDetailProductData data);
    }
}
