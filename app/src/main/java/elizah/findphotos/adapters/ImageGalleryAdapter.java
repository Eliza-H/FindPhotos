package elizah.findphotos.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import elizah.findphotos.GalleryImageAction;
import elizah.findphotos.ImageList;
import elizah.findphotos.R;

/**
 * Created by elh on 08.06.17.
 */

public class ImageGalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ImageList {
    private final List<File> images;
    private final Activity activity;
    private final int countPerLine = 3;
    private int itemWidth;

    public ImageGalleryAdapter(Activity context) {
        super();
        this.activity = context;
        images = new ArrayList<>();
        itemWidth = getItemWidth();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {//класс-шаблон в кот описуем как должен выглядеть элем списка
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_thumbnail, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = layoutParams.width = itemWidth;
        view.setLayoutParams(layoutParams);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final ImageViewHolder holder = (ImageViewHolder) viewHolder;

        final File image = images.get(position);

        //библиотека для загрузки изображений
        RequestBuilder<Drawable> apply = Glide.with(activity)
                .load(image)
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder))//картинка что изображение загружается
                .apply(RequestOptions.overrideOf(itemWidth, itemWidth))
                .apply(RequestOptions.centerCropTransform());

        apply.preload(itemWidth, itemWidth);
        apply.into(holder.imageView);

        holder.frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//обработка клика на фото
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    final GalleryImageAction galleryImageAction = (GalleryImageAction) activity;
                    galleryImageAction.selectImage(image.getPath());//перейти к фото активность
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void addImages(List<String> newImages) {

        for (String image : newImages) {
            images.add(new File(image));
        }

        notifyDataSetChanged();//уведомляет recycler view о изменениях
    }

    @Override
    public void clearImages() {
        notifyItemRangeRemoved(0, images.size());
        images.clear();
    }

    private int getItemWidth() {//расчитывает ширину на основе текущего экрана
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels / countPerLine;
    }


    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        // region Views
        private final ImageView imageView;
        private final FrameLayout frameLayout;
        // endregion

        // region Constructors
        public ImageViewHolder(final View view) {//создает элементы списка
            super(view);

            imageView = (ImageView) view.findViewById(R.id.iv);
            frameLayout = (FrameLayout) view.findViewById(R.id.fl);
        }
        // endregion
    }
}
