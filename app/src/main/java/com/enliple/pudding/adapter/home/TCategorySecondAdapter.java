package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.shoppingcaster.data.MainCategoryModel;

import java.util.ArrayList;
import java.util.List;

import com.enliple.pudding.commons.data.CategoryItem;

public class TCategorySecondAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public Listener listener;
    private List<MainCategoryModel.SubItem> mItems = new ArrayList<>();
    private int clickedPosition = -1;
    private Context context;

    public interface Listener {
        public void setSecondCategory(CategoryItem secondCategory);

        public void setThirdCategory(ArrayList<CategoryItem> thirdCategorys);
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public TCategorySecondAdapter(Context context, Listener listener) {
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

    public void setItems(List<MainCategoryModel.SubItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_category_item, parent, false);
        return new SecondCategorySelectViewHolder(view, context, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindSecondCategorySelectHolder((SecondCategorySelectViewHolder) holder, position);
    }

    private void bindSecondCategorySelectHolder(final SecondCategorySelectViewHolder holder, int position) {
        final MainCategoryModel.SubItem item = mItems.get(position);
        if (item == null)
            return;
//        holder.subLayer.setBackgroundColor(Color.parseColor("#ffffff"));
        holder.botLine.setVisibility(View.GONE);
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
                    setSecondCategory(position);
                    if (listener != null) {
                        CategoryItem c_item = new CategoryItem(0L,
                                item.categoryId,
                                item.categoryName, "", "", "");
                        listener.setSecondCategory(c_item);
                        listener.setThirdCategory(new ArrayList<CategoryItem>());
                    }
                    notifyDataSetChanged();
                }
            }
        });
    }

    static class SecondCategorySelectViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout first_category;
        public LinearLayout subLayer;
        public AppCompatTextView firstIcon, firstCategoryName;
        public RecyclerView subRecyclerView;
        public View botLine;
        public TCategoryThirdAdapter subAdapter;
        public LinearLayoutManager layoutManager;
        public Listener listener;

        public SecondCategorySelectViewHolder(View itemView, Context context, Listener listener) {
            super(itemView);
            this.listener = listener;
            first_category = itemView.findViewById(R.id.first_category);
            botLine = itemView.findViewById(R.id.botLine);
            subLayer = itemView.findViewById(R.id.subLayer);
            firstIcon = itemView.findViewById(R.id.firstIcon);
            firstCategoryName = itemView.findViewById(R.id.firstCategoryName);
            subRecyclerView = itemView.findViewById(R.id.subRecyclerView);
            layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            subRecyclerView.setHasFixedSize(false);
            subRecyclerView.setNestedScrollingEnabled(false);
            subRecyclerView.setLayoutManager(layoutManager);
            subAdapter = new TCategoryThirdAdapter(context, new TCategoryThirdAdapter.Listener() {
                @Override
                public void setThirdCategory(ArrayList<CategoryItem> thirdCategories) {
                    if ( listener != null ) {
                        listener.setThirdCategory(thirdCategories);
                    }
                }
            });
            subRecyclerView.setAdapter(subAdapter);
        }
    }

    private void setSecondCategory(int position) {
        for (int i = 0; i < mItems.size(); i++) {
            MainCategoryModel.SubItem item = mItems.get(i);
            if (i == position) {
                item.isSelected = true;
            } else {
                item.isSelected = false;
            }
            List<MainCategoryModel.ThirdItem> list = mItems.get(i).sub;
            for (int j = 0; j < list.size(); j++) {
                MainCategoryModel.ThirdItem sItem = list.get(j);
                sItem.isSelected = false;
                list.set(j, sItem);
            }
            item.sub = list;
            mItems.set(i, item);
        }
    }
}
