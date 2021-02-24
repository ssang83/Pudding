package com.enliple.pudding.model;

import java.util.ArrayList;

public class PermissionObject {
    private String title;
    private ArrayList<Objects> objectArray;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setObjectArray(ArrayList<Objects> objectArray) {
        this.objectArray = objectArray;
    }

    public String getTitle() {
        return this.title;
    }

    public ArrayList<Objects> getObjectArray() {
        return this.objectArray;
    }

    public static class Objects {
        public int getImage() {
            return image;
        }

        public void setImage(int image) {
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        private int image;
        private String title;
        private String content;


    }
}
