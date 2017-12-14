package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.to.let.bd.R;
import com.to.let.bd.utils.DBConstants;

public class SubletFlatList extends AdListBaseFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All sublet type ad
        return databaseReference.child(DBConstants.adList).orderByChild(DBConstants.flatType).equalTo(getString(R.string.sublet));
    }

    @Override
    public Query refreshQuery(DatabaseReference databaseReference) {
        return getQuery(databaseReference);
    }

    public static final String keyPosition = "position";

    public static Fragment newInstance(int position) {
        SubletFlatList subletFlatList = new SubletFlatList();
        final Bundle bundle = new Bundle();
        bundle.putInt(keyPosition, position);
        subletFlatList.setArguments(bundle);
        return subletFlatList;
    }

    @Override
    public int getSubQuery() {
        return -1;
    }
}
