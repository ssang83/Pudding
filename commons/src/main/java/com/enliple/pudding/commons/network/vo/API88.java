package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-07.
 */
public class API88 {
    public String result;
    public int nTotalCount;
    public String rankdDate;
    public List<DataBean> data;

    public static class DataBean {
        public String mb_id;
        public String nickName;
        public String userImage;
        public String rank;
        public String hitCnt;
        public String followCnt;
        public String followingCnt;
        public String favorCnt;
        public String giftCnt;
        public String ca_id;
        public String rankCal;
    }
}
