package com.enliple.pudding.commons.network.vo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Kim Joonsung on 2019-01-02.
 */
public class API54 {

    /**
     * result : success
     * nTotalCount : 2
     * data : [{"no":1473,"send_mb_id":"보낸사람 id","send_mb_nick":"보낸사람 닉네임","send_mb_img":"보낸사람 이미지","datetime":"2018-12-28T10:14:48.000Z","content":"메시지내용","new":"1     0 => 모두 읽은 메시지  1 => 안읽은 메시지 존재","isFollow":"내가 메시지를 보낸사람을 팔로우 중이면 1 아니면 0"}]
     */

    public String result;
    public int nTotalCount;
    public List<MessageItem> data;

    public static class MessageItem {
        /**
         * no : 1473
         * send_mb_id : 보낸사람 id
         * send_mb_nick : 보낸사람 닉네임
         * send_mb_img : 보낸사람 이미지
         * datetime : 2018-12-28T10:14:48.000Z
         * content : 메시지내용
         * new : 1     0 => 모두 읽은 메시지  1 => 안읽은 메시지 존재
         * isFollow : 내가 메시지를 보낸사람을 팔로우 중이면 1 아니면 0
         */

        public int no;
        public String send_mb_id;
        public String send_mb_nick;
        public String send_mb_img;
        public String datetime;
        public String content;
        public @SerializedName("new") int isNew;
        public String isFollow;
        public String push;
        public String is_send_mb_leave;
    }
}
