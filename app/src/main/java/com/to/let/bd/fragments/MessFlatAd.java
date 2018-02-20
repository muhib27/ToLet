package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.to.let.bd.R;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.MessInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;

public class MessFlatAd extends BaseFragment {
    public static final String TAG = MessFlatAd.class.getSimpleName();

    public static MessFlatAd newInstance() {
        return new MessFlatAd();
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

        AdView adView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return rootView;
    }

    private final String[] messTypes = {"Seat", "Room", "Total Member"};
    private final int[] startPositions = {0, 0, 0};
    private final int[] endPositions = {8, 5, 12};
    private final int[] defaultPositions = {2, 1, 1};

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
                if (messInfoArray[0] > 0) {
                    setMessageForRentPrice(getString(R.string.rent_per_seat));
                } else if (messInfoArray[1] > 0) {
                    if (messInfoArray[1] == 1)
                        setMessageForRentPrice(getString(R.string.rent_per_room));
                    else
                        setMessageForRentPrice(getString(R.string.rent_per_month) + " (" + messInfoArray[1] + " rooms)");
                }
            }
        });
        defaultCheck();
        setMessageForRentPrice(getString(R.string.rent_per_seat));
        updateData();
    }

    public void defaultCheck() {
        for (int i = 0; i < 3; i++)
            messInfoArray[i] = startPositions[i] + defaultPositions[i];
//        calculateRentSpace();
    }

    private NewAdActivity2 activity;

    private LinearLayout roomNumberLay;
    private RadioGroup messMemberType, messManagementSystem;
    private RadioButton messMemberMale, messMemberFemale,
            circulateEveryMonth, manageByIndividual, manageByOffice;
    private CheckBox mealFacilityCB, maidServantCB, twentyFourWaterCB,
            attachedBathCB, attachedBalconyCB,
            nonSmokerCB, onlyStudentsCB, onlyJobHoldersCB, wifiCB, fridgeCB;
    private EditText mealRate;

    private void init() {
        roomNumberLay = rootView.findViewById(R.id.roomNumberLay);
        messMemberType = rootView.findViewById(R.id.messMemberType);
        messMemberMale = rootView.findViewById(R.id.messMemberMale);
        messMemberFemale = rootView.findViewById(R.id.messMemberFemale);

        messManagementSystem = rootView.findViewById(R.id.messManagementSystem);
        circulateEveryMonth = rootView.findViewById(R.id.circulateEveryMonth);
        manageByIndividual = rootView.findViewById(R.id.manageByIndividual);
        manageByOffice = rootView.findViewById(R.id.manageByOffice);

        mealFacilityCB = rootView.findViewById(R.id.mealFacilityCB);
        maidServantCB = rootView.findViewById(R.id.maidServantCB);

        twentyFourWaterCB = rootView.findViewById(R.id.twentyFourWaterCB);
        nonSmokerCB = rootView.findViewById(R.id.nonSmokerCB);

        attachedBathCB = rootView.findViewById(R.id.attachedBathCB);
        attachedBalconyCB = rootView.findViewById(R.id.attachedBalconyCB);

        onlyStudentsCB = rootView.findViewById(R.id.onlyStudentsCB);
        onlyJobHoldersCB = rootView.findViewById(R.id.onlyJobHoldersCB);

        wifiCB = rootView.findViewById(R.id.wifiCB);
        fridgeCB = rootView.findViewById(R.id.fridgeCB);

        mealRate = rootView.findViewById(R.id.mealRate);
    }

    public String getRoomSummary() {
        StringBuilder stringBuilder = new StringBuilder();
        if (messInfoArray[0] > 0) {
            stringBuilder
                    .append(messInfoArray[0])
                    .append("seat");
        }

        if (messInfoArray[1] > 0) {
            if (messInfoArray[0] > 0) {
                stringBuilder.append(" in ");
            }
            stringBuilder
                    .append(messInfoArray[1])
                    .append("room");
        }

        if (attachedBathCB.isChecked() || attachedBalconyCB.isChecked()) {
            if (attachedBathCB.isChecked() && attachedBalconyCB.isChecked()) {
                stringBuilder.append(" with attached bath and balcony.");
            } else if (attachedBathCB.isChecked()) {
                stringBuilder.append(" with attached bathroom.");
            } else {
                stringBuilder.append(" with attached balcony.");
            }
        }

        if (messInfoArray[2] > 0) {
            stringBuilder
                    .append(" may have")
                    .append(messInfoArray[1])
                    .append(" total mess member.");
        }

        return stringBuilder.toString().trim();
    }

    public String getRoomOthersFacility() {
        StringBuilder stringBuilder = new StringBuilder();

        String rentType = getString(R.string.mess);
        if (messMemberType.getCheckedRadioButtonId() == R.id.messMemberFemale) {
            rentType += ", " + getString(R.string.only_female);
        } else {
            rentType += ", " + getString(R.string.only_male);
        }

        stringBuilder.append(rentType);
        stringBuilder.append("\n");

        if (mealFacilityCB.isChecked()) {
            stringBuilder.append(getString(R.string.meal_facility));
            stringBuilder.append("\n");

            if (!mealRate.getText().toString().trim().isEmpty()) {
                stringBuilder.append(getString(R.string.approximate_meal_rate));
                stringBuilder.append(" ");
                stringBuilder.append(mealRate.getText().toString());
                stringBuilder.append("\n");
            }
        }
        if (maidServantCB.isChecked()) {
            stringBuilder.append(getString(R.string.maid_servant));
            stringBuilder.append("\n");
        }
        if (twentyFourWaterCB.isChecked()) {
            stringBuilder.append(getString(R.string.twenty_four_water_facility));
            stringBuilder.append("\n");
        }
        if (nonSmokerCB.isChecked()) {
            stringBuilder.append(getString(R.string.only_non_smoker));
            stringBuilder.append("\n");
        }
        if (fridgeCB.isChecked()) {
            stringBuilder.append(getString(R.string.have_fridge_facility));
            stringBuilder.append("\n");
        }
        if (wifiCB.isChecked()) {
            stringBuilder.append(getString(R.string.have_wifi_facility));
            stringBuilder.append("\n");
        }
        if (onlyStudentsCB.isChecked() && !onlyJobHoldersCB.isChecked()) {
            stringBuilder.append(getString(R.string.only_students));
            stringBuilder.append("\n");
        } else if (!onlyStudentsCB.isChecked() && onlyJobHoldersCB.isChecked()) {
            stringBuilder.append(getString(R.string.only_job_holders));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public MessInfo getMessInfo() {
        MessInfo messInfo = new MessInfo();
        messInfo.memberType = messMemberType.getCheckedRadioButtonId() == R.id.messMemberFemale ? 1 : 0;
        messInfo.numberOfSeat = messInfoArray[0];
        messInfo.numberOfRoom = messInfoArray[1];
        messInfo.totalMember = messInfoArray[2];

        int messManagementSystem = 0;
        if (this.messManagementSystem.getCheckedRadioButtonId() == R.id.manageByIndividual) {
            messManagementSystem = 1;
        } else if (this.messManagementSystem.getCheckedRadioButtonId() == R.id.manageByOffice) {
            messManagementSystem = 2;
        }

        messInfo.messManagementSystem = messManagementSystem;

        messInfo.mealFacility = mealFacilityCB.isChecked();
        messInfo.maidServant = maidServantCB.isChecked();
        messInfo.twentyFourWater = twentyFourWaterCB.isChecked();
        messInfo.nonSmoker = nonSmokerCB.isChecked();
        messInfo.attachedBath = attachedBathCB.isChecked();
        messInfo.attachedBalcony = attachedBalconyCB.isChecked();
        messInfo.onlyStudents = onlyStudentsCB.isChecked();
        messInfo.onlyJobHolders = onlyJobHoldersCB.isChecked();
        messInfo.wifi = wifiCB.isChecked();
        messInfo.fridge = fridgeCB.isChecked();

        messInfo.mealRate = mealRate.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(mealRate.getText().toString());

        return messInfo;
    }

    public void updateData() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        MessInfo messInfo = (MessInfo) bundle.getSerializable(DBConstants.messInfo);
        if (messInfo == null)
            return;

        mealFacilityCB.setChecked(messInfo.mealFacility);
        maidServantCB.setChecked(messInfo.maidServant);
        twentyFourWaterCB.setChecked(messInfo.twentyFourWater);
        nonSmokerCB.setChecked(messInfo.nonSmoker);
        onlyStudentsCB.setChecked(messInfo.onlyStudents);
        onlyJobHoldersCB.setChecked(messInfo.onlyJobHolders);
        wifiCB.setChecked(messInfo.wifi);
        fridgeCB.setChecked(messInfo.fridge);

        if (messInfo.mealRate > 0) mealRate.setText(String.valueOf(messInfo.mealRate));
        else mealRate.setText("");

        if (messInfo.memberType == 1) messMemberFemale.setChecked(true);
        else messMemberMale.setChecked(true);

        if (messInfo.messManagementSystem == 1) manageByIndividual.setChecked(true);
        else if (messInfo.messManagementSystem == 2) manageByOffice.setChecked(true);
        else circulateEveryMonth.setChecked(true);

        for (int i = 0; i < roomNumberLay.getChildCount(); i++) {
            if (roomNumberLay.getChildAt(i).getTag() instanceof String) {
                String title = roomNumberLay.getChildAt(i).getTag().toString();
                String subTitle = "";
                if (roomNumberLay.getChildAt(i).getTag().toString().equalsIgnoreCase(messTypes[0])) {
                    if (messInfo.numberOfSeat == 0)
                        subTitle = "N/A";
                    else
                        subTitle = messInfo.numberOfSeat + " " + title + (messInfo.numberOfSeat > 1 ? "'s" : "");
                    messInfoArray[0] = messInfo.numberOfSeat;
                } else if (roomNumberLay.getChildAt(i).getTag().toString().equalsIgnoreCase(messTypes[1])) {
                    if (messInfo.numberOfRoom == 0)
                        subTitle = "N/A";
                    else
                        subTitle = messInfo.numberOfRoom + " " + title + (messInfo.numberOfRoom > 1 ? "'s" : "");
                    messInfoArray[1] = messInfo.numberOfRoom;
                } else if (roomNumberLay.getChildAt(i).getTag().toString().equalsIgnoreCase(messTypes[2])) {
                    if (messInfo.totalMember == 0)
                        subTitle = "N/A";
                    else
                        subTitle = messInfo.totalMember + " " + title + (messInfo.totalMember > 1 ? "'s" : "");
                    messInfoArray[2] = messInfo.totalMember;
                }

                AppConstants.updatePickerView(roomNumberLay.getChildAt(i), title, subTitle);
            }
        }
    }

    public boolean isSeatOrRoomSelected() {
        if (messInfoArray[0] > 0 || messInfoArray[1] > 0) {
            return true;
        }

        roomNumberLay.requestFocus();
        return false;
    }

    public void setMessageForRentPrice(String message) {
        if (myListener != null) {
            myListener.onItemSelect(message);
        }
    }

    public void setListener(MyListener myListener) {
        this.myListener = myListener;
    }

    public MyListener myListener;

    public interface MyListener {
        void onItemSelect(String message);
    }
}
