package com.enliple.pudding.adapter.shoptree;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.PhotoReviewActivity;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API46;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PhotoReviewMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private int itemWidth;
    private List<API46.ReviewItem> mItems = new ArrayList<>();

    public PhotoReviewMoreAdapter(Context context, int width) {
        this.mContext = context;
        itemWidth = width;
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_review_more_item, parent, false);
        return new ReviewMoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindReviewMoreHolder((ReviewMoreViewHolder) holder, position);
    }

    public void setItems(List<API46.ReviewItem> itemData) {
        if (mItems.size() > 0)
            mItems.clear();
        else
            mItems = new ArrayList<API46.ReviewItem>();

        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindReviewMoreHolder(final ReviewMoreViewHolder holder, int position) {
        API46.ReviewItem model = mItems.get(position);
        String path = model.photo_thumb;
        Logger.e("itemWidth :: " + itemWidth + " , path :: " + path);
        ViewGroup.LayoutParams param = holder.image.getLayoutParams();
        param.width = itemWidth;
        param.height = itemWidth;
        holder.image.setLayoutParams(param);

//        Glide.with(mContext)
//                .load(path)
//                .priority(Priority.HIGH)
//                .centerCrop()
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(holder.image);
        ImageLoad.setImage(mContext, holder.image, path, null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model != null) {
                    String result;
                    JSONObject object = new JSONObject();
                    try {
                        object.put("is_id", model.is_id);
                        object.put("ct_id", model.ct_id);
                        object.put("is_type", model.is_type);
                        object.put("is_score", model.is_score);
                        object.put("is_subject", model.is_subject);
                        object.put("is_content", model.is_content);
                        object.put("is_time", model.is_time);
                        object.put("mb_id", model.mb_id);
                        object.put("mb_nick", model.mb_nick);
                        object.put("mb_user_img", model.mb_user_img);
                        object.put("ct_option", model.ct_option);
                        object.put("recommend", model.recommend);
                        object.put("not_recommend", model.not_recommend);
                        object.put("is_mine", model.is_mine);
                        object.put("is_recommend", model.is_recommend);

                        JSONArray array = new JSONArray();
                        if (model.is_photo != null && model.is_photo.size() > 0) {
                            for (int i = 0; i < model.is_photo.size(); i++) {
                                array.put(model.is_photo.get(i));
                            }
                            object.put("is_photo", array);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result = object.toString();
                    Intent intent = new Intent(mContext, PhotoReviewActivity.class);
                    intent.putExtra("PHOTO_REVIEW", result);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    static class ReviewMoreViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView image;

        public ReviewMoreViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }
}
