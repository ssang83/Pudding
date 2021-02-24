package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusData;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusItems;
import com.enliple.pudding.commons.shoptree.data.DeliveryStatusProductData;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kim Joonsung on 2018-09-20.
 */

public class DeliveryStatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOOTER = 0;
    private static final int TYPE_ITEM = 1;

    private List<DeliveryStatusData> mItems = new ArrayList<>();
    private Listener mListener;


    public DeliveryStatusAdapter() {
        setHasStableIds(true);
    }

    public DeliveryStatusAdapter(Listener listener) {
        this();
        setListener(listener);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_delivery_status, parent, false);
            return new DeliveryStatusViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.delivery_status_footer, parent, false);
            return new DeliveryStatusFooterHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(holder.getItemViewType() == TYPE_ITEM) {
            onBindItem((DeliveryStatusViewHolder) holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size() + 1;
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
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<DeliveryStatusData> items) {
        mItems.clear();
        mItems.addAll(items);
        Logger.e("items.size :: " + items.size());
        notifyDataSetChanged();
    }

    public void addItems(List<DeliveryStatusData> items) {
        int currentItemCount = mItems.size();
        int newItemCount = items != null ? items.size() : 0;
        mItems.addAll(items);
        notifyItemRangeInserted(currentItemCount, newItemCount);
    }

    public DeliveryStatusData getItem(int itemPosition) {
        return itemPosition < mItems.size() ? mItems.get(itemPosition) : null;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void onBindItem(DeliveryStatusViewHolder holder, int position) {
        final DeliveryStatusData item = getItem(position);

        String[] array = item.date.split(" ");
        String time = array[0];
        String[] aTime = time.split("-");
        String fTime = aTime[0] + "년 " + aTime[1] + "월 " + aTime[2] + "일";
        holder.textViewDate.setText(fTime);
//        holder.textViewOrderNumber.setText(item.orderNumber);

        if(position == 0) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener((v) -> {
            if (mListener != null) {
                mListener.onItemClicked(position);
            }
        });

        holder.layoutProductContainer.removeAllViews();
        if (item.datas != null) {
            for (int i = 0; i < item.datas.size(); i++) {
                for (int j = 0; j < item.datas.get(i).productData.size(); j++) {
                    DeliveryStatusItems items = item.datas.get(i);
                    DeliveryStatusProductData data = items.productData.get(j);
                    DeliveryStatusProductHolder productHolder =
                            new DeliveryStatusProductHolder(holder.itemView.getContext(), holder.layoutProductContainer);

                    if (j == 0) {
                        productHolder.top.setVisibility(View.VISIBLE);
                        productHolder.dividerTop.setVisibility(View.GONE);
                    } else {
                        productHolder.top.setVisibility(View.GONE);
                        productHolder.dividerTop.setVisibility(View.VISIBLE);
                    }
                    productHolder.textViewShopName.setText(items.shopName);
                    productHolder.textViewTitle.setText(data.title);
                    productHolder.textViewOption.setText(data.option);
                    // 배송진행상태
                    if ("결제 완료".equalsIgnoreCase(data.status)) {
                        productHolder.textViewStatus
                                .setText(holder.itemView.getContext()
                                        .getString(R.string.msg_my_shopping_delivery_status_payment));
                    } else if ("상품 준비중".equalsIgnoreCase(data.status)) {
                        productHolder.textViewStatus
                                .setText(holder.itemView.getContext()
                                        .getString(R.string.msg_my_shopping_delivery_status_pending));
                    } else if ("배송중".equalsIgnoreCase(data.status)) {
                        productHolder.textViewStatus
                                .setText(holder.itemView.getContext()
                                        .getString(R.string.msg_my_shopping_delivery_status_delivering));
                    } else if ("배송 완료".equalsIgnoreCase(data.status)) {
                        productHolder.textViewStatus.setText("배송완료");
                    }

                    if ("배송 완료".equalsIgnoreCase(data.status) || "배송중".equalsIgnoreCase(data.status) ) {
                        productHolder.btnLayer.setVisibility(View.GONE);
                        productHolder.searchDelivery.setVisibility(View.VISIBLE);
                    } else {
                        productHolder.btnLayer.setVisibility(View.VISIBLE);
                        productHolder.searchDelivery.setVisibility(View.GONE);
                        if ( "결제 완료".equalsIgnoreCase(data.status) || "상품 준비중".equalsIgnoreCase(data.status) ) {
                            productHolder.cancelPurchase.setVisibility(View.VISIBLE);
                        }
                    }
                    productHolder.searchDelivery.setVisibility(View.GONE);
                    productHolder.btnLayer.setVisibility(View.GONE);

//                    if (i == item.datas.size() - 1) {
//                        productHolder.divider.setVisibility(View.GONE);
//                    } else {
//                        productHolder.divider.setVisibility(View.VISIBLE);
//                    }

//                    if (item.datas.get(i).productData.size() >= 2) {
//                        if (j == 0) {
//                            productHolder.divider.setVisibility(View.GONE);
//                        }
//                    }

                    productHolder.cancelPurchase.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ( mListener != null )
                                mListener.onCancelPurchaseClicked(data);
                        }
                    });

                    productHolder.searchDelivery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ( mListener != null )
                                mListener.onDeliveryStatusClicked(data);
                        }
                    });

