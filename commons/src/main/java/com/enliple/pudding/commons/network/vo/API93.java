package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-05.
 */
public class API93 {

    /**
     * result : success
     * nTotalCount : 5
     * data : [{"key":1,"value":"상품"}]
     */

    public String result;
    public int nTotalCount;
    public List<QnaType> data;

    public static class QnaType {
        /**
         * key : 1
         * value : 상품
         */

        public int key;
        public String value;
    }
}
