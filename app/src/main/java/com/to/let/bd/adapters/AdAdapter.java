package com.to.let.bd.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.to.let.bd.R;
import com.to.let.bd.model.AdInfo;

import java.util.ArrayList;

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.MyViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<AdInfo> sampleList;
    private Context context;
    private ClickListener clickListener;

    public AdAdapter(Context context, ArrayList<AdInfo> sampleList, ClickListener clickListener) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.sampleList = sampleList;
        this.clickListener = clickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.row_item_ad, parent, false);
        return new MyViewHolder(view);
    }

    public void setData(ArrayList<AdInfo> sampleList) {
        this.sampleList = sampleList;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        AdInfo adInfo = sampleList.get(position);

        String title = "৳" + adInfo.getFlatRent() + ", " + adInfo.getBedRoom() + " bed, " + adInfo.getBathroom() + " bath";
        holder.adTitle.setText(title.trim());
        String subTitle = adInfo.getFullAddress();
        holder.adSubTitle.setText(subTitle.trim());

        if (adInfo.getImages() == null || adInfo.getImages().isEmpty()) {
            if (adInfo.getMap() == null) {
                holder.adMainPhoto.setImageResource(R.drawable.dummy_flat_image);
            } else {
                Glide.with(context)
                        .load(Uri.parse(adInfo.getMap().getDownloadUrl()))
                        .into(holder.adMainPhoto);
            }
            holder.photoCount.setText(context.getString(R.string.dummy_photo_count));
        } else {
            Glide.with(context)
                    .load(Uri.parse(adInfo.getImages().get(0).getDownloadUrl()))
                    .into(holder.adMainPhoto);

            String photoCount = adInfo.getImages().size() > 1 ? adInfo.getImages().size() + " Photo's" : adInfo.getImages().size() + " Photos";
            holder.photoCount.setText(photoCount);
        }
//        holder.progressBar.setVisibility(View.VISIBLE);
//
//        String imageUrl = null;
//        if (adInfo.getImages().size() > 0) {
//            imageUrl = adInfo.getImages().get(0).getImageUrl();
//        }
//        if (imageUrl != null) {
//            Picasso.with(context)
//                    .load(imageUrl)
//                    .error(R.drawable.party_flag)
//                    .fit()
//                    .tag(context)
//                    .into(holder.image, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            holder.progressBar.setVisibility(View.GONE);
//                        }
//
//                        @Override
//                        public void onError() {
//                            holder.progressBar.setVisibility(View.GONE);
//                        }
//                    });
//        } else {
//            holder.progressBar.setVisibility(View.GONE);
//            holder.image.setImageResource(R.drawable.party_flag);
//        }
    }

    @Override
    public int getItemCount() {
        return sampleList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView adMainPhoto, favIcon;
        TextView adTitle, adSubTitle, photoCount;

        MyViewHolder(final View itemView) {
            super(itemView);
            adMainPhoto = (ImageView) itemView.findViewById(R.id.adMainPhoto);
            favIcon = (ImageView) itemView.findViewById(R.id.favIcon);

            adTitle = (TextView) itemView.findViewById(R.id.adTitle);
            adSubTitle = (TextView) itemView.findViewById(R.id.adSubTitle);
            photoCount = (TextView) itemView.findViewById(R.id.photoCount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null)
                        clickListener.onItemClick(itemView, getLayoutPosition(), sampleList.get(getLayoutPosition()));
                }
            });
            favIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null)
                        clickListener.onFavClick(favIcon, getLayoutPosition(), sampleList.get(getLayoutPosition()));
                }
            });
        }
    }

    public interface ClickListener {
        void onItemClick(View view, int position, AdInfo adInfo);

        void onFavClick(View view, int position, AdInfo adInfo);
    }
}
