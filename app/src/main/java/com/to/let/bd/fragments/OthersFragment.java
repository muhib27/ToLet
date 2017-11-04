package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.to.let.bd.R;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.utils.AppConstants;

public class OthersFragment extends BaseFragment {
    public static final String TAG = OthersFragment.class.getSimpleName();

    public static OthersFragment newInstance() {
        return new OthersFragment();
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
        rootView = inflater.inflate(R.layout.fragment_others, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        AppConstants.addParticularView(activity, roomNumberLay, roomTypes, startPositions, endPositions, defaultPositions, new AppConstants.PopupMenuClickListener() {
            @Override
            public void onItemClick(String roomType, int selectedPosition) {
                int index = 0;
                for (int i = 0; i < roomType.length(); i++) {
                    if (roomType.equalsIgnoreCase(roomTypes[i])) {
                        index = i;
                        break;
                    }
                }
                familyRoom[index] = startPositions[index] + selectedPosition;
                calculateRentSpace();
            }
        });
        defaultCheck();
    }

    private NewAdActivity2 activity;

    private LinearLayout roomNumberLay;
    private EditText totalSpace;

    private void init() {
        roomNumberLay = rootView.findViewById(R.id.roomNumberLay);
        totalSpace = rootView.findViewById(R.id.totalSpace);
    }

    private final String[] roomTypes = {"Bedroom", "Bathroom", "Balcony"};
    private final int[] startPositions = {1, 1, 0};
    private final int[] endPositions = {10, 8, 6};
    private final int[] defaultPositions = {3, 3, 3};

    private final int[] familyRoom = new int[3];//0=bedroom, 1=bathroom, 2=balcony

    public void defaultCheck() {
        for (int i = 0; i < 3; i++)
            familyRoom[i] = startPositions[i] + defaultPositions[i];
        calculateRentSpace();
    }

    private void calculateRentSpace() {
        long calculatedRent = (familyRoom[0] * singleBedRoomRent) + (familyRoom[1] * bathroomRent) + (familyRoom[2] * balconyRent);
        long calculatedSpace = (familyRoom[0] * singleBedRoomSpace) + (familyRoom[1] * bathroomSpace) + (familyRoom[2] * balconySpace);

        totalSpace.setText(String.valueOf(calculatedSpace));
        activity.updateCalculatedRent(calculatedRent);
    }

    public String getRoomDetails() {
        return familyRoom[0] + " bedroom, " + familyRoom[1] + " bathroom, " + familyRoom[2] + " balcony.";
    }

    private final long singleBedRoomRent = 6000;//rent BDT
    private final long bathroomRent = 1500;//rent BDT
    private final long balconyRent = 1000;//rent BDT

    private final long singleBedRoomSpace = 210;//space sqrft
    private final long bathroomSpace = 60;//space sqrft
    private final long balconySpace = 50;//space sqrft

    private String[] rentType = {"Duplex", "Office", "Godown", "Mini-Shop", "Commercial-Space", "Market-Place"};
}
