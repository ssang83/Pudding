package com.enliple.pudding.adapter.shoptree;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.vo.API91;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim Joonsung on 2018-05-31.
 */

public class QnaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0X2001;
    private static final int VIEW_TYPE_FOOTER = 0X3001;

    private Context mContext;
    private AdapterListener mListener;

    private List<API91.QnaItem> mItems = new ArrayList<>();

    private boolean isEndOfData = false;                          // 더 이상 받을 데이터가 없는 경우 True

    public QnaAdapter(Context context) {
        this.mContext = context;

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems != null && position == mItems.size() && !isEndOfData) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_qna_item, parent, false);
            return new QnAItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_qna_footer, parent, false);
            return new QnAFooterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof QnAFooterViewHolder) {
            bindQnAFooterHolder((QnAFooterViewHolder) holder);
        } else {
            bindQnAItemViewHoldeR((QnAItemViewHolder) holder, position);
        }
    }

    @Override
    public int getItemCount() {
        if (mItems.size() > 0) {
            return mItems.size();
        } else {
            return 1;
        }
    }

    @Override
    public long getItemId(int position) {
        if (hasStableIds()) {
            return position;
        } else {
            return 0;
        }
    }

    /**
     * 해당 Adapter Position 에 위치한 QnA 데이터를 반환
     *
     * @param position
     * @return
     */
    public API91.QnaItem getItemByPosition(int position) {
        return mItems != null && position < mItems.size() ? mItems.get(position) : null;
    }

    /**
     * 더보기 (Load More) 전용 FooterViewHolder 를 Binding
     *
     * @param holder
     */
    private void bindQnAFooterHolder(final QnAFooterViewHolder holder) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onLoadMoreClicked();
            }
        });
    }

    /**
     * QnA 아이템 ViewHolder를 Binding
     *
     * @param holder
     * @param position
     */
    private void bindQnAItemViewHoldeR(final QnAItemViewHolder holder, final int position) {
        final API91.QnaItem data = getItemByPosition(position);
        if (data == null)
            return;
        if ( position == (mItems.size() - 1) )
            holder.root.setBackgroundResource(R.drawable.background_silver_rectangle_border_bot);
        else
            holder.root.setBackgroundResource(R.drawable.background_silver_rectangle_border);

        holder.textViewBuyer.setText(data.iq_name);

        String[] date = data.iq_time.split(" ");
        holder.textViewRegDate.setText(date[0]);
        holder.textViewType.setText(data.iq_type);

        if(data.iq_secret.equals("0")) {
            holder.imageViewSecret.setVisibility(View.GONE);
            holder.textViewComment.setText(data.iq_question);
        } else {
            holder.imageViewSecret.setVisibility(View.VISIBLE);
            holder.textViewComment.setText("비밀글입니다");
        }

        if (data.answer.size() > 0) {
            holder.viewForReplyStatus.setBackgroundResource(R.drawable.bg_reply_complete);
            holder.textViewReplyStatus.setText("답변완료");
            holder.textViewReplyStatus.setTextColor(Color.parseColor("#9f56f2"));

            String[] replyDate = data.answer.get(0).iq_time.split(" ");
            holder.textViewReplyDate.setText(replyDate[0]);
            holder.textViewReplyComment.setText(data.answer.get(0).iq_answer);
        } else {
            holder.viewForReplyStatus.setBackgroundResource(R.drawable.bg_no_reply);
            holder.textViewReplyStatus.setText("미답변");
            holder.textViewReplyStatus.setTextColor(Color.parseColor("#192028"));
        }

        holder.viewForSpread.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                v.setSelected(!v.isSelected());
                if (data.answer != null && data.answer.size() > 0) {
                    if ( holder.viewForReply.getVisibility() == View.VISIBLE ) {
                        holder.viewForReply.setVisibility(View.GONE);
                        holder.viewDashLine.setVisibility(View.GONE);
                    } else {
                        holder.viewForReply.setVisibility(View.VISIBLE);
                        holder.viewDashLine.setVisibility(View.VISIBLE);
                    }
//                    holder.viewForReply.setVisibility(View.VISIBLE);
//                    holder.viewDashLine.setVisibility(View.VISIBLE);
                }
            }
        });
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

    public void addItems(ArrayList<API91.QnaItem> items) {
        int currentItemSize = 0;
        int addedItemSize = items != null ? items.size() : 0;

        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            currentItemSize = mItems.size();
        }

        if (items != null) {
            mItems.addAll(items);
        }

        if (addedItemSize > 0) {
            notifyItemRangeInserted(currentItemSize, addedItemSize);
        }
    }

    public void setItems(List<API91.QnaItem> items) {
        if ( items != null ) {
            for ( int i = 0 ; i < items.size() ; i ++ ) {
                API91.QnaItem item = items.get(i);
            }
        }
        mItems.clear();
        mItems.addAll(items);

        notifyDataSetChanged();
    }

    static class QnAItemViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        public TextView textViewBuyer;
        public TextView textViewRegDate;
        public TextView textViewComment;
        public TextView textViewReplyDate;
        public TextView textViewReplyComment;
        public TextView textViewType;
        public TextView textViewReplyStatus;
        public ImageView imageViewSecret;
        public View viewForReply;
        public View viewDashLine;
        public View viewForReplyStatus;
        public View viewForSpread;

        public QnAItemViewHolder(View itemView) {
            super(itemView);
            root = (LinearLayout) itemView.findViewById(R.id.root);
            textViewBuyer = (TextView) itemView.findViewById(R.id.textViewBuyer);
            textViewRegDate = (TextView) itemView.findViewById(R.id.textViewRegDate);
            textViewType = (TextView) itemView.findViewById(R.id.textViewType);
            textViewComment = (TextView) itemView.findViewById(R.id.textViewComment);
            textViewReplyDate = (TextView) itemView.findViewById(R.id.textViewReplyDate);
            textViewReplyComment = (TextView) itemView.findViewById(R.id.textViewReplyComment);
            textViewReplyStatus = (TextView) itemView.findViewById(R.id.textViewReplyStatus);
            imageViewSecret = itemView.findViewById(R.id.imageViewSecret);
            viewForReply = itemView.findViewById(R.id.layoutForReply);
            viewDashLine = itemView.findViewById(R.id.viewDashLine);
            viewForReplyStatus = itemView.findViewById(R.id.layoutReplyStatus);
            viewForSpread = itemView.findViewById(R.id.layoutSpread);
        }
    }

    static class QnAFooterViewHolder extends RecyclerView.ViewHolder {
        public QnAFooterViewHolder(View itemView) {
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
