package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-19.
 */
public class API69 {

    /**
     * result : success
     * nTotalCount : 10
     * data : [{"storekey":"6DOZJML47Q","name":"도드비스","image":"https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605680952486999_400.jpg"}]
     */

    public String result;
    public int nTotalCount;
    public List<ShopItem> data;

    public static class ShopItem {
        /**
         * shopKey : 6DOZJML47Q
         * strShopName : 도드비스
         * strImageUrl : https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605680952486999_400.jpg
         */

        public String shopKey;
        public String strShopName;
        public String strImageUrl;
    }
}
