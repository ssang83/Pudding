package com.enliple.pudding.commons.app;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.enliple.pudding.commons.data.CategoryItem;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.API81;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CategoryModel {
    public static List<CategoryItem> getCategory(Context context, String categoryId) {
        List<CategoryItem> array = null;

        String key = NetworkHandler.Companion.getInstance(context).getKey(NetworkApi.API81.toString(), categoryId, "");
        String str = DBManager.getInstance(context).get(key);
        if (!TextUtils.isEmpty(str)) {
            API81 response = new Gson().fromJson(str, API81.class);
            if (response != null) {
                if (response.data != null && response.data.size() > 0) {
                    array = new ArrayList<>();
                    for (int i = 0; i < response.data.size(); i++) {
                        API81.CategoryItem data = response.data.get(i);
                        array.add(new CategoryItem(0L, data.categoryId, data.categoryName, data.categoryRgb, data.categoryHex, data.categoryImage));
                    }
                }
            }
        }
        return array;
    }

    public static List<API81.CategoryItem> getCategoryList(Context context, String categoryId) {
        List<API81.CategoryItem> array = new ArrayList<>();

        String key = NetworkHandler.Companion.getInstance(context).getKey(NetworkApi.API81.toString(), categoryId, "");
        String str = DBManager.getInstance(context).get(key);
        if (!TextUtils.isEmpty(str)) {
            API81 response = new Gson().fromJson(str, API81.class);
            if (response != null) {
                if (response.data != null && response.data.size() > 0) {
                    for (int i = 0; i < response.data.size(); i++) {
                        array.add(response.data.get(i));
                    }
                }
            }
        }
        return array;
    }

    public static List<API81.CategoryItem> getCategoryList(Context context, SparseArray array) {
        List<API81.CategoryItem> categoryList = new ArrayList<>();

        if (array == null || array.size() == 0) {
            return categoryList;
        }

        API81 response = null;
        String key = NetworkHandler.Companion.getInstance(context).getKey(NetworkApi.API81.toString(), "all", "");
        String str = DBManager.getInstance(context).get(key);
        if (!TextUtils.isEmpty(str)) {
            response = new Gson().fromJson(str, API81.class);
        }
        if (response == null || response.data == null || response.data.size() == 0) {
            Logger.e("data null.  parsing error!!");
            return categoryList;
        }

        for (int i = 0; i < array.size(); i++) {
            for (API81.CategoryItem item : response.data) {
                if (item.categoryId.equals(array.valueAt(i))) {
                    categoryList.add(item);
                }
            }
        }
        return categoryList;
    }

    public static List<API81.CategoryItem> getSelectedCategory(Context context) {
        List<API81.CategoryItem> items = new ArrayList<>();

        API81 response = null;

        String key = NetworkHandler.Companion.getInstance(context).getKey(NetworkApi.API81.toString(), "all", "");
        String str = DBManager.getInstance(context).get(key);
        if (!TextUtils.isEmpty(str)) {
            response = new Gson().fromJson(str, API81.class);
        }
        if (response == null) {
            Logger.e("data null.  parsing error!!");
            return items;
        }

        if (response.data != null && response.data.size() > 0) {
            String[] categoryId = AppPreferences.Companion.getHomeVideoCategoryCode(context).split(",");
            for (int i = 0; i < categoryId.length; i++) {
                for (int j = 0; j < response.data.size(); j++) {
                    if (categoryId[i].equals(response.data.get(j).categoryId)) {
                        items.add(response.data.get(j));
                    }
                }
            }
        }

        return items;
    }
}