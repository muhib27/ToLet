package com.to.let.bd.viewholder;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.to.let.bd.R;
import com.to.let.bd.activities.AdListActivity2;
import com.to.let.bd.adapters.AdAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DateUtils;
import com.to.let.bd.utils.MyAnalyticsUtil;

import java.text.DecimalFormat;

public class AdViewHolder extends RecyclerView.ViewHolder {

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
        adMainPhoto = itemView.findViewById(R.id.adMainPhoto);
        photoCount = itemView.findViewById(R.id.photoCount);
        adRent = itemView.findViewById(R.id.adRent);
        adDescription = itemView.findViewById(R.id.adDescription);
        rentDate = itemView.findViewById(R.id.rentDate);
        address = itemView.findViewById(R.id.address);
        starView = itemView.findViewById(R.id.star);
    }

    public void bindToAd(final AdInfo adInfo, final int clickedPosition, final AdAdapter.ClickListener clickListener) {
        String adRent = "Rent per month: TK " + AppConstants.rentFormatter(adInfo.getFlatRent());
        this.adRent.setText(adRent);

        String adDescription = AppConstants.flatDescription(context, adInfo);
        this.adDescription.setText(adDescription);

        String rentDate = "Rent from: " + DateUtils.getRentDateString(adInfo.getStartingFinalDate());
        this.rentDate.setText(rentDate);
        address.setText(adInfo.getFullAddress());

        String imagePath = null;
        int imageCount = 0;

        if (adInfo.getImages() == null || adInfo.getImages().isEmpty()) {
            if (adInfo.getMap() != null) {
                imagePath = adInfo.getMap().downloadUrl;
            }
        } else {
            imageCount = adInfo.getImages().size();
            imagePath = adInfo.getImages().get(0).downloadUrl;
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
