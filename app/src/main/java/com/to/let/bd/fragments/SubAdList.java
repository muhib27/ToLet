package com.to.let.bd.fragments;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.to.let.bd.utils.DBConstants;

/**
 * Created by MAKINUL on 12/13/17.
 */

public class SubAdList extends AdListBaseFragment {

    public static final String TAG = SubAdList.class.getSimpleName();

    @Override
    public int getSubQuery() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getInt(keySubListType);
        }
        return 0;
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All sub type ad like fav, all, user self, nearest, smart
        int subListTypeFav = getSubQuery();
        if (subListTypeFav == 0) {
            return databaseReference
                    .child(DBConstants.adList)
                    .orderByChild(DBConstants.favCount)
                    .startAt(1);
        } else {
            return databaseReference.child(DBConstants.adList);
        }
    }

    @Override
    public Query refreshQuery(DatabaseReference databaseReference) {
        return getQuery(databaseReference);
    }

    private static final String keySubListType = "subListType";

    public static SubAdList newInstance(int subListTypeFav) {
        SubAdList subAdList = new SubAdList();
        final Bundle bundle = new Bundle();
        bundle.putInt(keySubListType, subListTypeFav);
        subAdList.setArguments(bundle);
        return subAdList;
    }
}
