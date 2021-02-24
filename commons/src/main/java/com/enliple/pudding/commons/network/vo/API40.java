package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API40 {
    public String result;
    public String nTotalCount;
    public List<MyQAData> data;

    public class MyQAData {
        public String id;
        public String sub;
        public String con;
        public String time;
        public String status;
        public String answerId;
        public String category;
        public String answerCon;
        public String answerTime;
    }
}
