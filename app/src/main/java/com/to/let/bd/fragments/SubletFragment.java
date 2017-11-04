package com.to.let.bd.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.to.let.bd.R;
import com.to.let.bd.common.BaseFragment;

public class SubletFragment extends BaseFragment {
    public static final String TAG = SubletFragment.class.getSimpleName();

//    private static final String ARG_POSITION = "position";
//    private static final String ARG_ID = "id";
//
//    private int mPosition;
//
//    public static FamilyFragment newInstance(final int position, final int id) {
//        final FamilyFragment storeTabFragment = new FamilyFragment();
//        final Bundle b = new Bundle();
//        b.putInt(ARG_POSITION, position);
//        b.putInt(ARG_ID, id);
//        storeTabFragment.setArguments(b);
//        return storeTabFragment;
//    }

    public static SubletFragment newInstance() {
        return new SubletFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mPosition = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sublet, container, false);
        init(view);
        return view;
    }

    private void init(View view) {

    }

    public String getRoomDetails() {
        return null;
    }
}
