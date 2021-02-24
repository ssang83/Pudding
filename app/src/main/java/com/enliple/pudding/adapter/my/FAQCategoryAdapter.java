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
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.network.vo.API38;

import java.util.ArrayList;
import java.util.List;

public class FAQCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<API38.FAQCategoryData> mItems = new ArrayList<API38.FAQCategoryData>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        public void onItemClick(int position, API38.FAQCategoryData data);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FAQCategoryAdapter(Context context) {
        mContext = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.faq_category_item, parent, false);
        return new ViewHolder(view);
    }

    public void setItems(List<API38.FAQCategoryData> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public API38.FAQCategoryData getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        bindHolder((ViewHolder) holder, position);
    }

    private void bindHolder(final ViewHolder holder, final int position) {
        API38.FAQCategoryData data = getItemByPosition(position);
        if (data == null)
            return;

        holder.name.setText(data.subject);
//        Glide.with(mContext)
//                .load(data.img)
//                .fitCenter()
//                .priority(Priority.HIGH)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(holder.image);
        ImageLoad.setImage(mContext, holder.image, data.img, null, ImageLoad.SCALE_FIT_CENTER, DiskCacheStrategy.ALL);
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
        public RelativeLayout root;
        public AppCompatTextView name;
        public AppCompatImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
        }
    }
}
