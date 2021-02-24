package com.enliple.pudding.model;

import com.enliple.pudding.commons.network.vo.API150;

import java.util.List;

public class TravelModel {
    public API150.Data getBig() {
        return big;
    }

    public void setBig(API150.Data big) {
        this.big = big;
    }

    public List<API150.Data> getSmalls() {
        return smalls;
    }

    public void setSmalls(List<API150.Data> smalls) {
        this.smalls = smalls;
    }

    API150.Data big;
    List<API150.Data> smalls;
}
