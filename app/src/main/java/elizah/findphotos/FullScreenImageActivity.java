package elizah.findphotos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

/**
 * Created by elh on 17.06.17.
 */

public class FullScreenImageActivity extends AppCompatActivity {

    public static String IMAGE_PATH_KEY = "IMAGE_PATH_KEY";

    private ShareActionProvider shareActionProvider;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Intent intent = getIntent();
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            imagePath = extras.getString(IMAGE_PATH_KEY);
            drawImage(imagePath);
            addShareHandler();
        }
    }

    private void addShareHandler() {
        FloatingActionButton clickButton = (FloatingActionButton) findViewById(R.id.share_full_image);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIntent();
            }
        });
    }

    private void setIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imagePath)));
        intent.setType("image/jpeg");
        startActivity(Intent.createChooser(intent, "send file"));
    }

    private void drawImage(String imagePath) {
        final ImageView imageView = (ImageView) findViewById(R.id.full_screen_id);

        Glide.with(this)
                .load(new File(imagePath))
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                .into(imageView);

    }


}
