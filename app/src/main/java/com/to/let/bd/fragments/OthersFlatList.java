package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.to.let.bd.R;
import com.to.let.bd.utils.DBConstants;

public class OthersFlatList extends AdListFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child(DBConstants.adList).orderByChild(DBConstants.flatType).equalTo(getString(R.string.others));
    }

    @Override
    public Query refreshQuery(DatabaseReference databaseReference) {
        return databaseReference.child(DBConstants.adList).startAt(12000).endAt(20000);
    }

    public static final String keyPosition = "position";

    public static Fragment newInstance(int position) {
        OthersFlatList othersFlatList = new OthersFlatList();
        final Bundle bundle = new Bundle();
        bundle.putInt(keyPosition, position);
        othersFlatList.setArguments(bundle);
        return othersFlatList;
    }
}
