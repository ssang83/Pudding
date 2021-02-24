package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-27.
 */
public class API58 {

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"block_user":"test1 // 차단한 사용자 ID","bl_status_YN":"Y// 차단 설정 [Y:차단, N:차단해제]","bl_reg_date":"2018-12-24 15:03:24 // 차단 설정일","mb_user_img":"차단한 사용자 이미지 URL"}]
     */

    public String result;
    public int nTotalCount;
    public List<BlockItem> data;

    public static class BlockItem {
        /**
         * block_user : test1 // 차단한 사용자 ID
         * bl_status_YN : Y// 차단 설정 [Y:차단, N:차단해제]
         * bl_reg_date : 2018-12-24 15:03:24 // 차단 설정일
         * mb_user_img : 차단한 사용자 이미지 URL
         */

        public String block_user;
        public String bl_status_YN;
        public String bl_reg_date;
        public String mb_user_img;
    }
}
