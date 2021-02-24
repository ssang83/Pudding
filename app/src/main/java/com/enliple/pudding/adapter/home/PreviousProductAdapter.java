package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.app.StringUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API34;

import java.util.ArrayList;
import java.util.List;

public class PreviousProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0X1001;
    private static final int VIEW_TYPE_ITEM = 0X2001;

    public Context mContext;
    public List<API34.Data> mItems = new ArrayList<>();
    private int sQuantity = -1;
    private String isFavorite = "0";
    private ClickCallbackListener listener;

    public interface ClickCallbackListener {
        public void onItemClick(API34.Data item, int position);
        public void onFavoriteClicked();
        public void onRecentClicked();
    }

    public PreviousProductAdapter(Context mContext, ClickCallbackListener listener) {
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.shopping_previous_header, parent, false);
            return new HeaderHolder(mContext, view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.common_landscape_item, parent, false);
            return new PreviousProductItemHolder(mContext, view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeaderHolder) {
            bindHeaderHolder((HeaderHolder) holder);
        } else {
            bindPreviousProductItem((PreviousProductItemHolder) holder, position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        if (hasStableIds()) {
            return position + 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size() + 1;
        } else {
            return 1;
        }
    }

    public int getItemSize() {
        if ( mItems != null )
            return mItems.size();
        else
            return 0;
    }

    public List<API34.Data> getItems() {
        if ( mItems == null )
            return null;
         else
             return mItems;
    }

    public void addItems(ArrayList<API34.Data> items) {
        int currentItemSize = 0;
        int addedItemSize = items != null ? items.size() : 0;

        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            currentItemSize = mItems.size();
        }

        if (items != null) {
            mItems.addAll(items);
        }

        if (addedItemSize > 0) {
            notifyItemRangeInserted(currentItemSize, addedItemSize);
        }
    }

    public void clear() {
        if (mItems != null) {
            mItems.clear();
            mItems = new ArrayList<>();
        }
    }

    public void init() {
        this.sQuantity = -1;
    }

    public void setHeader(int quantity, String isFavorite) {
        this.sQuantity = quantity;
        this.isFavorite = isFavorite;
    }

    public void setItem(int position, API34.Data item) {
        if ( mItems != null ) {
            mItems.set(position, item);
            notifyDataSetChanged();
        }
    }

    public void setItems(List<API34.Data> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        if (!items.isEmpty()) {
            mItems.addAll(items);
        }

        notifyDataSetChanged();
    }

    public void addItems(List<API34.Data> items) {
        if (items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                mItems.add(items.get(i));
            }

            notifyItemInserted(mItems.size() - items.size());
        }
    }

    /**
     * 해당 Adapter Position 에 위치한 LiveProduct 데이터를 반환
     *
     * @param position
     * @return
     */
    public API34.Data getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    private void bindHeaderHolder(final HeaderHolder holder) {
        if ( sQuantity < 0 ) {
            holder.quantity.setText("");
        } else {
            holder.quantity.setText("총 " + Utils.ToNumFormat(sQuantity) + "개");
        }

        Logger.e("isFavorite :: " + isFavorite);
        if ( "1".equals(isFavorite) ) {
            holder.favorite.setTextColor(0xffbcc6d2);
            holder.recent.setTextColor(0xff9f56f2);
        } else {
            holder.favorite.setTextColor(0xff9f56f2);
            holder.recent.setTextColor(0xffbcc6d2);
        }

        holder.recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = "1";
                holder.favorite.setTextColor(0xffbcc6d2);
                holder.recent.setTextColor(0xff9f56f2);
                if ( listener != null ) {
                    listener.onRecentClicked();
                }
            }
        });

        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = "0";
                holder.favorite.setTextColor(0xff9f56f2);
                holder.recent.setTextColor(0xffbcc6d2);
                if ( listener != null ) {
                    listener.onFavoriteClicked();
                }
            }
        });
    }

    private void bindPreviousProductItem(final PreviousProductItemHolder holder, final int position) {
        API34.Data item = getItemByPosition(position);
        if (item != null) {
            holder.fullLayer.setBackgroundColor(Color.parseColor("#ffffff"));

            holder.title.setText(item.title);
            holder.likeCount.setText(StringUtils.Companion.getSnsStyleCountZeroBase(Integer.valueOf(item.favoriteCount)));
            holder.viewCount.setText(StringUtils.Companion.getSnsStyleCountZeroBase(Integer.valueOf(item.viewerCount)));
            holder.hashTag.setText(StringUtils.Companion.convertHashTagText(item.strTag));

            if (item.videoType.equals("VOD")) {
                holder.label.setBackgroundResource(R.drawable.trip_video_label);
            } else if (item.videoType.equals("LASTLIVE")) {
                holder.label.setBackgroundResource(R.drawable.trip_pass_live_label);
            } else {
                holder.label.setBackgroundResource(R.drawable.trip_onair_label);
            }

            holder.product.setText(item.min_price_product);

            if(item.min_price != null) {
                String price = item.min_price;
                price = PriceFormatter.Companion.getFormatter().getFormattedValue(price);
                if (item.relationPrd.data.size() < 1) {
                    price = String.format(mContext.getString(R.string.msg_price_format), price);
                } else {
                    price = price + "원~";
                }
                holder.price.setText(price);
            }

            ImageLoad.setImage(mContext, holder.thumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);
            ImageLoad.setImage(mContext, holder.profile, item.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL);

            holder.itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item, position);
                    }
                }
            });
        }
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView recent, favorite, quantity;
        public HeaderHolder(Context context, View itemView) {
            super(itemView);
            recent = itemView.findViewById(R.id.recent);
            favorite = itemView.findViewById(R.id.favorite);
            quantity = itemView.findViewById(R.id.quantity);
        }
    }

    public static class PreviousProductItemHolder extends RecyclerView.ViewHolder {
        public View fullLayer;
        public AppCompatImageView thumbnail, label, profile;
        public AppCompatTextView title, likeCount, product, price, viewCount, hashTag;

        public PreviousProductItemHolder(Context context, View itemView) {
            super(itemView);
            fullLayer = itemView.findViewById(R.id.fullLayer);
            thumbnail = (AppCompatImageView) itemView.findViewById(R.id.thumbnail);
            title = (AppCompatTextView) itemView.findViewById(R.id.title);
            likeCount = (AppCompatTextView) itemView.findViewById(R.id.likeCount);
            label = (AppCompatImageView) itemView.findViewById(R.id.label);
            product = itemView.findViewById(R.id.product);
            price = itemView.findViewById(R.id.price);
            viewCount = itemView.findViewById(R.id.viewCount);
            hashTag = itemView.findViewById(R.id.hashTag);
            profile = itemView.findViewById(R.id.profile);
        }
    }

    public void changeZzim(String idx) {
        if ( mItems != null ) {
            for ( int i = 0 ; i < mItems.size() ; i ++  ) {
                API34.Data bItem = mItems.get(i);
                API34.Data.RelationPrdData relationPrd = bItem.relationPrd;
                List<API34.Data.RelationPrdData.PrdData> relationPrdArr = relationPrd.data;
                for ( int j = 0 ; j < relationPrdArr.size() ; j ++  ) {
                    API34.Data.RelationPrdData.PrdData item = relationPrdArr.get(j);
                    if ( idx == item.idx ) {
                        Logger.e("mItems same idx is :: " + idx + " i is " + i + " , j is " + j);
                        String isWish = "N";
                        int wishCnt = 0;
                        Logger.e("mItems before wish  :: " + item.is_wish);
                        Logger.e("mItems before wish_cnt  :: " + item.wish_cnt);
                        if ( item.is_wish == "N" ) {
                            isWish = "Y";
                            wishCnt = Integer.valueOf(item.wish_cnt) + 1;
                        } else {
                            isWish = "N";
                            wishCnt = Integer.valueOf(item.wish_cnt) - 1;
                        }
                        item.is_wish = isWish;
                        item.wish_cnt = "$wishCnt";
                        Logger.e("mItems after wish  :: " + item.is_wish);
                        Logger.e("mItems after wish_cnt  :: " + item.wish_cnt);
                        relationPrdArr.set(j, item);
                    }
                }
                relationPrd.data = relationPrdArr;
                bItem.relationPrd = relationPrd;
                mItems.set(i, bItem);
            }
        }
    }
}
