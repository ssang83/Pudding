package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-03-13.
 */
public class API127 {

    /**
     * result : success
     * goods_cnt : 2
     * total_cnt : 2
     * goodsList : [{"idx":"6","sc_code":"17mqL938OE","sitename":"유찬몰","pcode":"W5ezdVDKbk","image1":"https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg","width":"1200","height":"1200","title":"[무료배송] 갤럭시S9, S9플러스 우레탄곡면 풀커버2매","orgprice":"15500","price":"15500","hit":"902","it_time":"0000-00-00 00:00:00","it_stock_qty":"321","shoptree_yn":"Y","is_wish":"Y"},{"idx":"9","sc_code":"17mqL938OE","sitename":"유찬몰","pcode":"j2eoEojKrm","image1":"https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896564788224_400.jpg","width":"1200","height":"1200","title":"[무료배송] 나비 메탈 젤리 케이스/LG G5(F700)","orgprice":"21000","price":"21000","hit":"258","it_time":"0000-00-00 00:00:00","it_stock_qty":"10","shoptree_yn":"Y","is_wish":"Y"}]
     */

    public String result;
    public int goods_cnt;
    public String total_cnt;
    public List<ProductItem> goodsList;

    public static class ProductItem {
        /**
         * idx : 6
         * sc_code : 17mqL938OE
         * sitename : 유찬몰
         * pcode : W5ezdVDKbk
         * image1 : https://images.shop-tree.com/images/stores/17mqL938OE/products/1620896730081038_400.jpg
         * width : 1200
         * height : 1200
         * title : [무료배송] 갤럭시S9, S9플러스 우레탄곡면 풀커버2매
         * orgprice : 15500
         * price : 15500
         * hit : 902
         * it_time : 0000-00-00 00:00:00
         * it_stock_qty : 321
         * shoptree_yn : Y
         * is_wish : Y
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
        public String hit;
        public String it_time;
        public String it_stock_qty;
        public String shoptree_yn;
        public String is_wish;
        public boolean isSelect = false;
    }
}
