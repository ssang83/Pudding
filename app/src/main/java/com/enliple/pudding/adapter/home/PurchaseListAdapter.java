package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API151;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class PurchaseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public Listener listener;
    public ItemViewHolder itemViewHolder;
    private List<API151.Data> mItems = new ArrayList<>();
    public interface Listener {
        public void onItemClick(API151.Data item);
        public void onLikeClick(API151.Data item, boolean status);
    }

    public PurchaseListAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_items, parent, false);
        itemViewHolder = new ItemViewHolder(context, view);
        return itemViewHolder;
    }

    public void setItems(List<API151.Data> items ) {
        if ( mItems == null )
            mItems = new ArrayList<>();
        else
            mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindItemViewHolder((ItemViewHolder) holder, position);
    }

    @Override
    public int getItemCount() {
        if ( mItems != null && mItems.size() > 0 )
            return mItems.size();
        else
            return 0;
    }

    private void bindItemViewHolder(ItemViewHolder holder, int position) {
        API151.Data item = mItems.get(position);
        String productName = item.title;
        String shopName = item.sitename;
        String productGubun = item.strType;
        String price = item.price;
        String originPrice = item.orgprice;
        String zzimStatus = item.is_wish;
        String isCart = item.is_cart;
        Logger.e("isCart :: " + isCart);

        String it_stock_qty = item.it_stock_qty;
        if ( "0".equals(it_stock_qty) || TextUtils.isEmpty(it_stock_qty) ) {
            holder.soldOut.setVisibility(View.VISIBLE);
            holder.priceLayer.setVisibility(View.GONE);
        } else {
            holder.soldOut.setVisibility(View.GONE);
            holder.priceLayer.setVisibility(View.VISIBLE);
        }

        String shopFeeStatus = item.it_sc_type;
        String percentage = "";
        double dPrice = 0;
        double dOriginPrice = 0;
        try {
            if ( price == null || originPrice == null ) {

            } else {
                dPrice = Double.valueOf(price);
                dOriginPrice = Double.valueOf(originPrice);

                if (dPrice != dOriginPrice) {
                    Logger.e("dPrice :: " + dPrice);
                    Logger.e("dOriginPrice :: " + dOriginPrice);
                    String sRate = getRate(dOriginPrice, dPrice);
                    Logger.e("sRate :: " + sRate);
                    if ( !TextUtils.isEmpty(sRate) )
                        percentage = getRate(dOriginPrice, dPrice) + "%";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String imagePath = item.image1;
        ImageLoad.setImage(context, holder.image, imagePath, null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);
        holder.productName.setText(productName);
        holder.price.setText(Utils.ToNumFormat(dPrice)+"원");
        if (TextUtils.isEmpty(originPrice) || price.equals(originPrice) ) {
            holder.originPrice.setVisibility(View.INVISIBLE);
        } else {
            if ( "0".equals(originPrice) )
                holder.originPrice.setVisibility(View.INVISIBLE);
            else {
                holder.originPrice.setVisibility(View.VISIBLE);
                holder.originPrice.setText(Utils.ToNumFormat(dOriginPrice)+"원");
                holder.originPrice.setPaintFlags(holder.originPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
        if ( TextUtils.isEmpty(percentage) ) {
            holder.sale.setVisibility(View.GONE);
        } else {
            holder.sale.setVisibility(View.VISIBLE);
            holder.sale.setText(percentage);
        }
        holder.shopName.setText(shopName);
        if ("Y".equals(zzimStatus) ) {
            holder.imageLike.setBackgroundResource(R.drawable.product_scrap_on_ico);
        } else {
            holder.imageLike.setBackgroundResource(R.drawable.product_scrap_off_ico);
        }
        if ( "Y".equals(isCart) ) {
            holder.imageCart.setBackgroundResource(R.drawable.product_cart_on_ico);
        } else {
            holder.imageCart.setBackgroundResource(R.drawable.product_cart_off_ico);
        }
        if ( "1".equals(shopFeeStatus) ) {
            holder.freeFeeBadge.setVisibility(View.VISIBLE);
        } else {
            holder.freeFeeBadge.setVisibility(View.GONE);
        }

        if ( TextUtils.isEmpty(item.pointRate) || "0".equals(item.pointRate) )
            holder.saveBadge.setVisibility(View.GONE);
        else {
            holder.saveBadge.setVisibility(View.VISIBLE);
            holder.saveBadge.setText(item.pointRate + "%적립");
        }

        if ( TextUtils.isEmpty(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_shop_background);
        } else if ( "1".equals(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_shop_background);
        } else if ( "2".equals(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_link_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_link_background);
        } else if ( "3".equals(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_link_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_link_background);
        } else {
            holder.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_shop_background);
        }

        holder.reviewPoint.setText(item.review_avg);
        holder.reviewCnt.setText(item.review_cnt);
        holder.zzimCnt.setText(item.wish_cnt);

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( listener != null ) {
                    listener.onItemClick(item);
                }
            }
        });
        holder.btnLike.setVisibility(View.GONE);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public String idx;
        public AppCompatTextView productName, shopName, sale, originPrice, price, freeFeeBadge, saveBadge, reviewPoint, reviewCnt, zzimCnt, soldOut;
        public AppCompatImageView imageLike, imageCart, productGubun;
        public SelectableRoundedImageView image;
        public RelativeLayout btnLike, btnCart, item, shopBg;
        public LinearLayout priceLayer;
        public ItemViewHolder(Context context, View itemView) {
            super(itemView);
            shopBg = itemView.findViewById(R.id.shopBg);
            priceLayer = itemView.findViewById(R.id.priceLayer);
            soldOut = itemView.findViewById(R.id.soldOut);
            item = itemView.findViewById(R.id.item);
            freeFeeBadge = itemView.findViewById(R.id.freeFeeBadge);
            saveBadge = itemView.findViewById(R.id.saveBadge);
            reviewPoint = itemView.findViewById(R.id.reviewPoint);
            reviewCnt = itemView.findViewById(R.id.reviewCnt);
            zzimCnt = itemView.findViewById(R.id.zzimCnt);
            productName = itemView.findViewById(R.id.productName);
            shopName = itemView.findViewById(R.id.shopName);
            productGubun = itemView.findViewById(R.id.productGubun);
            sale = itemView.findViewById(R.id.sale);
            originPrice = itemView.findViewById(R.id.originPrice);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.image);
            imageLike = itemView.findViewById(R.id.imageLike);
            imageCart = itemView.findViewById(R.id.imageCart);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnCart = itemView.findViewById(R.id.btnCart);
        }
    }

    private String getRate(double originPrice, double price) {
        String result = "";
        if (originPrice == 0 || price == 0) {
            return "";
        }

        try {
            if (originPrice >= 0) {
                double rate = (originPrice - price) / originPrice;
                Logger.d("rate :: "  + rate);
                double dPercentage = rate * 100;
                int salePercentage = (int)dPercentage;
                if (salePercentage > 0) {
                    result = "" + salePercentage;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
