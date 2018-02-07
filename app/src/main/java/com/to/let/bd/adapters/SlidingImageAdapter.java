package com.to.let.bd.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.to.let.bd.R;

import java.util.ArrayList;

public class SlidingImageAdapter extends PagerAdapter {
    private ArrayList<String> images;
    private LayoutInflater inflater;
    private Context context;
    private ImageClickListener imageClickListener;

    public SlidingImageAdapter(Context context, ArrayList<String> images, ImageClickListener imageClickListener) {
        this.context = context;
        this.images = images;
        this.imageClickListener = imageClickListener;
        inflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<String> images) {
        this.images = images;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View imageLayout = inflater.inflate(R.layout.slider_item, view, false);
        assert imageLayout != null;

        ImageView imageView = imageLayout.findViewById(R.id.imageView);
        if (images.get(position) != null)
            Glide.with(context)
                    .load(Uri.parse(images.get(position)))
                    .apply(new RequestOptions().placeholder(R.drawable.image_loading).error(R.drawable.image_error))
                    .into(imageView);
        else
            imageView.setImageResource(R.drawable.no_image_available);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageClickListener != null)
                    imageClickListener.imageClick(position);
            }
        });

        view.addView(imageLayout, 0);
        return imageLayout;
    }

    public interface ImageClickListener {
        void imageClick(int position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
