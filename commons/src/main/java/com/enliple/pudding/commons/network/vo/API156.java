package com.enliple.pudding.commons.network.vo;

import com.enliple.pudding.commons.app.StringUtils;
import com.enliple.pudding.commons.app.Utils;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-07-01.
 */
public class API156 {

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"no":"10","reserve_day":"2019-02-07 00:00:00","end_day":"9999-12-31 00:00:00","title":"test pop","link":"","content":"<a href=\"naver.com\"><p>pop<\/p><p><img src=\"http://lieon.test/data/editor/1902/31c2bf49e90449304a1f132641a86907_1549524081_0796.jpg\" title=\"31c2bf49e90449304a1f132641a86907_1549524081_0796.jpg\"><br style=\"clear:both;\"><br><\/p><\/a>"},{"no":"9","reserve_day":"2019-02-02 00:00:00","end_day":"2019-02-07 23:59:59","title":"test","link":"","content":"<p>하하하하<\/p>"}]
     */

    public String result;
    public int nTotalCount;
    public List<NotiItem> data;

    public static class NotiItem {
        /**
         * no : 10
         * reserve_day : 2019-02-07 00:00:00
         * end_day : 9999-12-31 00:00:00
         * title : test pop
         * link :
         * content : <a href="naver.com"><p>pop</p><p><img src="http://lieon.test/data/editor/1902/31c2bf49e90449304a1f132641a86907_1549524081_0796.jpg" title="31c2bf49e90449304a1f132641a86907_1549524081_0796.jpg"><br style="clear:both;"><br></p></a>
         */

        public String no;
        public String reserve_day;
        public String end_day;
        public String title;
        public String link;
        public String content;

        /**
         * 공지 팝업을 표현 해야하는지 여부를 확인
         * @return
         */
        public boolean hasShowing() {
            // 시스템 현재시간
            long currentTime = System.currentTimeMillis();

            // 시작시간 데이터가 올바르지 않을때 현재 시간 보다 1초를 증가 처리하여 표시되지 않도록 한다.
            long reserveDayMills = reserve_day != null && ! reserve_day.isEmpty()
                    ? StringUtils.Companion.getDateTimeFromString(reserve_day)
                    : System.currentTimeMillis() + 1000;

            // 종료시간 데이터가 올바르지 않으면 무조건 종료시간을 0으로 설정하여 초과한것으로 간주시킨다.
            long endDayMills = end_day != null && ! end_day.isEmpty()
                    ? StringUtils.Companion.getDateTimeFromString(end_day)
                    : 0;

            // 시작시간과 종료시간 사이에 있어야만 True 를 반환
            return currentTime >= reserveDayMills && currentTime <= endDayMills;
        }
    }
}
