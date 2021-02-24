package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API41 {
    public String result;
    public String nTotalCount;
    public List<FAQList> data;

    public class FAQList {
        public String id;
        public String sub;
        public String con;
    }
}
