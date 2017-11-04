package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.to.let.bd.R;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.utils.AppConstants;

public class MessFragment extends BaseFragment {
    public static final String TAG = MessFragment.class.getSimpleName();

    public static MessFragment newInstance() {
        return new MessFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View rootView;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        activity = (NewAdActivity2) getActivity();
        rootView = inflater.inflate(R.layout.fragment_mess, container, false);
        return rootView;
    }

    private final String[] messTypes = {"Seat", "Room", "Total Member"};
    private final int[] startPositions = {1, 1, 2};
    private final int[] endPositions = {7, 4, 12};
    private final int[] defaultPositions = {1, 0, 5};

    private final int[] familyRoom = new int[3];//0=bedroom, 1=bathroom, 2=balcony, 3=flatFace

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        AppConstants.addParticularView(activity, roomNumberLay, messTypes, startPositions, endPositions, defaultPositions, new AppConstants.PopupMenuClickListener() {
            @Override
            public void onItemClick(String roomType, int selectedPosition) {
                int index = 0;
                for (int i = 0; i < roomType.length(); i++) {
                    if (roomType.equalsIgnoreCase(messTypes[i])) {
                        index = i;
                        break;
                    }
                }
                familyRoom[index] = startPositions[index] + selectedPosition;
//                calculateRentSpace();
            }
        });
        defaultCheck();
    }

    public void defaultCheck() {
        for (int i = 0; i < 3; i++)
            familyRoom[i] = startPositions[i] + defaultPositions[i];
//        calculateRentSpace();
    }

    private NewAdActivity2 activity;

    private LinearLayout roomNumberLay;

    private void init() {
        roomNumberLay = rootView.findViewById(R.id.roomNumberLay);
    }

    public String getRoomDetails() {
        return familyRoom[0] + " bedroom, " + familyRoom[1] + " bathroom, " + familyRoom[2] + " balcony.";
    }

}
