package com.enliple.pudding.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2018-03-27.
 */

public class ShopTreeImageModel implements Serializable {
    private int mWidth, mHeight;
    private String mUrl, mTUrl;

    public void setWidth(int val) {
        mWidth = val;
    }

    public void setHeight(int val) {
        mHeight = val;
    }

    public void setUrl(String val) {
        mUrl = val;
    }

    public void setTUrl(String val) {
        mTUrl = val;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTUrl() {
        return mTUrl;
    }
}
