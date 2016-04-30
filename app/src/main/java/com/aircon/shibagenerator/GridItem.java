package com.aircon.shibagenerator;

import java.io.File;

/**
 * Created by ChenHsinHsuan on 2016/4/30.
 */
public class GridItem {
    private File imageFile;
    private String title;

    public GridItem() {
    }


    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
