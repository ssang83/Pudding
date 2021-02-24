package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-05-27.
 */
public class API145 {

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"idx":6,"sc_code":"17mqL938OE","sitename":"유찬몰","pcode":"W5ezdVDKbk","image1":"https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg","width":1200,"height":1200,"title":"갤럭시S9 S9플러스 우레탄곡면 풀커버","orgprice":15500,"price":15500,"it_sc_type":2,"hit":991,"it_time":"2018-12-26T15:59:40.000Z","it_stock_qty":388,"fee_income":10,"shoptree_yn":"Y","pointRate":1,"strType":1,"category":"휴대폰 액세사리","link_url":{},"is_soldout":0,"is_use":1,"feetype":"조건부 무료","review_avg":0,"review_cnt":0,"wish_cnt":1,"is_wish":"Y","is_cart":"N","discount":{}}]
     */

    public String result;
    public String nTotalCount;
    public List<ProductItem> data;

    public static class ProductItem {
        /**
         * idx : 6
         * sc_code : 17mqL938OE
         * sitename : 유찬몰
         * pcode : W5ezdVDKbk
         * image1 : https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg
         * width : 1200
         * height : 1200
         * title : 갤럭시S9 S9플러스 우레탄곡면 풀커버
         * orgprice : 15500
         * price : 15500
         * it_sc_type : 2
         * hit : 991
         * it_time : 2018-12-26T15:59:40.000Z
         * it_stock_qty : 388
         * fee_income : 10
         * shoptree_yn : Y
         * pointRate : 1
         * strType : 1
         * category : 휴대폰 액세사리
         * link_url : {}
         * is_soldout : 0
         * is_use : 1
         * feetype : 조건부 무료
         * review_avg : 0
         * review_cnt : 0
         * wish_cnt : 1
         * is_wish : Y
         * is_cart : N
         * discount : {}
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
        public String it_sc_type;
        public String hit;
        public String it_time;
        public String it_stock_qty;
        public String fee_income;
        public String shoptree_yn;
        public String pointRate;
        public String strType;
        public String category;
        public String link_url;
        public String is_soldout;
        public String is_use;
        public String feetype;
        public String review_avg;
        public String review_cnt;
        public String wish_cnt;
        public String is_wish;
        public String is_cart;
        public String discount;
        public boolean isSelect = false;
    }
}
