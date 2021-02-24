package com.enliple.pudding.model;

import android.net.Uri;

public class ReviewDetailImageModel {

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public Uri getImagePath() {
        return imagePath;
    }

    public void setImagePath(Uri imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isFromServer() {
        return isFromServer;
    }

    public void setFromServer(boolean fromServer) {
        isFromServer = fromServer;
    }

    private boolean isHeader = false;
    private Uri imagePath;
    private boolean isFromServer = false;
}
