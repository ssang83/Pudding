package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-06.
 */
public class API90 {

    /**
     * result : success
     * nTotalCount : 1  // 상품 총 갯수
     * pageCount : 1 // 현재 페이지
     * nVodTotalCount : 0  // 검색 영상 갯수
     * nTagTotalCount : 0  // 검색 태그 갯수
     * nUserTotalCount : 0 // 검색 유저 갯수
     * nProductTotalCount : 0 // 검색 상품 갯수
     * nFormationTotalCount : 0 // 편성표 정보 갯수
     * data : [{"idx":6,"sc_code":"17mqL938OE","sitename":"유찬몰","pcode":"W5ezdVDKbk","image1":"https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg","width":1200,"height":1200,"title":"갤럭시S9 S9플러스 우레탄곡면 풀커버2매","orgprice":15500,"price":15500,"it_sc_type":2,"hit":971,"it_time":"2018-12-26T15:59:40.000Z","it_stock_qty":388,"fee_income":10,"shoptree_yn":"Y","pointRate":1,"strType":1,"category":"휴대폰 액세사리","link_url":{},"is_soldout":0,"is_use":1,"feetype":"조건부 무료","review_avg":0,"review_cnt":0,"wish_cnt":1,"is_wish":"Y","is_cart":"N","discount":{}}]
     */

    public String result;
    public String nTotalCount;
    public String pageCount;
    public String nVodTotalCount;
    public String nTagTotalCount;
    public String nUserTotalCount;
    public String nProductTotalCount;
    public String nFormationTotalCount;
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
         * title : 갤럭시S9 S9플러스 우레탄곡면 풀커버2매
         * orgprice : 15500
         * price : 15500
         * it_sc_type : 2
         * hit : 971
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
        public int width;
        public int height;
        public String title;
        public int orgprice;
        public int price;
        public int it_sc_type;
        public int hit;
        public String it_time;
        public int it_stock_qty;
        public int fee_income;
        public String shoptree_yn;
        public int pointRate;
        public int strType;
        public String category;
        public String link_url;
        public int is_soldout;
        public int is_use;
        public String feetype;
        public int review_avg;
        public int review_cnt;
        public int wish_cnt;
        public String is_wish;
        public String is_cart;
        public String discount;
    }
}
