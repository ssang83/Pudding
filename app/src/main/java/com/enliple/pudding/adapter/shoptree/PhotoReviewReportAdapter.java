package com.enliple.pudding.adapter.shoptree;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.model.PhotoReviewReportData;

import java.util.ArrayList;
import java.util.List;

public class PhotoReviewReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<PhotoReviewReportData> mItems = new ArrayList<PhotoReviewReportData>();
    private OnCallback callback;

    public interface OnCallback {
        public void onSelected(PhotoReviewReportData selected);
    }

    public PhotoReviewReportAdapter(Context mContext) {
        this.context = mContext;
    }

    public void setCallback(OnCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photoreview_report_item, parent, false);
        return new PhotoReviewReportItemHolder(context, view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindPhotoReviewReportItem((PhotoReviewReportItemHolder) holder, position);
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
            return 1;
        }
    }

    public void setItems(List<PhotoReviewReportData> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);

        notifyDataSetChanged();
    }

    public PhotoReviewReportData getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    private void bindPhotoReviewReportItem(final PhotoReviewReportItemHolder holder, final int position) {
        PhotoReviewReportData item = getItemByPosition(position);

        holder.gubun.setText(item.getGubun());
        if (item.isSelected())
            holder.imgRadio.setBackgroundResource(R.drawable.radio_btn_on);
        else
            holder.imgRadio.setBackgroundResource(R.drawable.radio_grey_off_btn);

        holder.btnRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<PhotoReviewReportData> tempArray = new ArrayList<PhotoReviewReportData>();
                for (int i = 0; i < mItems.size(); i++) {
                    PhotoReviewReportData data = mItems.get(i);
                    if (i == position) {
                        data.setSelected(true);
                        if (callback != null) {
                            callback.onSelected(data);
                        }
                    } else {
                        data.setSelected(false);
                    }
                    tempArray.add(data);
                }
                mItems.clear();
                mItems = new ArrayList<>();
                mItems.addAll(tempArray);

                notifyDataSetChanged();
            }
        });
    }


    public static class PhotoReviewReportItemHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView gubun;
        public RelativeLayout btnRadio;
        public AppCompatImageView imgRadio;

        public PhotoReviewReportItemHolder(Context context, View itemView) {
            super(itemView);
            gubun = itemView.findViewById(R.id.gubun);
            btnRadio = itemView.findViewById(R.id.btnRadio);
            imgRadio = itemView.findViewById(R.id.imgRadio);
        }
    }
}
