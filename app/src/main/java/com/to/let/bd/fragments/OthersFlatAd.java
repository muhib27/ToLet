package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.to.let.bd.R;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.OthersInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;

import java.util.ArrayList;
import java.util.Arrays;

public class OthersFlatAd extends BaseFragment {
    public static final String TAG = OthersFlatAd.class.getSimpleName();

    public static OthersFlatAd newInstance() {
        return new OthersFlatAd();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rentType.clear();
        rentType.addAll(Arrays.asList(getResources().getStringArray(R.array.others_rent_type_array)));
    }

    private View rootView;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        activity = (NewAdActivity2) getActivity();
        rootView = inflater.inflate(R.layout.fragment_others, container, false);

        AdView adView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        updateData();
    }

    private NewAdActivity2 activity;

    private LinearLayout singleRoomNumberLay, liftGeneratorLay, securityParkingLay, decoratedFurnishedLay;
    private EditText totalSpace;
    private CheckBox liftCB, generatorCB, securityGuardCB,
            parkingGarageCB, fullyDecoratedCB, wellFurnishedCB;

    private void init() {
        singleRoomNumberLay = rootView.findViewById(R.id.singleRoomNumberLay);
        singleRoomNumberLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu();
            }
        });

        updatePickerView(rentType.get(0));
        totalSpace = rootView.findViewById(R.id.totalSpace);

        liftGeneratorLay = rootView.findViewById(R.id.liftGeneratorLay);
        liftCB = rootView.findViewById(R.id.liftCB);
        generatorCB = rootView.findViewById(R.id.generatorCB);
        securityParkingLay = rootView.findViewById(R.id.securityParkingLay);
        securityGuardCB = rootView.findViewById(R.id.securityGuardCB);
        parkingGarageCB = rootView.findViewById(R.id.parkingGarageCB);
        decoratedFurnishedLay = rootView.findViewById(R.id.decoratedFurnishedLay);
        fullyDecoratedCB = rootView.findViewById(R.id.fullyDecoratedCB);
        wellFurnishedCB = rootView.findViewById(R.id.wellFurnishedCB);
    }

    public String getTotalSpace() {
        return totalSpace.getText().toString();
    }

    private ArrayList<String> rentType = new ArrayList<>();

    private void showPopupMenu() {
        PopupMenu popup = new PopupMenu(activity, singleRoomNumberLay);

        for (int i = 0; i < rentType.size(); i++) {
            popup.getMenu().add(0, i, Menu.NONE, rentType.get(i));
        }

        //popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                updatePickerView((String) item.getTitle());
                selectedPosition = item.getItemId();
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    private int selectedPosition = 0;

    private void updatePickerView(String subtitle) {
        String title = getString(R.string.select_type);
        AppConstants.updatePickerView(singleRoomNumberLay, title, subtitle);

        if (subtitle.equalsIgnoreCase(rentType.get(rentType.size() - 1))) {
            activity.focusDescription();
        }
    }

    public String getRoomSummary() {
        if (getTotalSpace() == null || getTotalSpace().trim().isEmpty()) {
            totalSpace.setError(getString(R.string.error_field_required));
            totalSpace.requestFocus();
            return null;
        }
        return rentType.get(selectedPosition) + " with " + getTotalSpace() + " sqrft";
    }

    public String getRoomOthersFacility() {
        StringBuilder stringBuilder = new StringBuilder();

        if (liftCB.isChecked()) {
            stringBuilder.append(getString(R.string.lift_facility));
            stringBuilder.append("\n");
        }
        if (generatorCB.isChecked()) {
            stringBuilder.append(getString(R.string.generator_facility));
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
        if (fullyDecoratedCB.isChecked()) {
            stringBuilder.append(getString(R.string.interior_fully_decorated));
            stringBuilder.append("\n");
        }
        if (wellFurnishedCB.isChecked()) {
            stringBuilder.append(getString(R.string.fully_furnished));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public OthersInfo getOthersInfo() {
        OthersInfo subletInfo = new OthersInfo();
        subletInfo.rentType = rentType.get(selectedPosition);

        subletInfo.lift = liftCB.isChecked();
        subletInfo.generator = generatorCB.isChecked();
        subletInfo.securityGuard = securityGuardCB.isChecked();
        subletInfo.parkingGarage = parkingGarageCB.isChecked();
        subletInfo.fullyDecorated = fullyDecoratedCB.isChecked();
        subletInfo.wellFurnished = wellFurnishedCB.isChecked();
        return subletInfo;
    }

    public void updateData() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        OthersInfo othersInfo = (OthersInfo) bundle.getSerializable(DBConstants.othersInfo);
        if (othersInfo == null)
            return;

        updatePickerView(othersInfo.rentType);

        totalSpace.setText(String.valueOf(bundle.getLong(DBConstants.flatSpace)));

        liftCB.setChecked(othersInfo.lift);
        generatorCB.setChecked(othersInfo.generator);
        securityGuardCB.setChecked(othersInfo.securityGuard);
        parkingGarageCB.setChecked(othersInfo.parkingGarage);
        fullyDecoratedCB.setChecked(othersInfo.fullyDecorated);
        wellFurnishedCB.setChecked(othersInfo.wellFurnished);
    }
}
