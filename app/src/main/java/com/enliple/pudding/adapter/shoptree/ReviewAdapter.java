package com.enliple.pudding.adapter.shoptree;


import android.content.Context;
import android.content.Intent;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.PhotoReviewActivity;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.network.vo.API45;
import com.enliple.pudding.fragment.shoptree.ReviewFragment;
import com.enliple.pudding.widget.ReportDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String NONE_RECOM = "0";
    private static final String RECOM = "1";
    private static final String NOT_RECOM = "2";
    private Context mContext;
    private int itemWidth;
    private List<API45.ReviewItem> mItems = new ArrayList<>();
    private OnClickListener listener;

    public interface OnClickListener {
        //        public void onRecommendClicked(String is_id, boolean isDeleteCall);
//        public void onNotRecommendClicked(String is_id, boolean isDeleteCall);
        public void onRecommendClicked(String is_id, int recomStatus, boolean isLogedInUser);

        public void onNotRecommendClicked(String is_id, int recomStatus, boolean isLogedInUser);
    }


    public ReviewAdapter(Context context, int width, OnClickListener clickListener) {
        this.mContext = context;
        itemWidth = width;
        listener = clickListener;
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindReviewHolder((ReviewViewHolder) holder, position);
    }

    public void setItems(List<API45.ReviewItem> itemData) {
        Logger.e("setItems");
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<API45.ReviewItem>();
        }
        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindReviewHolder(final ReviewViewHolder holder, int position) {
        API45.ReviewItem model = mItems.get(position);
        List<String> images = model.is_photo;
        String iconPath = model.mb_user_img;
        String name = model.mb_nick;
        String date = model.is_time;
        String option = model.ct_option;
        String content = model.is_content;
        String recommand = model.recommend;
        String notRecommand = model.not_recommend;
        String is_recommend = model.is_recommend; // 0 추천안함, 1 추천, 2 비추천
//        Glide.with(mContext)
//                .load(iconPath)
//                .placeholder(R.drawable.profile_icon_bg)
//                .bitmapTransform(new CropCircleTransformation(mContext))
//                .priority(Priority.HIGH)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(holder.image);
        ImageLoad.setImage(mContext, holder.image, iconPath, R.drawable.profile_icon_bg, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL);
        holder.name.setText(name);
        holder.date.setText(date);
        holder.option.setText(option);
        holder.content.setText(content);
        // 내추천 비추천과 상관 없이 추천은 무조건 핑그 비추천은 무조건 회색. 이다현 과장님 요청 ( 2019.06.14 )
//        if (RECOM.equals(is_recommend)) {
//            holder.strRecom.setTextColor(0xffff6c6c);
//            holder.strNotRecom.setTextColor(0xff192028);
//        } else if (NOT_RECOM.equals(is_recommend)) {
//            holder.strRecom.setTextColor(0xff192028);
//            holder.strNotRecom.setTextColor(0xffff6c6c);
//        } else {
//            holder.strRecom.setTextColor(0xff192028);
//            holder.strNotRecom.setTextColor(0xff192028);
//        }
        holder.strRecom.setText(String.format(mContext.getString(R.string.msg_recom), recommand));
        holder.strNotRecom.setText(String.format(mContext.getString(R.string.msg_not_recom), notRecommand));

        if (images != null && images.size() > 0) {
            holder.imageLayer.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams param = holder.imageLayer1.getLayoutParams();
            param.width = itemWidth;
            param.height = itemWidth;

            RelativeLayout[] arr = {holder.imageLayer1, holder.imageLayer2, holder.moreLayer};
            AppCompatImageView[] i_arr = {holder.img1, holder.img2, holder.moreBg};
            ArrayList<RelativeLayout> layoutArray = new ArrayList<RelativeLayout>();
            ArrayList<AppCompatImageView> imageArray = new ArrayList<AppCompatImageView>();

            for (int i = 0; i < arr.length; i++) {
                if (i <= images.size() - 1) {
                    final int fi = i;
                    layoutArray.add(arr[i]);
                    arr[i].setLayoutParams(param);
                    imageArray.add(i_arr[i]);
                    arr[i].setVisibility(View.VISIBLE);
                    arr[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                        mContext.startActivity(new Intent(mContext, PhotoReviewActivity.class));
                            Intent intent = new Intent(mContext, PhotoReviewActivity.class);
                            intent.putExtra("PHOTO_REVIEW", getReviewStr(model));
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    arr[i].setVisibility(View.GONE);
                }
            }

            if (images.size() >= 2) {
                holder.moreLayer.setLayoutParams(param);
                holder.moreLayer.setVisibility(View.VISIBLE);
                holder.moreLayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.e("more CLICKED");
                        Intent intent = new Intent(mContext, PhotoReviewActivity.class);
                        intent.putExtra("PHOTO_REVIEW", getReviewStr(model));
                        mContext.startActivity(intent);
                    }
                });
            }
            loadImage(imageArray, images);
        } else {
            holder.imageLayer.setVisibility(View.GONE);
        }

        holder.btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportDialog dialog = new ReportDialog(mContext, model.is_id);
                dialog.show();
            }
        });

        holder.likeLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLogin = AppPreferences.Companion.getLoginStatus(mContext);
                if (isLogin) {
                    if (!"Y".equals(model.is_mine)) {
                        if (listener != null) {
                            int status = ReviewFragment.RECOM_STATUS_NONE;
                            if (NONE_RECOM.equals(model.is_recommend))
                                status = ReviewFragment.RECOM_STATUS_NONE;
                            else {
                                if (RECOM.equals(model.is_recommend))
                                    status = ReviewFragment.RECOM_STATUS_SAME_BUTTON_CLICKED;
                                else if (NOT_RECOM.equals(model.is_recommend))
                                    status = ReviewFragment.RECOM_STATUS_DIFF_BUTTON_CLICKED;
                            }
                            listener.onRecommendClicked(model.is_id, status, true);
                        }
                    }
                } else {
                    listener.onRecommendClicked(null, -1, false);
                }
            }
        });

        holder.notLikeLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLogin = AppPreferences.Companion.getLoginStatus(mContext);
                if (isLogin) {
                    if (!"Y".equals(model.is_mine)) {
                        if (listener != null) {
                            int status = ReviewFragment.RECOM_STATUS_NONE;
                            if (NONE_RECOM.equals(model.is_recommend))
                                status = ReviewFragment.RECOM_STATUS_NONE;
                            else {
                                if (NOT_RECOM.equals(model.is_recommend))
                                    status = ReviewFragment.RECOM_STATUS_SAME_BUTTON_CLICKED;
                                else if (RECOM.equals(model.is_recommend))
                                    status = ReviewFragment.RECOM_STATUS_DIFF_BUTTON_CLICKED;
                            }
                            listener.onNotRecommendClicked(model.is_id, status, true);
                        }
                    }
                } else {
                    listener.onNotRecommendClicked(null, -1, false);
                }
            }
        });
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView image, img1, img2, moreBg;
        public AppCompatTextView name, date, option, content, strRecom, strNotRecom;
        public RelativeLayout btnReport, imageLayer1, imageLayer2, moreLayer, notLikeLayer, likeLayer;
        public LinearLayout imageLayer;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            imageLayer = itemView.findViewById(R.id.imageLayer);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.date);
            option = itemView.findViewById(R.id.option);
            content = itemView.findViewById(R.id.content);
            strRecom = itemView.findViewById(R.id.strRecom);
            strNotRecom = itemView.findViewById(R.id.strNotRecom);
            btnReport = itemView.findViewById(R.id.btnReport);
            imageLayer1 = itemView.findViewById(R.id.imageLayer1);
            imageLayer2 = itemView.findViewById(R.id.imageLayer2);
            notLikeLayer = itemView.findViewById(R.id.notLikeLayer);
            likeLayer = itemView.findViewById(R.id.likeLayer);
            moreLayer = itemView.findViewById(R.id.moreLayer);
            img1 = itemView.findViewById(R.id.img1);
            img2 = itemView.findViewById(R.id.img2);
            moreBg = itemView.findViewById(R.id.moreBg);
        }
    }

    private void loadImage(ArrayList<AppCompatImageView> iv, List<String> path) {
        for (int i = 0; i < iv.size(); i++) {
//            Glide.with(mContext)
//                    .load(path.get(i))
//                    .centerCrop()
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(iv.get(i));
            ImageLoad.setImage(mContext, iv.get(i), path.get(i), null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);
        }
    }

    private String getReviewStr(API45.ReviewItem model) {
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
        return object.toString();
    }
}