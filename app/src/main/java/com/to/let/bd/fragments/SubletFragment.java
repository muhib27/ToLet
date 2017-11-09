package com.to.let.bd.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.to.let.bd.R;
import com.to.let.bd.common.BaseFragment;
import com.to.let.bd.model.SubletInfo;

public class SubletFragment extends BaseFragment {
    public static final String TAG = SubletFragment.class.getSimpleName();

    public static SubletFragment newInstance() {
        return new SubletFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View rootView;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sublet, container, false);
        init();
        return rootView;
    }

    private RadioGroup subletType, bathroomType;
    private TextInputLayout subletTypeOthersTI;
    private TextView subletTypeOthers;
    private CheckBox twentyFourWaterCB, gasSupplyCB, kitchenShareCB,
            wellFurnishedCB, liftCB, generatorCB;

    private void init() {
        subletType = rootView.findViewById(R.id.subletType);
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

        twentyFourWaterCB = rootView.findViewById(R.id.twentyFourWaterCB);
        gasSupplyCB = rootView.findViewById(R.id.gasSupplyCB);
        kitchenShareCB = rootView.findViewById(R.id.kitchenShareCB);

        wellFurnishedCB = rootView.findViewById(R.id.wellFurnishedCB);
        liftCB = rootView.findViewById(R.id.liftCB);
        generatorCB = rootView.findViewById(R.id.fridgeCB);
    }

    public String getRoomDetails() {
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
            subletInfo.setSubletType(subletType);
            subletInfo.setSubletTypeOthers(subletTypeOthers.getText().toString());
        }
        {
            int bathroomType = 0;
            if (this.bathroomType.getCheckedRadioButtonId() == R.id.nonAttached) {
                bathroomType = 1;
            } else if (this.bathroomType.getCheckedRadioButtonId() == R.id.shared) {
                bathroomType = 2;
            }
            subletInfo.setBathroomType(bathroomType);
        }

        subletInfo.setTwentyFourWater(twentyFourWaterCB.isChecked());
        subletInfo.setGasSupply(gasSupplyCB.isChecked());
        subletInfo.setKitchenShare(kitchenShareCB.isChecked());
        subletInfo.setWellFurnished(wellFurnishedCB.isChecked());
        subletInfo.setLift(liftCB.isChecked());
        subletInfo.setGenerator(generatorCB.isChecked());

        return subletInfo;
    }
}
