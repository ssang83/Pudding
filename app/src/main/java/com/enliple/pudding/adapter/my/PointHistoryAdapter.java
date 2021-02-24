package com.enliple.pudding.adapter.my;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.PriceFormatter;
import com.enliple.pudding.commons.network.vo.API101;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim Joonsung on 2018-09-19.
 */

public class PointHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CONTENT = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private List<API101.PointItem> mItems = new ArrayList<>();
    private AdapterListener mListener;
    private boolean isEndOfData = true;                          // 더 이상 받을 데이터가 없는 경우 True
    private Context context;

    public PointHistoryAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItemCount() > 0 && position == getItemCount() - 1 && !isEndOfData)
                ? VIEW_TYPE_FOOTER
                : VIEW_TYPE_CONTENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CONTENT:
                return PointHistoryItemHolder.newInstance(parent);
            case VIEW_TYPE_FOOTER:
                return PointHistoryFooterHolder.newInstance(parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PointHistoryItemHolder) {
            bindPointHistoryItemHolder((PointHistoryItemHolder) holder, position);
        } else if (holder instanceof PointHistoryFooterHolder) {
            bindPointHistoryFooterHolder((PointHistoryFooterHolder) holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return (mItems == null || mItems.size() == 0)
                ? 0                                             // 데이터가 아애 없는 경우에는 더보기를 표시할 이유가 없으므로 0으로 그대로 처리
                : mItems.size() + (isEndOfData ? 0 : 1);        // 데이터가 한개라도 존재하고 추가 요청가능한 데이터가 존재하는 경우 Footer 를 추가
    }

    /**
     * 일반 포인트 히스토리 아이템 ViewHolder 를 Binding
     *
     * @param holder
     * @param position
     */
    private void bindPointHistoryItemHolder(final PointHistoryItemHolder holder, final int position) {
        API101.PointItem data = getItemByPosition(position);

        if (data != null) {
            holder.textViewEventType.setText(data.status);
            holder.textViewDate.setText(data.reg_date);

            holder.textViewAnnotation.setText(data.content);

//            if(!TextUtils.isEmpty(data.od_id)) {
//                holder.textViewAnnotation.setText("주문번호 " + data.od_id);
//                holder.textViewAnnotation.setTextColor(PointHistoryItemHolder.TEXT_COLOR_ANNOTATION);
//            } else {
//                holder.textViewAnnotation.setText("소멸 예정일 " + data.expired_date);
//                holder.textViewAnnotation.setTextColor(PointHistoryItemHolder.TEXT_COLOR_EFFECT);
//            }

            if (data.status.equals("적립")) {
                holder.textViewPointAmount.setText(String.format(context.getString(R.string.msg_price_format),
                        PriceFormatter.Companion.getInstance().getFormattedValue(data.point)));
                holder.textViewPointAmount.setTextColor(PointHistoryItemHolder.TEXT_COLOR_EFFECT);
            } else {
                holder.textViewPointAmount.setText(String.format(context.getString(R.string.msg_price_format),
                        PriceFormatter.Companion.getInstance().getFormattedValue(data.point)));
                holder.textViewPointAmount.setTextColor(PointHistoryItemHolder.TEXT_COLOR_NORMAL);
            }
        }
    }

    /**
     * 더보기 (Load More) 전용 FooterViewHolder 를 Binding
     *
     * @param holder
     * @param position
     */
    private void bindPointHistoryFooterHolder(final PointHistoryFooterHolder holder, final int position) {
        holder.itemView.setOnClickListener((v) -> {
            if (mListener != null) mListener.onLoadMoreClicked();
        });
    }

    /**
     * 해당 Adapter Position 에 위치한 히스토리 데이터를 반환
     *
     * @param position
     * @return
     */
    public API101.PointItem getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    public void addItems(List<API101.PointItem> items) {
        int currentItemSize = 0;
        int addedItemSize = items != null ? items.size() : 0;

        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            currentItemSize = mItems.size();
        }

        mItems.addAll(items);
        if (addedItemSize > 0) {
            notifyItemRangeInserted(currentItemSize, addedItemSize);
        }
    }

    /**
     * Adapter 에서 발생되는 EventListener 를 설정
     *
     * @param listener
     */
    public void setAdapterListener(AdapterListener listener) {
        mListener = listener;
    }

    /**
     * 더보기(Load more)를 할 수 있는 상태인지 설정
     *
     * @param endOfData
     */
    public void setEndOfData(boolean endOfData) {
        this.isEndOfData = endOfData;
        notifyDataSetChanged();
    }


    static class PointHistoryItemHolder extends RecyclerView.ViewHolder {
        static final int TEXT_COLOR_NORMAL = 0xFF546170;
        static final int TEXT_COLOR_EFFECT = 0xFF9f56f2;
        static final int TEXT_COLOR_ANNOTATION = 0xFF8192A5;

        public AppCompatTextView textViewDate;
        public AppCompatTextView textViewContent;
        public AppCompatTextView textViewAnnotation;
        public AppCompatTextView textViewEventType;
        public AppCompatTextView textViewPointAmount;
        public View layoutSplitDivider;

        public static PointHistoryItemHolder newInstance(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_item_point_history_content, parent, false);
            return new PointHistoryItemHolder(v);
        }

        private PointHistoryItemHolder(View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewAnnotation = itemView.findViewById(R.id.textViewAnnotation);
            textViewEventType = itemView.findViewById(R.id.textViewStatus);
            textViewPointAmount = itemView.findViewById(R.id.textViewPointAmount);
            layoutSplitDivider = itemView.findViewById(R.id.layoutSplitDivider);
        }
    }


    static class PointHistoryFooterHolder extends RecyclerView.ViewHolder {

        public static PointHistoryFooterHolder newInstance(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_item_point_history_footer, parent, false);
            return new PointHistoryFooterHolder(v);
        }

        private PointHistoryFooterHolder(View itemView) {
            super(itemView);
        }
    }


    public interface AdapterListener {
        /**
         * 더보기를 클릭 하였음
         */
        void onLoadMoreClicked();
    }
}
