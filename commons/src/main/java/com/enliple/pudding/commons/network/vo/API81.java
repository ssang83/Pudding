package com.enliple.pudding.commons.network.vo;


import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-17.
 */
public class API81 {

    public List<CategoryItem> data;

    public static class CategoryItem {
        /**
         * categoryId : 10
         * categoryName : 패션의류
         * categoryImage : http://api.lieon.co.kr/image/category/category_10.png
         */
        public String categoryId;
        public String categoryName;
        public String categoryHex;
        public String categoryRgb;
        public String categoryImage;
        public List<SubItem> sub;
    }

    public static class SubItem {
        public String categoryId;
        public String categoryName;
        public List<ThirdItem> sub;
    }

    public static class ThirdItem {
        public String categoryId;
        public String categoryName;
    }
}
