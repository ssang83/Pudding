package com.enliple.pudding.shoppingplayer;

public class PreviousProductItem {
    private String image;
    private String streamUrl;
    private String title;
    private int favorite;

    public void setImage(String image) {
        this.image = image;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public String getImage() {
        return this.image;
    }

    public String getStreamUrl() {
        return this.streamUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public int getFavorite() {
        return this.favorite;
    }
}
