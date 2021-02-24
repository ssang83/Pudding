package com.enliple.pudding.commons.network;

import java.util.List;

/**
 * The type Data.
 */
public class Data {

    private class Base {
        public String status;
        public String message;
        public String code;
    }

    public class Token extends Base {
        public String JWT;
    }

    public class HomeList {
        public int nTotalCount;
        public int pageCount;
        public List<DataBeanX> data;

        public class DataBeanX {
            public String strChatlog;
            public String id;
            public String videoType;
            public String isOnAir;
            public String liveVod;
            public String userId;
            public String stream;
            public String streamHls;
            public String strChatKey;
            public String isFavorite;
            public String videoRato;
            public int viewerCount;
            public String categoryCode;
            public String title;
            public int favoriteCount;
            public String thumbnailUrl;
            //public RelationPrdBean relationPrd;

            public class RelationPrdBean {
                public int nTotalCount;
                public List<DataBean> data;

                public class DataBean {
                    public String idx;
                    public String strSTkey;
                    public String strType;
                    public String strPrdCategory;
                    public String strPrdName;
                    public String strPrdMaker;
                    public String strPrdOrigin;
                    public String strPrdBrand;
                    public String strPrdImg;
                    public String nPrdCustPrice;
                    public String nPrdSellPrice;
                    public String isUse;
                    public String isSoldOut;
                    public String nPrdStockQty;
                }
            }
        }
    }
}


