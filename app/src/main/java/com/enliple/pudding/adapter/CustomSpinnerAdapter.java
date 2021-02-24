package com.enliple.pudding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.model.SpinnerSelectModel;

import java.util.ArrayList;
import java.util.List;

public class CustomSpinnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<SpinnerSelectModel> categories;
    private boolean isInit = false;
    private Listener listener;
    public interface Listener {
        public void onItemClicked(String category);
    }

    public CustomSpinnerAdapter(Context context, List<SpinnerSelectModel> categories, Listener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_spinner_item, parent, false);
        return new SpinnerCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindCategoryHolder((SpinnerCategoryViewHolder) holder, position);
    }

    private void bindCategoryHolder(SpinnerCategoryViewHolder holder, int position) {
        SpinnerSelectModel ct = categories.get(position);
        holder.category.setText(ct.getCategory());

        if ( ct.isSelected() ) {
            holder.check.setBackgroundResource(R.drawable.radio_btn_on);
        } else {
            holder.check.setBackgroundResource(R.drawable.radio_btn_off);
        }

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SpinnerSelectModel> tArray = new ArrayList<>();
                tArray.addAll(categories);
                for ( int i = 0 ; i < tArray.size() ; i ++ ) {
                    if ( ct == tArray.get(i) ) {
                        SpinnerSelectModel model = new SpinnerSelectModel();
                        model.setCategory(categories.get(i).getCategory());
                        model.setSelected(true);
                        categories.set(i, model);
                    } else {
                        SpinnerSelectModel model = new SpinnerSelectModel();
                        model.setCategory(categories.get(i).getCategory());
                        model.setSelected(false);
                        categories.set(i, model);
                    }
                }
                if ( listener != null )
                    listener.onItemClicked(ct.getCategory());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (categories != null && categories.size() > 0 )
            return categories.size();
        else
            return 0;
    }

    static class SpinnerCategoryViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView category;
        public AppCompatImageView check;
        public RelativeLayout root;
        public SpinnerCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            category = itemView.findViewById(R.id.category);
            check = itemView.findViewById(R.id.check);
        }
    }
}
