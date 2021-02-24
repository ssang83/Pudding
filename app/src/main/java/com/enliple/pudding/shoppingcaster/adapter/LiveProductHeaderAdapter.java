package com.enliple.pudding.shoppingcaster.adapter;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.R;
import com.enliple.pudding.shoppingcaster.data.LiveProductMainCategory;

import java.util.ArrayList;
import java.util.List;

public class LiveProductHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int HEADER_CATEGORY_SELECTED_BG_COLOR = 0xFFffebeb;
    private int HEADER_CATEGORY_NORMAL_BG_COLOR = 0xFFFFFFFF;
    private int HEADER_CATEGORY_SELECTED_TEXT_COLOR = 0xFFff6c6c;
    private int HEADER_CATEGORY_NORMAL_TEXT_COLOR = 0xFF8192a5;

    private Context mContext;
    private ArrayList<LiveProductMainCategory> mItems = new ArrayList<LiveProductMainCategory>();
    private ItemClick itemClick;
    private int clickedPosition = 0;

    public interface ItemClick {
        public void onClick(View view, int position);
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public LiveProductHeaderAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.first_category_item, parent, false);
        return new HeaderViewHolder(view);
    }

    public void setItems(List<LiveProductMainCategory> items) {
        Logger.e("setItems called");
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }
        mItems.addAll(items);
        Logger.e("mItems size :::::: " + mItems.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public LiveProductMainCategory getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        bindHeaderHolder((HeaderViewHolder) holder, position);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root;
        public AppCompatTextView headerText;
        public ImageView headerImage;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            root = (RelativeLayout) itemView.findViewById(R.id.root);
            headerImage = (ImageView) itemView.findViewById(R.id.headerImage);
            headerText = (AppCompatTextView) itemView.findViewById(R.id.headerText);
        }
    }

    private void bindHeaderHolder(final HeaderViewHolder holder, final int position) {
        Logger.e("bindViewHolder ");
        final LiveProductMainCategory data = getItemByPosition(position);
        if (data == null)
            return;
        int margin = (int) Utils.ConvertDpToPx(mContext, 0.5f);
        Logger.e("margin :: " + margin);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
        if (position % 5 == 0) {
            params.setMargins(0, margin, margin, margin);
        } else if (position % 5 == 4) {
            params.setMargins(margin, margin, 0, margin);
        } else {
            params.setMargins(margin, margin, margin, margin);
        }
        holder.root.setLayoutParams(params);

        if (clickedPosition == position) {
            holder.root.setBackgroundColor(HEADER_CATEGORY_SELECTED_BG_COLOR);
            holder.headerText.setTextColor(HEADER_CATEGORY_SELECTED_TEXT_COLOR);
        } else {
            holder.root.setBackgroundColor(HEADER_CATEGORY_NORMAL_BG_COLOR);
            holder.headerText.setTextColor(HEADER_CATEGORY_NORMAL_TEXT_COLOR);
        }

        holder.headerText.setText(data.getCategoryName());
        Logger.e("bindHeader data.getCategoryName() :: " + data.getCategoryName());
        Logger.e("bindHeader data.getCategoryImage :: " + data.getCategoryImage());
//        Glide.with(mContext)
//                .load(data.getCategoryImage())
//                .fitCenter()
//                .priority(Priority.HIGH)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(holder.headerImage);
        ImageLoad.setImage(mContext, holder.headerImage, data.getCategoryImage(), null, ImageLoad.SCALE_FIT_CENTER, DiskCacheStrategy.ALL);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(data.getCategoryName())) {
                    if (itemClick != null) {
                        itemClick.onClick(v, position);
                        clickedPosition = position;
                        notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public LiveProductMainCategory getSelectedCategory() {
        if (clickedPosition >= 0) {
            return getItemByPosition(clickedPosition);
        } else {
            return null;
        }
    }
}






