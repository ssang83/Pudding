package com.enliple.pudding.api.dao;

import java.io.Serializable;
import java.util.ArrayList;

public class ReviewModel implements Serializable {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecommand() {
        return recommand;
    }

    public void setRecommand(String recommand) {
        this.recommand = recommand;
    }

    public String getNotRecommand() {
        return notRecommand;
    }

    public void setNotRecommand(String notRecommand) {
        this.notRecommand = notRecommand;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    private String iconPath;
    private String name;
    private String idx;
    private String date;
    private String option;
    private String message;
    private String recommand;
    private String notRecommand;
    private ArrayList<String> images;
}
