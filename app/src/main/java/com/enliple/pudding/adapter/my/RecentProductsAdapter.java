package com.enliple.pudding.adapter.my;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API55;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecentProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<API55.ProductItem> mItems = new ArrayList<>();
    private RecentProductsViewHolder holder;
    private Listener listener;

    public interface Listener {
        void onItemClicked(API55.ProductItem item);
        void setDeletedCount(int deletedCount);
        void onZzimClicked(API55.ProductItem item);
    }

    public RecentProductsAdapter(Context context, Listener listener) {
        this.mContext = context;
        this.listener = listener;
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.recentproduct_subitem, parent, false);
        holder = new RecentProductsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindRecentProductsHolder((RecentProductsViewHolder) holder, position);
    }

    public void addItems(List<API55.ProductItem> itemData) {
        if ( mItems != null ) {
            mItems.addAll(itemData);
            notifyDataSetChanged();
        }
    }

    public void setItems(List<API55.ProductItem> itemData) {
        Logger.e("setItems");
        if (mItems.size() > 0) {
            mItems.clear();
        } else {
            mItems = new ArrayList<>();
        }
        mItems.addAll(itemData);

        notifyDataSetChanged();
    }

    private void bindRecentProductsHolder(final RecentProductsViewHolder holder, int position) {
        API55.ProductItem model = mItems.get(position);
        final boolean isCheck = model.isSelect;
        String image = model.image1;
        String isLive = model.live;
        String isVOD = model.vod;
        String name = model.title;
        String sale = model.discount;
        String likeNum = "" + model.wish_cnt;

        final boolean isShow = model.isShow;
        if (isShow) {
            holder.btnCheckAll.setVisibility(View.VISIBLE);
            holder.empty.setVisibility(View.GONE);
        } else {
            holder.btnCheckAll.setVisibility(View.GONE);
            holder.empty.setVisibility(View.VISIBLE);
        }

        if (sale.length() > 0) {
            sale = sale + "%";
        }
        String price = model.price;
        int iPrice = 0;
        if (price.length() > 0) {
            try {
                iPrice = Integer.valueOf(price);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        price = Utils.ToNumFormat(iPrice) + "원";
        String like = model.is_wish;


        if (isCheck) {
            holder.imgAllCheck.setBackgroundResource(R.drawable.check_on);
        } else {
            holder.imgAllCheck.setBackgroundResource(R.drawable.cart_check_off);
        }

        ImageLoad.setImage(mContext,
                holder.image,
                image,
                R.drawable.product_no_img,
                ImageLoad.SCALE_NONE,
                DiskCacheStrategy.ALL);

        if ("Y".equals(isLive)) {
            holder.isLive.setVisibility(View.VISIBLE);
        } else {
            holder.isLive.setVisibility(View.GONE);
        }

        if ("Y".equals(isVOD)) {
            holder.isVOD.setVisibility(View.VISIBLE);
        } else {
            holder.isVOD.setVisibility(View.GONE);
        }

        holder.name.setText(name);
        holder.sale.setText(sale);
        holder.price.setText(price);
        holder.numLike.setText(likeNum);

        if ("Y".equals(like)) {
            setLikeSelect(holder, true);
        } else {
            setLikeSelect(holder, false);
        }

        if (mItems.size() - 1 == position) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }

        holder.btnCheckAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItems != null && mItems.size() > 0) {
                    API55.ProductItem model = mItems.get(position);
                    boolean selected = model.isSelect;
                    if (selected) {
                        model.isSelect = false;
                        holder.imgAllCheck.setBackgroundResource(R.drawable.cart_check_off);
                    } else {
                        model.isSelect = true;
                        holder.imgAllCheck.setBackgroundResource(R.drawable.check_on);
                    }
                    mItems.remove(position);
                    mItems.add(position, model);

                    boolean sel;
                    if (model.isSelect) {
                        sel = true;
                    } else {
                        sel = false;
                    }

                    Intent intent = new Intent("selected");
                    intent.putExtra("increase", sel);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            }
        });

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( listener != null ) {
                    listener.onZzimClicked(model);
                }
