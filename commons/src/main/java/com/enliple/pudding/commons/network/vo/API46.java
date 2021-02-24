package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API46 {
    public String result;
    public String nTotalCount;
    public List<ReviewItem> data;

    public static class ReviewItem {
        public String is_id;
        public String ct_id;
        public String is_type;
        public String is_score;
        public String is_subject;
        public String is_content;
        public String photo_thumb;
        public List<String> is_photo;
        public String is_time;
        public String mb_id;
        public String mb_nick;
        public String mb_user_img;
        public String ct_option;
        public String recommend;
        public String not_recommend;
        public String is_mine;
        public String is_recommend;    // 0: 추천안함, 1:추천, 2:비추천
    }
}