//                    Glide.with(holder.itemView.getContext())
//                            .load(data.image)
//                            .asBitmap()
//                            .priority(Priority.HIGH)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(productHolder.imageViewThumbnail);
                    ImageLoad.setImage(holder.itemView.getContext(), productHolder.imageViewThumbnail, data.image, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);

                    holder.layoutProductContainer.addView(productHolder.rootView);
                }
            }
        }
    }

    /**
     * Inner View Holder from DeliveryStatusViewHolder
     */
    static class DeliveryStatusProductHolder {

        public LinearLayout rootView;
        public AppCompatImageView imageViewThumbnail;
        public AppCompatTextView textViewTitle;
        public AppCompatTextView textViewStatus;
        public AppCompatTextView searchDelivery;
        public AppCompatTextView textViewOption;
        public AppCompatTextView textViewShopName;
        public AppCompatTextView cancelPurchase;
        public RelativeLayout btnLayer, top;
//        public View divider;
        public View layoutForShopName, dividerTop;

        public DeliveryStatusProductHolder(Context context, ViewGroup root) {
            rootView = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.layout_delivery_product_item, root, false);
            imageViewThumbnail = rootView.findViewById(R.id.imageViewThumbnail);
            textViewTitle = rootView.findViewById(R.id.textViewTitle);
            textViewStatus = rootView.findViewById(R.id.textViewStatus);
            textViewOption = rootView.findViewById(R.id.textViewOption);
            textViewShopName = rootView.findViewById(R.id.textViewShopName);
            searchDelivery = rootView.findViewById(R.id.searchDelivery);
            cancelPurchase = rootView.findViewById(R.id.cancelPurchase);
            top = rootView.findViewById(R.id.top);
            dividerTop = rootView.findViewById(R.id.dividerTop);
            btnLayer = rootView.findViewById(R.id.btnLayer);
//            divider = rootView.findViewById(R.id.divider);
            layoutForShopName = rootView.findViewById(R.id.layoutForShopName);
        }
    }

    /**
     * RecyclerView Main ViewHolder Class
     */
    static class DeliveryStatusViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout layoutProductContainer;
        public TextView textViewDate;
        public View line;
//        public TextView textViewOrderNumber;
//        public View viewForDetail;

        public DeliveryStatusViewHolder(View itemView) {
            super(itemView);
            layoutProductContainer = itemView.findViewById(R.id.layoutProductContainer);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            line = itemView.findViewById(R.id.line);
//            textViewOrderNumber = itemView.findViewById(R.id.textViewOrderNumber);
//            viewForDetail = itemView.findViewById(R.id.layoutForDetail);
        }
    }

    static class DeliveryStatusFooterHolder extends RecyclerView.ViewHolder {

        public DeliveryStatusFooterHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * Event Callbacks
     */
    public interface Listener {
        void onItemClicked(int position);
        void onDeliveryStatusClicked(DeliveryStatusProductData data);
        void onCancelPurchaseClicked(DeliveryStatusProductData data);
    }
}
