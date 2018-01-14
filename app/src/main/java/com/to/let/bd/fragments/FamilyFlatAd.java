package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
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
import com.to.let.bd.model.FamilyInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;

public class FamilyFlatAd extends BaseFragment {
    public static final String TAG = FamilyFlatAd.class.getSimpleName();

    public static FamilyFlatAd newInstance() {
        return new FamilyFlatAd();
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
        rootView = inflater.inflate(R.layout.fragment_family, container, false);

        AdView adView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return rootView;
    }

    private final String[] roomTypes = {"Bedroom", "Bathroom", "Balcony"};
    private final int[] startPositions = {1, 1, 0};
    private final int[] endPositions = {7, 5, 5};
    private final int[] defaultPositions = {1, 1, 1};

    private final int[] familyRoom = new int[3];//0=bedroom, 1=bathroom, 2=balcony

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

                if (index == 0) {
                    if (startPositions[index] + selectedPosition > 4)
                        isItDuplexLay.setVisibility(View.VISIBLE);
                    else
                        isItDuplexLay.setVisibility(View.GONE);
                }
                familyRoom[index] = startPositions[index] + selectedPosition;
                calculateRentSpace();
            }
        });
        defaultCheck();
        updateData();
    }

    private void defaultCheck() {
        drawingDining.check(R.id.drawingDiningYes);
        drawingDining.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                calculateRentSpace();
            }
        });

        for (int i = 0; i < 3; i++)
            familyRoom[i] = startPositions[i] + defaultPositions[i];
        calculateRentSpace();
    }

    private NewAdActivity2 activity;

    private LinearLayout roomNumberLay, isItDuplexLay;
    private EditText totalSpace;
    private RadioGroup drawingDining, isItDuplex;
    private RadioButton drawingDiningYes, drawingDiningNo, isItDuplexYes, isItDuplexNo;
    private CheckBox twentyFourWaterCB, gasSupplyCB, wellFurnishedCB,
            securityGuardCB, liftCB, generatorCB, parkingGarageCB, kitchenCabinetCB;

    private void init() {
        roomNumberLay = rootView.findViewById(R.id.roomNumberLay);
        isItDuplexLay = rootView.findViewById(R.id.isItDuplexLay);
        totalSpace = rootView.findViewById(R.id.totalSpace);

        drawingDining = rootView.findViewById(R.id.drawingDining);
        drawingDiningYes = rootView.findViewById(R.id.drawingDiningYes);
        drawingDiningNo = rootView.findViewById(R.id.drawingDiningNo);

        isItDuplex = rootView.findViewById(R.id.isItDuplex);
        isItDuplexYes = rootView.findViewById(R.id.isItDuplexYes);
        isItDuplexNo = rootView.findViewById(R.id.isItDuplexNo);

        twentyFourWaterCB = rootView.findViewById(R.id.twentyFourWaterCB);
        gasSupplyCB = rootView.findViewById(R.id.gasSupplyCB);
        wellFurnishedCB = rootView.findViewById(R.id.wellFurnishedCB);
        securityGuardCB = rootView.findViewById(R.id.securityGuardCB);
        liftCB = rootView.findViewById(R.id.liftCB);
        generatorCB = rootView.findViewById(R.id.generatorCB);
        parkingGarageCB = rootView.findViewById(R.id.parkingGarageCB);
        kitchenCabinetCB = rootView.findViewById(R.id.kitchenCabinetCB);
    }

    private void calculateRentSpace() {
        long calculatedRent = (familyRoom[0] * singleBedRoomRent) + (familyRoom[1] * bathroomRent) + (familyRoom[2] * balconyRent);
        long calculatedSpace = (familyRoom[0] * singleBedRoomSpace) + (familyRoom[1] * bathroomSpace) + (familyRoom[2] * balconySpace);

        if (drawingDining.getCheckedRadioButtonId() == R.id.drawingDiningYes) {
            calculatedRent += drawingDiningRent;
            calculatedSpace += drawingDiningSpace;
        }

        totalSpace.setText(String.valueOf(calculatedSpace));
        activity.updateCalculatedRent(calculatedRent);
    }

    public String getTotalSpace() {
        return totalSpace.getText().toString();
    }

    public String getRoomSummary() {
        return familyRoom[0] + " bedroom, " + familyRoom[1] + " bathroom, " + familyRoom[2] + " balcony.";
    }

    public String getRoomOthersFacility() {
        StringBuilder stringBuilder = new StringBuilder();
        if (twentyFourWaterCB.isChecked()) {
            stringBuilder.append(getString(R.string.twenty_four_water_facility));
            stringBuilder.append("\n");
        }
        if (gasSupplyCB.isChecked()) {
            stringBuilder.append(getString(R.string.supply_gas_facility));
            stringBuilder.append("\n");
        }
        if (securityGuardCB.isChecked()) {
            stringBuilder.append(getString(R.string.always_security_guard));
            stringBuilder.append("\n");
        }

        if (parkingGarageCB.isChecked()) {
            stringBuilder.append(getString(R.string.parking_garage_facility));
            stringBuilder.append("\n");
        }
        if (liftCB.isChecked()) {
            stringBuilder.append(getString(R.string.lift_facility));
            stringBuilder.append("\n");
        }
        if (generatorCB.isChecked()) {
            stringBuilder.append(getString(R.string.generator_facility));
            stringBuilder.append("\n");
        }
        if (wellFurnishedCB.isChecked()) {
            stringBuilder.append(getString(R.string.fully_furnished));
            stringBuilder.append("\n");
        }
        if (kitchenCabinetCB.isChecked()) {
            stringBuilder.append(getString(R.string.have_a_kitchen_cabinet));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public FamilyInfo getFamilyInfo() {
        FamilyInfo familyInfo = new FamilyInfo();
        familyInfo.bedRoom = familyRoom[0];
        familyInfo.bathroom = familyRoom[1];
        familyInfo.balcony = familyRoom[2];
        familyInfo.hasDrawingDining = drawingDining.getCheckedRadioButtonId() == R.id.drawingDiningYes;
        familyInfo.isItDuplex = isItDuplex.getCheckedRadioButtonId() == R.id.isItDuplexYes;

        familyInfo.wellFurnished = wellFurnishedCB.isChecked();
        familyInfo.gasSupply = gasSupplyCB.isChecked();
        familyInfo.twentyFourWater = twentyFourWaterCB.isChecked();
        familyInfo.securityGuard = securityGuardCB.isChecked();
        familyInfo.lift = liftCB.isChecked();
        familyInfo.generator = generatorCB.isChecked();
        familyInfo.parkingGarage = parkingGarageCB.isChecked();
        familyInfo.kitchenCabinet = kitchenCabinetCB.isChecked();

        return familyInfo;
    }

    private final long singleBedRoomRent = 6000;//rent BDT
    private final long bathroomRent = 1500;//rent BDT
    private final long balconyRent = 1000;//rent BDT
    private final long drawingDiningRent = 8000;//rent BDT

    private final long singleBedRoomSpace = 210;//space sqrft
    private final long bathroomSpace = 60;//space sqrft
    private final long balconySpace = 50;//space sqrft
    private final long drawingDiningSpace = 280;//space sqrft

    public void updateData() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        FamilyInfo familyInfo = (FamilyInfo) bundle.getSerializable(DBConstants.familyInfo);
        if (familyInfo == null)
            return;

        twentyFourWaterCB.setChecked(familyInfo.twentyFourWater);
        gasSupplyCB.setChecked(familyInfo.gasSupply);
        wellFurnishedCB.setChecked(familyInfo.wellFurnished);
        securityGuardCB.setChecked(familyInfo.securityGuard);
        liftCB.setChecked(familyInfo.lift);
        generatorCB.setChecked(familyInfo.generator);
        parkingGarageCB.setChecked(familyInfo.parkingGarage);
        kitchenCabinetCB.setChecked(familyInfo.kitchenCabinet);

        if (bundle.getLong(DBConstants.flatSpace) > 0)
            totalSpace.setText(String.valueOf(bundle.getLong(DBConstants.flatSpace)));

        if (familyInfo.hasDrawingDining) drawingDiningYes.setChecked(true);
        else drawingDiningNo.setChecked(true);

        if (familyInfo.isItDuplex) {
            isItDuplexLay.setVisibility(View.VISIBLE);
            isItDuplexYes.setChecked(true);
        } else {
            isItDuplexLay.setVisibility(View.GONE);
        }

        for (int i = 0; i < roomNumberLay.getChildCount(); i++) {
            if (roomNumberLay.getChildAt(i).getTag() instanceof String) {
                String title = roomNumberLay.getChildAt(i).getTag().toString();
                String subTitle = "";
                if (roomNumberLay.getChildAt(i).getTag().toString().equalsIgnoreCase(roomTypes[0])) {
                    subTitle = familyInfo.bedRoom + " " + title + (familyInfo.bedRoom > 1 ? "'s" : "");
                } else if (roomNumberLay.getChildAt(i).getTag().toString().equalsIgnoreCase(roomTypes[1])) {
                    subTitle = familyInfo.bathroom + " " + title + (familyInfo.bathroom > 1 ? "'s" : "");
                } else if (roomNumberLay.getChildAt(i).getTag().toString().equalsIgnoreCase(roomTypes[2])) {
                    subTitle = (familyInfo.balcony == 0 ? "No" : familyInfo.balcony) + " " + title + (familyInfo.balcony > 1 ? "'s" : "");
                }

                AppConstants.updatePickerView(roomNumberLay.getChildAt(i), title, subTitle);
            }
        }
    }
}
