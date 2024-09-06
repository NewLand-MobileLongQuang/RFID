package com.nlscan.uhf.demox;


import androidx.multidex.MultiDexApplication;

import com.nlscan.uhf.demox.util.ScreenUtil;
import com.nlscan.uhf.demox.util.constant.SharePreferenceConfig;
import com.nlscan.uhf.demox.util.storage.LocalStorageManager;
import com.nlscan.uhf.lib.UHFManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppApplication extends MultiDexApplication {
    public static AppApplication instance;
    public boolean mSound = true;
    public boolean mVibrate = false;

    private List<String> mTagDatas;

    public static AppApplication getInstance() {
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        UHFManager.getInstance(this).powerOff();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mTagDatas = new ArrayList<>();
        ScreenUtil.resetDensity(getApplicationContext());
        LocalStorageManager.setBoolean(SharePreferenceConfig.Key.SHOULD_REFRESH_LIST, false);

        //Uncaught exception handler
        //CrashHandler crashHandler = CrashHandler.getInstance();
        //crashHandler.init(this);
    }

    public List<String> getTagDatas() {
        if (mTagDatas == null)
            mTagDatas = new ArrayList<>();
        return mTagDatas;
    }

    public void addTagData(String data) {
        if (!mTagDatas.contains(data))
            mTagDatas.add(data);
    }

    public void clearTagData() {
        mTagDatas.clear();
    }
}
