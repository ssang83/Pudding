package com.enliple.pudding.commons.network.vo;

import java.util.List;

public class API42 {
    public String result;
    public String nTotalCount;
    public List<ReportData> data;

    public class ReportData {
        public String key;
        public String value;
    }
}
