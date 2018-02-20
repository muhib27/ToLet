package com.to.let.bd.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.to.let.bd.R;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.viewholder.AdViewHolder;

import java.util.ArrayList;

public class AdAdapter extends RecyclerView.Adapter<AdViewHolder> {

    private ArrayList<AdInfo> sampleList = new ArrayList<>();
    private Context context;
    private ClickListener clickListener;

    public AdAdapter(Context context, ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public AdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new AdViewHolder(inflater.inflate(R.layout.item_ad, parent, false));
    }

    public void setData(ArrayList<AdInfo> sampleList) {
        this.sampleList = sampleList;
    }

    @Override
    public void onBindViewHolder(final AdViewHolder holder, int position) {
        AdInfo adInfo = sampleList.get(position);
        holder.bindToAd(adInfo, position, clickListener);
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
    public void onViewRecycled(AdViewHolder holder) {
        Glide.with(context).clear(holder.adMainPhoto);
        super.onViewRecycled(holder);
    }

    public interface ClickListener {
        void onItemClick(int clickedPosition);

        void onFavClick(View view, int clickedPosition, AdInfo adInfo);
    }
}
