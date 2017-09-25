package elizah.findphotos.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elh on 17.06.17.
 */

public class ImagesCache {

    private static ImagesCache imagesCache;

    public static ImagesCache getInstance() {
        if (imagesCache == null) {
            return imagesCache = new ImagesCache();
        }
        return imagesCache;
    }

    private List<String> images = new ArrayList<>();

    public void addImage(String image) {
        images.add(image);
    }

    public void extendImages(List<String> images) {
        for (String image : images) {
            this.images.add(image);
        }
    }

    public List<String> getImages() {
        return images;
    }

    public boolean isEmpty() {
        return images.size() > 0;
    }

}
