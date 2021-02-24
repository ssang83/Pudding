package com.enliple.pudding.shoppingcaster.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.network.vo.API69;
import com.enliple.pudding.R;

import java.util.ArrayList;
import java.util.List;

public class LiveStoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private AdapterListener mListener;
    private List<API69.ShopItem> mItems = new ArrayList<>();
    private int viewWidth;

    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(View view, int position);
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public LiveStoreAdapter(Context mContext) {
        this.mContext = mContext;
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewWidth = size.x - (int) (Utils.ConvertDpToPx(mContext, 15));
        Logger.e("viewWidth :: " + viewWidth);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.live_store_item, parent, false);
        return new LiveStoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        bindLiveStoreHolder((LiveStoreViewHolder) holder, position);
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
        } else {
            return 0;
        }
    }

    public API69.ShopItem getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    private void bindLiveStoreHolder(final LiveStoreViewHolder holder, final int position) {
        final API69.ShopItem data = getItemByPosition(position);
        if (data == null)
            return;
        int width = (int) ((viewWidth / 3) - Utils.ConvertDpToPx(mContext, 5));
        ViewGroup.LayoutParams params = holder.container.getLayoutParams();
        params.width = width;
        params.height = params.height;
        holder.container.setLayoutParams(params);

        ViewGroup.LayoutParams params1 = holder.image.getLayoutParams();
        width = width - Utils.ConvertDpToPx(mContext, 22);
        params1.width = width;
        params1.height = width;
        holder.image.setLayoutParams(params1);

        String imagePath = data.strImageUrl;
        String name = data.strShopName;
        holder.name.setText(name);

        RequestOptions options = new RequestOptions();
        options.centerCrop();
        Glide.with(mContext).setDefaultRequestOptions(options).asBitmap().load(imagePath).into(new BitmapImageViewTarget(holder.image) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                holder.image.setImageDrawable(circularBitmapDrawable);
            }
        });

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick != null) {
                    itemClick.onClick(v, position);
                }
            }
        });
    }

    public void setAdapterListener(AdapterListener listener) {
        mListener = listener;
    }

    public void addItems(ArrayList<API69.ShopItem> items) {
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

    public void setItems(List<API69.ShopItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);

        notifyDataSetChanged();
    }

    static class LiveStoreViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout container;
        public ImageView image;
        public AppCompatTextView name;

        public LiveStoreViewHolder(View itemView) {
            super(itemView);
            name = (AppCompatTextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
        }
    }

    public interface AdapterListener {
        /**
         * 더보기를 클릭 하였음
         */
        void onLoadMoreClicked();

        void onSendQnA(String contents);
    }
}
