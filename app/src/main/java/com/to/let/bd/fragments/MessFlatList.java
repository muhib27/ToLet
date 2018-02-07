package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.to.let.bd.R;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.DateUtils;

public class MessFlatList extends AdListBaseFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All mess type ad
        return databaseReference
                .child(DBConstants.adList)
                .child(DBConstants.keyMess)
                .orderByChild(DBConstants.startingFinalDate)
                .startAt(DateUtils.todayYearMonthDate());
    }

    @Override
    public Query refreshQuery(DatabaseReference databaseReference) {
        return getQuery(databaseReference);
    }

    public static final String keyPosition = "position";

    public static Fragment newInstance(int position) {
        MessFlatList messFlatList = new MessFlatList();
        final Bundle bundle = new Bundle();
        bundle.putInt(keyPosition, position);
        messFlatList.setArguments(bundle);
        return messFlatList;
    }
}
