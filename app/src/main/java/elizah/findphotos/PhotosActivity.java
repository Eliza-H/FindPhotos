package elizah.findphotos;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.IOException;
import java.util.ArrayList;

import elizah.findphotos.adapters.ImageGalleryAdapter;
import elizah.findphotos.utils.Utils;

/**
 * Created by elh on 17.06.17.
 */

public class PhotosActivity extends Activity implements GalleryImageAction {

    private int PLACE_PICKER_REQUEST = 1;
    private Location selectLocation = null;
    private ImageGalleryAdapter imageGalleryAdapter = null;
    private Thread imageLoadThread;
    private boolean isBreak = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_photos);

        requestPlace();
        createImageList();
        addRefreshLocationHandler();
    }

    private void addRefreshLocationHandler() {
        FloatingActionButton clickButton = (FloatingActionButton) findViewById(R.id.refresh_location_button);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBreak = true;
                if (imageLoadThread.isAlive()) {
                    imageLoadThread.interrupt();
                }
                imageGalleryAdapter.clearImages();
                selectLocation = null;
                requestPlace();
            }
        });
    }

    private void requestPlace() {
        int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void createImageList() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        final ImageGalleryAdapter adapter = new ImageGalleryAdapter(this);
        this.imageGalleryAdapter = adapter;
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(this, data);
                selectLocation = new Location("");
                selectLocation.setLatitude(place.getLatLng().latitude);
                selectLocation.setLongitude(place.getLatLng().longitude);
                isBreak = false;
                updateAdapter();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        updateAdapter();
    }

    private void updateAdapter() {
        // Inflate the layout for this fragment

        int status = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE); //android 6 dynamic permission
        if (status == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        final Activity activity = this;

        imageLoadThread = new Thread() {
            @Override
            public void run() {
                try {
                    ArrayList<String> images = new ArrayList<>();
                    Cursor cursor = Utils.getImageCursor(activity);
                    int column_index_data;
                    String image = null;

                    column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                    while (cursor.moveToNext()) {
                        if (isBreak) {
                            break;
                        }
                        image = cursor.getString(column_index_data);
                        if (Utils.isImage(image) && Utils.isNeedLocation(image, selectLocation, 300)) {
                            images.add(image);
                            if (images.size() == 3) {
                                Utils.addImagesToExtendList(activity, imageGalleryAdapter, images);
                                images = new ArrayList<>();
                            }
                        }
                    }

                    cursor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        imageLoadThread.start();

    }

    @Override
    public void selectImage(String imagePath) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(FullScreenImageActivity.IMAGE_PATH_KEY, imagePath);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
