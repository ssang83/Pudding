package com.enliple.pudding.model;

import java.util.ArrayList;

public class ShopTreeOption {
    private String key;
    private String name;
    private String prekey;
    private ArrayList<Options> subOption;

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPreKey(String prekey) {
        this.prekey = prekey;
    }

    public void setSubOption(ArrayList<Options> subOption) {
        this.subOption = subOption;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public String getPreKey() {
        return this.prekey;
    }

    public ArrayList<Options> getSubOption() {
        return this.subOption;
    }
}
