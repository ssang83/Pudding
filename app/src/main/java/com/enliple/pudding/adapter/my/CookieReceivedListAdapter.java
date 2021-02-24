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

public class CookieReceivedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<API51.Data> mItems = new ArrayList<>();

    public CookieReceivedListAdapter(Context context) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.cookie_received_list_item, parent, false);
        return new CookieReceivedListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCookieReceivedListHolder((CookieReceivedListViewHolder) holder, position);
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

    private String getNumber(String number, String unit) {
        int iPrice = 0;
        try {
            iPrice = Integer.valueOf(number);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Utils.ToNumFormat(iPrice) + unit;
    }

    private void bindCookieReceivedListHolder(final CookieReceivedListViewHolder holder, int position) {
        Logger.e("bindCookiePurchaseListHolder");
        API51.Data model = mItems.get(position);
        String date = model.gf_reg_date;
//        String fanName = model.gf_to_nick;
        String cookieNo = getNumber(model.gf_quantity, "개");
        String dueDate = model.gf_expire_date;
        String fanName = getName(model.gf_to_nick, model.gf_to_id);
        holder.date.setText(date);
        holder.fanName.setText(fanName);
        holder.cookieNo.setText(cookieNo);
        holder.dueDate.setText(dueDate);
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

    static class CookieReceivedListViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView date, fanName, cookieNo, dueDate;

        public CookieReceivedListViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            fanName = itemView.findViewById(R.id.fanName);
            cookieNo = itemView.findViewById(R.id.cookieNo);
            dueDate = itemView.findViewById(R.id.dueDate);
        }
    }
}
