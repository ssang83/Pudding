package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API49 {
    public String strChatlog;
    public String share_user;
    public String share_space;
    public String share_date;
    public String id;
    public String videoType;
    public String isOnAir;
    public String liveVod;
    public String userId;
    public String userNick;
    public String userImage;
    public String stream;
    public String streamHis;
    public String strChatKey;
    public String strNoti;
    public String isScrap;
    public String isFavorite;
    public int viewerCount;
    public String categoryCode;
    public String title;
    public String notice;
    public int favoriteCount;
    public String scrapCount;
    public String thumbnailUrl;
    public String share_YN;
    public String comment_YN;
    public String user_show_YN;
    public RelationPrdBean relationPrd;

    public static class RelationPrdBean {
        public int nTotalCount;
        public List<DataBean> data;

        public static class DataBean {
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

//public class API49 {
//    public String share_user;
//    public String share_space;
//    public String share_date;
//    public String id;
//    public String videoType;
//    public String isOnAir;
//    public String liveVod;
//    public String userid;
//    public String userNick;
//    public String userImage;
//    public String stream;
//    public String streamHis;
//    public String strChatKey;
//    public String isScrap;
//    public String isFavorite;
//    public String viewerCount;
//    public String categoryCode;
//    public String title;
//    public String notice;
//    public String favoriteCount;
//    public String scrapCount;
//    public String thumbnailUrl;
//    public String share_YN;
//    public String comment_YN;
//    public RelationPrd relationPrd;
//
//    public static class RelationPrd {
//        public String nTotalCount;
//
//        public List<Data> data;
//    }
//
//    public static class Data {
//        public String idx;
//        public String sc_code;
//        public String pcode;
//        public String strType;
//        public String strPrdCategory;
//        public String strPrdName;
//        public String strPrdMaker;
//        public String strPrdOrigin;
//        public String strPrdBrand;
//        public String strPrdImg;
//        public String nPrdCustPrice;
//        public String nPrdSellPrice;
//        public String isUse;
//        public String isSoldOut;
//        public String nPrdStockQty;
//        public String strLinkUrl;
//    }
//}
