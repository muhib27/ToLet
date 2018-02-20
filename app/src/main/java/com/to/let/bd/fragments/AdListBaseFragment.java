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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.to.let.bd.adapters.AdAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.MyAnalyticsUtil;
import com.to.let.bd.utils.NetworkConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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

        AdView adView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

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

    public void reload() {
        loadingLay.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingMessage.setText(R.string.loading_please_wait);
        tapToRetry.setVisibility(View.GONE);
        hasNetwork = NetworkConnection.getInstance().isAvailable();
        if (adList.isEmpty() && !hasNetwork) {
            loadingProgressBar.setVisibility(View.GONE);
            loadingMessage.setText(R.string.no_network_connection_available);
            tapToRetry.setVisibility(View.VISIBLE);
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyNoNetworkEvent, "no network, list is empty");
            return;
        }

        if (!hasNetwork)
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyNoNetworkEvent, "no network, list is not empty." + adList.isEmpty());
        Query adQuery = getQuery(databaseReference);

        if (adQuery != null) {
            loadData(adQuery);
        }
    }

    public void sort(final int sortType) {
        if (!adList.isEmpty()) {
            Collections.sort(adList, new Comparator<AdInfo>() {
                @Override
                public int compare(AdInfo o1, AdInfo o2) {
                    if (sortType == 2) {
                        return (int) (o1.startingFinalDate - o2.startingFinalDate);
                    } else if (sortType == 3) {
                        return (int) (o2.startingFinalDate - o1.startingFinalDate);
                    } else if (sortType == 1) {
                        return (int) (o1.flatRent - o2.flatRent);
                    } else {
                        return (int) (o2.flatRent - o1.flatRent);
                    }
                }
            });

            adAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        adRecyclerView.setLayoutManager(mManager);
        adAdapter = new AdAdapter(getActivity(), this);
        adRecyclerView.setAdapter(adAdapter);
        reload();
    }

    @Override
    public void onResume() {
        super.onResume();
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

    protected ArrayList<AdInfo> adList = new ArrayList<>();

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            adList.clear();

            int subQueryValue = getSubQueryType();

            for (DataSnapshot adSnapshot : dataSnapshot.getChildren()) {
                AdInfo adInfo = adSnapshot.getValue(AdInfo.class);

                if (adInfo != null) {
                    if (subQueryValue == AppConstants.subQueryMy && adInfo.deleteReason >= 0 && adInfo.deleteReason != 3) {
                        adList.add(adInfo);
                    } else if (adInfo.isActive) {
                        if (subQueryValue == AppConstants.subQueryQuery) {
                            if (BaseActivity.fromDateTime > 0 || BaseActivity.toDateTime > 0) {
                                if (BaseActivity.fromDateTime > 0 && BaseActivity.toDateTime > 0) {
                                    if (adInfo.startingFinalDate >= BaseActivity.fromDateTime && adInfo.startingFinalDate <= BaseActivity.toDateTime)
                                        adList.add(adInfo);
                                } else if (BaseActivity.fromDateTime > 0) {
                                    if (adInfo.startingFinalDate >= BaseActivity.fromDateTime)
                                        adList.add(adInfo);
                                } else {//toDate > 0
                                    if (adInfo.startingFinalDate <= BaseActivity.toDateTime)
                                        adList.add(adInfo);
                                }
                            } else {
                                adList.add(adInfo);
                            }
                        } else {
                            adList.add(adInfo);
                        }
                    }
                }
            }

            if (adList.size() > 0) {
                Collections.sort(adList, new Comparator<AdInfo>() {
                    @Override
                    public int compare(AdInfo o1, AdInfo o2) {
                        return (int) (o2.modifiedTime - o1.modifiedTime);
                    }
                });
//                adList = addAdvertiseView(adList);
                displayAdList();
            } else {
                loadingLay.setVisibility(View.VISIBLE);
                loadingMessage.setText(R.string.no_data_found);
                loadingProgressBar.setVisibility(View.GONE);
                tapToRetry.setVisibility(View.GONE);
            }

            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyFirebaseDatabaseQueryRefEvent, dataSnapshot.getRef().toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            loadingLay.setVisibility(View.VISIBLE);
            loadingMessage.setText(R.string.internal_server_error);
            loadingProgressBar.setVisibility(View.GONE);
            tapToRetry.setVisibility(View.VISIBLE);

            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyAdLoadedFailedEvent, databaseError.getDetails());
        }
    };

    private AdAdapter adAdapter;

    private void displayAdList() {
        loadingLay.setVisibility(View.GONE);
        adAdapter.setData(adList);
        adAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int clickedPosition) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).startAdDetailsActivity(clickedPosition < adList.size() ? adList.get(clickedPosition) : adList.get(0));
    }

    @Override
    public void onFavClick(View view, int clickedPosition, AdInfo adInfo) {
        String flatType = DBConstants.keyFamily;
        if (adInfo.familyInfo != null) {
            flatType = DBConstants.keyFamily;
        } else if (adInfo.messInfo != null) {
            flatType = DBConstants.keyMess;
        } else if (adInfo.subletInfo != null) {
            flatType = DBConstants.keySublet;
        } else if (adInfo.othersInfo != null) {
            flatType = DBConstants.keyOthers;
        }
        DatabaseReference userFavAdRef = databaseReference
                .child(DBConstants.userFavAdList)
                .child(getUid())
                .child(adInfo.adId);

        myAnalyticsUtil.favItem(adInfo.adId, !view.isSelected());
        if (view.isSelected()) {
            userFavAdRef.removeValue();
        } else {
            AdInfo tmpAdInfo = adInfo;
            tmpAdInfo.favCount++;
            if (tmpAdInfo.fav == null) {
                tmpAdInfo.fav = new HashMap<>();
            }
            tmpAdInfo.fav.put(getUid(), true);
            userFavAdRef.setValue(tmpAdInfo);
        }
        view.setSelected(!view.isSelected());

        DatabaseReference globalAdRef = databaseReference.child(DBConstants.adList).child(flatType).child(adInfo.adId);
        DatabaseReference userAdRef = databaseReference.child(DBConstants.userAdList).child(adInfo.userId).child(adInfo.adId);

        // Run two transactions
        onFavClicked(globalAdRef, view, clickedPosition);
        onFavClicked(userAdRef, view, clickedPosition);
    }

    // [START ad_stars_transaction]
    private void onFavClicked(DatabaseReference adRef, final View view, final int clickedPosition) {
        view.setEnabled(false);
        adRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                AdInfo adInfo = mutableData.getValue(AdInfo.class);
                if (adInfo == null) {
                    return Transaction.success(mutableData);
                }
                if (adInfo.fav.containsKey(getUid())) {
                    // UnFav the ad and remove self from stars
                    adInfo.favCount = adInfo.favCount - 1;
                    adInfo.fav.remove(getUid());
                } else {
                    // Fav the ad and add self to stars
                    adInfo.favCount = adInfo.favCount + 1;
                    adInfo.fav.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(adInfo);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                view.setEnabled(true);
                if (databaseError == null) {
                    AdInfo adInfo = dataSnapshot.getValue(AdInfo.class);
                    if (adInfo != null)
                        adList.set(clickedPosition, adInfo);
//                    adAdapter.notifyItemChanged(clickedPosition);
                }
            }
        });
    }
    // [END ad_fav_transaction]

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

    public int getSubQueryType() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getInt(AppConstants.keySubAdListType);
        }
        return -1;
    }

    private ArrayList<AdInfo> addAdvertiseView(ArrayList<AdInfo> foundList) {
        ArrayList<AdInfo> finalList = new ArrayList<>();
        finalList.clear();

        for (int i = 0; i < foundList.size(); i++) {
            finalList.add(foundList.get(i));
            if (i % 3 == 0) {
                AdInfo adInfo = new AdInfo();
                finalList.add(adInfo);
            }
        }
        return finalList;
    }

//    private ArrayList<AdInfo> filterList(int type, ArrayList<AdInfo> foundList) {
//        if (type == AppConstants.subQueryFav) {
//            ArrayList<AdInfo> filterList = new ArrayList<>();
//            filterList.clear();
//
//            String userId = BaseActivity.getUid();
//            for (AdInfo adInfo : foundList) {
//                if (adInfo.fav == null)
//                    continue;
//                if (adInfo.fav.containsKey(userId) && adInfo.fav.get(userId)) {
//                    filterList.add(adInfo);
//                }
//            }
//            return filterList;
//        }
//        return foundList;
//    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    public abstract Query refreshQuery(DatabaseReference databaseReference);
}
