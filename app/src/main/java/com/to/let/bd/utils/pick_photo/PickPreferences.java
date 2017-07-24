package com.to.let.bd.utils.pick_photo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.to.let.bd.model.pick_photo.DirImage;
import com.to.let.bd.model.pick_photo.GroupImage;
import com.to.let.bd.model.pick_photo.PickData;

public class PickPreferences {

    private static PickPreferences mInstance = null;
    private final SharedPreferences mSharedPreferences;
    private Context context;

    private static final String IMAGE_LIST = "image_list";
    private static final String DIR_NAMES = "dir_names";
    private static final String PICK_DATA = "pick_data";
    private GroupImage listImage;
    private DirImage dirImage;
    private PickData pickData;

    public static PickPreferences getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PickPreferences.class) {
                if (mInstance == null) {
                    mInstance = new PickPreferences(context);
                }
            }
        }
        return mInstance;
    }

    private PickPreferences(Context context) {
        this.context = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean saveImageList(GroupImage images) {
        listImage = images;
        Editor editor = mSharedPreferences.edit();
        editor.putString(IMAGE_LIST, PickGson.toJson(images));
        return editor.commit();
    }

    public GroupImage getListImage() {
        if (listImage == null) {
            String ss = mSharedPreferences.getString(IMAGE_LIST, "");
            if (TextUtils.isEmpty(ss)) {
                return null;
            } else {
                listImage = PickGson.fromJson(GroupImage.class, ss);
            }
        }
        return listImage;
    }

    public boolean saveDirNames(DirImage images) {
        dirImage = images;
        Editor editor = mSharedPreferences.edit();
        editor.putString(DIR_NAMES, PickGson.toJson(images));
        return editor.commit();
    }

    public DirImage getDirImage() {
        if (dirImage == null) {
            String ss = mSharedPreferences.getString(DIR_NAMES, "");
            if (TextUtils.isEmpty(ss)) {
                return null;
            } else {
                dirImage = PickGson.fromJson(DirImage.class, ss);
            }
        }
        return dirImage;
    }

    public boolean savePickData(PickData data) {
        pickData = data;
        Editor editor = mSharedPreferences.edit();
        editor.putString(PICK_DATA, PickGson.toJson(data));
        return editor.commit();
    }

    public PickData getPickData() {
        if (pickData == null) {
            String ss = mSharedPreferences.getString(PICK_DATA, "");
            if (TextUtils.isEmpty(ss)) {
                return null;
            } else {
                pickData = PickGson.fromJson(PickData.class, ss);
            }
        }
        return pickData;
    }
}
