package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.network.vo.API51;

import java.util.ArrayList;
import java.util.List;

public class CookiePurchaseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<API51.Data> mItems = new ArrayList<>();

    public CookiePurchaseListAdapter(Context context) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.cookie_purchase_list_item, parent, false);
        return new CookiePurchaseListViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCookiePurchaseListHolder((CookiePurchaseListViewHolder) holder, position);
    }

    public void setItems(List<API51.Data> itemData) {
        Logger.e("setItems size :: " + itemData.size());
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<API51.Data>();
        }

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private String getNumber(String price, String unit) {
        int iPrice = 0;
        try {
            iPrice = Integer.valueOf(price);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Utils.ToNumFormat(iPrice) + unit;
    }

    private void bindCookiePurchaseListHolder(final CookiePurchaseListViewHolder holder, int position) {
        Logger.e("bindCookiePurchaseListHolder");
        API51.Data model = mItems.get(position);
        String date = model.gf_reg_date;
        String quantity = getNumber(model.gf_quantity, "개");
        String means = model.gf_payment;
        String price = getNumber(model.gf_price, "원");

        holder.date.setText(date);
        holder.quantity.setText(quantity);
        holder.means.setText(means);
        holder.price.setText(price);

    }

    static class CookiePurchaseListViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView date, quantity, means, price;

        public CookiePurchaseListViewHolder(View itemView, Context context) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            quantity = itemView.findViewById(R.id.quantity);
            means = itemView.findViewById(R.id.means);
            price = itemView.findViewById(R.id.price);
        }
    }
}
