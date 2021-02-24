package com.enliple.pudding.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.enliple.pudding.R;
import com.enliple.pudding.adapter.CustomCategoryAdapter;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkConst;
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager;
import com.enliple.pudding.model.SecondCategoryItem;
import com.enliple.pudding.model.ThirdCategoryItem;
import com.enliple.pudding.model.ThreeCategoryItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryView extends LinearLayout {

    private Context context;
    private RecyclerView recyclerView;
    private WrappedLinearLayoutManager layoutManager;
    private CustomCategoryAdapter categoryAdapter;
    private CategorySelectListener listener;
    private String firstCategoryId;
    private boolean isLastLineVisible = true;
    public interface CategorySelectListener {
        public void onCategorySelected(String categoryId);
    }

    public void setListener(CategorySelectListener listener) {
        this.listener = listener;
    }

    public CategoryView(Context context) {
        super(context);
        Logger.e("creator 1 ");
        initViews(context);
    }

    public CategoryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Logger.e("creator 2 ");
        initViews(context);
    }

    public CategoryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Logger.e("creator 3 ");
        initViews(context);
    }

    public CategoryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Logger.e("creator 4 ");
        initViews(context);
    }

    private void initViews(Context context) {
        this.context = context;

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.category_view, this, false);
        addView(v);
        categoryAdapter = new CustomCategoryAdapter(context, new CustomCategoryAdapter.Listener() {
            @Override
            public void categoryClicked(String categoryId) {
                Logger.e("CategoryView categoryClicked id :: " + categoryId);
                if ( listener != null )
                    listener.onCategorySelected(categoryId);
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new WrappedLinearLayoutManager(context);
        layoutManager.setOrientation(WrappedLinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(categoryAdapter);
    }

    public void isLastLineVisible(boolean isLastLineVisible ) {
        this.isLastLineVisible = isLastLineVisible;
    }

    public void setItems(String categoryJSON, String firstCategoryId ) {
        Logger.e("SetItems");
        if ( TextUtils.isEmpty(categoryJSON) )
            return;
        if ( firstCategoryId == null || TextUtils.isEmpty(firstCategoryId) ) {
            try {
                JSONObject obj = new JSONObject(categoryJSON);
                JSONArray array = obj.optJSONArray("data");
                ArrayList<ThreeCategoryItem> itemArray = new ArrayList<>();

                // 1차 카테고리 전체를 add
                ThreeCategoryItem firstAll = new ThreeCategoryItem();
                firstAll.setCategoryId("");
                firstAll.setCategoryName("전체");
                firstAll.setSelected(true);
                firstAll.setCategoryImage(NetworkConst.CATEGORY_ALL_IMAGE_API);
                firstAll.setCategoryImageOn(NetworkConst.CATEGORY_ALL_ON_IMAGE_API);
                firstAll.setCategoryImageOff(NetworkConst.CATEGORY_ALL_OFF_IMAGE_API);
                firstAll.setSecondCategory(null);

                itemArray.add(firstAll);

                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.optJSONObject(i);
                        ThreeCategoryItem item = new ThreeCategoryItem();

                        String categoryId = object.optString("categoryId");
                        String categoryName = object.optString("categoryName");
                        String categoryImage = object.optString("categoryImage");
                        String categoryImageOn = object.optString("categoryImageOn");
                        String categoryImageOff = object.optString("categoryImageOff");
                        boolean selected = false;
                        ArrayList<SecondCategoryItem> secondCategoryArray = null;

                        JSONArray secondArray = object.optJSONArray("sub");
                        if (secondArray != null && secondArray.length() > 0) {
                            secondCategoryArray = new ArrayList<>();
                            SecondCategoryItem secondAllItem = new SecondCategoryItem();
                            secondAllItem.setCategoryId(categoryId);
                            secondAllItem.setCategoryName("전체");
                            secondAllItem.setSelected(true);
                            secondAllItem.setThirdCategory(null);

                            secondCategoryArray.add(secondAllItem);

                            for (int j = 0; j < secondArray.length(); j++) {
                                JSONObject secondObject = secondArray.optJSONObject(j);
                                SecondCategoryItem secondItem = new SecondCategoryItem();
                                String s_categoryId = secondObject.optString("categoryId");
                                String s_categoryName = secondObject.optString("categoryName");
                                boolean s_selected = false;

                                ArrayList<ThirdCategoryItem> thirdCategoryArray = null;

                                JSONArray thirdArray = secondObject.optJSONArray("sub");
                                if (thirdArray != null && thirdArray.length() > 0) {
                                    thirdCategoryArray = new ArrayList<>();
                                    ThirdCategoryItem thirdAllItem = new ThirdCategoryItem();
                                    thirdAllItem.setCategoryId(s_categoryId);
                                    thirdAllItem.setCategoryName("전체");
                                    thirdAllItem.setSelected(true);

                                    thirdCategoryArray.add(thirdAllItem);

                                    for (int k = 0; k < thirdArray.length(); k++) {
                                        JSONObject thirdObject = thirdArray.optJSONObject(k);
                                        ThirdCategoryItem thirdItem = new ThirdCategoryItem();
                                        String t_categoryId = thirdObject.optString("categoryId");
                                        String t_categoryName = thirdObject.optString("categoryName");
                                        boolean t_selected = false;

                                        thirdItem.setCategoryId(t_categoryId);
                                        thirdItem.setCategoryName(t_categoryName);
                                        thirdItem.setSelected(t_selected);

                                        thirdCategoryArray.add(thirdItem);
                                    }
                                }
                                secondItem.setCategoryId(s_categoryId);
                                secondItem.setCategoryName(s_categoryName);
                                secondItem.setSelected(s_selected);
                                secondItem.setThirdCategory(thirdCategoryArray);
                                secondCategoryArray.add(secondItem);
                            }
                        }
                        item.setCategoryId(categoryId);
                        item.setCategoryName(categoryName);
                        item.setSelected(selected);
                        item.setCategoryImage(categoryImage);
                        item.setCategoryImageOn(categoryImageOn);
                        item.setCategoryImageOff(categoryImageOff);
                        item.setSecondCategory(secondCategoryArray);
                        itemArray.add(item);
                    }
                }
                categoryAdapter.setCategoryItem(itemArray, firstCategoryId, isLastLineVisible);

                Logger.e("itemArray.size :: " + itemArray.size());

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONObject obj = new JSONObject(categoryJSON);
                JSONArray array = obj.optJSONArray("data");
                ArrayList<ThreeCategoryItem> itemArray = new ArrayList<>();

                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.optJSONObject(i);
                        ThreeCategoryItem item = new ThreeCategoryItem();

                        String categoryId = object.optString("categoryId");
                        String categoryName = object.optString("categoryName");
                        String categoryImage = object.optString("categoryImage");
                        String categoryImageOn = object.optString("categoryImageOn");
                        String categoryImageOff = object.optString("categoryImageOff");
                        if ( firstCategoryId.equals(categoryId) ) {
                            boolean selected = false;
                            ArrayList<SecondCategoryItem> secondCategoryArray = null;

                            JSONArray secondArray = object.optJSONArray("sub");
                            if (secondArray != null && secondArray.length() > 0) {
                                secondCategoryArray = new ArrayList<>();
                                SecondCategoryItem secondAllItem = new SecondCategoryItem();
                                secondAllItem.setCategoryId(categoryId);
                                secondAllItem.setCategoryName("전체");
                                secondAllItem.setSelected(true);
                                secondAllItem.setThirdCategory(null);

                                secondCategoryArray.add(secondAllItem);

                                for (int j = 0; j < secondArray.length(); j++) {
                                    JSONObject secondObject = secondArray.optJSONObject(j);
                                    SecondCategoryItem secondItem = new SecondCategoryItem();
                                    String s_categoryId = secondObject.optString("categoryId");
                                    String s_categoryName = secondObject.optString("categoryName");
                                    boolean s_selected = false;

                                    ArrayList<ThirdCategoryItem> thirdCategoryArray = null;

                                    JSONArray thirdArray = secondObject.optJSONArray("sub");
                                    if (thirdArray != null && thirdArray.length() > 0) {
                                        thirdCategoryArray = new ArrayList<>();
                                        ThirdCategoryItem thirdAllItem = new ThirdCategoryItem();
                                        thirdAllItem.setCategoryId(s_categoryId);
                                        thirdAllItem.setCategoryName("전체");
                                        thirdAllItem.setSelected(true);

                                        thirdCategoryArray.add(thirdAllItem);

                                        for (int k = 0; k < thirdArray.length(); k++) {
                                            JSONObject thirdObject = thirdArray.optJSONObject(k);
                                            ThirdCategoryItem thirdItem = new ThirdCategoryItem();
                                            String t_categoryId = thirdObject.optString("categoryId");
                                            String t_categoryName = thirdObject.optString("categoryName");
                                            boolean t_selected = false;

                                            thirdItem.setCategoryId(t_categoryId);
                                            thirdItem.setCategoryName(t_categoryName);
                                            thirdItem.setSelected(t_selected);

                                            thirdCategoryArray.add(thirdItem);
                                        }
                                    }
                                    secondItem.setCategoryId(s_categoryId);
                                    secondItem.setCategoryName(s_categoryName);
                                    secondItem.setSelected(s_selected);
                                    secondItem.setThirdCategory(thirdCategoryArray);
                                    secondCategoryArray.add(secondItem);
                                }
                            }
                            item.setCategoryId(categoryId);
                            item.setCategoryName(categoryName);
                            item.setSelected(selected);
                            item.setCategoryImage(categoryImage);
                            item.setCategoryImageOn(categoryImageOn);
                            item.setCategoryImageOff(categoryImageOff);
                            item.setSecondCategory(secondCategoryArray);
                            itemArray.add(item);
                        }
                    }
                }
                categoryAdapter.setCategoryItem(itemArray, firstCategoryId, isLastLineVisible);

                Logger.e("itemArray.size :: " + itemArray.size());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
