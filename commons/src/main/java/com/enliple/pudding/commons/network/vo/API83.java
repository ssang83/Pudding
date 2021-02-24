package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-12.
 */
public class API83 {

    /**
     * result : success
     * nTotalCount : 8
     * data : [{"it_id":"243  // 상품 INDEX","option":"100/블루  // 구매 상품 옵션","title":"도트 랩 롱 원피스  // 상품 이름","image1":"https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1602679623363419_400.gif    // 상품 이미지 URL","image_width":"600  // 상품 이미지 가로 사이즈","image_height":"600 // 상품 이지미 세로 사이즈","cnt":"2 // 상품 구매 수량"}]
     */

    public String result;
    public int nTotalCount;
    public List<ProductItem> data;

    public static class ProductItem {
        /**
         * it_id : 243  // 상품 INDEX
         * option : 100/블루  // 구매 상품 옵션
         * title : 도트 랩 롱 원피스  // 상품 이름
         * image1 : https://images-stage.shaptree.com/images/stores/Jo7nRpZwye/products/1602679623363419_400.gif    // 상품 이미지 URL
         * image_width : 600  // 상품 이미지 가로 사이즈
         * image_height : 600 // 상품 이지미 세로 사이즈
         * cnt : 2 // 상품 구매 수량
         */

        public String it_id;
        public String option;
        public String title;
        public String image1;
        public String image_width;
        public String image_height;
        public String cnt;
        public String it_stock_qty;     // 재고수량
    }
}
