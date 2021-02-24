package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.LoginActivity;
import com.enliple.pudding.adapter.ItemBannerAdapter;
import com.enliple.pudding.adapter.TravelBannerAdapter;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API114;
import com.enliple.pudding.commons.network.vo.API70;
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.SecondCategoryItem;
import com.enliple.pudding.model.ThirdCategoryItem;
import com.enliple.pudding.model.ThreeCategoryItem;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_BANNER = 0X1000;
    private static final int TYPE_FIRST_CATEGORY = 0X1001;
    private static final int TYPE_SECOND_CATEGORY = 0X2001;
    private static final int TYPE_THIRD_CATEGORY = 0X3001;
    private static final int TYPE_SORT = 0X4001;
    private static final int TYPE_ITEM = 0X5001;

    public BannerViewHolder bannerViewHolder;
    public ItemFirstCategoryViewHolder itemFirstCategoryViewHolder;
    public ItemSecondCategoryViewHolder itemSecondCategoryViewHolder;
    public ItemThirdCategoryViewHolder itemThirdCategoryViewHolder;
    public ItemSortViewHolder itemSortViewHolder;
    public ItemViewHolder itemViewHolder;
    public ItemBannerAdapter mBannerAdapter;

    private Context context;
    private List<API70.ProductItem> mItems = new ArrayList<>();
    private List<API70.EventItem> mEItems = new ArrayList<>();
    private List<ThreeCategoryItem> categoryItems = new ArrayList<>();
    public Listener listener;

    private boolean priceStatus = false; // false 낮은 가격
    private String selectedStatus = "1";
    private String selectedMainStatus = "";
    private String selectedFirstCategory = null;
    private String selectedSecondCategory = null;
    private String selectedThirdCategory = null;
    private boolean isFromLive = false;
    private boolean isFromStore = false;
    public int pagerHeight = 0;

    public interface Listener {
        public void bannerClicked(API70.EventItem item);
        public void categoryClicked(String categoryId);
        public void onSortClicked(String od);
        public void onSort2Clicked(String od);
        public void onItemClick(API70.ProductItem item);
        public void onLikeClick(API70.ProductItem item, boolean status);
    }

    public ItemAdapter(Context context, boolean isFromLive, boolean isFromStore, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.isFromLive = isFromLive;
        this.isFromStore = isFromStore;
        int bannerWidth = AppPreferences.Companion.getScreenWidth(context) - Utils.ConvertDpToPx(context, 30);
        pagerHeight = (bannerWidth * 195) / 330;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if ( viewType == TYPE_BANNER ) {
            View view = LayoutInflater.from(context).inflate(R.layout.travel_banner_items, parent, false);
            bannerViewHolder = new BannerViewHolder(context, view);
            return bannerViewHolder;
        }else if (viewType == TYPE_FIRST_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_first_category, parent, false);
            itemFirstCategoryViewHolder = new ItemFirstCategoryViewHolder(context, view);
            return itemFirstCategoryViewHolder;
        } else if (viewType == TYPE_SECOND_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_second_category, parent, false);
            itemSecondCategoryViewHolder = new ItemSecondCategoryViewHolder(context, view);
            return itemSecondCategoryViewHolder;
        }
        if (viewType == TYPE_THIRD_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_third_category, parent, false);
            itemThirdCategoryViewHolder = new ItemThirdCategoryViewHolder(context, view);
            return itemThirdCategoryViewHolder;
        }
        if (viewType == TYPE_SORT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_s_sort, parent, false);
            itemSortViewHolder = new ItemSortViewHolder(context, view);
            return itemSortViewHolder;
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_items, parent, false);
            itemViewHolder = new ItemViewHolder(context, view);
            return itemViewHolder;
        }
    }

    public void setCategoryItem(ArrayList<ThreeCategoryItem> items) {
        if (categoryItems != null)
            categoryItems.clear();
        Logger.e("items. size :: " + items.size());
        for (int i = 0; i < items.size(); i++) {
            Logger.e("items. val :: " + items.get(i).getCategoryName());
        }
        categoryItems.addAll(items);
        notifyDataSetChanged();
    }

    public void clearItems() {
        Logger.e("clearItems");
        if (mItems != null) {
            mItems.clear();
            mItems = new ArrayList<>();
        }
    }

    public void addItems(List<API70.ProductItem> items) {
        Logger.e("addItems");
        if (mItems != null) {
            mItems.addAll(items);
            notifyDataSetChanged();
        }
    }

    public void setItems(List<API70.ProductItem> items, List<API70.EventItem> eItems, int count) {
        Logger.e("setItems");
        if (itemSortViewHolder.count != null) {
            String strCount = Utils.ToNumFormat(count);
            itemSortViewHolder.count.setText(strCount);
        }

        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);

        if ( mEItems == null) {
            mEItems = new ArrayList<>();
        } else {
            mEItems.clear();
        }

        mEItems.addAll(eItems);

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if ( isFromStore || isFromLive) {
            if ( position == 0 ) {
                return TYPE_FIRST_CATEGORY;
            } if (position == 1) {
                return TYPE_SECOND_CATEGORY;
            } else if (position == 2) {
                return TYPE_THIRD_CATEGORY;
            } else if (position == 3) {
                return TYPE_SORT;
            } else {
                return TYPE_ITEM;
            }
        } else {
            if ( position == 0 ) {
                return TYPE_BANNER;
            } if (position == 1) {
                return TYPE_FIRST_CATEGORY;
            } else if (position == 2) {
                return TYPE_SECOND_CATEGORY;
            } else if (position == 3) {
                return TYPE_THIRD_CATEGORY;
            } else if (position == 4) {
                return TYPE_SORT;
            } else {
                return TYPE_ITEM;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if ( holder instanceof BannerViewHolder ) {
            bindBannerViewHolder( (BannerViewHolder) holder);
        } else if (holder instanceof ItemFirstCategoryViewHolder) {
            bindFirstCategoryViewHolder((ItemFirstCategoryViewHolder) holder);
        } else if (holder instanceof ItemSecondCategoryViewHolder) {
            bindSecondCategoryViewHolder((ItemSecondCategoryViewHolder) holder);
        } else if (holder instanceof ItemThirdCategoryViewHolder) {
            bindThirdCategoryViewHolder((ItemThirdCategoryViewHolder) holder);
        } else if (holder instanceof ItemSortViewHolder) {
            bindSortViewHolder((ItemSortViewHolder) holder);
        } else {
            if ( isFromStore || isFromLive) {
                bindItemViewHolder((ItemViewHolder) holder, position - 4);
            } else {
                bindItemViewHolder((ItemViewHolder) holder, position - 5);
            }
        }
    }

    public void setPagerIndicatorState(BannerViewHolder holder, int position) {
        try {
            if (holder != null) {
                for (int i = 0 ; i < mEItems.size() ; i ++ ) {
                    if (i == position) {
                        holder.indicator.getChildAt(i).setSelected(true);
                    } else {
                        holder.indicator.getChildAt(i).setSelected(false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPagerIndicator(BannerViewHolder holder, int pageCount) {
        if (holder.indicator.getChildCount() > 0) {
            holder.indicator.removeAllViews();
        }
        for (int i = 0 ; i < pageCount ; i ++ ) {
            RadioButton dot = new RadioButton(context);
            dot.setButtonDrawable(R.drawable.dot_selector);
            dot.setId(i);
            holder.indicator.addView(dot);

            RadioGroup.LayoutParams param = (RadioGroup.LayoutParams)dot.getLayoutParams();
            param.width = Utils.ConvertDpToPx(context, 12);
            param.height = Utils.ConvertDpToPx(context, 12);
            dot.setLayoutParams(param);
        }
    }

    private void bindBannerViewHolder(BannerViewHolder holder) {
        ViewGroup.LayoutParams pr = holder.bannerPager.getLayoutParams();
        pr.width = AppPreferences.Companion.getScreenWidth(context);
        pr.height = pagerHeight;
        holder.bannerPager.setLayoutParams(pr);

        mBannerAdapter = new ItemBannerAdapter(context, new ItemBannerAdapter.Listener() {
            @Override
            public void onBannerClicked(API70.EventItem banner) {
                if ( listener != null )
                    listener.bannerClicked(banner);
            }
        });

        setPagerIndicator(holder, mEItems.size());

        holder.bannerPager.setAdapter(mBannerAdapter);
        holder.bannerPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setPagerIndicatorState(holder, position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mBannerAdapter.setItems(mEItems);
    }

    private void bindFirstCategoryViewHolder(ItemFirstCategoryViewHolder holder) {
        if (holder.firstAdapter != null) {
            Logger.e("holder.first adapter not null");
            Logger.e("categoryItems.size :: " + categoryItems.size());
            holder.firstAdapter.setItems(categoryItems);
        } else {
            Logger.e("holder.first adapter null");
        }
    }

    private void bindSecondCategoryViewHolder(ItemSecondCategoryViewHolder holder) {
        Logger.e("bindSecondCategoryViewHolder");
    }

    private void bindThirdCategoryViewHolder(ItemThirdCategoryViewHolder holder) {
        Logger.e("bindThirdCategoryViewHolder");
    }

    private void bindSortViewHolder(ItemSortViewHolder holder) {

        if ( isFromStore ) {
            holder.topLine.setVisibility(View.GONE);
            holder.cate_1.setVisibility(View.GONE);
        } else {
            holder.topLine.setVisibility(View.VISIBLE);
            holder.cate_1.setVisibility(View.VISIBLE);
        }

        holder.btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(selectedMainStatus)) {
                    if (listener != null) {
                        selectedMainStatus = "";
                        listener.onSort2Clicked(selectedMainStatus);
                        holder.btnAll.setBackgroundResource(R.drawable.main_cat_sel);
                        holder.btnPudding.setBackgroundResource(R.drawable.main_cat_default);
                        holder.btnLink.setBackgroundResource(R.drawable.main_cat_default);

                        holder.strAll.setTextColor(Color.parseColor("#9f56f2"));
                        holder.strPudding.setTextColor(Color.parseColor("#bcc6d2"));
                        holder.strLink.setTextColor(Color.parseColor("#bcc6d2"));

                        holder.iconAll.setBackgroundResource(R.drawable.check_circle_on);
                        holder.iconPudding.setBackgroundResource(R.drawable.item_check_off);
                        holder.iconLink.setBackgroundResource(R.drawable.item_check_off);
                    }

                }
            }
        });
        holder.btnPudding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"1".equals(selectedMainStatus)) {
                    if (listener != null) {
                        selectedMainStatus = "1";
                        listener.onSort2Clicked(selectedMainStatus);

                        holder.btnAll.setBackgroundResource(R.drawable.main_cat_default);
                        holder.btnPudding.setBackgroundResource(R.drawable.main_cat_sel);
                        holder.btnLink.setBackgroundResource(R.drawable.main_cat_default);

                        holder.strAll.setTextColor(Color.parseColor("#bcc6d2"));
                        holder.strPudding.setTextColor(Color.parseColor("#9f56f2"));
                        holder.strLink.setTextColor(Color.parseColor("#bcc6d2"));

                        holder.iconAll.setBackgroundResource(R.drawable.item_check_off);
                        holder.iconPudding.setBackgroundResource(R.drawable.check_circle_on);
                        holder.iconLink.setBackgroundResource(R.drawable.item_check_off);
                    }
                }
            }
        });
        holder.btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"3".equals(selectedMainStatus)) {
                    if (listener != null) {
                        selectedMainStatus = "3";
                        listener.onSort2Clicked(selectedMainStatus);
                        holder.btnAll.setBackgroundResource(R.drawable.main_cat_default);
                        holder.btnPudding.setBackgroundResource(R.drawable.main_cat_default);
                        holder.btnLink.setBackgroundResource(R.drawable.main_cat_sel);

                        holder.strAll.setTextColor(Color.parseColor("#bcc6d2"));
                        holder.strPudding.setTextColor(Color.parseColor("#bcc6d2"));
                        holder.strLink.setTextColor(Color.parseColor("#9f56f2"));

                        holder.iconAll.setBackgroundResource(R.drawable.item_check_off);
                        holder.iconPudding.setBackgroundResource(R.drawable.item_check_off);
                        holder.iconLink.setBackgroundResource(R.drawable.check_circle_on);
                    }
                }
            }
        });
        holder.btnRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(selectedStatus)) {
                    if (listener != null) {
                        selectedStatus = "";
                        listener.onSortClicked(selectedStatus);
                    }
                    holder.imageRecent.setBackgroundResource(R.drawable.product_new_on_ico);
                    holder.imageFav.setBackgroundResource(R.drawable.product_rank_off_ico);
                    holder.imagePriceUp.setBackgroundResource(R.drawable.up_arrow_off_ico);
                    holder.imagePriceDown.setBackgroundResource(R.drawable.down_arrow_off_ico);
                }
            }
        });
        holder.btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"1".equals(selectedStatus)) {
                    if (listener != null) {
                        selectedStatus = "1";
                        listener.onSortClicked(selectedStatus);
                    }
                    holder.imageRecent.setBackgroundResource(R.drawable.product_new_off_ico);
                    holder.imageFav.setBackgroundResource(R.drawable.product_rank_on_ico);
                    holder.imagePriceUp.setBackgroundResource(R.drawable.up_arrow_off_ico);
                    holder.imagePriceDown.setBackgroundResource(R.drawable.down_arrow_off_ico);
                }
            }
        });

        holder.btnPriceDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"2".equals(selectedStatus)) {
                    if (listener != null) {
                        selectedStatus = "2";
                        listener.onSortClicked(selectedStatus);
                    }
                    holder.imageRecent.setBackgroundResource(R.drawable.product_new_off_ico);
                    holder.imageFav.setBackgroundResource(R.drawable.product_rank_off_ico);
                    holder.imagePriceUp.setBackgroundResource(R.drawable.up_arrow_off_ico);
                    holder.imagePriceDown.setBackgroundResource(R.drawable.down_arrow_on_ico);
                }
            }
        });

        holder.btnPriceUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"3".equals(selectedStatus)) {
                    if (listener != null) {
                        selectedStatus = "3";
                        listener.onSortClicked(selectedStatus);
                    }
                    holder.imageRecent.setBackgroundResource(R.drawable.product_new_off_ico);
                    holder.imageFav.setBackgroundResource(R.drawable.product_rank_off_ico);
                    holder.imagePriceUp.setBackgroundResource(R.drawable.up_arrow_on_ico);
                    holder.imagePriceDown.setBackgroundResource(R.drawable.down_arrow_off_ico);
                }
            }
        });
    }

    private void bindItemViewHolder(ItemViewHolder holder, int position) {
        Logger.e("bindItemViewHolder");
        API70.ProductItem item = mItems.get(position);
        String productName = item.title;
        String shopName = item.sitename;
        String productGubun = item.strType;
        String price = item.price;
        String originPrice = item.orgprice;
        String zzimStatus = item.is_wish;
        String isCart = item.is_cart;
        Logger.e("isCart :: " + isCart);

        String it_stock_qty = item.it_stock_qty;
        if ( "0".equals(it_stock_qty) || TextUtils.isEmpty(it_stock_qty) ) {
            holder.soldOut.setVisibility(View.VISIBLE);
            holder.priceLayer.setVisibility(View.GONE);
        } else {
            holder.soldOut.setVisibility(View.GONE);
            holder.priceLayer.setVisibility(View.VISIBLE);
        }

        String shopFeeStatus = item.it_sc_type;
        String percentage = "";
        double dPrice = 0;
        double dOriginPrice = 0;
        try {
            if ( price == null || originPrice == null ) {

            } else {
                dPrice = Double.valueOf(price);
                dOriginPrice = Double.valueOf(originPrice);

                if (dPrice != dOriginPrice) {
                    Logger.e("dPrice :: " + dPrice);
                    Logger.e("dOriginPrice :: " + dOriginPrice);
                    String sRate = getRate(dOriginPrice, dPrice);
                    Logger.e("sRate :: " + sRate);
                    if ( !TextUtils.isEmpty(sRate) )
                        percentage = getRate(dOriginPrice, dPrice) + "%";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String imagePath = item.image1;
        ImageLoad.setImage(context, holder.image, imagePath, R.drawable.product_no_img, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);
        holder.productName.setText(productName);
        holder.price.setText(Utils.ToNumFormat(dPrice)+"원");
        if (TextUtils.isEmpty(originPrice) || price.equals(originPrice) ) {
            holder.originPrice.setVisibility(View.INVISIBLE);
        } else {
            if ( "0".equals(originPrice) )
                holder.originPrice.setVisibility(View.INVISIBLE);
            else {
                holder.originPrice.setVisibility(View.VISIBLE);
                holder.originPrice.setText(Utils.ToNumFormat(dOriginPrice)+"원");
                holder.originPrice.setPaintFlags(holder.originPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        if ( TextUtils.isEmpty(percentage) ) {
            holder.sale.setVisibility(View.GONE);
        } else {
            holder.sale.setVisibility(View.VISIBLE);
            holder.sale.setText(percentage);
        }
        holder.shopName.setText(shopName);
        if ("Y".equals(zzimStatus) ) {
            holder.imageLike.setBackgroundResource(R.drawable.product_scrap_on_ico);
        } else {
            holder.imageLike.setBackgroundResource(R.drawable.product_scrap_off_ico);
        }
        if ( "Y".equals(isCart) ) {
            holder.imageCart.setBackgroundResource(R.drawable.product_cart_on_ico);
        } else {
            holder.imageCart.setBackgroundResource(R.drawable.product_cart_off_ico);
        }
        if ( "1".equals(shopFeeStatus) ) {
            holder.freeFeeBadge.setVisibility(View.VISIBLE);
        } else {
            holder.freeFeeBadge.setVisibility(View.GONE);
        }

        if ( TextUtils.isEmpty(item.pointRate) || "0".equals(item.pointRate) )
            holder.saveBadge.setVisibility(View.GONE);
        else {
            holder.saveBadge.setVisibility(View.VISIBLE);
            holder.saveBadge.setText(item.pointRate + "%적립");
        }

        if ( TextUtils.isEmpty(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_shop_background);
            holder.puddingPay.setVisibility(View.GONE);
        } else if ( "1".equals(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_shop_background);
            holder.puddingPay.setVisibility(View.VISIBLE);
        } else if ( "2".equals(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_link_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_link_background);
            holder.puddingPay.setVisibility(View.GONE);
        } else if ( "3".equals(productGubun) ) {
            holder.productGubun.setBackgroundResource(R.drawable.item_link_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_link_background);
            holder.puddingPay.setVisibility(View.GONE);
        } else {
            holder.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1);
            holder.shopBg.setBackgroundResource(R.drawable.item_shop_background);
            holder.puddingPay.setVisibility(View.GONE);
        }

        holder.reviewPoint.setText(item.review_avg);
        holder.reviewCnt.setText(item.review_cnt);
        holder.zzimCnt.setText(item.wish_cnt);

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( listener != null ) {
                    listener.onItemClick(item);
                }
            }
        });

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = AppPreferences.Companion.getUserId(context);
                if ( TextUtils.isEmpty(userId) ) {
                    context.startActivity(new Intent(context, LoginActivity.class));
                } else {
                    if ( listener != null ) {
                        boolean status = false;
                        if ( "Y".equals(item.is_wish) )
                            status = false;
                        else
                            status = true;
                        listener.onLikeClick(item, status);
                    }
                }
            }
        });

        if ( isFromLive ) {
            holder.btnLike.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if ( isFromStore || isFromLive) {
            if (mItems != null) {
                return mItems.size() + 4;
            } else {
                return 0;
            }
        } else {
            if (mItems != null) {
                return mItems.size() + 5;
            } else {
                return 0;
            }
        }
    }

    private String getRate(double originPrice, double price) {
        String result = "";
        if (originPrice == 0 || price == 0) {
            return "";
        }

        try {
            if (originPrice >= 0) {
                double rate = (originPrice - price) / originPrice;
                Logger.d("rate :: "  + rate);
                double dPercentage = rate * 100;
                int salePercentage = (int)dPercentage;
                if (salePercentage > 0) {
                    result = "" + salePercentage;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder {
        public ViewPager bannerPager;
        public RadioGroup indicator;
        public BannerViewHolder(Context context, View itemView) {
            super(itemView);
            bannerPager = itemView.findViewById(R.id.bannerPager);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }

    public class ItemFirstCategoryViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView recyclerViewFirstCategory;
        public FirstCategoryAdapter firstAdapter;

        public ItemFirstCategoryViewHolder(Context context, View itemView) {
            super(itemView);
            firstAdapter = new FirstCategoryAdapter(context, new FirstCategoryAdapter.FirstCategoryListener() {
                @Override
                public void firstCategoryClicked(String categoryId) {
                    if (categoryId.equals(selectedFirstCategory))
                        return;
                    selectedFirstCategory = categoryId;
                    selectedSecondCategory = null;
                    selectedThirdCategory = null;
                    if (itemSecondCategoryViewHolder != null) {
                        itemSecondCategoryViewHolder.recyclerViewSecondCategory.scrollTo(0, 0);
                    }
                    if (itemThirdCategoryViewHolder != null) {
                        itemThirdCategoryViewHolder.recyclerViewThirdCategory.scrollTo(0, 0);
                    }

                    if (categoryId.isEmpty()) {
                        Logger.e("categoryEmpty");
                        if (itemSecondCategoryViewHolder != null) {
                            Logger.e("visible second gone f1 ");
                            itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.GONE);
                            itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.GONE);
                        }


                        for (int i = 0; i < categoryItems.size(); i++) {
                            Logger.e("categoryId :: " + categoryId);
                            Logger.e("categoryId1 :: " + categoryItems.get(i).getCategoryId());
                            Logger.e("*********");
                            if (categoryId.equals(categoryItems.get(i).getCategoryId())) {
                                ArrayList<SecondCategoryItem> arr = categoryItems.get(i).getSecondCategory();
                                if (itemSecondCategoryViewHolder.secondAdapter != null) {
                                    if (arr == null) {
                                        Logger.e("visible second gone f2 ");
                                        itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.GONE);
                                        itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.GONE);
                                    } else {
                                        Logger.e("visible second visible f2 ");
                                        itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.VISIBLE);
                                        itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.VISIBLE);
                                        itemSecondCategoryViewHolder.secondAdapter.setItems(arr);
                                    }

                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            itemSecondCategoryViewHolder.recyclerViewSecondCategory.scrollToPosition(0);
                                            if (itemThirdCategoryViewHolder != null) {
                                                Logger.e("visible third gone f4 ");
                                                itemThirdCategoryViewHolder.thirdCategoryLayer.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.recyclerViewThirdCategory.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.f_line.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.s_line.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }

                            }
                        }
                    } else {
                        Logger.e("firstCategory clilcked not empty");

                        for (int i = 0; i < categoryItems.size(); i++) {
                            Logger.e("categoryId :: " + categoryId);
                            Logger.e("categoryId1 :: " + categoryItems.get(i).getCategoryId());
                            Logger.e("*********");
                            if (categoryId.equals(categoryItems.get(i).getCategoryId())) {
                                ArrayList<SecondCategoryItem> arr = categoryItems.get(i).getSecondCategory();
                                if (itemSecondCategoryViewHolder.secondAdapter != null) {
                                    if (arr == null) {
                                        Logger.e("visible second gone f3 ");
                                        itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.GONE);
                                        itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.GONE);
                                    } else {
                                        Logger.e("visible second visible f3 ");
                                        itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.VISIBLE);
                                        itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.VISIBLE);
                                        itemSecondCategoryViewHolder.secondAdapter.setItems(arr);
                                    }

                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            itemSecondCategoryViewHolder.recyclerViewSecondCategory.scrollToPosition(0);
                                            if (itemThirdCategoryViewHolder != null) {
                                                Logger.e("visible third gone f4 ");
                                                itemThirdCategoryViewHolder.thirdCategoryLayer.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.recyclerViewThirdCategory.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.f_line.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.s_line.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }

                    if (listener != null) {
                        listener.categoryClicked(categoryId);
                    }
                    notifyDataSetChanged();
                }
            });
            recyclerViewFirstCategory = itemView.findViewById(R.id.recyclerViewFirstCategory);
            WrappedLinearLayoutManager layoutManager = new WrappedLinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewFirstCategory.setHasFixedSize(false);
            recyclerViewFirstCategory.setLayoutManager(layoutManager);
            recyclerViewFirstCategory.setAdapter(firstAdapter);
        }
    }

    public class ItemSecondCategoryViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView recyclerViewSecondCategory;
        public SecondCategoryAdapter secondAdapter;
        public LinearLayout secondCategoryLayer;

        public ItemSecondCategoryViewHolder(Context context, View itemView) {
            super(itemView);
            secondAdapter = new SecondCategoryAdapter(context, new SecondCategoryAdapter.SecondCategoryListener() {
                @Override
                public void secondCategoryClicked(String categoryId, String categoryName) {
                    if (categoryId.equals(selectedSecondCategory))
                        return;
                    selectedSecondCategory = categoryId;
                    selectedThirdCategory = null;
                    if (itemThirdCategoryViewHolder != null) {
                        itemThirdCategoryViewHolder.recyclerViewThirdCategory.scrollTo(0, 0);
                    }

                    if ("전체".equals(categoryName)) {
                        if (itemThirdCategoryViewHolder != null) {
                            Logger.e("visible third gone s1 ");
                            itemThirdCategoryViewHolder.thirdCategoryLayer.setVisibility(View.GONE);
                            itemThirdCategoryViewHolder.recyclerViewThirdCategory.setVisibility(View.GONE);
                            itemThirdCategoryViewHolder.f_line.setVisibility(View.GONE);
                            itemThirdCategoryViewHolder.s_line.setVisibility(View.GONE);
                        }

                    } else {
                        if (itemThirdCategoryViewHolder != null) {
                            Logger.e("visible third visible s1 ");
                            itemThirdCategoryViewHolder.thirdCategoryLayer.setVisibility(View.VISIBLE);
                            itemThirdCategoryViewHolder.recyclerViewThirdCategory.setVisibility(View.VISIBLE);
                            itemThirdCategoryViewHolder.f_line.setVisibility(View.VISIBLE);
                            itemThirdCategoryViewHolder.s_line.setVisibility(View.VISIBLE);
                        }


                        for (int i = 0; i < categoryItems.size(); i++) {
                            ArrayList<SecondCategoryItem> arr = categoryItems.get(i).getSecondCategory();
                            if (arr != null && arr.size() > 0) {
                                for (int j = 0; j < arr.size(); j++) {
                                    String secondCategory = arr.get(j).getCategoryId();
                                    if (categoryId.equals(secondCategory)) {
                                        ArrayList<ThirdCategoryItem> thirdArray = arr.get(j).getThirdCategory();
                                        if (thirdArray == null) {
                                            if (itemThirdCategoryViewHolder != null) {
                                                Logger.e("visible third gone s2 ");
                                                itemThirdCategoryViewHolder.thirdCategoryLayer.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.recyclerViewThirdCategory.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.f_line.setVisibility(View.GONE);
                                                itemThirdCategoryViewHolder.s_line.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (itemThirdCategoryViewHolder.thirdAdapter != null)
                                                itemThirdCategoryViewHolder.thirdAdapter.setItems(thirdArray);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (listener != null) {
                        listener.categoryClicked(categoryId);
                    }
                }
            });
            secondCategoryLayer = itemView.findViewById(R.id.secondCategoryLayer);
            recyclerViewSecondCategory = itemView.findViewById(R.id.recyclerViewSecondCategory);
            WrappedLinearLayoutManager layoutManager = new WrappedLinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewSecondCategory.setHasFixedSize(false);
            recyclerViewSecondCategory.setLayoutManager(layoutManager);
            recyclerViewSecondCategory.setAdapter(secondAdapter);
        }
    }

    public class ItemThirdCategoryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout thirdCategoryLayer;
        public RecyclerView recyclerViewThirdCategory;
        public View f_line, s_line;
        public ThirdCategoryAdapter thirdAdapter;

        public ItemThirdCategoryViewHolder(Context context, View itemView) {
            super(itemView);
            thirdAdapter = new ThirdCategoryAdapter(context, new ThirdCategoryAdapter.ThirdCategoryListener() {
                @Override
                public void thirdCategoryClicked(String categoryId) {
                    if (categoryId.equals(selectedThirdCategory))
                        return;
                    selectedThirdCategory = categoryId;
                    if (listener != null) {
                        listener.categoryClicked(categoryId);
                    }
                }
            });
            thirdCategoryLayer = itemView.findViewById(R.id.thirdCategoryLayer);
            f_line = itemView.findViewById(R.id.f_line);
            s_line = itemView.findViewById(R.id.s_line);
            recyclerViewThirdCategory = itemView.findViewById(R.id.recyclerViewThirdCategory);
            WrappedGridLayoutManager layoutManager = new WrappedGridLayoutManager(context, 2);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            recyclerViewThirdCategory.setHasFixedSize(false);
            recyclerViewThirdCategory.setLayoutManager(layoutManager);
            recyclerViewThirdCategory.setAdapter(thirdAdapter);
        }
    }

    public class ItemSortViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout btnRecent, btnFav, btnPriceDown, btnPriceUp;
        public AppCompatImageView imageRecent, imageFav, imagePriceDown, imagePriceUp, iconAll, iconPudding, iconLink;
        public LinearLayout btnAll, btnPudding, btnLink, cate_1;

        public AppCompatTextView count, strAll, strPudding, strLink;
        public View topLine;

        public ItemSortViewHolder(Context context, View itemView) {
            super(itemView);
            topLine = itemView.findViewById(R.id.topLine);
            count = itemView.findViewById(R.id.count);
            btnRecent = itemView.findViewById(R.id.btnRecent);
            btnFav = itemView.findViewById(R.id.btnFav);
            btnPriceDown = itemView.findViewById(R.id.btnPriceDown);
            btnPriceUp = itemView.findViewById(R.id.btnPriceUp);
            imageRecent = itemView.findViewById(R.id.imageRecent);
            imageFav = itemView.findViewById(R.id.imageFav);
            imagePriceDown = itemView.findViewById(R.id.imagePriceDown);
            imagePriceUp = itemView.findViewById(R.id.imagePriceUp);

            btnAll = itemView.findViewById(R.id.btnAll);
            btnPudding = itemView.findViewById(R.id.btnPudding);
            btnLink = itemView.findViewById(R.id.btnLink);
            strAll = itemView.findViewById(R.id.strAll);
            strPudding = itemView.findViewById(R.id.strPudding);
            strLink = itemView.findViewById(R.id.strLink);
            iconAll = itemView.findViewById(R.id.iconAll);
            iconPudding = itemView.findViewById(R.id.iconPudding);
            iconLink = itemView.findViewById(R.id.iconLink);

            cate_1 = itemView.findViewById(R.id.cate_1);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public String idx;
        public AppCompatTextView productName, shopName, sale, originPrice, price, freeFeeBadge, saveBadge, reviewPoint, reviewCnt, zzimCnt, soldOut;
        public AppCompatImageView imageLike, imageCart, productGubun;
        public SelectableRoundedImageView image;
        public RelativeLayout btnLike, btnCart, item, shopBg;
        public LinearLayout priceLayer, puddingPay;

        public ItemViewHolder(Context context, View itemView) {
            super(itemView);
            shopBg = itemView.findViewById(R.id.shopBg);
            puddingPay = itemView.findViewById(R.id.puddingPay);
            priceLayer = itemView.findViewById(R.id.priceLayer);
            soldOut = itemView.findViewById(R.id.soldOut);
            item = itemView.findViewById(R.id.item);
            freeFeeBadge = itemView.findViewById(R.id.freeFeeBadge);
            saveBadge = itemView.findViewById(R.id.saveBadge);
            reviewPoint = itemView.findViewById(R.id.reviewPoint);
            reviewCnt = itemView.findViewById(R.id.reviewCnt);
            zzimCnt = itemView.findViewById(R.id.zzimCnt);
            productName = itemView.findViewById(R.id.productName);
            shopName = itemView.findViewById(R.id.shopName);
            productGubun = itemView.findViewById(R.id.productGubun);
            sale = itemView.findViewById(R.id.sale);
            originPrice = itemView.findViewById(R.id.originPrice);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.image);
            imageLike = itemView.findViewById(R.id.imageLike);
            imageCart = itemView.findViewById(R.id.imageCart);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnCart = itemView.findViewById(R.id.btnCart);
        }
    }

    public void likeSuccess(String idx) {
        Logger.e("storeProduct likeSuccess");
        if ( mItems != null ) {
            for ( int i = 0 ; i < mItems.size() ; i ++ ) {
                String itemIdx = mItems.get(i).idx;
                if ( itemIdx.equals(idx) ) {
                    API70.ProductItem item = mItems.get(i);
                    String wish = item.is_wish;
                    String wish_cnt = item.wish_cnt;
                    int iWishCnt = 0;
                    try {
                        iWishCnt = Integer.valueOf(wish_cnt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Logger.e("item.wish_cnt = > " + iWishCnt);
                    if ( "Y".equals(wish) ) {
                        wish = "N";
                        if ( iWishCnt > 0 )
                            iWishCnt --;
                    } else {
                        wish = "Y";
                        iWishCnt ++;
                    }
                    Logger.e("item.is_siwh = > " + item.is_wish);
                    Logger.e("item.wish_cnt = > " + iWishCnt);
                    item.is_wish = wish;
                    item.wish_cnt = iWishCnt + "";
                    Logger.e("item.is_siwh = > " + item.is_wish);
                    mItems.set(i, item);
                }
            }
            notifyDataSetChanged();
        }
    }

    public void setLikeChaged(String idx, String status) {
        if ( idx == null || categoryItems == null || mItems == null )
            return;
        Logger.e("categoryItems.size :: " + categoryItems.size());
        Logger.e("mItems.size :: " + mItems.size());
        for ( int i = 0 ; i < mItems.size() ; i ++ ) {
            if ( idx.equals(mItems.get(i).idx) ) {
                API70.ProductItem item = mItems.get(i);
                if ( !item.is_wish.equals(status ) ) {
                    int iWishCnt = 0;
                    try {
                        iWishCnt = Integer.valueOf(item.wish_cnt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Logger.e("item.wish_cnt = > " + iWishCnt);
                    if ( "N".equals(status) ) {
                        if ( iWishCnt > 0 ) {
                            iWishCnt --;
                        }
                    } else {
                        iWishCnt ++;
                    }

                    item.is_wish = status;
                    item.wish_cnt = iWishCnt + "";
                    Logger.e("item wish :: " + item.is_wish);
                    Logger.e("item.wish_cnt = > " + iWishCnt);
                    mItems.set(i, item);
                    notifyDataSetChanged();
                }
            }
        }
    }
}
