package com.enliple.pudding.shoppingcaster.adapter;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.data.CategoryItem;
import com.enliple.pudding.shoppingcaster.data.MainCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public Listener listener;
    private List<MainCategoryModel.CategoryItem> mItems = new ArrayList<>();
    private int clickedPosition = -1;
    private Context context;

    public interface Listener {
        public void setFirstCategory(CategoryItem firstCategory);

        public void setSecondCategory(ArrayList<CategoryItem> subCategory);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public CategorySelectAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_category_item, parent, false);
        return new CategorySelectViewHolder(view, context, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCategorySelectHolder((CategorySelectViewHolder) holder, position);
    }

    public void setItems(List<MainCategoryModel.CategoryItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);

        notifyDataSetChanged();
    }

    private void bindCategorySelectHolder(final CategorySelectViewHolder holder, int position) {
        final MainCategoryModel.CategoryItem item = mItems.get(position);
        if (item == null)
            return;
        if (item.isSelected) {
            holder.firstIcon.setBackgroundResource(R.drawable.radio_btn_on);
            holder.subLayer.setVisibility(View.VISIBLE);
        } else {
            holder.firstIcon.setBackgroundResource(R.drawable.radio_btn_off);
            holder.subLayer.setVisibility(View.GONE);
        }

        holder.subAdapter.setItems(item.sub);

        holder.firstCategoryName.setText(item.categoryName);
        holder.first_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickedPosition != position) {
                    clickedPosition = position;
                    setFirstCategory(position);
                    if (listener != null) {
                        CategoryItem c_item = new CategoryItem(0L,
                                item.categoryId,
                                item.categoryName, "", "", "");
                        listener.setFirstCategory(c_item);
                        listener.setSecondCategory(new ArrayList<CategoryItem>());
                    }
                    notifyDataSetChanged();
                }
            }
        });
    }

    static class CategorySelectViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout first_category;
        public LinearLayout subLayer;
        public AppCompatTextView firstIcon, firstCategoryName;
        public RecyclerView subRecyclerView;
        public CategorySubAdapter subAdapter;
        public LinearLayoutManager layoutManager;
        public Listener listener;

        public CategorySelectViewHolder(View itemView, Context context, Listener listener) {
            super(itemView);
            this.listener = listener;
            first_category = itemView.findViewById(R.id.first_category);
            subLayer = itemView.findViewById(R.id.subLayer);
            firstIcon = itemView.findViewById(R.id.firstIcon);
            firstCategoryName = itemView.findViewById(R.id.firstCategoryName);
            subRecyclerView = itemView.findViewById(R.id.subRecyclerView);
            layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            subRecyclerView.setHasFixedSize(false);
            subRecyclerView.setNestedScrollingEnabled(false);
            subRecyclerView.setLayoutManager(layoutManager);
            subAdapter = new CategorySubAdapter(context);
            subAdapter.setListener(new CategorySubAdapter.Listener() {
                @Override
                public void setSubCategory(ArrayList<CategoryItem> subArray) {
                    if (listener != null) {
                        listener.setSecondCategory(subArray);
                    }
                }
            });
            subRecyclerView.setAdapter(subAdapter);
        }
    }

    private void setFirstCategory(int position) {
        for (int i = 0; i < mItems.size(); i++) {
            MainCategoryModel.CategoryItem item = mItems.get(i);
            if (i == position) {
                item.isSelected = true;
            } else {
                item.isSelected = false;
            }
            List<MainCategoryModel.SubItem> list = mItems.get(i).sub;
            for (int j = 0; j < list.size(); j++) {
                MainCategoryModel.SubItem sItem = list.get(j);
                sItem.isSelected = false;
                list.set(j, sItem);
            }
            item.sub = list;
            mItems.set(i, item);
        }
    }
}
