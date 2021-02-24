package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-19.
 */
public class API70 {

    /**
     * result : success
     * goods_cnt : 10
     * goodsList : [{"idx":"상품 idx","sc_code":"샵트리 상점키","sitename":"샵트리 상점이름","pcode":"샵트리 상품키","image1":"상품 URL","width":"이미지 가로길이","height":"이미지 세로길이","title":"상품명","orgprice":"상품 금액","price":"상품 실제 금액","shoptree_yn":"샵트리 여부"}]
     */

    public String result;
    public int goods_cnt;
    public int total_cnt;
    public List<ProductItem> goodsList;
    public List<EventItem> event;
    public static class EventItem {
        public String ev_id;
        public String ev_title;
        public String ev_type;
        public String ev_content1;
        public String ev_content2;
        public String ev_todate;
        public String main_img;
        public String main_img_width;
        public String main_img_height;
        public String ev_hit;
        public String ev_it_id;
        public String is_tab;
    }
    public static class ProductItem {
        /**
         * idx : 상품 idx
         * sc_code : 샵트리 상점키
         * sitename : 샵트리 상점이름
         * pcode : 샵트리 상품키
         * image1 : 상품 URL
         * width : 이미지 가로길이
         * height : 이미지 세로길이
         * title : 상품명
         * orgprice : 상품 금액
         * price : 상품 실제 금액
         * shoptree_yn : 샵트리 여부
         * it_sc_type : [1:무료배송 2:조건부 무료배송 3:유료배송 4:수량별 부과]
         * is_cart : 장바구니 여부
         */

        public String idx;
        public String sc_code;
        public String sitename;
        public String pcode;
        public String image1;
        public String width;
        public String height;
        public String title;
        public String orgprice;
        public String price;
        public String it_time;
        public String shoptree_yn;
        public String is_wish;
        public String is_cart;
        public String it_sc_type;
        public String it_stock_qty;
        public String hits;
        public String fee_income;
        public String pointRate;
        public String review_avg;
        public String review_cnt;
        public String wish_cnt;
        public String strType;
        public String hit;
    }
}
