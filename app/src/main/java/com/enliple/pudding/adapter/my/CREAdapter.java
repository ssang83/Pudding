package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.shoptree.data.CREData;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kim Joonsung on 2018-09-21.
 */

public class CREAdapter extends RecyclerView.Adapter<CREAdapter.CREViewHolder> {

    private Context context;
    private CREAdapterListener mListener;
    private List<CREData> mItems;

    public interface CREAdapterListener {
        void onDetailClcik(String idx);
    }

    public CREAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public CREViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return CREViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(CREViewHolder holder, int position) {
        bindCREItemHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return (mItems == null || mItems.size() == 0)
                ? 0
                : mItems.size();
    }

    /**
     * Adapter 에서 발생되는 EventListener 를 설정
     *
     * @param listener
     */
    public void setAdapterListener(CREAdapterListener listener) {
        this.mListener = listener;
    }

    public void addItems(List<CREData> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        }

        mItems.addAll(items);

        notifyDataSetChanged();
    }

    private void bindCREItemHolder(final CREViewHolder holder, final int position) {
        final CREData data = mItems.get(position);

        holder.itemView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mListener != null) {
                    mListener.onDetailClcik(data.ct_id);
                }
            }
        });

        if (data != null) {
            holder.textViewStatus.setText(data.ct_status);
            holder.textViewTitle.setText(data.product_name);
            holder.textViewOption.setText(data.option);
            holder.textViewShopName.setText(data.store_name);

            // 2018-12-06 18:33:48
            String[] array = data.complaints_date.split(" ");
            String date = array[0];
            String time = array[1];
            String[] aDate = date.split("-");
            String[] aTime = time.split(":");
            String fTime = aDate[0] + "년 " + aDate[1] + "월 " + aDate[2] + "일" + "(" + aTime[0] + ":" + aTime[1] + ")";
            holder.textViewRequestDate.setText(fTime);

            ImageLoad.setImage(holder.itemView.getContext(), holder.imageViewThumbnail, data.it_img1, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL);
        }
    }

    static class CREViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView imageViewThumbnail;
        private AppCompatImageView imageViewProductType;
        private AppCompatTextView textViewShopName;
        private AppCompatTextView textViewOption;
        private AppCompatTextView textViewTitle;
        private AppCompatTextView textViewStatus;
        private AppCompatTextView textViewRequestDate;

        public static CREViewHolder newInstance(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_cre_item, parent, false);
            return new CREViewHolder(v);
        }

        private CREViewHolder(View itemView) {
            super(itemView);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);
            imageViewProductType = itemView.findViewById(R.id.imageViewProductType);
            textViewShopName = itemView.findViewById(R.id.textViewShopName);
            textViewOption = itemView.findViewById(R.id.textViewOption);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewRequestDate = itemView.findViewById(R.id.textViewRequestDate);
        }
    }
}
