package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API34 {
    public int nTotalCount;
    public int pageCount;
    public List<Data> data;

    public static class Data {
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
        public String viewerCount;
        public String shareCount;
        public String categoryCode;
        public String title;
        public String favoriteCount;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String share_YN;     // 공유 여부 Y or N
        public String comment_YN;   // 댓글 허용 여부 Y or N
        public String scrapCount;
        public String isScrap;
        public String strTag;
        public String userNick;
        public String userImage;
        public String strContentSize;  // 영상사이즈 [v:세로, h:가로]
        public String min_price;
        public String min_price_product;
        public String comment_cnt;
        public String order_cnt;
        public String order_price;
        public RelationPrdData relationPrd;

        public static class RelationPrdData {
            public int nTotalCount;
            public List<PrdData> data;

            public static class PrdData {
                public String idx;
                public String sc_code;
                public String sc_name;
                public String pcode;
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
