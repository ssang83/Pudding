package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-27.
 */
public class API55 {

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"reg_date":"2018-12-27","products":[{"it_id":"231","sc_code":"Jo7nRpZwye","store_name":"유찬몰","pcode":"KvQeRp63zy","title":"린넨 벤딩 pt","image1":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1603223353871008_400.gif","image_width":"1200","image_height":"1200","price":"100000","discount":"20","live":"Y","vod":"N"},{"it_id":"228","sc_code":"Jo7nRpZwye","store_name":"유찬몰","pcode":"7ln92dEZjq","title":"한여름에 입기좋은 얇은소재의 화이트 썸머 자켓","image1":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1603936607406274_400.jpg","image_width":"1200","image_height":"1200","price":"300","discount":"","live":"N","vod":"N"}]},{"reg_date":"2018-12-24","products":[{"it_id":"6","sc_code":"Jo7nRpZwye","store_name":"유찬몰","pcode":"B02ez5gZjE","title":"미니 피규어 세트","image1":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1605843950331292_400.jpg","image_width":"1200","image_height":"1200","price":"70000","discount":"14","live":"Y","vod":"N"},{"it_id":"249","sc_code":"Jo7nRpZwye","store_name":"유찬몰","pcode":"z5Q30g9Kbw","title":"123123","image1":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1602397498661549_400.jpg","image_width":"1200","image_height":"1200","price":"123123456","discount":"","live":"N","vod":"N"},{"it_id":"267","sc_code":"Jo7nRpZwye","store_name":"유찬몰","pcode":"7ln92EejqK","title":"Test product","image1":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1600405924983683_400.jpg","image_width":"1200","image_height":"1200","price":"3000","discount":"","live":"N","vod":"N"}]}]
     * pageCount : 0
     */

    public String result;
    public int nTotalCount;
    public int pageCount;
    public List<ProductItem> data;

    public static class ProductItem {
        /**
         * it_id : 231
         * sc_code : Jo7nRpZwye
         * store_name : 유찬몰
         * pcode : KvQeRp63zy
         * title : 린넨 벤딩 pt
         * image1 : https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1603223353871008_400.gif
         * image_width : 1200
         * image_height : 1200
         * price : 100000
         * discount : 20
         * live : Y
         * vod : N
         */

        public String idx;
        public String sc_code;
        public String store_name;
        public String pcode;
        public String title;
        public String image1;
        public String image_width;
        public String image_height;
        public String price;
        public String discount;
        public String live;
        public String vod;
        public String strType;
        public boolean isSelect = false;
        public boolean isShow = false;
        public int wish_cnt;
        public String is_wish;
        public String it_stock_qty;
    }
}
