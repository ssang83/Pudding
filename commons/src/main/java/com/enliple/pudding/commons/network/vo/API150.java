package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-04-01.
 */
public class API150 {
    public String result;
    public int nBannerTotalCount;
    public int nTotalCount;
    public List<Banner> banner;
    public List<Data> data;

    public static class Banner {
        public String bn_url;
        public String bn_image;
        public String bn_end_time;
        public String bn_id;
    }

    public static class Data {
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
        public String strChatKey;
        public String strContentSize;
        public String isFavorite;
        public String scrapCount;
        public String isScrap;
        public String videoRato;
        public String viewerCount;
        public String categoryCode;
        public String secondCategoryCode1;
        public String title;
        public int favoriteCount;
        public String strNoti;
        public String strTag;
        public String thumbnailUrl;
        public String largeThumbnailUrl;
        public String show_YN;
        public String user_show_YN;
        public String gender;
        public String age;
        public String reg_date;
        public String strRegDate;
        public String update_date;
        public String share_YN;
        public String comment_YN;
        public RelationPrd relationPrd;
    }

    public static class RelationPrd {
        public int nTotalCount;
        public List<Data> data;
        public static class Data {
            public String idx;
            public String sc_code;
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
        }
    }
}
