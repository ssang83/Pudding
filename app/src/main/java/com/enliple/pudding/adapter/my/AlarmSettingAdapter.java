package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.network.vo.API65;

import java.util.ArrayList;
import java.util.List;

public class AlarmSettingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<API65.AlarmItem.FollowAlarmItem> mItems = new ArrayList<>();
    private AdapterListener mListener;
    private String status;

    public AlarmSettingAdapter(Context context) {
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_alarm_item, parent, false);
        return new AlarmSettingViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindAlarmSettingHolder((AlarmSettingViewHolder) holder, position);
    }

    public void setItems(List<API65.AlarmItem.FollowAlarmItem> itemData) {
        Logger.e("setItems size :: " + itemData.size());
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<API65.AlarmItem.FollowAlarmItem>();
        }

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindAlarmSettingHolder(final AlarmSettingViewHolder holder, int position) {
        Logger.e("bindAlarmSettingHolder");
        final API65.AlarmItem.FollowAlarmItem model = mItems.get(position);
        String image = model.mb_user_img;
        String name = model.mb_nick;
        status = model.is_alarm;

        ImageLoad.setImage(mContext, holder.image, image, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL);
        holder.name.setText(name);

        if ( position == mItems.size() - 1 )
            holder.underLine.setVisibility(View.GONE);
        else
            holder.underLine.setVisibility(View.VISIBLE);

        if (status.equals("Y"))
            holder.imgSetting.setBackgroundResource(R.drawable.switch_on);
        else
            holder.imgSetting.setBackgroundResource(R.drawable.switch_off);

        holder.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.equals("Y")) {
                    holder.imgSetting.setBackgroundResource(R.drawable.switch_off);
                    status = "N";
                } else {
                    holder.imgSetting.setBackgroundResource(R.drawable.switch_on);
                    status = "Y";
                }

                if (mListener != null) {
                    mListener.onAlarmSet(model, status);
                }
            }
        });
    }

    static class AlarmSettingViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView image, imgSetting;
        public AppCompatTextView name;
        public RelativeLayout btnSetting;
        public View underLine;

        public AlarmSettingViewHolder(View itemView, Context context) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            imgSetting = itemView.findViewById(R.id.imgSetting);
            name = itemView.findViewById(R.id.name);
            btnSetting = itemView.findViewById(R.id.btnSetting);
            underLine = itemView.findViewById(R.id.underLine);
        }
    }

    public void setListener(AdapterListener listener) {
        this.mListener = listener;
    }

    public interface AdapterListener {
        void onAlarmSet(API65.AlarmItem.FollowAlarmItem item, String followYN);
    }
}