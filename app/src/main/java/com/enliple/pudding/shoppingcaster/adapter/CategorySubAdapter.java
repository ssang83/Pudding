package com.enliple.pudding.shoppingcaster.adapter;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.data.CategoryItem;
import com.enliple.pudding.shoppingcaster.data.MainCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategorySubAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public Listener listener;
    private List<MainCategoryModel.SubItem> mItems = new ArrayList<>();
    public Context context;
    public interface Listener {
        public void setSubCategory(ArrayList<CategoryItem> subArray);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public CategorySubAdapter(Context context) {
        this.context = context;
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_sub_category_item, parent, false);
        return new CategorySelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCategorySelectHolder((CategorySelectViewHolder) holder, position);
    }

    public void setItems(List<MainCategoryModel.SubItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }

        mItems.addAll(items);
        notifyDataSetChanged();
    }

    private void bindCategorySelectHolder(final CategorySelectViewHolder holder ,final int position) {
        final MainCategoryModel.SubItem item = mItems.get(position);
        if ( item == null )
            return;
        holder.secondCategoryName.setText(item.categoryName);

        if ( item.isSelected ) {
            holder.secondIcon.setBackgroundResource(R.drawable.checkbox_on);
        } else {
            holder.secondIcon.setBackgroundResource(R.drawable.checkbox_off);
        }

        holder.second_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = !item.isSelected;
                int selectedCount = getSelectedCount();
                if ( isSelected ) {
                    if ( selectedCount >= 3 ) {
                        Toast.makeText(context, "2차 카테고리는 최대 3개까지 선택이 가능합니다.", Toast.LENGTH_SHORT).show();
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

    static class CategorySelectViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout second_category;
        public AppCompatTextView secondCategoryName, secondIcon;
        public CategorySelectViewHolder(View itemView) {
            super(itemView);
            second_category = itemView.findViewById(R.id.second_category);
            secondCategoryName = itemView.findViewById(R.id.secondCategoryName);
            secondIcon = itemView.findViewById(R.id.secondIcon);
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
            listener.setSubCategory(array);
    }

    private int getSelectedCount() {
        int selected = 0;
        ArrayList<CategoryItem> array = new ArrayList<>();
        for ( int i = 0 ; i < mItems.size() ; i ++ ) {
            if ( mItems.get(i).isSelected ) {
                selected ++;
//                CategoryItem item = new CategoryItem(
//                        0L,
//                        mItems.get(i).categoryId,
//                        mItems.get(i).categoryName, "", "", "");
//                array.add(item);
            }
        }
//        selectedCategoryStr = builder.toString();
//        selectedCategoryCodeStr = codeBuilder.toString();


        return selected;
    }

//    private int getSelectedCount() {
//        int selected = 0;
//        StringBuilder builder = new StringBuilder();
//        StringBuilder codeBuilder = new StringBuilder();
//        for ( int i = 0 ; i < mItems.size() ; i ++ ) {
//            if ( mItems.get(i).isSelected ) {
//                selected ++;
//                builder.append(mItems.get(i).categoryName);
//                codeBuilder.append(mItems.get(i).categoryId);
//                if ( mItems.size() != i ) {
//                    builder.append(",");
//                    codeBuilder.append(",");
//                }
//            }
//        }
//        selectedCategoryStr = builder.toString();
//        selectedCategoryCodeStr = codeBuilder.toString();
//
//        if ( listener != null )
//            listener.setSubCategory(selectedCategoryStr, selectedCategoryCodeStr);
//        return selected;
//    }
}