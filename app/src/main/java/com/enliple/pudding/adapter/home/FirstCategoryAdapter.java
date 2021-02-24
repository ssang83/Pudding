package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.model.ThreeCategoryItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

public class FirstCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public FirstViewholder viewHolder;
    public Context context;
    private List<ThreeCategoryItem> items = new ArrayList<>();
    private int clickedPosition = 0;
    public FirstCategoryListener firstListener;
    private List<Integer[]> imgs = new ArrayList<>();

    public interface FirstCategoryListener {
        public void firstCategoryClicked(String categoryId);
    }

    public FirstCategoryAdapter(Context context, FirstCategoryListener listener) {
        this.context = context;
        firstListener = listener;

        imgs.add(new Integer[]{R.drawable.item_all_on, R.drawable.item_all_off});
        imgs.add(new Integer[]{R.drawable.category_10_on, R.drawable.category_10_off});
        imgs.add(new Integer[]{R.drawable.category_20_on, R.drawable.category_20_off});
        imgs.add(new Integer[]{R.drawable.item_home_appliance_on, R.drawable.item_home_appliance_off});
        imgs.add(new Integer[]{R.drawable.item_digital_on, R.drawable.item_digital_off});
        imgs.add(new Integer[]{R.drawable.item_deco_on, R.drawable.item_deco_off});
        imgs.add(new Integer[]{R.drawable.item_baby_on, R.drawable.item_baby_off});
        imgs.add(new Integer[]{R.drawable.category_70_on, R.drawable.category_70_off});
        imgs.add(new Integer[]{R.drawable.item_sport_on, R.drawable.item_sport_off});
        imgs.add(new Integer[]{R.drawable.item_necessaries_on, R.drawable.item_necessaries_off});
        imgs.add(new Integer[]{R.drawable.item_car_on, R.drawable.item_car_off});
        imgs.add(new Integer[]{R.drawable.item_healthy_on, R.drawable.item_healthy_off});
        imgs.add(new Integer[]{R.drawable.item_book_on, R.drawable.item_book_off});
        imgs.add(new Integer[]{R.drawable.category_d0_on, R.drawable.category_d0_off});
        imgs.add(new Integer[]{R.drawable.item_stationery_on, R.drawable.item_stationery_off});
        imgs.add(new Integer[]{R.drawable.item_game_on, R.drawable.item_game_off});
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_s_first_category, parent, false);
        viewHolder = new FirstViewholder(context, view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindViewHolder((FirstViewholder) holder, position);
    }

    private void bindViewHolder(FirstViewholder holder, int position) {
        ThreeCategoryItem item = items.get(position);
        if (item.isSelected()) {
            holder.selectedLayer.setVisibility(View.VISIBLE);
            holder.unSelectedLayer.setVisibility(View.GONE);
        } else {
            holder.selectedLayer.setVisibility(View.GONE);
            holder.unSelectedLayer.setVisibility(View.VISIBLE);
        }

        ImageLoad.setImage(
                context,
                holder.selectImage,
//                item.getCategoryImageOn(),
                imgs.get(position)[0],
                null,
                ImageLoad.SCALE_CIRCLE_CROP,
                null);

        ImageLoad.setImage(
                context,
                holder.unSelectImage,
//                item.getCategoryImageOff(),
                imgs.get(position)[1],
                null,
                ImageLoad.SCALE_CIRCLE_CROP,
                null);

        holder.unSelectedLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.e("unSelectedLayer clicked");
                itemClicked(position);
            }
        });
        holder.selectedLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.e("selectedLayer clicked");
                itemClicked(position);
//                clickedPosition = position;
//                Logger.e("items size :: " + items.size());
//                for ( int i = 0 ; i < items.size() ; i ++ ) {
//                    Logger.e("clickedPosition :: " + clickedPosition);
//                    if ( i == clickedPosition ) {
//                        Logger.e("i is position :: " + i);
//                        ThreeCategoryItem tc = items.get(i);
//                        tc.setSelected(true);
//                        items.set(i, tc);
//                        if ( firstListener != null ) {
//                            firstListener.firstCategoryClicked(tc.getCategoryId());
//                        }
//                    } else {
//                        Logger.e("i else :: " + i);
//                        ThreeCategoryItem tc = items.get(i);
//                        tc.setSelected(false);
//                        items.set(i, tc);
//                    }
//                }
//                notifyDataSetChanged();
            }
        });
    }

    private void itemClicked(int position) {
        clickedPosition = position;
        Logger.e("items size :: " + items.size());
        for (int i = 0; i < items.size(); i++) {
            Logger.e("clickedPosition :: " + clickedPosition);
            if (i == clickedPosition) {
                Logger.e("i is position :: " + i);
                ThreeCategoryItem tc = items.get(i);
                tc.setSelected(true);
                items.set(i, tc);
                if (firstListener != null) {
                    firstListener.firstCategoryClicked(tc.getCategoryId());
                }
            } else {
                Logger.e("i else :: " + i);
                ThreeCategoryItem tc = items.get(i);
                tc.setSelected(false);
                items.set(i, tc);
            }
        }
        notifyDataSetChanged();
    }

    public void setItems(List<ThreeCategoryItem> item) {
        if (items != null) {
            items.clear();
        }
        clickedPosition = 0;
        items.addAll(item);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class FirstViewholder extends RecyclerView.ViewHolder {
        public RelativeLayout selectedLayer, unSelectedLayer;
        public AppCompatImageView selectImage, unSelectImage;

        public FirstViewholder(Context context, View itemView) {
            super(itemView);
            selectedLayer = itemView.findViewById(R.id.selectedLayer);
            unSelectedLayer = itemView.findViewById(R.id.unSelectedLayer);
            selectImage = itemView.findViewById(R.id.selectImage);
            unSelectImage = itemView.findViewById(R.id.unSelectImage);
        }
    }
}
