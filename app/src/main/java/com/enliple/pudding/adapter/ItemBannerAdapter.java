package com.enliple.pudding.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API70;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class ItemBannerAdapter extends PagerAdapter {
    private Context context = null ;
    private List<API70.EventItem> items = new ArrayList<>();
    private LayoutInflater inflater;
    private Listener listener;
    private BannerViewHolder holder;
    private int bannerWidth = 0;
    private int bannerHeight = 0;

    public interface Listener {
        public void onBannerClicked(API70.EventItem banner);
    }
    public ItemBannerAdapter(Context context, Listener listener) {
        this.context = context ;
        this.listener = listener;
        inflater = LayoutInflater.from(context);
        bannerWidth = AppPreferences.Companion.getScreenWidth(context) - Utils.ConvertDpToPx(context, 30);
        bannerHeight = (bannerWidth * 195) / 330;
    }

    public int getItemSize() {
        if ( this.items != null ) {
            return items.size();
        } else {
            return 0;
        }
    }

    public void setItems(List<API70.EventItem> items) {
        Logger.e("TravelBannerAdapter setItems ");
        if (this.items != null) {
            this.items.clear();
        }
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Logger.e("instantiateItem");
        View view = null;

        if ( view != null ) {
            holder = (BannerViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.travel_banner_item, container, false);
            holder = new BannerViewHolder(view);
            view.setTag(holder);
        }

        API70.EventItem item = items.get(position);
        if ( item != null ) {
            ViewGroup.LayoutParams param = holder.image.getLayoutParams();
            param.width = bannerWidth;
            param.height = bannerHeight;
            holder.image.setLayoutParams(param);

            Glide.with(context)
                    .load(item.main_img)
                    .apply(RequestOptions.centerCropTransform())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(holder.image);

            // holder.image 에 corner round 주기 위해
            GradientDrawable drawable = (GradientDrawable) context.getDrawable(R.drawable.image_corner);
            holder.image.setBackground(drawable);
            holder.image.setClipToOutline(true);

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( listener != null )
                        listener.onBannerClicked(item);
                }
            });
        }

        // 뷰페이저에 추가.
        container.addView(view) ;

        return view ;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void setPagerIndicatorState(int position) {
        if ( holder != null ) {
            for (int i = 0; i < items.size(); i++) {
                if (i == position) {
                    holder.indicator.getChildAt(i).setSelected(true);
                } else {
                    holder.indicator.getChildAt(i).setSelected(false);
                }
            }
        }
    }

    private void setPagerIndicator(int pageCount) {
        if ( holder.indicator.getChildCount() > 0 ) {
            holder.indicator.removeAllViews();
        }
        for (int i = 0; i < pageCount; i++) {
            RadioButton dot = new RadioButton(context);
            dot.setButtonDrawable(R.drawable.dot_selector);
            dot.setId(i);
            holder.indicator.addView(dot);

            if ( i != 0 ) {
                RadioGroup.LayoutParams param = (RadioGroup.LayoutParams) dot.getLayoutParams();
                param.leftMargin = Utils.ConvertDpToPx(context, 3);
                dot.setLayoutParams(param);
            }
        }
    }

    public class BannerViewHolder {
        public  AppCompatImageView image;
        public RadioGroup indicator;
        public BannerViewHolder(View view) {
            image = view.findViewById(R.id.image);
            indicator = view.findViewById(R.id.indicator);
        }
    }
}
