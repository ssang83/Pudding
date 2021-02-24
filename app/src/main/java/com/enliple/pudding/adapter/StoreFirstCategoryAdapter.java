package com.enliple.pudding.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.data.CategoryItem;
import com.enliple.pudding.commons.log.Logger;

import java.util.ArrayList;
import java.util.List;

public class StoreFirstCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CategoryItem> mItems = new ArrayList<CategoryItem>();
    private Listener listener;
    private Context context;
    private String clickedCategory = "all";
    public StoreFirstCategoryAdapter(Context context) {
        this.context = context;
    }

    @Override
    public long getItemId(int position) {
        if (hasStableIds()) {
            return position;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
        } else
            return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_first_category_item, parent, false);
        return new StoreFirstCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindFirstCategoryViewHolder((StoreFirstCategoryViewHolder) holder, position);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<CategoryItem> itemData) {
        Logger.e("setItems size :: " + itemData.size());
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<CategoryItem>();
        }

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    public void addItems(ArrayList<CategoryItem> itemData) {
        Logger.e("addItems size :: " + itemData.size());
        if (mItems == null)
            mItems = new ArrayList<CategoryItem>();

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindFirstCategoryViewHolder(final StoreFirstCategoryViewHolder holder, int position) {
        CategoryItem item = mItems.get(position);
        if ( item != null ) {
            holder.categoryName.setText(item.getCategoryName());
            ImageLoad.setImage(context, holder.categoryIcon, item.getCategoryImage(), null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);

            if ( position == mItems.size() - 1 ) {
                holder.empty.setVisibility(View.VISIBLE);
            } else {
                holder.empty.setVisibility(View.GONE);
            }

            if ( clickedCategory.equals(item.getCategoryId())) {
                holder.item.setBackgroundResource(R.drawable.store_first_category_on);
                holder.categoryName.setTextColor(Color.parseColor("#ffffff"));
            } else {
                holder.item.setBackgroundResource(R.drawable.store_first_category_off);
                holder.categoryName.setTextColor(Color.parseColor("#192028"));
            }

            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickedCategory = item.getCategoryId();
                    notifyDataSetChanged();
                    if ( listener != null ) {
                    listener.onFirstCategoryClicked(item.getCategoryId());
                    }
                }
            });
        }
    }

    static class StoreFirstCategoryViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView categoryName;
        public RelativeLayout item;
        public AppCompatImageView categoryIcon;
        public View empty;
        public StoreFirstCategoryViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            empty = itemView.findViewById(R.id.empty);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
        }
    }

    public interface Listener {
        void onFirstCategoryClicked(String categoryId);
    }
}