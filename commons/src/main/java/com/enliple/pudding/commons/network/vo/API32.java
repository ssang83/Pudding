package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
public class API32 {
    public int nTotalCount;
    public int pageCount;
    public List<DataBeanX> data;

    public static class DataBeanX {
        public String strChatlog;
        public String id;
        public String videoType;
        public String isOnAir;
        public String liveVod;
        public String userId;
        public String stream;
        public String streamHls;
        public String userNick;
        public String userImage;
        public Object strChatKey;
        public String strContentSize;
        public String isFavorite;
        public String scrapCount;
        public String shareCount;
        public String isScrap;
        public String videoRato;
        public int viewerCount;
        public String categoryCode;
        public String title;
        public int favoriteCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String show_YN;
        public String user_show_YN;
        public String share_YN;
        public String comment_YN;
        public String min_price;
        public String min_price_product;
        public String comment_cnt;
        public String order_cnt;
        public String order_price;
        public RelationPrdBean relationPrd;

        public static class RelationPrdBean {
            public int nTotalCount;
            public List<DataBean> data;

            public static class DataBean {
                public String idx;
                public String sc_code;
                public String pcode;
                public String sc_name;
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
                public String strLinkUrl;
                public String wish_cnt;     // 찜 갯수
                public String is_wish;      // 찜 여부
                public String is_cart;      // 장바구니 여부
            }
        }
    }
}
