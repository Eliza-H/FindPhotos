package elizah.findphotos.utils;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.List;

import elizah.findphotos.ImageList;

/**
 * Created by elh on 07.06.17.
 */

public class Utils {

    public static Cursor getImageCursor(final Activity activity) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        return activity.getContentResolver().query(uri, projection, null,
                null, null);
    }

    public static void addImagesToExtendList(final Activity activity, final ImageList imageList, final List<String> images) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageList.addImages(images);
                images.clear();
            }
        });
    }

    public static boolean isNeedLocation(final String image, final Location searchingPoint, int radius) throws IOException {
        final ExifInterface exifInterface = new ExifInterface(image);
        float[] latLong = new float[2];
        exifInterface.getLatLong(latLong);

        if (latLong.length == 2) {
            Location location = new Location("");
            location.setLatitude(latLong[0]);
            location.setLongitude(latLong[1]);
            return location.distanceTo(searchingPoint) <= radius;
        }
        return false;
    }

    public static boolean isImage(String name) {
        return name.endsWith("jpg") || name.endsWith("png") || name.endsWith("gif");
    }
}
