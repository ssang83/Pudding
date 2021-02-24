package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.model.SecondCategoryItem;
import com.enliple.pudding.model.ThirdCategoryItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class ThirdCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ThirdViewholder viewHolder;
    public  Context context;
    private List<ThirdCategoryItem> items = new ArrayList<>();
    private int clickedPosition = 0;
    public ThirdCategoryListener thirdListener;
    public interface ThirdCategoryListener {
        public void thirdCategoryClicked(String categoryId);
    }

    public ThirdCategoryAdapter(Context context, ThirdCategoryListener listener) {
        this.context = context;
        thirdListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_s_third_category, parent, false);
        viewHolder = new ThirdViewholder(context, view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindViewHolder((ThirdViewholder) holder, position);
    }

    private void bindViewHolder(ThirdViewholder holder, int position) {
        ThirdCategoryItem item = items.get(position);
        holder.thirdCategory.setText(item.getCategoryName());
        if ( item.isSelected() ) {
            holder.thirdCategory.setTextColor(0xFF9f56f2);
            holder.thirdIco.setVisibility(View.VISIBLE);
        } else {
            holder.thirdCategory.setTextColor(0xFF202c37);
            holder.thirdIco.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedPosition = position;
                Logger.e("items size :: " + items.size());
                for ( int i = 0 ; i < items.size() ; i ++ ) {
                    Logger.e("clickedPosition :: " + clickedPosition);
                    if ( i == clickedPosition ) {
                        Logger.e("i is position :: " + i);
                        ThirdCategoryItem tc = items.get(i);
                        tc.setSelected(true);
                        items.set(i, tc);
                        if ( thirdListener != null ) {
                            thirdListener.thirdCategoryClicked(tc.getCategoryId());
                        }
                    } else {
                        Logger.e("i else :: " + i);
                        ThirdCategoryItem tc = items.get(i);
                        tc.setSelected(false);
                        items.set(i, tc);
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    public void setItems(List<ThirdCategoryItem> item) {

        if ( items != null ) {
            items.clear();
        }
        clickedPosition = 0;
        if ( item != null ) {
            for ( int i = 0 ; i < item.size() ; i ++ ) {
                ThirdCategoryItem temp = item.get(i);
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

    public class ThirdViewholder extends RecyclerView.ViewHolder {
        public AppCompatTextView thirdCategory;
        public AppCompatImageView thirdIco;
        public ThirdViewholder(Context context, View itemView) {
            super(itemView);
            thirdCategory = itemView.findViewById(R.id.thirdCategory);
            thirdIco = itemView.findViewById(R.id.thirdIco);
        }
    }
}
