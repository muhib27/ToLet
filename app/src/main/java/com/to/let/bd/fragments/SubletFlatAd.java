package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.to.let.bd.R;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.SubletInfo;
import com.to.let.bd.utils.DBConstants;

public class SubletFlatAd extends BaseFragment {
    public static final String TAG = SubletFlatAd.class.getSimpleName();

    public static SubletFlatAd newInstance() {
        return new SubletFlatAd();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View rootView;
    private NewAdActivity2 activity;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        activity = (NewAdActivity2) getActivity();
        rootView = inflater.inflate(R.layout.fragment_sublet, container, false);
        init();

        AdView adView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateData();
    }

    private RadioGroup subletType, bathroomType;
    private RadioButton tinyFamily, smallFamily, femaleSublet, othersSublet, attached, nonAttached, shared;
    private TextInputLayout subletTypeOthersTI;
    private TextView subletTypeOthers;
    private CheckBox twentyFourWaterCB, gasSupplyCB, kitchenShareCB,
            wellFurnishedCB, liftCB, generatorCB;

    private void init() {
        subletType = rootView.findViewById(R.id.subletType);
        tinyFamily = rootView.findViewById(R.id.tinyFamily);
        smallFamily = rootView.findViewById(R.id.smallFamily);
        femaleSublet = rootView.findViewById(R.id.femaleSublet);
        othersSublet = rootView.findViewById(R.id.othersSublet);
        subletType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (subletType.getCheckedRadioButtonId() == R.id.othersSublet) {
                    subletTypeOthersTI.setVisibility(View.VISIBLE);
                } else {
                    subletTypeOthersTI.setVisibility(View.GONE);
                }
            }
        });
        subletTypeOthersTI = rootView.findViewById(R.id.subletTypeOthersTI);
        subletTypeOthers = rootView.findViewById(R.id.subletTypeOthers);
        bathroomType = rootView.findViewById(R.id.bathroomType);
        attached = rootView.findViewById(R.id.attached);
        nonAttached = rootView.findViewById(R.id.nonAttached);
        shared = rootView.findViewById(R.id.shared);

        twentyFourWaterCB = rootView.findViewById(R.id.twentyFourWaterCB);
        gasSupplyCB = rootView.findViewById(R.id.gasSupplyCB);
        kitchenShareCB = rootView.findViewById(R.id.kitchenShareCB);
        wellFurnishedCB = rootView.findViewById(R.id.wellFurnishedCB);
        liftCB = rootView.findViewById(R.id.liftCB);
        generatorCB = rootView.findViewById(R.id.generatorCB);
    }

    public String getRoomSummary() {
        String subletType;
        if (this.subletType.getCheckedRadioButtonId() == R.id.smallFamily) {
            subletType = getString(R.string.small_family);
        } else if (this.subletType.getCheckedRadioButtonId() == R.id.femaleSublet) {
            subletType = getString(R.string.female_sublet);
        } else if (this.subletType.getCheckedRadioButtonId() == R.id.othersSublet) {
            if (subletTypeOthers.getText().toString().trim().isEmpty()) {
                subletTypeOthers.requestFocus();
                subletTypeOthers.setError(getString(R.string.error_field_required));
                return null;
            }
            subletType = subletTypeOthers.getText().toString();
        } else {
            subletType = getString(R.string.tiny_family);
        }

        String bathroomType;
        if (this.bathroomType.getCheckedRadioButtonId() == R.id.nonAttached) {
            bathroomType = getString(R.string.non_attached);
        } else if (this.bathroomType.getCheckedRadioButtonId() == R.id.shared) {
            bathroomType = getString(R.string.shared);
        } else {
            bathroomType = getString(R.string.attached);
        }

        return subletType + " with " + bathroomType.toLowerCase() + " bathroom";
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
        if (generatorCB.isChecked()) {
            stringBuilder.append(getString(R.string.generator_facility));
            stringBuilder.append("\n");
        }
        if (liftCB.isChecked()) {
            stringBuilder.append(getString(R.string.lift_facility));
            stringBuilder.append("\n");
        }
        if (wellFurnishedCB.isChecked()) {
            stringBuilder.append(getString(R.string.fully_furnished));
            stringBuilder.append("\n");
        }
        if (kitchenShareCB.isChecked()) {
            stringBuilder.append(getString(R.string.need_to_share_kitchen));
            stringBuilder.append("\n");
        } else {
            stringBuilder.append(getString(R.string.no_need_to_share_kitchen));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public SubletInfo getSubletInfo() {
        SubletInfo subletInfo = new SubletInfo();
        {
            int subletType = 0;
            if (this.subletType.getCheckedRadioButtonId() == R.id.smallFamily) {
                subletType = 1;
            } else if (this.subletType.getCheckedRadioButtonId() == R.id.femaleSublet) {
                subletType = 2;
            } else if (this.subletType.getCheckedRadioButtonId() == R.id.othersSublet) {
                subletType = 3;
            }
            subletInfo.subletType = subletType;
            subletInfo.subletTypeOthers = subletTypeOthers.getText().toString();
        }
        {
            int bathroomType = 0;
            if (this.bathroomType.getCheckedRadioButtonId() == R.id.nonAttached) {
                bathroomType = 1;
            } else if (this.bathroomType.getCheckedRadioButtonId() == R.id.shared) {
                bathroomType = 2;
            }
            subletInfo.bathroomType = bathroomType;
        }

        subletInfo.twentyFourWater = twentyFourWaterCB.isChecked();
        subletInfo.gasSupply = gasSupplyCB.isChecked();
        subletInfo.kitchenShare = kitchenShareCB.isChecked();
        subletInfo.wellFurnished = wellFurnishedCB.isChecked();
        subletInfo.lift = liftCB.isChecked();
        subletInfo.generator = generatorCB.isChecked();

        return subletInfo;
    }

    public void updateData() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        SubletInfo subletInfo = (SubletInfo) bundle.getSerializable(DBConstants.othersInfo);
        if (subletInfo == null)
            return;

        if (subletInfo.subletType == 1) smallFamily.setChecked(true);
        else if (subletInfo.subletType == 2) femaleSublet.setChecked(true);
        else if (subletInfo.subletType == 3) othersSublet.setChecked(true);
        else tinyFamily.setChecked(true);

        if (subletInfo.bathroomType == 1) nonAttached.setChecked(true);
        else if (subletInfo.bathroomType == 2) shared.setChecked(true);
        else attached.setChecked(true);

        twentyFourWaterCB.setChecked(subletInfo.twentyFourWater);
        gasSupplyCB.setChecked(subletInfo.gasSupply);
        kitchenShareCB.setChecked(subletInfo.kitchenShare);
        wellFurnishedCB.setChecked(subletInfo.wellFurnished);
        liftCB.setChecked(subletInfo.lift);
        generatorCB.setChecked(subletInfo.generator);
    }
}
