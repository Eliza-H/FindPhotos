package elizah.findphotos;

import java.util.List;

/**
 * Created by elh on 17.06.17.
 */

public interface ImageList {
    void addImages(final List<String> newImages);

    void clearImages();
}
