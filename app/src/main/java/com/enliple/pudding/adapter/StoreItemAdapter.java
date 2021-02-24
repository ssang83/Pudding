package com.enliple.pudding.adapter;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.network.vo.API70;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class StoreItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<API70.ProductItem> mItems = new ArrayList<>();
    private int imageWidth, imageHeight, itemWidth;
    private Context context;
    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(API70.ProductItem item);
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public StoreItemAdapter(Context context) {
        this.context = context;
        int screenWidth = AppPreferences.Companion.getScreenWidth(context);
        itemWidth = (screenWidth - Utils.ConvertDpToPx(context, 24)) / 2; // recycler view 좌우 padding 값제외한 순수 item 의 width
        imageWidth = itemWidth - Utils.ConvertDpToPx(context, 8);  // item의 마진 3과 라인을 위한 좌우 패딩 1을 계산한 이미지의 width
        imageHeight = (162 * imageWidth) / 162; // image width 의 비율에 맞춘 height 값
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_product_item, parent, false);
        return new StoreFirstCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCustomerSearchHolder((StoreFirstCategoryViewHolder) holder, position);
    }

    public void addItems(List<API70.ProductItem> items) {
        if (mItems != null) {
            mItems.addAll(items);
            notifyDataSetChanged();
        }
    }

    public void setItems(List<API70.ProductItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);
        notifyDataSetChanged();
    }

    private void bindCustomerSearchHolder(final StoreFirstCategoryViewHolder holder, int position) {
        final API70.ProductItem item = mItems.get(position);
        if (item == null)
            return;

        ViewGroup.LayoutParams params = holder.main_root.getLayoutParams();
        params.width = itemWidth;
        params.height = params.height;
        holder.main_root.setLayoutParams(params);


        ViewGroup.LayoutParams param1 = holder.image.getLayoutParams();
        param1.width = imageWidth;
        param1.height = imageHeight;
        holder.image.setLayoutParams(param1);
        String name = item.title;
        String sale = "";
        if (!item.orgprice.equals(item.price)) {
            try {
                int i_origin = Integer.valueOf(item.orgprice);
                int i_saled = Integer.valueOf(item.price);

                if (i_origin == 0) {
                    sale = "";
                } else {
                    double div = i_origin - i_saled;
                    double s_rt = div / i_origin;
                    int sale_percentage = (int) (s_rt * 100);
                    if (sale_percentage <= 0)
                        sale = "";
                    else
                        sale = sale_percentage + "%";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String price = item.price;
        holder.name.setText(name);
        if (TextUtils.isEmpty(sale)) {
            holder.sale.setVisibility(View.GONE);
        } else {
            holder.sale.setVisibility(View.VISIBLE);
            holder.sale.setText(sale);
        }

        try {
            int iPrice = Integer.valueOf(item.price);
            String sPrice = Utils.ToNumFormat(iPrice);
            holder.price.setText(sPrice);
        } catch (Exception e) {
            e.printStackTrace();
            holder.price.setText(price);
        }

        ImageLoad.setImage(
                context,
                holder.image,
                item.image1,
                null,
                ImageLoad.SCALE_CENTER_CROP,
                DiskCacheStrategy.ALL);

        holder.main_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick != null) {
                    itemClick.onClick(item);
                }
            }
        });
    }

    static class StoreFirstCategoryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout main_root;
        public SelectableRoundedImageView image;
        public AppCompatTextView name, sale, price;
        public StoreFirstCategoryViewHolder(View itemView) {
            super(itemView);
            main_root = itemView.findViewById(R.id.main_root);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            sale = itemView.findViewById(R.id.sale);
            price = itemView.findViewById(R.id.price);
        }
    }
}