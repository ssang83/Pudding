package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API51 {
    public String result;
    public int nTotalCount;
    public List<Data> data;

    public static class Data {
        public String gf_to_id;
        public String gf_quantity;
        public String gf_reg_date;
        public String gf_expire_date;
        public String gf_payment;
        public String gf_price;
        public String vod_type;
        public String gf_to_nick;
    }
}
