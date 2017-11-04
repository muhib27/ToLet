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

public class AdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<AdInfo> sampleList;
    private Context context;
    private ClickListener clickListener;

    public AdAdapter(Context context, ArrayList<AdInfo> sampleList, ClickListener clickListener) {
        this.context = context;
        this.sampleList = sampleList;
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.row_item_ad, parent, false));
    }

    public void setData(ArrayList<AdInfo> sampleList) {
        this.sampleList = sampleList;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;

            AdInfo adInfo = sampleList.get(position);

            String title = "৳" + adInfo.getFlatRent() + ", " + adInfo.getBedRoom() + " bed, " + adInfo.getBathroom() + " bath";
            myViewHolder.adTitle.setText(title.trim());
            String subTitle = adInfo.getFullAddress();
            myViewHolder.adSubTitle.setText(subTitle.trim());

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
                Glide.with(context)
                        .load(Uri.parse(imagePath))
                        .into(myViewHolder.adMainPhoto);
            else
                myViewHolder.adMainPhoto.setImageResource(R.drawable.dummy_flat_image);

            String photoCount = imageCount > 1 ? imageCount + " Photo's" : imageCount + " Photo";
            myViewHolder.photoCount.setText(photoCount);
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
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return sampleList.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            Glide.with(context).clear(myViewHolder.adMainPhoto);
        }
        super.onViewRecycled(holder);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
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
