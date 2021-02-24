package com.enliple.pudding.shoppingcaster.adapter.productdialog;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enliple.pudding.commons.network.vo.API82;
import com.enliple.pudding.R;

import java.util.ArrayList;
import java.util.List;

public class CastCookieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<API82.CookieItem> mItems = new ArrayList<>();

    public CastCookieAdapter(Context context) {
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
        } else {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cast_cookie_item, parent, false);
        return new CastPurchaseViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCastPurchaseListHolder((CastPurchaseViewHolder) holder, position);
    }

    public void setItems(List<API82.CookieItem> itemData) {
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<>();
        }

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindCastPurchaseListHolder(final CastPurchaseViewHolder holder, int position) {
        API82.CookieItem item = mItems.get(position);

        if (position < 3) {
            holder.rank.setTextColor(0xff9f56f2);
        } else {
            holder.rank.setTextColor(0xff192028);
        }

        holder.quantity.setText(item.cnt);
        holder.rank.setText("" + item.rank);
        holder.id.setText(item.mb_nick + "(" + item.mb_id + ")");
    }

    static class CastPurchaseViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView rank, id, quantity;

        public CastPurchaseViewHolder(View itemView, Context context) {
            super(itemView);

            rank = itemView.findViewById(R.id.rank);
            id = itemView.findViewById(R.id.id);
            quantity = itemView.findViewById(R.id.quantity);
        }
    }
}