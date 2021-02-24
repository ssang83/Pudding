package com.enliple.pudding.adapter;

import android.graphics.Color;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.data.CategoryItem;
import com.enliple.pudding.commons.log.Logger;

import java.util.ArrayList;
import java.util.List;

public class StoreSecondCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CategoryItem> mItems = new ArrayList<>();
    private String clickedCategory = "all";
    private Listener listener;
    public StoreSecondCategoryAdapter() {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_second_category_item, parent, false);
        return new StoreSecondCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindSecondCategoryHolder((StoreSecondCategoryViewHolder) holder, position);
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
        if ( itemData != null && itemData.size() > 0 )
            clickedCategory = itemData.get(0).getCategoryId();
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

    private void bindSecondCategoryHolder(final StoreSecondCategoryViewHolder holder, int position) {
        Logger.e("bindSecondCategoryHolder");
        CategoryItem item = mItems.get(position);
        holder.textSubcategory.setText(item.getCategoryName());

        if ( clickedCategory.equals(item.getCategoryId())) {
            holder.textSubcategory.setTextColor(Color.parseColor("#ff6c6c"));
            holder.background.setBackgroundResource(R.drawable.special_bg_selected);
        } else {
            holder.textSubcategory.setTextColor(Color.parseColor("#464646"));
            holder.background.setBackgroundResource(R.drawable.white_bg);
        }

        holder.sub_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !clickedCategory.equals(item.getCategoryId()) ) {
                    clickedCategory = item.getCategoryId();
                    notifyDataSetChanged();
                    if ( listener != null ) {
                        listener.onSecondCategoryClicked(item.getCategoryId());
                    }
                }
            }
        });
//        if (position == mItems.size() - 1 ) {
//            holder.empty.setVisibility(View.VISIBLE);
//        } else {
//            holder.empty.setVisibility(View.GONE);
//        }
    }

    static class StoreSecondCategoryViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout sub_root, background;
        public AppCompatTextView textSubcategory;
        public StoreSecondCategoryViewHolder(View itemView) {
            super(itemView);
            sub_root = itemView.findViewById(R.id.sub_root);
            background = itemView.findViewById(R.id.background);
            textSubcategory = itemView.findViewById(R.id.textSubcategory);
        }
    }

    public interface Listener {
        void onSecondCategoryClicked(String categoryId);
    }
}