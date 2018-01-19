package com.to.let.bd.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.to.let.bd.R;
import com.to.let.bd.components.ImageViewZoomT;

public class SlidingImageAdapter extends PagerAdapter {
    private String[] images;
    private LayoutInflater inflater;
    private Context context;
    private ImageClickListener imageClickListener;

    public SlidingImageAdapter(Context context, String[] images, ImageClickListener imageClickListener) {
        this.context = context;
        this.images = images;
        this.imageClickListener = imageClickListener;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View imageLayout = inflater.inflate(R.layout.slider_item, view, false);
        assert imageLayout != null;

        ImageView imageView = imageLayout.findViewById(R.id.imageView);
        ProgressBar progressBar = imageLayout.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        if (images[position] != null)
            Glide.with(context)
                    .load(Uri.parse(images[position]))
                    .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                    .into(imageView);
        else
            imageView.setImageResource(R.drawable.dummy_flat_image);

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
