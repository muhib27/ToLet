package com.to.let.bd.viewholder;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.to.let.bd.R;
import com.to.let.bd.activities.AdListActivity2;
import com.to.let.bd.adapters.AdAdapter;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.DateUtils;

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
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String adRent = "Rent per month: TK " + formatter.format(adInfo.getFlatRent());
        this.adRent.setText(adRent);

        String adDescription = "";

        if (adInfo.getFamilyInfo() != null) {
            boolean isItDulpex = adInfo.getFamilyInfo().getIsItDuplex();
            if (isItDulpex)
                adDescription = "Duplex house with ";
            adDescription = adDescription + adInfo.getFamilyInfo().getBedRoom() + "bed " +
                    adInfo.getFamilyInfo().getBathroom() + "bath ";

            if (adInfo.getFamilyInfo().getBalcony() > 0) {
                adDescription = adDescription + adInfo.getFamilyInfo().getBalcony() + "balcony ";
            }
            if (adInfo.getFlatSpace() > 0) {
                adDescription = adDescription + adInfo.getFlatSpace() + " sqft";
            }
        } else if (adInfo.getMessInfo() != null) {
            String[] messTypeArray = context.getResources().getStringArray(R.array.mess_member_type_array);
            adDescription = messTypeArray[adInfo.getMessInfo().getMemberType()] + " member ";
            adDescription += adInfo.getMessInfo().getNumberOfSeat() + "seat " +
                    adInfo.getMessInfo().getNumberOfRoom() + "room ";
        } else if (adInfo.getSubletInfo() != null) {
            String[] subletTypeArray = context.getResources().getStringArray(R.array.sublet_type_array);
            adDescription = adInfo.getSubletInfo().getSubletType() >= 3 ? adInfo.getSubletInfo().getSubletTypeOthers() :
                    subletTypeArray[adInfo.getSubletInfo().getSubletType()];

            adDescription += " with ";

            String[] subletBathTypeArray = context.getResources().getStringArray(R.array.sublet_bath_type_array);
            adDescription += subletBathTypeArray[adInfo.getSubletInfo().getBathroomType()] + " bath";
        }
        this.adDescription.setText(adDescription);

        String rentDate = "Rent from: " + DateUtils.getRentDateString(adInfo.getStartingFinalDate());
        this.rentDate.setText(rentDate);
        address.setText(adInfo.getFullAddress());

        String imagePath = null;
        int imageCount = 0;

        if (adInfo.getImages() == null || adInfo.getImages().isEmpty()) {
            if (adInfo.getMap() != null) {
                imagePath = adInfo.getMap().getDownloadUrl();
            }
        } else {
            imageCount = adInfo.getImages().size();
            imagePath = adInfo.getImages().get(0).getDownloadUrl();
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
            if (adInfo.fav.containsKey(AdListActivity2.firebaseUserId)) {
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
