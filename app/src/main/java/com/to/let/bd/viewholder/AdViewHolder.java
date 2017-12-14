package com.to.let.bd.viewholder;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.NativeExpressAdView;
import com.to.let.bd.R;
import com.to.let.bd.adapters.AdAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DateUtils;

public class AdViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout mainLay;
    public ImageView adMainPhoto;
    private TextView photoCount;
    private TextView adRent;
    private TextView adDescription;
    private TextView rentDate;
    private TextView address;
    private ImageView starView;

    private View itemView;
    private Context context;

    public AdViewHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();
        this.itemView = itemView;
        mainLay = itemView.findViewById(R.id.mainLay);
        adMainPhoto = itemView.findViewById(R.id.adMainPhoto);
        photoCount = itemView.findViewById(R.id.photoCount);
        adRent = itemView.findViewById(R.id.adRent);
        adDescription = itemView.findViewById(R.id.adDescription);
        rentDate = itemView.findViewById(R.id.rentDate);
        address = itemView.findViewById(R.id.address);
        starView = itemView.findViewById(R.id.star);
    }

    public void bindToAd(final AdInfo adInfo, final int clickedPosition, final AdAdapter.ClickListener clickListener) {
        if (adInfo.adId == null) {
//            NativeExpressAdView adView = new NativeExpressAdView(context);
//            adView.setAdSize(new AdSize(400, 100));
//            adView.setAdUnitId("ca-app-pub-3940256099942544/2793859312");
            AdView adView = new AdView(context);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(context.getString(R.string.ad_mob_banner_id));
            AdRequest adRequest = new AdRequest
                    .Builder()
                    .addTestDevice(context.getString(R.string.test_device_id1))
                    .build();
            adView.loadAd(adRequest);
            mainLay.removeAllViews();
            mainLay.addView(adView);
            return;
        }
        String adRent = "Rent per month: TK " + AppConstants.rentFormatter(adInfo.flatRent);
        this.adRent.setText(adRent);

        String adDescription = AppConstants.flatDescription(context, adInfo);
        this.adDescription.setText(adDescription);

        String rentDate = "Rent from: " + DateUtils.getRentDateString(adInfo.startingFinalDate);
        this.rentDate.setText(rentDate);
        address.setText(adInfo.fullAddress);

        String imagePath = null;
        int imageCount = 0;

        if (adInfo.images == null || adInfo.images.isEmpty()) {
            if (adInfo.map != null) {
                imagePath = adInfo.map.downloadUrl;
            }
        } else {
            imageCount = adInfo.images.size();
            imagePath = adInfo.images.get(0).downloadUrl;
        }

        if (imagePath != null)
            Glide.with(adMainPhoto.getContext())
                    .load(Uri.parse(imagePath))
                    .into(adMainPhoto);
        else
            adMainPhoto.setImageResource(R.drawable.dummy_flat_image);

        String photoCount = imageCount > 1 ? imageCount + " Photo's" : imageCount + " Photo";
        this.photoCount.setText(photoCount);

        this.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null)
                    clickListener.onItemClick(adInfo);
            }
        });

        starView.setSelected(false);

        if (adInfo.favCount > 0) {
            if (adInfo.fav.containsKey(BaseActivity.getUid())) {
                starView.setSelected(true);
            }
        }
        starView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starView.setSelected(!starView.isSelected());

                if (clickListener != null)
                    clickListener.onFavClick(starView, clickedPosition, adInfo);
            }
        });
    }
}
