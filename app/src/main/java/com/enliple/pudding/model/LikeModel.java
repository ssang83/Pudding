package com.enliple.pudding.model;

import java.io.Serializable;

public class LikeModel implements Serializable {
    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getIncreaseLikeCount() {
        return increaseLikeCount;
    }

    public void setIncreaseLikeCount(int increaseLikeCount) {
        this.increaseLikeCount = increaseLikeCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String streamKey = "";
    private int likeCount = 0;
    private int increaseLikeCount = 0;
    private String category = "";
}