//                Logger.e("canZzimClick :: " + canZzimClick);
//                if ( canZzimClick ) {
//                    if (mItems != null && mItems.size() > 0) {
//                        Logger.e("mItems not null");
//                        API55.Data.ProductItem model = mItems.get(position);
//                        String like = model.is_wish;
//                        boolean status = false;
//                        if ( "Y".equals(like) )
//                            status = false;
//                        else
//                            status = true;
//                        if ( subCallback != null ) {
//                            Logger.e("subCallback NOT null");
//                            canZzimClick = false;
//                            zzimClickedPosition = position;
//                            zzimClickedModel = model;
//                            if ("Y".equals(like)) {
//                                zzimClickedModel.is_wish = "N";
//                                setLikeSelect(holder, false);
//                            } else {
//                                zzimClickedModel.is_wish = "Y";
//                                setLikeSelect(holder, true);
//                            }
//
//                            subCallback.onZzimClicked(model.it_id, status);
//                        } else {
//                            Logger.e("subCallback null");
//                        }
//                    } else {
//                        Logger.e("mItems null");
//                    }
//                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClicked(model);
                }
            }
        });
        String quantity = model.it_stock_qty;

        int iQuantity = 0;
        if (!TextUtils.isEmpty(quantity) || quantity != null) {
            try {
                iQuantity = Integer.valueOf(quantity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.e("iQuantity :: " + iQuantity);
            if (iQuantity > 0) {
                holder.strSoldOut.setVisibility(View.GONE);
                holder.saleLayer.setVisibility(View.VISIBLE);
            } else {
                holder.strSoldOut.setVisibility(View.VISIBLE);
                holder.saleLayer.setVisibility(View.GONE);
            }
        } else {
            holder.strSoldOut.setVisibility(View.GONE);
            holder.saleLayer.setVisibility(View.VISIBLE);
        }
    }

    static class RecentProductsViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout btnCheckAll, btnLike, saleLayer, bgLike;
        public AppCompatImageView imgAllCheck, isLive, isVOD, likeImage;
        public AppCompatTextView name, sale, price, strSoldOut, numLike;
        public LinearLayout item;
        public View line, empty;
        public AppCompatImageView image;

        public RecentProductsViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            empty = itemView.findViewById(R.id.empty);
            btnCheckAll = itemView.findViewById(R.id.btnCheckAll);
            btnLike = itemView.findViewById(R.id.btnLike);

            imgAllCheck = itemView.findViewById(R.id.imgAllCheck);
            image = itemView.findViewById(R.id.image);
            isLive = itemView.findViewById(R.id.isLive);
            isVOD = itemView.findViewById(R.id.isVOD);

            bgLike = itemView.findViewById(R.id.bgLike);
            likeImage = itemView.findViewById(R.id.likeImage);
            numLike = itemView.findViewById(R.id.numLike);

            name = itemView.findViewById(R.id.name);
            sale = itemView.findViewById(R.id.sale);
            price = itemView.findViewById(R.id.price);

            line = itemView.findViewById(R.id.line);

            saleLayer = itemView.findViewById(R.id.saleLayer);
            strSoldOut = itemView.findViewById(R.id.strSoldOut);
        }
    }

    private void setLikeSelect(RecentProductsViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.bgLike.setBackgroundResource(R.drawable.like_circle_on);
            holder.likeImage.setBackgroundResource(R.drawable.my_jjim_on_ico);
            holder.numLike.setTextColor(0xFFff6c6c);
        } else {
            holder.bgLike.setBackgroundResource(R.drawable.like_circle_off);
            holder.likeImage.setBackgroundResource(R.drawable.my_jjim_off_ico);
            holder.numLike.setTextColor(0xFFbcc6d2);
        }
    }

    public boolean getCheckStatus() {
        if (mItems != null) {
            if (mItems.get(0).isShow) {
                return true;
            }
        }
        return false;
    }

    public void forcedZzim(String idx, boolean status ) {
        try {
            for ( int i = 0 ; i < mItems.size() ; i ++ ) {
                API55.ProductItem model = mItems.get(i);
                if ( idx.equals(model.idx ) ) {
                    int count = model.wish_cnt;
                    if ( "Y".equals(model.is_wish) && !status ) {
                        if ( count > 0 )
                            count = count - 1;
                        model.is_wish = "N";
                        model.wish_cnt = count;
                    } else if ( "N".equals(model.is_wish) && status ){
                        count = count + 1;
                        model.is_wish = "Y";
                        model.wish_cnt = count;
                    }
                    mItems.remove(i);
                    mItems.add(i, model);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeZzim(String idx) {
        try {
            for ( int i = 0 ; i < mItems.size() ; i ++ ) {
                API55.ProductItem model = mItems.get(i);
                if ( idx.equals(model.idx ) ) {
                    int count = model.wish_cnt;
                    if ( "Y".equals(model.is_wish) ) {
                        if ( count > 0 )
                            count = count - 1;
                        model.is_wish = "N";
                        model.wish_cnt = count;
                    } else {
                        count = count + 1;
                        model.is_wish = "Y";
                        model.wish_cnt = count;
                    }
                    mItems.remove(i);
                    mItems.add(i, model);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteChecked() {
        int delCount = 0;
        try {
            for (Iterator<API55.ProductItem> it = mItems.iterator(); it.hasNext(); ) {
                API55.ProductItem model = it.next();
                if ( model.isSelect ) {
                    it.remove();
                    delCount ++;
                }
            }

            if (listener != null) {
                listener.setDeletedCount(delCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    public void setCheckVisible(int visiblity) {
        if (mItems != null && mItems.size() > 0) {
            for (int i = 0; i < mItems.size(); i++) {
                API55.ProductItem item = mItems.get(i);
                if (visiblity == View.VISIBLE)
                    item.isShow = true;
                else
                    item.isShow = false;

                mItems.remove(i);
                mItems.add(i, item);
            }
        }

        notifyDataSetChanged();
    }

    public void setCheck(boolean checkAll) {
        if (mItems != null && mItems.size() > 0) {
            for (int i = 0; i < mItems.size(); i++) {
                API55.ProductItem item = mItems.get(i);
                item.isSelect = checkAll;

                mItems.remove(i);
                mItems.add(i, item);

                Logger.e( "is show value :: " + mItems.get(i).isSelect);
            }
        }

        notifyDataSetChanged();
    }

    public String getProductId() {
        String idx = "";
        if ( mItems != null && mItems.size() > 0 ) {
            for (Iterator<API55.ProductItem> it = mItems.iterator(); it.hasNext(); ) {
                API55.ProductItem item = it.next();
                if ( item.isSelect )
                    idx += item.idx + ", ";
            }
        }

        return idx;
    }

    public int getSelectedCount() {
        int count = 0;

        if (mItems != null && mItems.size() > 0) {
            for (Iterator<API55.ProductItem> in_it = mItems.iterator(); in_it.hasNext(); ) {
                API55.ProductItem pModel = in_it.next();
                if (pModel.isSelect) {
                    count++;
                }
            }
        }

        return count;
    }
}
