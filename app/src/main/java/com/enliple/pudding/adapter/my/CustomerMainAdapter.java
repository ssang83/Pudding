package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.vo.API41;
import com.enliple.pudding.widget.HTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class CustomerMainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int MODE_PURCHASE_LIST = 0;
    public static final int MODE_SEND_LIST = 1;
    public static final int MODE_RECEIVED_LIST = 2;
    private Context mContext;
    private List<API41.FAQList> mItems = new ArrayList<>();
    private List<Boolean> closeItem = new ArrayList<>();

    public CustomerMainAdapter(Context context) {
        this.mContext = context;
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.customer_main_item, parent, false);
        return new CustomerMainViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCustomerMainHolder((CustomerMainViewHolder) holder, position);
    }

    public void setItems(List<API41.FAQList> itemData) {
        Logger.e("setItems size :: " + itemData.size());
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<API41.FAQList>();
        }

        if (closeItem != null && closeItem.size() > 0) {
            closeItem.clear();
        } else {
            closeItem = new ArrayList<>();
        }

        for (int i = 0; i < itemData.size(); i++) {
            closeItem.add(false);
        }

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindCustomerMainHolder(final CustomerMainViewHolder holder, int position) {
        Logger.e("bindRecentProductHolder");
        API41.FAQList model = mItems.get(position);
        int i_position = position + 1;
        String question = i_position + ". " + model.sub;
        String answer = model.con;

        holder.question.setText(question);
//        holder.answer.setText(answer);
        holder.answer.setHtmlText(answer, Utils.ConvertDpToPx(mContext, 30));
        Logger.e("closeItem value :: " + closeItem.get(position));
        if (closeItem.get(position)) {
            holder.answerLayer.setVisibility(View.VISIBLE);
        } else {
            holder.answerLayer.setVisibility(View.GONE);
        }

        holder.question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean setValue = false;
                if ( holder.answerLayer.getVisibility() == View.GONE ) {
                    setValue = true;

                    EventBus.getDefault().post(new NetworkBus(NetworkApi.API123.name(), "faq", model.id));
                } else {
                    setValue = false;
                }
                Logger.e("setValue :: " + setValue);
                if ( closeItem != null ) {
                    closeItem.clear();
                    closeItem = new ArrayList<>();
                }
                for (int i = 0; i < mItems.size(); i++) {
                    if ( i == position ) {
                        Logger.e("change Position :: " + i + " , val :: " + setValue);
                        closeItem.add(setValue);
                    } else {
                        Logger.e("false change Position :: " + i);
                        closeItem.add(false);
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    static class CustomerMainViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView question;
        public HTextView answer;
        public LinearLayout answerLayer;

        public CustomerMainViewHolder(View itemView, Context context) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);
            answerLayer = itemView.findViewById(R.id.answerLayer);
        }
    }
}
