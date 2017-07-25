package com.to.let.bd.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.to.let.bd.R;
import com.to.let.bd.model.AdInfo;

import java.util.ArrayList;

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.MyViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<AdInfo> sampleList;
    private Context context;

    public AdAdapter(Context context, ArrayList<AdInfo> sampleList) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.sampleList = sampleList;
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

        holder.title.setText("" + adInfo.getFlatType());
        holder.address.setText("" + adInfo.getFullAddress());
        holder.roomDetails.setText("" + adInfo.getBedRoom());

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

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title, address, roomDetails;
        public ProgressBar progressBar;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

            title = (TextView) itemView.findViewById(R.id.title);
            address = (TextView) itemView.findViewById(R.id.address);
            roomDetails = (TextView) itemView.findViewById(R.id.roomDetails);
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
