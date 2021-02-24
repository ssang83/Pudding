package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class VOD {
    public int nTotalCount;
    public int pageCount;
    public List<DataBeanX> data;
    public VODSummary summary;

    public static class DataBeanX {
        public String strChatlog;
        public String share_user;
        public String share_space;
        public String share_date;
        public String id;
        public String videoType;
        public String isOnAir;
        public String liveVod;
        public String userId;
        public String stream;
        public String streamHls;
        public String userNick;
        public String userImage;
        public String strChatKey;
        public String isFavorite;
        public String scrapCount;
        public String strRegDate;
        public String isScrap;
        public String videoRato;
        public String viewerCount;
        public String categoryCode;
        public String title;
        public String favoriteCount;
        public String shareCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String user_show_YN;
        public String share_YN;     // 공유 여부 Y or N
        public String comment_YN;   // 댓글 허용 여부 Y or N
        public String strContentSize;  // 영상사이즈 [v:세로, h:가로]
        public String comment_cnt;
        public String min_price;
        public String min_price_product;
        public String shop_total_cnt;
        public String shop_total_price;
        public String shop_total_fee;
        public String shop_max_video_ratio;
        public String link_total_click;
        public String link_total_fee;
        public long start_date;
        public RelationPrdBean relationPrd;

        public static class RelationPrdBean {
            public int nTotalCount;
            public List<DataBean> data;

            public static class DataBean {
                public String idx;
                public String sc_code;
                public String pcode;
                public String sc_name;
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
                public String strLinkUrl;
                public String isUse;
                public String isSoldOut;
                public String nPrdStockQty;
                public String wish_cnt;     // 찜 갯수
                public String is_wish;      // 찜 여부
                public String is_cart;      // 장바구니 여부
            }
        }
    }

    public static class VODSummary {
        public String total_order;
        public String total_hit;
        public String total_share;
        public String total_favor;
        public String total_scrap;
    }
}

