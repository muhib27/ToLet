package com.to.let.bd.fragments;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.to.let.bd.common.BaseActivity;
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
        int subQueryType = getSubQueryType();
        if (subQueryType == AppConstants.subQuerySmart) {
            return null;
        } else if (subQueryType == AppConstants.subQueryMy) {
            return databaseReference
                    .child(DBConstants.userAdList)
                    .child(getUid())
                    .orderByChild(DBConstants.userId)
                    .equalTo(getUid());
        } else if (subQueryType == AppConstants.subQueryFav) {
            return databaseReference
                    .child(DBConstants.userFavAdList)
                    .child(getUid());
        } else if (subQueryType == AppConstants.subQueryQuery) {
//            Bundle bundle = getArguments();
//            String[] childArray = null;
//            long fromDateTime = 0, toDateTime = 0, rentMinLong = 0, rentMaxLong = 0;
//            if (bundle != null) {
//                childArray = bundle.getStringArray(AppConstants.keyChildArray);
//                fromDateTime = bundle.getLong(AppConstants.keyFromDateTime, 0);
//                toDateTime = bundle.getLong(AppConstants.keyToDateTime, 0);
//                rentMinLong = bundle.getLong(AppConstants.keyRentMinLong, 0);
//                rentMaxLong = bundle.getLong(AppConstants.keyRentMaxLong, 0);
//            }

            if (BaseActivity.childArray == null)
                return null;

            for (String child : BaseActivity.childArray) {
                databaseReference = databaseReference.child(child);
            }

//            if (BaseActivity.fromDateTime == 0 && BaseActivity.toDateTime == 0
//                    && BaseActivity.rentMinLong == 0 && BaseActivity.rentMaxLong == 0) {
//                showSimpleDialog(R.string.please_insert_valid_data);
//                return null;
//            }
//
//            if (fromDateTime > 0 && toDateTime > 0 && fromDateTime > toDateTime &&
//                    rentMinLong > 0 && rentMaxLong > 0 && rentMinLong > rentMaxLong) {
//                showSimpleDialog(R.string.please_insert_valid_date_range_and_rent_range);
//                return null;
//            } else {
//                if (fromDateTime > 0 && toDateTime > 0 && fromDateTime > toDateTime) {
//                    showSimpleDialog(R.string.please_insert_valid_date_range);
//                    return null;
//                } else if (rentMinLong > 0 && rentMaxLong > 0 && rentMinLong > rentMaxLong) {
//                    showSimpleDialog(R.string.please_insert_valid_rent_range);
//                    return null;
//                }
//            }

            Query query = databaseReference;
            if ((BaseActivity.fromDateTime == 0 && BaseActivity.toDateTime == 0)
                    || (BaseActivity.rentMinLong == 0 && BaseActivity.rentMaxLong == 0)) {
                if (BaseActivity.fromDateTime == 0 && BaseActivity.toDateTime == 0) {
                    query = query.orderByChild(DBConstants.flatRent);
                    if (BaseActivity.rentMinLong > 0 && BaseActivity.rentMaxLong > 0) {
                        query = query.startAt(BaseActivity.rentMinLong).endAt(BaseActivity.rentMaxLong);
                    } else {
                        if (BaseActivity.rentMinLong == 0) {
                            query = query.endAt(BaseActivity.rentMaxLong);
                        } else {
                            query = query.startAt(BaseActivity.rentMinLong);
                        }
                    }
                } else {
                    query = query.orderByChild(DBConstants.startingFinalDate);
                    if (BaseActivity.fromDateTime > 0 && BaseActivity.toDateTime > 0) {
                        query = query.startAt(BaseActivity.fromDateTime).endAt(BaseActivity.toDateTime);
                    } else {
                        if (BaseActivity.fromDateTime == 0) {
                            query = query.endAt(BaseActivity.toDateTime);
                        } else {
                            query = query.startAt(BaseActivity.fromDateTime);
                        }
                    }
                }
//                loadData(query, 0, 0);
//                bundle.putLong(AppConstants.keyFromDateTime, 0);
//                bundle.putLong(AppConstants.keyToDateTime, 0);
//                setArguments(bundle);
                return query;
            } else {
                query = query.orderByChild(DBConstants.flatRent);
                if (BaseActivity.rentMinLong > 0 && BaseActivity.rentMaxLong > 0) {
                    query = query.startAt(BaseActivity.rentMinLong).endAt(BaseActivity.rentMaxLong);
                } else {
                    if (BaseActivity.rentMinLong == 0) {
                        query = query.endAt(BaseActivity.rentMaxLong);
                    } else {
                        query = query.startAt(BaseActivity.rentMinLong);
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
