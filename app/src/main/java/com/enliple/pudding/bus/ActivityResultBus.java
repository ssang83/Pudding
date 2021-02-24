package com.enliple.pudding.bus;

import android.content.Intent;

public class ActivityResultBus {
    public int requestCode;
    public int resultCode;
    public Intent data;

    public ActivityResultBus(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }
}