package com.enliple.pudding.shoppingcaster.adapter.productdialog;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.network.vo.API83;
import com.enliple.pudding.R;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class CastPurchaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<API83.ProductItem> mItems = new ArrayList<>();

    public CastPurchaseAdapter(Context context) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.cast_purchase_item, parent, false);
        return new CastPurchaseViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCastPurchaseListHolder((CastPurchaseViewHolder) holder, position);
    }

    public void setItems(List<API83.ProductItem> itemData) {
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList();
        }

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindCastPurchaseListHolder(final CastPurchaseViewHolder holder, int position) {
        API83.ProductItem item = mItems.get(position);

        holder.name.setText(item.title);
        holder.quantity.setText(item.cnt);
        holder.option.setText(item.option);
        holder.remainCnt.setText(item.it_stock_qty);

        ImageLoad.setImage(context, holder.image, item.image1, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);
    }

    static class CastPurchaseViewHolder extends RecyclerView.ViewHolder {
        private SelectableRoundedImageView image;
        private AppCompatTextView name, quantity, option, remainCnt;

        public CastPurchaseViewHolder(View itemView, Context context) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
            option = itemView.findViewById(R.id.option);
            remainCnt = itemView.findViewById(R.id.remainCnt);
        }
    }
}
