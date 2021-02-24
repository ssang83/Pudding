package com.enliple.pudding.commons.network.vo;

/**
 * Created by Kim Joonsung on 2018-11-20.
 */
public class BaseAPI {

    /**
     * status : true
     * message : false 인경우 메시지
     */

    public boolean status;
    public String message;

    @Override
    public String toString() {
        return "BaseAPI{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
