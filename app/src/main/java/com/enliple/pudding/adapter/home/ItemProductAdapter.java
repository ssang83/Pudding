package com.enliple.pudding.adapter.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.CategoryModel;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.data.CategoryItem;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkConst;
import com.enliple.pudding.commons.network.vo.API70;
import com.enliple.pudding.shoppingcaster.adapter.LiveProductHeaderAdapter;
import com.enliple.pudding.shoppingcaster.data.LiveProductMainCategory;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0X1001;
    private static final int VIEW_TYPE_ITEM = 0X2001;

    private int HEADER_CATEGORY_NORMAL_TEXT_COLOR = 0xFF464646;
    private int HEADER_CATEGORY_SELECTED_TEXT_COLOR = 0xFFff6c6c;
    public Context mContext;
    private AdapterListener mListener;
    private final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private ArrayList<Integer> mTabIdArray = new ArrayList<Integer>();
    private ArrayList<AppCompatTextView> mTextViewArray = new ArrayList<AppCompatTextView>();
    private ArrayList<RelativeLayout> mBackgroundArray = new ArrayList<RelativeLayout>();
    private List<API70.ProductItem> mItems = new ArrayList<>();
    public ArrayList<CategoryItem> subArray = new ArrayList<CategoryItem>();
    public ItemProductHeaderViewHolder headerViewHolder;
    public ItemProductItemViewHolder itemViewHolder;

    private boolean isEndOfData = false;                          // 더 이상 받을 데이터가 없는 경우 True
    private int viewWidth;
    private String firstCategory, secondCategory;

    private ItemClick itemClick;
    private boolean isMainClicked = true;
    private String selectedCategory = "all";
    private boolean priceStatus = false; // false 낮은 가격
    private String selectedStatus = "1";
    public interface ItemClick {
        public void onClick(View view, int position);
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public ItemProductAdapter(Context mContext) {
        this.mContext = mContext;
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewWidth = size.x;
        Logger.e("viewWidth :: " + viewWidth);
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.live_product_grid_header, parent, false);
            headerViewHolder = new ItemProductHeaderViewHolder(mContext, view);
            return headerViewHolder;
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_product_item, parent, false);
            itemViewHolder = new ItemProductItemViewHolder(view);
            return itemViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Logger.d("onBindViewHolder:" + position);

        if (holder instanceof ItemProductHeaderViewHolder) {
            bindItemProductHeaderHolder((ItemProductHeaderViewHolder) holder);
        } else {
            bindItemProductItemViewHolder((ItemProductItemViewHolder) holder, position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        if (hasStableIds()) {
            return position + 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size() + 1;
        } else {
            return 1;
        }
    }

    /**
     * 해당 Adapter Position 에 위치한 LiveProduct 데이터를 반환
     *
     * @param position
     * @return
     */
    public API70.ProductItem getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    /**
     * LiveProduct HeaderViewholder 를 Binding
     * 메인카테고리 클릭 시 해당 카테고리 리스트를 서버에 요청해서 세팅하도록 해야함
     * 서브카테고리를 클릭 시 해당 카테고리 리스트를 서버에 요청해서 세팅하도록 해야함
     *
     * @param holder
     */
    private void bindItemProductHeaderHolder(final ItemProductHeaderViewHolder holder) {

        holder.headerAdapter.setItemClick(new LiveProductHeaderAdapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                LiveProductMainCategory data = holder.headerAdapter.getItemByPosition(position);
                if (selectedCategory != data.getCategoryId()) {
                    isMainClicked = true;
                    selectedCategory = data.getCategoryId();
                    setSubCategory(holder);

                    if (mListener != null) {
                        mListener.onCategoryClicked(true, selectedCategory, holder);
                    }
                }
            }
        });
        holder.btnRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !"".equals(selectedStatus) ) {
                    if (mListener != null) {
                        selectedStatus = "";
                        mListener.onSortClicked(selectedStatus);
                    }
                    holder.btnRecent.setTextColor(Color.parseColor("#ff6c6c"));
                    holder.btnFav.setTextColor(Color.parseColor("#bcc6d2"));
                    holder.strPrice.setTextColor(Color.parseColor("#bcc6d2"));
                    if ( priceStatus ) {
                        holder.iconPrice.setBackgroundResource(R.drawable.up_arrow_off_ico);
                    } else {
                        holder.iconPrice.setBackgroundResource(R.drawable.down_arrow_off_ico);
                    }
                }
            }
        });
        holder.btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !"1".equals(selectedStatus) ) {
                    if (mListener != null) {
                        selectedStatus = "1";
                        mListener.onSortClicked(selectedStatus);
                    }
                    holder.btnRecent.setTextColor(Color.parseColor("#bcc6d2"));
                    holder.btnFav.setTextColor(Color.parseColor("#ff6c6c"));
                    holder.strPrice.setTextColor(Color.parseColor("#bcc6d2"));
                    if ( priceStatus ) {
                        holder.iconPrice.setBackgroundResource(R.drawable.up_arrow_off_ico);
                    } else {
                        holder.iconPrice.setBackgroundResource(R.drawable.down_arrow_off_ico);
                    }
                }
            }
        });
        holder.btnPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( "3".equals(selectedStatus) || "2".equals(selectedStatus) ) // 인기 또는 최신순에서 가격순으로 넘어올 경우에만 토글을 한다.
                    togglePriceStatus();
                if ( priceStatus ) {
                    selectedStatus = "3";
                } else {
                    selectedStatus = "2";
                }
                if (mListener != null)
                    mListener.onSortClicked(selectedStatus);
                holder.btnRecent.setTextColor(Color.parseColor("#bcc6d2"));
                holder.btnFav.setTextColor(Color.parseColor("#bcc6d2"));
                holder.strPrice.setTextColor(Color.parseColor("#ff6c6c"));
                if ( priceStatus ) {
                    holder.iconPrice.setBackgroundResource(R.drawable.up_arrow_on_ico);
                } else {
                    holder.iconPrice.setBackgroundResource(R.drawable.down_arrow_on_ico);
                }
            }
        });
        setSubCategory(holder);
    }

    /**
     * ItemProduct 아이템 ViewHolder를 Binding
     *
     * @param holder
     * @param position
     */
    private void bindItemProductItemViewHolder(final ItemProductItemViewHolder holder, final int position) {
        Logger.e("bindItemProductItemViewHolder called");
        final API70.ProductItem data = getItemByPosition(position);
        if (data == null)
            return;
        int width = (int) ((viewWidth / 2) - Utils.ConvertDpToPx(mContext, 8)) - Utils.ConvertDpToPx(mContext, 14);

        if (position % 2 == 0) {
            holder.leftSpace.setVisibility(View.VISIBLE);
            holder.rightSpace.setVisibility(View.GONE);
        } else {
            holder.leftSpace.setVisibility(View.GONE);
            holder.rightSpace.setVisibility(View.VISIBLE);
        }

        ViewGroup.LayoutParams params = holder.container.getLayoutParams();
        params.width = width;
        params.height = params.height;
        holder.container.setLayoutParams(params);

        ViewGroup.LayoutParams param1 = holder.image.getLayoutParams();
        param1.width = width;
        param1.height = width;
        holder.image.setLayoutParams(param1);

        String imagePath = data.image1;
        String name = data.title;
        String sale = "";

        String quantity = data.it_stock_qty;
        int iQuantity = 0;
        if ( !TextUtils.isEmpty(quantity) || quantity != null ) {
            try {
                iQuantity = Integer.valueOf(quantity);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        Logger.e("iQuantity :: " + iQuantity);
        if ( iQuantity > 0 ) {
            holder.strSoldOut.setVisibility(View.GONE);
            holder.saleLayer.setVisibility(View.VISIBLE);
        } else {
            holder.strSoldOut.setVisibility(View.VISIBLE);
            holder.saleLayer.setVisibility(View.GONE);
        }

        if (!data.orgprice.equals(data.price)) {
            try {
                int i_origin = Integer.valueOf(data.orgprice);
                int i_saled = Integer.valueOf(data.price);

                Logger.e("i_origin :: " + i_origin);
                Logger.e("i_saled :: " + i_saled);
                if (i_origin == 0) {
                    sale = "";
                } else {
                    double div = i_origin - i_saled;
                    Logger.e("div :: " + div);
                    double s_rt = div / i_origin;
                    Logger.e("s_rt :: " + s_rt);
                    int sale_percentage = (int) (s_rt * 100);
                    Logger.e("sale_percentage :: " + sale_percentage);
                    if (sale_percentage <= 0)
                        sale = "";
                    else
                        sale = sale_percentage + "%";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String price = data.price;
        holder.name.setText(name);
        if (TextUtils.isEmpty(sale)) {
            holder.sale.setVisibility(View.GONE);
        } else {
            holder.sale.setVisibility(View.VISIBLE);
            holder.sale.setText(sale);
        }

        try {
            int iPrice = Integer.valueOf(data.price);
            String sPrice = Utils.ToNumFormat(iPrice);
            holder.price.setText(sPrice);
        } catch (Exception e) {
            e.printStackTrace();
            holder.price.setText(price);
        }

        ImageLoad.setImage(mContext, holder.image, imagePath, null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick != null) {
                    itemClick.onClick(v, position);
                }
            }
        });
//        if (headerViewHolder != null) {
//            String count = headerViewHolder.item_count.getText().toString();
//            Logger.e("mItems SIZE :: " + mItems.size());
//            Logger.e("count :: " + count);
//            if (!count.equals("" + mItems.size())) {
//                headerViewHolder.item_count.setText("" + mItems.size());
//            }
//        }
    }

    /**
     * Adapter 에서 발생되는 EventListener 를 설정
     *
     * @param listener
     */
    public void setAdapterListener(AdapterListener listener) {
        mListener = listener;
    }

    public void setHeaderData(ArrayList<CategoryItem> subArray) {
        this.subArray = subArray;
        notifyDataSetChanged();
    }

    public void setHeaderData(ArrayList<CategoryItem> subArray, ItemProductHeaderViewHolder holder) {
        this.subArray = subArray;
        notifyDataSetChanged();
        holder.headerAdapter.notifyDataSetChanged();
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
//            if ( headerViewHolder != null ) {
//                headerViewHolder.item_count.setText(mItems.size());
//            }
            notifyDataSetChanged();
        }
    }

    public void setItems(List<API70.ProductItem> items, int count) {
        Logger.e("setItems");
//        if (items != null && items.size() == 0) {
//            headerViewHolder.item_count.setText("" + count);
//        }
        if (headerViewHolder.item_count != null) {
            headerViewHolder.item_count.setText("" + count);
        }

        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);

//        if ( headerViewHolder != null ) {
//            headerViewHolder.item_count.setText(mItems.size());
//        }
        notifyDataSetChanged();
    }

    public void setCategoryData(ArrayList<CategoryItem> items, ItemProductHeaderViewHolder holder) {
        setHeaderData(items, holder);
    }

    @SuppressLint("NewApi")
    private int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    private View.OnClickListener mTabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabIdArray.size(); i++) {
                if (v.getId() == mTabIdArray.get(i)) {
                    mTextViewArray.get(i).setTextColor(HEADER_CATEGORY_SELECTED_TEXT_COLOR);
                    mBackgroundArray.get(i).setBackgroundResource(R.drawable.special_bg_selected);
                } else {
                    mTextViewArray.get(i).setTextColor(HEADER_CATEGORY_NORMAL_TEXT_COLOR);
                    mBackgroundArray.get(i).setBackgroundResource(R.drawable.white_bg);
                }
            }
        }
    };

    public interface AdapterListener {
        void onCategoryClicked(boolean isMainClicked, String categoryId, ItemProductHeaderViewHolder holder);

        void onSortClicked(String order);
    }

    public static class ItemProductHeaderViewHolder extends RecyclerView.ViewHolder {
        public static final int NUMBER_COLUMNS = 5;
        public RecyclerView headerRecyclerView;
        public LinearLayout subCategoryLayer;
        public AppCompatTextView btnRecent, btnFav, item_count, strPrice;
        public AppCompatImageView iconPrice;
        public RelativeLayout btnPrice;
        public LiveProductHeaderAdapter headerAdapter;
        public GridLayoutManager gridLayoutManager;
        public ArrayList<CategoryItem> firstCategoryArray = new ArrayList<CategoryItem>();
        public ArrayList<LiveProductMainCategory> mainArray = new ArrayList<LiveProductMainCategory>();

        public ItemProductHeaderViewHolder(Context context, View itemView) {
            super(itemView);
            subCategoryLayer = itemView.findViewById(R.id.subCategoryLayer);
            headerRecyclerView = itemView.findViewById(R.id.recyclerHeader);
            btnRecent = itemView.findViewById(R.id.btnRecent);
            btnFav = itemView.findViewById(R.id.btnFav);
            btnPrice = itemView.findViewById(R.id.btnPrice);
            strPrice = itemView.findViewById(R.id.strPrice);
            iconPrice = itemView.findViewById(R.id.iconPrice);
            item_count = itemView.findViewById(R.id.item_count);
            gridLayoutManager = new GridLayoutManager(context, NUMBER_COLUMNS);
            headerRecyclerView.setLayoutManager(gridLayoutManager);
            headerAdapter = new LiveProductHeaderAdapter(context);
            headerRecyclerView.setAdapter(headerAdapter);

//            firstCategoryArray = DatabaseAPI.INSTANCE.getFirstCategorysWithoutPopularity(context.getContentResolver());
            firstCategoryArray.addAll(CategoryModel.getCategory(context, "all"));
            LiveProductMainCategory cate = new LiveProductMainCategory();
            cate.setCategoryName("전체");
            cate.setCategoryImage(NetworkConst.CATEGORY_ALL_IMAGE_API);
            cate.setCategoryId("");
            mainArray.add(cate);

            if (firstCategoryArray != null && firstCategoryArray.size() > 0) {
                for (int i = 0; i < firstCategoryArray.size(); i++) {
                    CategoryItem item = firstCategoryArray.get(i);
                    cate = new LiveProductMainCategory();
                    cate.setId(item.getRecordId());
                    cate.setCategoryId(item.getCategoryId());
                    cate.setCategoryName(item.getCategoryName());
                    cate.setCategoryImage(item.getCategoryImage());
                    mainArray.add(cate);
                }
            }

            // 1차 카테고리 갯수가 5의 배수가 아닐 경우 ui 작업위해
            int rest = mainArray.size() % 5;
            if (mainArray.size() % 5 != 0) {
                int addedCategory = 5 - rest;
                for (int i = 0; i < addedCategory; i++) {
                    cate = new LiveProductMainCategory();
                    cate.setCategoryId("");
                    cate.setCategoryName("");
                    cate.setCategoryImage("");
                    mainArray.add(cate);
                }
            }
            headerAdapter.setItems(mainArray);
        }
    }

    public static class ItemProductItemViewHolder extends RecyclerView.ViewHolder {
        public SelectableRoundedImageView image;
        public AppCompatTextView name, sale, price, strSoldOut;
        public RelativeLayout container, badge;
        public View leftSpace, rightSpace;
        public LinearLayout saleLayer;
        public ItemProductItemViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            sale = itemView.findViewById(R.id.sale);
            price = itemView.findViewById(R.id.price);
            container = itemView.findViewById(R.id.container);
            badge = itemView.findViewById(R.id.badge);
            leftSpace = itemView.findViewById(R.id.leftSpace);
            rightSpace = itemView.findViewById(R.id.rightSpace);
            strSoldOut = itemView.findViewById(R.id.strSoldOut);
            saleLayer = itemView.findViewById(R.id.saleLayer);
        }
    }

    private void setSubCategory(ItemProductHeaderViewHolder holder) {
        Logger.e("setSubCategory subArray size :: " + subArray.size() + " and " + isMainClicked);
        if (isMainClicked) {
            holder.subCategoryLayer.removeAllViews();
            mTabIdArray = new ArrayList<Integer>();
            mTextViewArray = new ArrayList<AppCompatTextView>();
            mBackgroundArray = new ArrayList<RelativeLayout>();
        }

        for (int i = 0; i < subArray.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            RelativeLayout layer = (RelativeLayout) inflater.inflate(R.layout.subcategory_item, holder.subCategoryLayer, false);

            AppCompatTextView textView = layer.findViewById(R.id.textSubcategory);
            RelativeLayout background = layer.findViewById(R.id.background);
            if (i == 0) {
                textView.setTextColor(HEADER_CATEGORY_SELECTED_TEXT_COLOR);
                background.setBackgroundResource(R.drawable.special_bg_selected);
            } else {
                textView.setTextColor(HEADER_CATEGORY_NORMAL_TEXT_COLOR);
                background.setBackgroundResource(R.drawable.white_bg);
            }
            background.setGravity(Gravity.CENTER_VERTICAL);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setText(subArray.get(i).getCategoryName());
            layer.setId(generateViewId());
            mTabIdArray.add(layer.getId());
            mTextViewArray.add(textView);
            mBackgroundArray.add(background);
            layer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < mTabIdArray.size(); i++) {
                        if (v.getId() == mTabIdArray.get(i)) {
                            mTextViewArray.get(i).setTextColor(HEADER_CATEGORY_SELECTED_TEXT_COLOR);
                            mBackgroundArray.get(i).setBackgroundResource(R.drawable.special_bg_selected);
                            if (selectedCategory != subArray.get(i).getCategoryId()) {
                                isMainClicked = false;
                                selectedCategory = subArray.get(i).getCategoryId();
                                setSubCategory(holder);

                                if (mListener != null) {
                                    mListener.onCategoryClicked(false, selectedCategory, holder);
                                }
                            }
                        } else {
                            mTextViewArray.get(i).setTextColor(HEADER_CATEGORY_NORMAL_TEXT_COLOR);
                            mBackgroundArray.get(i).setBackgroundResource(R.drawable.white_bg);
                        }
                    }
                }
            });
            if (isMainClicked) {
                holder.subCategoryLayer.addView(layer);
            }
        }
    }

    private void togglePriceStatus() {
        priceStatus = !priceStatus;
    }
}
