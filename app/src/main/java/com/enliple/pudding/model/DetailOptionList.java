package com.enliple.pudding.model;

import java.util.ArrayList;

public class DetailOptionList {
    private String key;
    private String name;
    private ArrayList<DetailSubOption> subOption;
    private DetailSubOption selectedSubOption;
    private boolean isOpen = false;

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public DetailSubOption getSelectedSubOption() {
        return selectedSubOption;
    }

    public void setSelectedSubOption(DetailSubOption selectedSubOption) {
        this.selectedSubOption = selectedSubOption;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubOption(ArrayList<DetailSubOption> subOption) {
        this.subOption = subOption;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<DetailSubOption> getSubOption() {
        return this.subOption;
    }


}
