package com.enliple.pudding.adapter.my;

import android.content.Context;
import android.os.Build;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API41;
import com.enliple.pudding.widget.HTextView;

import java.util.ArrayList;
import java.util.List;

public class CustomerSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<API41.FAQList> mItems = new ArrayList<>();
    private String searchWord;

    public CustomerSearchAdapter(Context context) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_search_item, parent, false);
        return new CustomerSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCustomerSearchHolder((CustomerSearchViewHolder) holder, position);
    }

    public void setItems(List<API41.FAQList> itemData) {
        Logger.e("setItems size :: " + itemData.size());
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<API41.FAQList>();
        }

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    public void addItems(ArrayList<API41.FAQList> itemData) {
        Logger.e("addItems size :: " + itemData.size());
        if (mItems == null)
            mItems = new ArrayList<API41.FAQList>();

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindCustomerSearchHolder(final CustomerSearchViewHolder holder, int position) {
        Logger.e("bindRecentProductHolder");
        API41.FAQList model = mItems.get(position);
        String question = model.sub;
        String answer = model.con;
        int i = position + 1;
        if (TextUtils.isEmpty(searchWord))
            holder.question.setText(question);
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String spWord = "<font color='#ff6c6c'>" + searchWord + "</font>";
                question = question.replaceAll(searchWord, spWord);
                Logger.e("question :: " + question);
                holder.question.setText(Html.fromHtml(question, Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.question.setText(Html.fromHtml(question));
            }
        }
        holder.index.setText(i + ".");
        holder.answer.setHtmlText(answer, Utils.ConvertDpToPx(mContext, 30));

        holder.questionLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.answerLayer.getVisibility() == View.GONE)
                    holder.answerLayer.setVisibility(View.VISIBLE);
                else
                    holder.answerLayer.setVisibility(View.GONE);
            }
        });
    }

    static class CustomerSearchViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView question, index;
        public LinearLayout answerLayer, questionLayer;
        public HTextView answer;

        public CustomerSearchViewHolder(View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.index);
            questionLayer = itemView.findViewById(R.id.questionLayer);
            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);
            answerLayer = itemView.findViewById(R.id.answerLayer);
        }
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }
}
