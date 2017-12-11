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
import android.widget.RadioGroup;

import com.to.let.bd.R;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.FamilyInfo;
import com.to.let.bd.utils.AppConstants;

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
    private CheckBox twentyFourWaterCB, gasSupplyCB, wellFurnishedCB,
            securityGuardCB, liftCB, generatorCB, parkingGarageCB, kitchenCabinetCB;

    private void init() {
        roomNumberLay = rootView.findViewById(R.id.roomNumberLay);
        isItDuplexLay = rootView.findViewById(R.id.isItDuplexLay);
        totalSpace = rootView.findViewById(R.id.totalSpace);
        drawingDining = rootView.findViewById(R.id.drawingDining);
        isItDuplex = rootView.findViewById(R.id.isItDuplex);

        twentyFourWaterCB = rootView.findViewById(R.id.twentyFourWaterCB);
        gasSupplyCB = rootView.findViewById(R.id.gasSupplyCB);
        wellFurnishedCB = rootView.findViewById(R.id.wellFurnishedCB);
        securityGuardCB = rootView.findViewById(R.id.securityGuardCB);
        liftCB = rootView.findViewById(R.id.liftCB);
        generatorCB = rootView.findViewById(R.id.generatorCB);
        parkingGarageCB = rootView.findViewById(R.id.parkingGarageCB);
        kitchenCabinetCB = rootView.findViewById(R.id.kitchenCabinetCB);
    }

    //    private final int[] roomArray = {0, 1, 2, 3, 4, 5};
//
//    public void addParticularView() {
//        LayoutInflater inflater = LayoutInflater.from(activity);
//        for (int i = 0; i < roomTypes.length; i++) {
//            final View inflatedView = inflater.inflate(R.layout.single_room_number_lay, roomNumberLay, false);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.weight = 1;
//            inflatedView.setLayoutParams(layoutParams);
//            final int pos = i;
//            inflatedView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    showRoomNumberPopupMenu(inflatedView, roomTypes[pos]);
//                }
//            });
//            int defaultSelection;
//            if (i == 2) {
//                defaultSelection = 1;
//                activity.updatePickerView(inflatedView, roomTypes[i], (roomArray[1] + " " + roomTypes[i]));
//            } else {
//                defaultSelection = 2;
//                activity.updatePickerView(inflatedView, roomTypes[i], (roomArray[defaultSelection] + " " + roomTypes[i] + "'s"));
//            }
//
//            familyRoom[i] = defaultSelection;
//            roomNumberLay.addView(inflatedView);
//        }
//    }
//
//    private void showRoomNumberPopupMenu(final View view, final String roomType) {
//        PopupMenu popup = new PopupMenu(activity, view);
//
//        for (int room : roomArray) {
//            String s;
//            if (room <= 1) {
//                if (room == 0 && roomType.equals(roomTypes[2])) {
//                    s = "No " + roomType;
//                } else {
//                    if (room == 0) {
//                        continue;
//                    }
//                    s = room + " " + roomType;
//                }
//            } else {
//                s = room + " " + roomType + "'s";
//            }
//            popup.getMenu().add(s);
//        }
//
//        //popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());
//        //registering popup with OnMenuItemClickListener
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                String subtitle = String.valueOf(item.getTitle());
//                String title = (subtitle.split(" "))[1].replace("'s", "");
//
//                int roomNumber;
//                if ((subtitle.split(" "))[0].equalsIgnoreCase("no")) {
//                    roomNumber = 0;
//                } else {
//                    roomNumber = Integer.parseInt((subtitle.split(" "))[0]);
//                }
//
//                if (title.equalsIgnoreCase(roomTypes[0])) {
//                    familyRoom[0] = roomNumber;
//                } else if (title.equalsIgnoreCase(roomTypes[1])) {
//                    familyRoom[1] = roomNumber;
//                } else if (title.equalsIgnoreCase(roomTypes[2])) {
//                    familyRoom[2] = roomNumber;
//                }
//                activity.updatePickerView(view, title, subtitle);
//                calculateRentSpace();
//                return true;
//            }
//        });
//        popup.show(); //showing popup menu
//    }
//
//    //
////    private int[] date = new int[3];//0=dayOfMonth, 1=monthOfYear, 2=year

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

    public String getRoomDetails() {
        return familyRoom[0] + " bedroom, " + familyRoom[1] + " bathroom, " + familyRoom[2] + " balcony.";
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
}
