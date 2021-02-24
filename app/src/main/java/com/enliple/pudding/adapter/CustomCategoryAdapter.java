package com.enliple.pudding.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enliple.pudding.R;
import com.enliple.pudding.adapter.home.FirstCategoryAdapter;
import com.enliple.pudding.adapter.home.SecondCategoryAdapter;
import com.enliple.pudding.adapter.home.ThirdCategoryAdapter;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.SecondCategoryItem;
import com.enliple.pudding.model.ThirdCategoryItem;
import com.enliple.pudding.model.ThreeCategoryItem;

import java.util.ArrayList;
import java.util.List;

public class CustomCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FIRST_CATEGORY = 0X1001;
    private static final int TYPE_SECOND_CATEGORY = 0X2001;
    private static final int TYPE_THIRD_CATEGORY = 0X3001;

    public ItemFirstCategoryViewHolder itemFirstCategoryViewHolder;
    public ItemSecondCategoryViewHolder itemSecondCategoryViewHolder;
    public ItemThirdCategoryViewHolder itemThirdCategoryViewHolder;

    private Context context;
    private List<ThreeCategoryItem> categoryItems = new ArrayList<>();
    public Listener listener;
    private String selectedFirstCategory = null;
    private String selectedSecondCategory = null;
    private String selectedThirdCategory = null;
    private String categoryId;
    private boolean isLastLineVisible = true;

    public interface Listener {
        public void categoryClicked(String categoryId);
    }

    public CustomCategoryAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FIRST_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_first_category, parent, false);
            itemFirstCategoryViewHolder = new ItemFirstCategoryViewHolder(context, view);
            return itemFirstCategoryViewHolder;
        } else if (viewType == TYPE_SECOND_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_second_category, parent, false);
            itemSecondCategoryViewHolder = new ItemSecondCategoryViewHolder(context, view);
            return itemSecondCategoryViewHolder;
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_third_category, parent, false);
            itemThirdCategoryViewHolder = new ItemThirdCategoryViewHolder(context, view);
            return itemThirdCategoryViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemFirstCategoryViewHolder) {
            bindFirstCategoryViewHolder((ItemFirstCategoryViewHolder) holder);
        } else if (holder instanceof ItemSecondCategoryViewHolder) {
            bindSecondCategoryViewHolder((ItemSecondCategoryViewHolder) holder);
        } else {
            bindThirdCategoryViewHolder((ItemThirdCategoryViewHolder) holder);
        }
    }

    private void bindFirstCategoryViewHolder(ItemFirstCategoryViewHolder holder) {
        if (holder.firstAdapter != null) {
            holder.firstAdapter.setItems(categoryItems);
            if (categoryId != null && !TextUtils.isEmpty(categoryId)) {
                RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) holder.firstCategoryLayer.getLayoutParams();
                param.height = 0;
                holder.firstCategoryLayer.setLayoutParams(param);
            } else {
                RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) holder.firstCategoryLayer.getLayoutParams();
                param.height = Utils.ConvertDpToPx(context, 84);
                holder.firstCategoryLayer.setLayoutParams(param);
            }
        }
    }

    private void bindSecondCategoryViewHolder(ItemSecondCategoryViewHolder holder) {
    }

    private void bindThirdCategoryViewHolder(ItemThirdCategoryViewHolder holder) {
        if (categoryId != null && !TextUtils.isEmpty(categoryId)) {
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
                if (itemSecondCategoryViewHolder != null) {
                    itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.GONE);
                    itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.GONE);
                }

                for (int i = 0; i < categoryItems.size(); i++) {
                    if (categoryId.equals(categoryItems.get(i).getCategoryId())) {
                        ArrayList<SecondCategoryItem> arr = categoryItems.get(i).getSecondCategory();
                        if (itemSecondCategoryViewHolder.secondAdapter != null) {
                            if (arr == null) {
                                itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.GONE);
                                itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.GONE);
                            } else {
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
                for (int i = 0; i < categoryItems.size(); i++) {
                    if (categoryId.equals(categoryItems.get(i).getCategoryId())) {
                        ArrayList<SecondCategoryItem> arr = categoryItems.get(i).getSecondCategory();
                        if (itemSecondCategoryViewHolder.secondAdapter != null) {
                            if (arr == null) {
                                itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.GONE);
                                itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.GONE);
                            } else {
                                itemSecondCategoryViewHolder.secondCategoryLayer.setVisibility(View.VISIBLE);
                                itemSecondCategoryViewHolder.recyclerViewSecondCategory.setVisibility(View.VISIBLE);
                                itemSecondCategoryViewHolder.secondAdapter.setItems(arr);
                            }

                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    itemSecondCategoryViewHolder.recyclerViewSecondCategory.scrollToPosition(0);
                                    if (itemThirdCategoryViewHolder != null) {
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
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void setCategoryItem(ArrayList<ThreeCategoryItem> items, String categoryId, boolean isLastLineVisible) {
        this.isLastLineVisible = isLastLineVisible;
        if (categoryItems != null) {
            categoryItems.clear();
        }

        categoryItems.addAll(items);
        this.categoryId = categoryId;

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_FIRST_CATEGORY;
        } else if (position == 1) {
            return TYPE_SECOND_CATEGORY;
        } else {
            return TYPE_THIRD_CATEGORY;
        }
    }

    public class ItemFirstCategoryViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView recyclerViewFirstCategory;
        public FirstCategoryAdapter firstAdapter;
        public LinearLayout firstCategoryLayer;

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
                        Logger.e("CustomCategoryAdapter categoryClicked id f :: " + categoryId);
                        listener.categoryClicked(categoryId);
                    } else {
                        Logger.e("CustomCategoryAdapter listener null f ");
                    }
                    notifyDataSetChanged();
                }
            });
            firstCategoryLayer = itemView.findViewById(R.id.firstCategoryLayer);
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
                            Logger.e("isLastLineVisible :: " + isLastLineVisible);
                            if (isLastLineVisible)
                                itemThirdCategoryViewHolder.s_line.setVisibility(View.VISIBLE);
                            else
                                itemThirdCategoryViewHolder.s_line.setVisibility(View.GONE);
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
                        Logger.e("CustomCategoryAdapter categoryClicked id s :: " + categoryId);
                        listener.categoryClicked(categoryId);
                    } else {
                        Logger.e("CustomCategoryAdapter listener null s ");
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
                        Logger.e("CustomCategoryAdapter categoryClicked id t:: " + categoryId);
                        listener.categoryClicked(categoryId);
                    } else {
                        Logger.e("CustomCategoryAdapter listener null t");
                    }
                }
            });
            thirdCategoryLayer = itemView.findViewById(R.id.thirdCategoryLayer);
            f_line = itemView.findViewById(R.id.f_line);
            s_line = itemView.findViewById(R.id.s_line);
            recyclerViewThirdCategory = itemView.findViewById(R.id.recyclerViewThirdCategory);
            WrappedGridLayoutManager layoutManager = new WrappedGridLayoutManager(context, 2);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerViewThirdCategory.setHasFixedSize(false);
            recyclerViewThirdCategory.setLayoutManager(layoutManager);
            recyclerViewThirdCategory.setAdapter(thirdAdapter);
        }
    }
}
