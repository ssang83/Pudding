package com.enliple.pudding.model;

import com.enliple.pudding.shoppingcaster.data.MediaInfo;

public class CoverMultiSelectModel {
    public boolean isSelected;
    public MediaInfo info;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public MediaInfo getInfo() {
        return info;
    }

    public void setInfo(MediaInfo info) {
        this.info = info;
    }


}
