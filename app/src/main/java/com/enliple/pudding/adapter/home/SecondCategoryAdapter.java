package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.model.SecondCategoryItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class SecondCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public SecondViewholder viewHolder;
    public  Context context;
    private List<SecondCategoryItem> items = new ArrayList<>();
    private int clickedPosition = 0;
    public SecondCategoryListener secondListener;
    public interface SecondCategoryListener {
        public void secondCategoryClicked(String categoryId, String categoryName);
    }

    public SecondCategoryAdapter(Context context, SecondCategoryListener listener) {
        this.context = context;
        secondListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_s_second_category, parent, false);
        viewHolder = new SecondViewholder(context, view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindViewHolder((SecondViewholder) holder, position);
    }

    private void bindViewHolder(SecondViewholder holder, int position) {
        SecondCategoryItem item = items.get(position);
        holder.secondCategory.setText(item.getCategoryName());
        if ( item.isSelected() ) {
            holder.secondCategory.setBackgroundResource(R.drawable.sub_category_selected);
            holder.secondCategory.setTextColor(0xFFffffff);
        } else {
            holder.secondCategory.setBackgroundResource(R.drawable.sub_category_unselected);
            holder.secondCategory.setTextColor(0xFF202c37);
        }
        holder.secondCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedPosition = position;
                Logger.e("second items size :: " + items.size());
                for ( int i = 0 ; i < items.size() ; i ++ ) {
                    Logger.e("clickedPosition :: " + clickedPosition);
                    if ( i == clickedPosition ) {
                        Logger.e("i is position :: " + i);
                        SecondCategoryItem tc = items.get(i);
                        tc.setSelected(true);
                        items.set(i, tc);
                        if ( secondListener != null ) {
                            secondListener.secondCategoryClicked(tc.getCategoryId(), tc.getCategoryName());
                        }
                    } else {
                        Logger.e("i else :: " + i);
                        SecondCategoryItem tc = items.get(i);
                        tc.setSelected(false);
                        items.set(i, tc);
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    public void setItems(List<SecondCategoryItem> item) {
        Logger.e("SecondCategoryAdapter setItems");
        if ( items != null ) {
            items.clear();
        }
        clickedPosition = 0;
        if ( item != null ) {
            for ( int i = 0 ; i < item.size() ; i ++ ) {
                SecondCategoryItem temp = item.get(i);
                if ( i == 0 ) {
                    temp.setSelected(true);
                    item.set(i, temp);
                } else {
                    temp.setSelected(false);
                    item.set(i, temp);
                }
            }
            items.addAll(item);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class SecondViewholder extends RecyclerView.ViewHolder {
        public AppCompatTextView secondCategory;
        public RelativeLayout root;
        public SecondViewholder(Context context, View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            secondCategory = itemView.findViewById(R.id.secondCategory);
            secondCategory.setElevation(10);
        }
    }
}
