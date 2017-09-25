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

        requestPlace();//идет асинхронно, в отдельном потоке
        createImageList();//создает адаптер для recycler view
        addRefreshLocationHandler();//возвращает к карте, новый выбор позиции
    }

    private void addRefreshLocationHandler() {
        FloatingActionButton clickButton = (FloatingActionButton) findViewById(R.id.refresh_location_button);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBreak = true;//убить поток
                if (imageLoadThread.isAlive()) {
                    imageLoadThread.interrupt();//прервание потока как можно быстрее
                }
                imageGalleryAdapter.clearImages();
                selectLocation = null;
                requestPlace();//покругу
            }
        });
    }

    private void requestPlace() {
        int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();//стандартный интент андроида для геолокации, из гугл карт, запускает новую активность, выполняется паралельно

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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));//выводит таблицей
        final ImageGalleryAdapter adapter = new ImageGalleryAdapter(this);//создает наш адаптер
        this.imageGalleryAdapter = adapter;
        recyclerView.setAdapter(adapter);//передаеться компоненту адаптер

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {//проверяет вход интент
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(this, data);//получает координаты
                selectLocation = new Location("");
                selectLocation.setLatitude(place.getLatLng().latitude);//широта
                selectLocation.setLongitude(place.getLatLng().longitude);//долгота
                isBreak = false;
                updateAdapter();
            }
        }
    }

    private void updateAdapter() {
        // Inflate the layout for this fragment

        int status = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE); //android 6 dynamic permission
        if (status == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        final Activity activity = this;

        imageLoadThread = new Thread() {
            @Override
            public void run() {//все что в run выполн в отдел потоке
                try {
                    ArrayList<String> images = new ArrayList<>();//список с картинками
                    Cursor cursor = Utils.getImageCursor(activity);//подтягивает все картинки
                    int column_index_data;
                    String image = null;

                    column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);//тип который искать

                    while (cursor.moveToNext()) {//пока есть след картинки выполняется цыкл
                        if (isBreak) {//если пользователь нажал назад, поток останавливаеться и удаляется, поток активен пока в нем чтото выполняется
                            break;
                        }
                        image = cursor.getString(column_index_data);
                        if (image.endsWith("jpg") && Utils.isNeedLocation(image, selectLocation, 300)) {
                            images.add(image);
                            if (images.size() == 3) {
                                Utils.addImagesToExtendList(activity, imageGalleryAdapter, images);//добвляет путь к картинке в адаптер
                                images = new ArrayList<>();//новый список для новых изображ, так как потоки асинхронны и может еще этот метод не выполнился
                            }
                        }
                    }

                    cursor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        imageLoadThread.start();//запуск потока

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
