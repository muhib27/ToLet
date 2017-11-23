package com.to.let.bd.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.to.let.bd.R;
import com.to.let.bd.model.AdInfo;

import java.text.DecimalFormat;

public class AdViewHolder extends RecyclerView.ViewHolder {

    private ImageView adMainPhoto;
    private TextView photoCount;
    private TextView adRent;
    private TextView adDescription;
    private TextView rentDate;
    private TextView address;
    private ImageView starView;

    public AdViewHolder(View itemView) {
        super(itemView);

        adMainPhoto = itemView.findViewById(R.id.adMainPhoto);
        photoCount = itemView.findViewById(R.id.photoCount);
        adRent = itemView.findViewById(R.id.adRent);
        adDescription = itemView.findViewById(R.id.adDescription);
        rentDate = itemView.findViewById(R.id.rentDate);
        address = itemView.findViewById(R.id.address);
        starView = itemView.findViewById(R.id.star);
    }

    public void bindToAd(AdInfo adInfo, View.OnClickListener starClickListener) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String adRent = "Rent per month: Tk " + formatter.format(adInfo.getFlatRent());
        this.adRent.setText(adRent);

        if (adInfo.getFamilyInfo() != null) {
            boolean isItDulpex = adInfo.getFamilyInfo().isItDuplex();
            String adDescription = "";
            if (isItDulpex)
                adDescription = "Duplex house with ";
            adDescription = adDescription + adInfo.getFamilyInfo().getBedRoom() + "bed " +
                    adInfo.getFamilyInfo().getBathroom() + "bath " +
                    adInfo.getFamilyInfo().getBalcony() + "balcony " +
                    adInfo.getFlatSpace() + " sqft";
            this.adDescription.setText(adDescription);
        }

        String rentDate = "Rent from: " + adInfo.getStartingFinalDate();
        this.rentDate.setText(rentDate);
        address.setText(adInfo.getFullAddress());

        starView.setOnClickListener(starClickListener);
    }
}
