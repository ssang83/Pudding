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

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ProductViewholder viewHolder;
    public  Context context;
    private int clickedPosition = 0;
    public ProductListener productListener;
    public interface ProductListener {
        public void productClicked();
    }

    public ProductAdapter(Context context, ProductListener listener) {
        this.context = context;
        productListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_s_second_category, parent, false);
        viewHolder = new ProductViewholder(context, view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindViewHolder((ProductViewholder) holder, position);
    }

    private void bindViewHolder(ProductViewholder holder, int position) {

    }

    public void setItems(List<SecondCategoryItem> item) {

    }

    @Override
    public int getItemCount() {
//        return items.size();
        return 0;
    }

    public class ProductViewholder extends RecyclerView.ViewHolder {
        public ProductViewholder(Context context, View itemView) {
            super(itemView);
        }
    }
}
