package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-05-08.
 */
public class API144 {

    /**
     * result : success
     * nTotalCount : 30
     * data : [{"rank":1,"keyword":"ㅂㅂ","rankCal":"랭킹 변동 (-숫자, +숫자, new)"}]
     */

    public String result;
    public int nTotalCount;
    public List<KeywordItem> data;

    public static class KeywordItem {
        /**
         * rank : 1
         * keyword : ㅂㅂ
         * rankCal : 랭킹 변동 (-숫자, +숫자, new)
         */

        public String rank;
        public String keyword;
        public String rankCal;
    }
}
