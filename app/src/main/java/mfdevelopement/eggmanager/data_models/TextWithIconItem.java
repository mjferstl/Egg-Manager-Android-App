package mfdevelopement.eggmanager.data_models;

import androidx.annotation.NonNull;

public class TextWithIconItem {

    private final int imageId;
    private final String name;

    public TextWithIconItem(@NonNull String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }
}
