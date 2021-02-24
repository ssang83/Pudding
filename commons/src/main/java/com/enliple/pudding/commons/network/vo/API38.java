package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API38 {
    public String result;
    public String nTotalCount;
    public List<FAQCategoryData> data;

    public class FAQCategoryData {
        public String id;
        public String subject;
        public String img;
    }
}
