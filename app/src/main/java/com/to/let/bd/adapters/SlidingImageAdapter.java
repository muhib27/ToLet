package com.to.let.bd.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.to.let.bd.R;
import com.to.let.bd.components.ImageViewZoomT;

public class SlidingImageAdapter extends PagerAdapter {
    private String[] images;
    private LayoutInflater inflater;
    private Context context;

    public SlidingImageAdapter(Context context, String[] images) {
        this.context = context;
        this.images = images;
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


        ImageViewZoomT imageView = (ImageViewZoomT) imageLayout.findViewById(R.id.imageView);
        ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.progressBar);

        imageView.setMaxZoom(4f);

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(Uri.parse(images[position]))
                .into(imageView);

        view.addView(imageLayout, 0);
        return imageLayout;
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
