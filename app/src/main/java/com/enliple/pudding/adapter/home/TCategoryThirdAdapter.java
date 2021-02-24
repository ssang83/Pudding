package com.enliple.pudding.adapter.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.shoppingcaster.data.MainCategoryModel;

import java.util.ArrayList;
import java.util.List;
import com.enliple.pudding.commons.data.CategoryItem;

public class TCategoryThirdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public Listener listener;
    private List<MainCategoryModel.ThirdItem> mItems = new ArrayList<>();
    public Context context;
    public interface Listener {
        public void setThirdCategory(ArrayList<CategoryItem> subArray);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public TCategoryThirdAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_sub_category_item, parent, false);
        return new ThirdCategorySelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindThirdCategorySelectHolder((ThirdCategorySelectViewHolder) holder, position);
    }

    public void setItems(List<MainCategoryModel.ThirdItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);
        notifyDataSetChanged();
    }

    private void bindThirdCategorySelectHolder(final ThirdCategorySelectViewHolder holder ,final int position) {
        final MainCategoryModel.ThirdItem item = mItems.get(position);
        if ( item == null )
            return;
        holder.secondCategoryName.setText(item.categoryName);

        if ( item.isSelected ) {
            holder.secondIcon.setBackgroundResource(R.drawable.checkbox_on);
        } else {
            holder.secondIcon.setBackgroundResource(R.drawable.checkbox_off);
        }

        if ( position == mItems.size() )
            holder.uLine.setVisibility(View.VISIBLE);
        else
            holder.uLine.setVisibility(View.GONE);

        holder.second_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = !item.isSelected;
                int selectedCount = getSelectedCount();
                if ( isSelected ) {
                    if ( selectedCount >= 3 ) {
                        Toast.makeText(context, "3차 카테고리는 최대 3개까지 선택이 가능합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        item.isSelected = isSelected;
                        mItems.set(position, item);
                        setSelectedCategory();
                        notifyDataSetChanged();
                    }
                } else {
                    item.isSelected = isSelected;
                    mItems.set(position, item);
                    setSelectedCategory();
                    notifyDataSetChanged();
                }
            }
        });
    }

    static class ThirdCategorySelectViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout second_category;
        public AppCompatTextView secondCategoryName, secondIcon;
        public View uLine;
        public ThirdCategorySelectViewHolder(View itemView) {
            super(itemView);
            second_category = itemView.findViewById(R.id.second_category);
            secondCategoryName = itemView.findViewById(R.id.secondCategoryName);
            secondIcon = itemView.findViewById(R.id.secondIcon);
            uLine = itemView.findViewById(R.id.uLine);
        }
    }

    private void setSelectedCategory() {
        ArrayList<CategoryItem> array = new ArrayList<>();
        for ( int i = 0 ; i < mItems.size() ; i ++ ) {
            if ( mItems.get(i).isSelected ) {
                CategoryItem item = new CategoryItem(
                        0L,
                        mItems.get(i).categoryId,
                        mItems.get(i).categoryName, "", "", "");
                array.add(item);
            }
        }
        if ( listener != null )
            listener.setThirdCategory(array);
    }

    private int getSelectedCount() {
        int selected = 0;
        ArrayList<CategoryItem> array = new ArrayList<>();
        for ( int i = 0 ; i < mItems.size() ; i ++ ) {
            if ( mItems.get(i).isSelected ) {
                selected ++;
            }
        }
        return selected;
    }
}
