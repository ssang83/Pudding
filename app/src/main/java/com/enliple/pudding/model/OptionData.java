package com.enliple.pudding.model;

/**
 * Created by Administrator on 2018-04-03.
 */

public class OptionData {
    private int mId;
    private String mName;
    private String mKey;
    private String mPreKey;

    public void setId(int val) {
        mId = val;
    }

    public void setName(String val) {
        mName = val;
    }

    public void setKey(String val) {
        mKey = val;
    }

    public void setPreKey(String val) {
        mPreKey = val;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getKey() {
        return mKey;
    }

    public String getPreKey() {
        return mPreKey;
    }
}
