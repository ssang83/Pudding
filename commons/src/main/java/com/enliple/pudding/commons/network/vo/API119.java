package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-02-21.
 */
public class API119 {

    /**
     * result : success
     * nTotalCount : 10
     * data : [{"storekey":"6DOZJML47Q","name":"도드비스","image":"https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605680952486999_400.jpg"}]
     */

    public String result;
    public int nTotalCount;
    public List<DataItem> data;

    public static class DataItem {
        /**
         * storekey : 6DOZJML47Q
         * name : 도드비스
         * image : https://images-stage.shaptree.com/images/stores/z04vp8vERx/products/1605680952486999_400.jpg
         */

        public String shopKey;
        public String strShopName;
        public String strImageUrl;
    }
}
