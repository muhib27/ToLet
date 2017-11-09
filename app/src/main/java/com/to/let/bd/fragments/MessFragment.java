package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.to.let.bd.R;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.MessInfo;
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
    private final int[] startPositions = {1, 1, 1};
    private final int[] endPositions = {7, 4, 12};
    private final int[] defaultPositions = {1, 0, 5};

    private final int[] messInfoArray = new int[3];//0=Seat, 1=Room, 2=Total Member

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
                messInfoArray[index] = startPositions[index] + selectedPosition;
            }
        });
        defaultCheck();
    }

    public void defaultCheck() {
        for (int i = 0; i < 3; i++)
            messInfoArray[i] = startPositions[i] + defaultPositions[i];
//        calculateRentSpace();
    }

    private NewAdActivity2 activity;

    private LinearLayout roomNumberLay;
    private RadioGroup messMemberType, messManagementSystem;
    private CheckBox mealFacilityCB, maidServantCB, twentyFourWaterCB,
            nonSmokerCB, wifiCB, fridgeCB;
    private EditText mealRate;

    private void init() {
        roomNumberLay = rootView.findViewById(R.id.roomNumberLay);
        messMemberType = rootView.findViewById(R.id.messMemberType);
        messManagementSystem = rootView.findViewById(R.id.messManagementSystem);

        mealFacilityCB = rootView.findViewById(R.id.mealFacilityCB);
        maidServantCB = rootView.findViewById(R.id.maidServantCB);
        twentyFourWaterCB = rootView.findViewById(R.id.twentyFourWaterCB);

        nonSmokerCB = rootView.findViewById(R.id.nonSmokerCB);
        wifiCB = rootView.findViewById(R.id.wifiCB);
        fridgeCB = rootView.findViewById(R.id.fridgeCB);

        mealRate = rootView.findViewById(R.id.mealRate);
    }

    public String getRoomDetails() {
        return messInfoArray[0] + " seat with " + messInfoArray[1] + " room, " + messInfoArray[2] + " total mess member.";
    }

    public MessInfo getMessInfo() {
        MessInfo messInfo = new MessInfo();
        messInfo.setMemberType(messMemberType.getCheckedRadioButtonId() == R.id.messMemberMale ? 0 : 1);
        messInfo.setNumberOfSeat(messInfoArray[0]);
        messInfo.setNumberOfRoom(messInfoArray[1]);
        messInfo.setTotalMember(messInfoArray[2]);

        int messManagementSystem = 0;
        if (this.messManagementSystem.getCheckedRadioButtonId() == R.id.manageByIndividual) {
            messManagementSystem = 1;
        } else if (this.messManagementSystem.getCheckedRadioButtonId() == R.id.manageByOffice) {
            messManagementSystem = 2;
        }

        messInfo.setMessManagementSystem(messManagementSystem);

        messInfo.setMealFacility(mealFacilityCB.isChecked());
        messInfo.setMaidServant(maidServantCB.isChecked());
        messInfo.setTwentyFourWater(twentyFourWaterCB.isChecked());
        messInfo.setNonSmoker(nonSmokerCB.isChecked());
        messInfo.setWifi(wifiCB.isChecked());
        messInfo.setFridge(fridgeCB.isChecked());

        messInfo.setMealRate(mealRate.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(mealRate.getText().toString()));

        return messInfo;
    }

}
