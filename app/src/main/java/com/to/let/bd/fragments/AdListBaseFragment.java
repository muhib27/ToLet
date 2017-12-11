package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.to.let.bd.R;
import com.to.let.bd.activities.SplashActivity;
import com.to.let.bd.adapters.AdAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.MyAnalyticsUtil;
import com.to.let.bd.utils.NetworkConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class AdListBaseFragment extends BaseFragment implements AdAdapter.ClickListener {

    private static final String TAG = AdListBaseFragment.class.getSimpleName();

    // [START define_database_reference]
    private DatabaseReference databaseReference;
    // [END define_database_reference]

    //    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView adRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START create_database_reference]
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        myAnalyticsUtil = new MyAnalyticsUtil(getActivity());
//        networkCheck();
    }

    private MyAnalyticsUtil myAnalyticsUtil;

    private boolean hasNetwork = false;

//    private void networkCheck() {
//        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
//        connectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                Boolean tmpValue = snapshot.getValue(Boolean.class);
//                if (tmpValue != null)
//                    hasNetwork = tmpValue;
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                hasNetwork = false;
//            }
//        });
//    }

    private LinearLayout loadingLay;
    private ProgressBar loadingProgressBar;
    private TextView tapToRetry, loadingMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_ad_list, container, false);

        adRecyclerView = rootView.findViewById(R.id.adList);
        adRecyclerView.setHasFixedSize(true);

        loadingLay = rootView.findViewById(R.id.loadingLay);
        loadingProgressBar = rootView.findViewById(R.id.loadingProgressBar);
        tapToRetry = rootView.findViewById(R.id.tapToRetry);
        loadingMessage = rootView.findViewById(R.id.loadingMessage);

        loadingLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tapToRetry.getVisibility() == View.VISIBLE)
                    reload();
            }
        });
//        swipeRefresh = rootView.findViewById(R.id.swipeRefresh);
//        swipeRefresh.setColorSchemeResources(R.color.swipeRefresh1, R.color.swipeRefresh2, R.color.swipeRefresh3);
//        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                reload();
//            }
//        });
        return rootView;
    }

    private void reload() {
        loadingLay.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMessage.setText(R.string.loading_please_wait);
        tapToRetry.setVisibility(View.GONE);
        hasNetwork = NetworkConnection.getInstance().isAvailable();
        if (adList.isEmpty()) {
            if (!hasNetwork) {
                loadingProgressBar.setVisibility(View.GONE);
                loadingMessage.setText(R.string.no_network_connection_available);
                tapToRetry.setVisibility(View.VISIBLE);
                return;
            }
        }
        Query adQuery = getQuery(databaseReference);
        loadData(adQuery);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//        mManager.setReverseLayout(true);
//        mManager.setStackFromEnd(true);
        adRecyclerView.setLayoutManager(mManager);
        reload();
    }

    private void loadData(Query adQuery) {
        if (adList.size() > 0) {
            displayAdList();
        }
        adQuery.addListenerForSingleValueEvent(valueEventListener);
//        adQuery.addChildEventListener(childEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (databaseReference != null)
            databaseReference.removeEventListener(valueEventListener);
    }

    private ArrayList<AdInfo> adList = new ArrayList<>();

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            adList.clear();
            for (DataSnapshot adSnapshot : dataSnapshot.getChildren()) {
                AdInfo adInfo = adSnapshot.getValue(AdInfo.class);

                if ((adInfo != null ? adInfo.getStartingFinalDate() : 0) >= SplashActivity.todayYearMonthDate)
                    adList.add(adInfo);
            }

            if (adList.size() > 0) {
                Collections.sort(adList, new Comparator<AdInfo>() {
                    @Override
                    public int compare(AdInfo o1, AdInfo o2) {
                        return (int) (o2.getCreatedTime() - o1.getCreatedTime());
                    }
                });
                displayAdList();
            } else {
                loadingLay.setVisibility(View.VISIBLE);
                loadingMessage.setText(R.string.no_data_found);
                loadingProgressBar.setVisibility(View.GONE);
                tapToRetry.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            loadingLay.setVisibility(View.VISIBLE);
            loadingMessage.setText(R.string.internal_server_error);
            loadingProgressBar.setVisibility(View.GONE);
            tapToRetry.setVisibility(View.VISIBLE);
        }
    };
    private AdAdapter adAdapter;

    private void displayAdList() {
        loadingLay.setVisibility(View.GONE);
        adAdapter = new AdAdapter(getActivity(), this);
        adRecyclerView.setAdapter(adAdapter);

        adAdapter.setData(adList);
        adAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdInfo adInfo) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).startAdDetailsActivity(adInfo);
    }

    @Override
    public void onFavClick(View view, int clickedPosition, AdInfo adInfo) {
//        if (view instanceof ImageView) {
//            ((ImageView) view).setImageResource(R.drawable.ic_fav_selected);
//        }

        DatabaseReference userAdRef = databaseReference.child(DBConstants.adList).child(adInfo.getAdId());
        onStarClicked(userAdRef, clickedPosition);
        myAnalyticsUtil.favItem(adInfo, getUid());
    }

    // [START ad_stars_transaction]
    private void onStarClicked(DatabaseReference adRef, final int clickedPosition) {
        adRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                AdInfo p = mutableData.getValue(AdInfo.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                if (p.fav.containsKey(getUid())) {
                    // Unstar the ad and remove self from stars
                    p.favCount = p.favCount - 1;
                    p.fav.remove(getUid());
                } else {
                    // Star the ad and add self to stars
                    p.favCount = p.favCount + 1;
                    p.fav.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                    AdInfo adInfo = dataSnapshot.getValue(AdInfo.class);
                    adList.set(clickedPosition, adInfo);
//                    adAdapter.notifyItemChanged(clickedPosition);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public String getUid() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null)
            return null;
        return firebaseUser.getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    public abstract Query refreshQuery(DatabaseReference databaseReference);
}
