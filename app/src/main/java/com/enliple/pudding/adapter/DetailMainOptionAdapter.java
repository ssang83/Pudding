package com.enliple.pudding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.model.DetailOptionItem;
import com.enliple.pudding.model.DetailOptionList;
import com.enliple.pudding.model.DetailSubOption;

import java.util.ArrayList;

public class DetailMainOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public MainOptionViewHolder holder;
    private Context context;
    private ArrayList<DetailSubOption> items;
    private ArrayList<DetailOptionItem> fOptionList;
    public Listener listener;
    public DetailOptionList optionList;
    public interface Listener {
        public void onItemClck(DetailOptionList mainOption, DetailSubOption item, int position);
    }

    public DetailMainOptionAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sub_option, parent, false);
        holder = new MainOptionViewHolder(context, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindMainOptionViewHolder((MainOptionViewHolder)holder, position);
    }

    private void bindMainOptionViewHolder(MainOptionViewHolder holder, int position) {
        DetailSubOption option = items.get(position);
        if ( position == items.size() - 1 )
            holder.botLine.setVisibility(View.GONE);
        else
            holder.botLine.setVisibility(View.VISIBLE);

        holder.optionName.setText(option.getOptionname());

        if ( fOptionList != null && fOptionList.size() > 0 ) {
            for ( int i = 0 ; i < fOptionList.size() ;  i ++ ) {
                DetailOptionItem item = fOptionList.get(i);
                if (option.getOptionname().equals(item.getName()) ) {
                    int quantity = item.getQuantity();
                    holder.optionQty.setText("" + quantity + "개");
                }
            }
        }

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( listener != null ) {
                    listener.onItemClck(optionList, items.get(position), position);
                }
            }
        });
    }

    public class MainOptionViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout item;
        public AppCompatTextView optionName, optionQty;
        public View botLine;
        public MainOptionViewHolder(Context context, View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            optionName = itemView.findViewById(R.id.optionName);
            optionQty = itemView.findViewById(R.id.optionQty);
            botLine = itemView.findViewById(R.id.botLine);
        }
    }


    public void setItems(DetailOptionList option, ArrayList<DetailOptionItem> fOptionList) {
        if ( items == null ) {
            items = new ArrayList<>();
        } else {
            items.clear();
        }
        optionList = option;
        items.addAll(option.getSubOption());
        this.fOptionList = fOptionList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
