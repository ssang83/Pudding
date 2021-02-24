package com.enliple.pudding.commons.network.vo;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-03-20.
 */
public class API137 {

    /**
     * result : success
     * nTotalCount : 4
     * data : [{"ad_title":"미나그램 // 광고주 이름","ad_site":"http://serendibeauty.co.kr/ // 광고주 사이트 URL","ad_img":"http://serendibeauty.co.kr/test.jpg // 광고주 이미지 URL","ad_usemoney":"3300 // 광고예산(단위미정)"}]
     */

    public String result;
    public int nTotalCount;
    public List<MobionItem> data;

    public static class MobionItem {
        /**
         * ad_title : 미나그램 // 광고주 이름
         * ad_site : http://serendibeauty.co.kr/ // 광고주 사이트 URL
         * ad_img : http://serendibeauty.co.kr/test.jpg // 광고주 이미지 URL
         * ad_usemoney : 3300 // 광고예산(단위미정)
         */

        public String ad_title;
        public String ad_site;
        public String ad_img;
        public String ad_usemoney;
    }
}
