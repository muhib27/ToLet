package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.viewholder.AdViewHolder;

public abstract class AdListFragment extends BaseFragment {

    private static final String TAG = AdListFragment.class.getSimpleName();

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private SwipeRefreshLayout swipeRefresh;
    private FirebaseRecyclerAdapter<AdInfo, AdViewHolder> mAdapter;
    private RecyclerView adList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_ad_list, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        swipeRefresh = rootView.findViewById(R.id.swipeRefresh);

        adList = rootView.findViewById(R.id.adList);
        adList.setHasFixedSize(true);

        swipeRefresh.setColorSchemeResources(R.color.swipeRefresh1, R.color.swipeRefresh2, R.color.swipeRefresh3);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Set up FirebaseRecyclerAdapter with the Query
                Query adQuery = refreshQuery(mDatabase);
                loadData(adQuery);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        adList.setLayoutManager(mManager);
        // Set up FirebaseRecyclerAdapter with the Query
        Query adQuery = getQuery(mDatabase);
        loadData(adQuery);
    }

    private void loadData(Query adQuery) {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<AdInfo>()
                .setQuery(adQuery, AdInfo.class)
                .build();
        swipeRefresh.setRefreshing(true);
        if (mAdapter == null) {
            mAdapter = new FirebaseRecyclerAdapter<AdInfo, AdViewHolder>(options) {

                @Override
                public AdViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                    return new AdViewHolder(inflater.inflate(R.layout.item_ad, viewGroup, false));
                }

                @Override
                protected void onBindViewHolder(AdViewHolder viewHolder, int position, final AdInfo model) {
                    swipeRefresh.setRefreshing(false);
                    final DatabaseReference adRef = getRef(position);

                    // Set click listener for the whole ad view
                    final String adKey = adRef.getKey();
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Launch AdDetailActivity
//                        Intent intent = new Intent(getActivity(), AdDetailActivity.class);
//                        intent.putExtra(AdDetailActivity.EXTRA_POST_KEY, adKey);
//                        startActivity(intent);
                        }
                    });

//                // Determine if the current user has liked this ad and set UI accordingly
//                if (model.stars.containsKey(getUid())) {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
//                } else {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
//                }

                    // Bind AdInfo to ViewHolder, setting OnClickListener for the star button
                    viewHolder.bindToAd(model, new View.OnClickListener() {
                        @Override
                        public void onClick(View starView) {
//                        // Need to write to both places the ad is stored
//                        DatabaseReference globalAdRef = mDatabase.child(DBConstants.adList).child(adRef.getKey());
//                        DatabaseReference userAdRef = mDatabase.child("user-ads").child(model.getAdId()).child(adRef.getKey());
//
//                        // Run two transactions
//                        onStarClicked(globalAdRef);
//                        onStarClicked(userAdRef);
                        }
                    });
                }
            };
            adList.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    // [START ad_stars_transaction]
    private void onStarClicked(DatabaseReference adRef) {
        adRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                AdInfo p = mutableData.getValue(AdInfo.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
//                if (p.stars.containsKey(getUid())) {
//                    // Unstar the ad and remove self from stars
//                    p.starCount = p.starCount - 1;
//                    p.stars.remove(getUid());
//                } else {
//                    // Star the ad and add self to stars
//                    p.starCount = p.starCount + 1;
//                    p.stars.put(getUid(), true);
//                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "adTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END ad_stars_transaction]

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    public abstract Query refreshQuery(DatabaseReference databaseReference);
}
