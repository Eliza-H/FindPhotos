package elizah.findphotos.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import elizah.findphotos.ImageList;

/**
 * Created by elh on 07.06.17.
 */

public class Utils {
    public static Location getSearchingPoint(Context context) throws IOException {
        Location location = new Location("");
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocationName("Poland lublin ul. Pana Balcera", 1);// I suppose that it find only one city
        location.setLatitude(addresses.get(0).getLatitude());
        location.setLongitude(addresses.get(0).getLongitude());
        return location;
    }

    public static Cursor getImageCursor(final Activity activity) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        return activity.getContentResolver().query(uri, projection, null,
                null, null);
    }

    public static void addImagesToExtendList(final Activity activity, final ImageList imageList, final List<String> images) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {//метод выполняется внутри основн потока, хотя так это поток
                imageList.addImages(images);
                images.clear();
            }
        });
    }

    public static boolean isNeedLocation(final String image, final Location searchingPoint, int radius) throws IOException {
        final ExifInterface exifInterface = new ExifInterface(image);//exif формат метаданных
        float[] latLong = new float[2];
        exifInterface.getLatLong(latLong);//записует данные в массив

        if (latLong.length == 2) {
            Location location = new Location("");
            location.setLatitude(latLong[0]);
            location.setLongitude(latLong[1]);
            return location.distanceTo(searchingPoint) <= radius;//если растояние между точкой на карте и коорд карт меньше радиуса то true
        }
        return false;
    }

}
