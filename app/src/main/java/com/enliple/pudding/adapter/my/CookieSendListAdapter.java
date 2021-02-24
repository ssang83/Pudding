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

public class CookieSendListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<API51.Data> mItems = new ArrayList<>();

    public CookieSendListAdapter(Context context) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.cookie_send_list_item, parent, false);
        return new CookieSendListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCookieSendListHolder((CookieSendListViewHolder) holder, position);
    }

    public void clearItems() {
        if (mItems != null) {
            mItems.clear();
            mItems = new ArrayList<API51.Data>();
            notifyDataSetChanged();
        }
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

    private String getName(String nick, String id) {
        String str = "";
        if (id != null && id.length() > 0) {
            if (id.length() == 2 || id.length() == 3) {
                str = id.substring(0, id.length() - 1) + "*";
            } else if (id.length() == 4) {
                str = id.substring(0, id.length() - 2) + "**";
            } else {
                str = id.substring(0, id.length() - 3) + "***";
            }
        }
        str = nick + "\n" + "(" + str + ")";
        return str;
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

    private void bindCookieSendListHolder(final CookieSendListViewHolder holder, int position) {
        Logger.e("bindCookieSendListHolder");
        API51.Data model = mItems.get(position);
        String date = model.gf_reg_date;
        String bjName = getName(model.gf_to_nick, model.gf_to_id);
        String cookieNo = getNumber(model.gf_quantity, "개");

        holder.date.setText(date);
        holder.bjName.setText(bjName);
        holder.cookieNo.setText(cookieNo);

    }

    static class CookieSendListViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView date, bjName, cookieNo;

        public CookieSendListViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            bjName = itemView.findViewById(R.id.bjName);
            cookieNo = itemView.findViewById(R.id.cookieNo);
        }
    }
}
