package com.enliple.pudding.adapter.my;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.model.ExchangeListModel;

import java.util.ArrayList;
import java.util.List;

public class ExchangeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private boolean isJelly = false;
    public ExchangeListAdapter(Context context, boolean isJelly) {
        this.context = context;
        this.isJelly = isJelly;
    }
    public List<ExchangeListModel> items = new ArrayList<>();
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exchage_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindHolder((ViewHolder) holder, position);
    }

    private void bindHolder(final ViewHolder holder, final int position) {
        ExchangeListModel model = items.get(position);
        if ( model == null )
            return;
        String price = model.getPoint();
        String exchangedPrice = model.getPrice();
        String date = model.getReg_date();
        String status = model.getStatus();
        try {
            double dPrice = Double.valueOf(price);
            double dExchangedPrice = Double.valueOf(exchangedPrice);
            price = Utils.ToNumFormat(dPrice);
            exchangedPrice = Utils.ToNumFormat(dExchangedPrice);
            date = date.replaceAll(" ", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.e("price :: " + price);
        Logger.e("exchangedPrice :: " + exchangedPrice);
        Logger.e("date :: " + date);
        Logger.e("status :: " + status);
        holder.price.setText(price);
        holder.exchangePrice.setText(exchangedPrice);
        holder.date.setText(date);
        holder.status.setText(status);
    }


    public void setItems(List<ExchangeListModel> it) {
        if (items == null) {
            items = new ArrayList<>();
        } else {
            items.clear();
        }
        Logger.e("items size :: " + items.size());
        items.addAll(it);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
//        return mItems.size();
        if ( items != null && items.size() > 0 )
            return items.size();
        else
            return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView date, status, price, exchangePrice;

        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            status = itemView.findViewById(R.id.status);
            price = itemView.findViewById(R.id.price);
            exchangePrice = itemView.findViewById(R.id.exchangePrice);
        }
    }
}
