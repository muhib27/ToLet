package com.to.let.bd.fragments;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.to.let.bd.R;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;

/**
 * Created by MAKINUL on 12/13/17.
 */

public class SubAdList extends AdListBaseFragment {

    public static final String TAG = SubAdList.class.getSimpleName();

    public int getSubQueryType() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getInt(AppConstants.keySubAdListType);
        }
        return -1;
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All sub type ad like fav, all, user self, nearest, smart
        int subListTypeFav = getSubQueryType();
        if (subListTypeFav == AppConstants.subQuerySmart) {
            return null;
        } else if (subListTypeFav == AppConstants.subQueryMy) {
            return databaseReference
                    .child(DBConstants.userAdList)
                    .child(getUid())
                    .orderByChild(DBConstants.userId)
                    .equalTo(getUid());
        } else if (subListTypeFav == AppConstants.subQueryFav) {
            return databaseReference
                    .child(DBConstants.userFavAdList)
                    .child(getUid());
        } else if (subListTypeFav == AppConstants.subQueryQuery) {
            Bundle bundle = getArguments();
            String[] childArray = null;
            long fromDateTime = 0, toDateTime = 0, rentMinLong = 0, rentMaxLong = 0;
            if (bundle != null) {
                childArray = bundle.getStringArray(AppConstants.keyChildArray);
                fromDateTime = bundle.getLong(AppConstants.keyFromDateTime, 0);
                toDateTime = bundle.getLong(AppConstants.keyToDateTime, 0);
                rentMinLong = bundle.getLong(AppConstants.keyRentMinLong, 0);
                rentMaxLong = bundle.getLong(AppConstants.keyRentMaxLong, 0);
            }

            if (childArray == null)
                return null;

            for (String child : childArray) {
                databaseReference = databaseReference.child(child);
            }

            if (fromDateTime == 0 && toDateTime == 0 && rentMinLong == 0 && rentMaxLong == 0) {
                showSimpleDialog(R.string.please_insert_valid_data);
                return null;
            }

            if (fromDateTime > 0 && toDateTime > 0 && fromDateTime > toDateTime &&
                    rentMinLong > 0 && rentMaxLong > 0 && rentMinLong > rentMaxLong) {
                showSimpleDialog(R.string.please_insert_valid_date_range_and_rent_range);
                return null;
            } else {
                if (fromDateTime > 0 && toDateTime > 0 && fromDateTime > toDateTime) {
                    showSimpleDialog(R.string.please_insert_valid_date_range);
                    return null;
                } else if (rentMinLong > 0 && rentMaxLong > 0 && rentMinLong > rentMaxLong) {
                    showSimpleDialog(R.string.please_insert_valid_rent_range);
                    return null;
                }
            }

            Query query = databaseReference;
            if ((fromDateTime == 0 && toDateTime == 0) || (rentMinLong == 0 && rentMaxLong == 0)) {
                if (fromDateTime == 0 && toDateTime == 0) {
                    query = query.orderByChild(DBConstants.flatRent);
                    if (rentMinLong > 0 && rentMaxLong > 0) {
                        query = query.startAt(rentMinLong).endAt(rentMaxLong);
                    } else {
                        if (rentMinLong == 0) {
                            query = query.endAt(rentMaxLong);
                        } else {
                            query = query.startAt(rentMinLong);
                        }
                    }
                } else {
                    query = query.orderByChild(DBConstants.startingFinalDate);
                    if (fromDateTime > 0 && toDateTime > 0) {
                        query = query.startAt(fromDateTime).endAt(toDateTime);
                    } else {
                        if (fromDateTime == 0) {
                            query = query.endAt(toDateTime);
                        } else {
                            query = query.startAt(fromDateTime);
                        }
                    }
                }
//                loadData(query, 0, 0);
                bundle.putLong(AppConstants.keyFromDateTime, 0);
                bundle.putLong(AppConstants.keyToDateTime, 0);
                setArguments(bundle);
                return query;
            } else {
                query = query.orderByChild(DBConstants.flatRent);
                if (rentMinLong > 0 && rentMaxLong > 0) {
                    query = query.startAt(rentMinLong).endAt(rentMaxLong);
                } else {
                    if (rentMinLong == 0) {
                        query = query.endAt(rentMaxLong);
                    } else {
                        query = query.startAt(rentMinLong);
                    }
                }
//                loadData(query, fromDateTime, toDateTime);
                return query;
            }
        } else {
            return null;
        }
    }

    @Override
    public Query refreshQuery(DatabaseReference databaseReference) {
        return getQuery(databaseReference);
    }

    public static SubAdList newInstance() {
        return new SubAdList();
    }
}
