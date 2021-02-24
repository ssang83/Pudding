package com.enliple.pudding.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.model.ReviewDetailImageModel;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class ReviewDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private int size;
    private ArrayList<ReviewDetailImageModel> mItems = new ArrayList<ReviewDetailImageModel>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        public void onItemClick(int position, ReviewDetailImageModel data);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ReviewDetailAdapter(Context context, int itemSize) {
        mContext = context;
        size = itemSize;

        ReviewDetailImageModel model = new ReviewDetailImageModel();
        model.setHeader(true);
        model.setImagePath(null);

        mItems.add(model);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_detail_item, parent, false);
        return new ViewHolder(view);
    }

    public void setItemss(List<ReviewDetailImageModel> items, boolean isHeaderRemove) {
        if (items == null)
            return;
        mItems = new ArrayList<>();
        mItems.addAll(items);
        if (!isHeaderRemove) {
            ReviewDetailImageModel model = new ReviewDetailImageModel();
            model.setHeader(true);
            model.setImagePath(null);

            mItems.add(model);
        }

        notifyDataSetChanged();
    }

    public void addItems(List<ReviewDetailImageModel> items) {
        if (mItems != null && mItems.size() > 0) {
            List<ReviewDetailImageModel> temp = new ArrayList<>();
            temp.addAll(mItems);
            temp.remove(temp.size() - 1);
            temp.addAll(items);
            if (temp.size() > 5)
                setItemss(temp, true);
            else
                setItemss(temp, false);
        }
    }

    public void removeItem(int position) {
        if (mItems != null) {
            mItems.remove(position);
            boolean isHeaderExist = false;
            for (int i = 0; i < mItems.size(); i++) {
                if (mItems.get(i).isHeader()) {
                    isHeaderExist = true;
                }
            }

            if (!isHeaderExist) {
                ReviewDetailImageModel model = new ReviewDetailImageModel();
                model.setHeader(true);
                model.setImagePath(null);

                mItems.add(model);
            }
            notifyDataSetChanged();
        }
    }

    public ArrayList<ReviewDetailImageModel> getItems() {
        if (mItems != null && mItems.size() > 0) {
            ArrayList<ReviewDetailImageModel> tempArray = new ArrayList<ReviewDetailImageModel>();
            for (int i = 0; i < mItems.size(); i++) {
                if (!mItems.get(i).isHeader()) {
                    tempArray.add(mItems.get(i));
                }
            }
            return tempArray;
        } else
            return null;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public ReviewDetailImageModel getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        bindHolder((ViewHolder) holder, position);
    }

    private void bindHolder(final ViewHolder holder, final int position) {
        final ReviewDetailImageModel data = mItems.get(position);
        if (data == null)
            return;
        Logger.e("image path :: " + data.getImagePath());
        ViewGroup.LayoutParams params = holder.root.getLayoutParams();
        params.width = size;
        params.height = size;
        holder.root.setPadding(6, 6, 6, 6);
        holder.root.setLayoutParams(params);

        holder.headerLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position, data);
                }
            }
        });

        holder.itemLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position, data);
                }
            }
        });

        if (data.isHeader()) {
            holder.headerLayer.setVisibility(View.VISIBLE);
            holder.itemLayer.setVisibility(View.GONE);
        } else {
            holder.headerLayer.setVisibility(View.GONE);
            holder.itemLayer.setVisibility(View.VISIBLE);

            ImageLoad.setImage1(mContext, holder.image, data.getImagePath(), null, ImageLoad.SCALE_CENTER_CROP, null);
        }


        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position, data);
                }
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root, headerLayer, itemLayer;
        public SelectableRoundedImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            headerLayer = itemView.findViewById(R.id.headerLayer);
            itemLayer = itemView.findViewById(R.id.itemLayer);
            image = itemView.findViewById(R.id.image);
        }
    }
}
