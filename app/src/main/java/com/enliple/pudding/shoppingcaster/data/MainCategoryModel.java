package com.enliple.pudding.shoppingcaster.data;

import java.util.List;

public class MainCategoryModel {

    public List<CategoryItem> data;

    public static class CategoryItem {

        /**
         * categoryId : 10
         * categoryName : 패션의류
         * categoryImage : http://api.lieon.co.kr/image/category/category_10.png
         */
        public String categoryId;
        public String categoryName;
        public boolean isSelected;
        public List<SubItem> sub;

    }

    public static class SubItem {
        public String categoryId;
        public String categoryName;
        public boolean isSelected;
        public List<ThirdItem> sub;
    }

    public static class ThirdItem {
        public String categoryId;
        public String categoryName;
        public boolean isSelected;
    }
}