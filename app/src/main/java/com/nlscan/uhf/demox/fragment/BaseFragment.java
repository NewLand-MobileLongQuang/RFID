package com.nlscan.uhf.demox.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.util.Constant;
import com.nlscan.uhf.lib.UHFManager;
import com.nlscan.uhf.lib.UHFReader;
import com.nlscan.uhf.lib.UHFSDK;
import com.nlscan.uhf.lib.adapters.silion.UHFSilionParams;

public class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();
    protected View mLayoutView;
    protected UHFManager mUHFMgr;
    protected String mModelName;
    protected Context mContext;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUHFMgr = UHFManager.getInstance(mContext);
        Log.i(TAG, "onCreate: " + String.format("mMName:%s,mUhfMgr:%s", mModelName, mUHFMgr));
        mModelName = mUHFMgr.getUHFDeviceModel();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateHeader();
    }

    protected void setParamWithToast(String k, String v, String sValue) {
        UHFReader.READER_STATE er = mUHFMgr.setParam(k, v, sValue);
        Log.i(TAG, "setParamWithToast: " + String.format("err:%s", er));

        if (er == UHFReader.READER_STATE.OK_ERR) {
            Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onUhfPowerOning() {
    }

    public void onUhfPowerOn() {
    }

    public void onUhfPowerOff() {
    }

    public void onUhfStartInventory() {
    }

    public void onUhfStopInventory() {
    }

    private void updateHeader() {
        if (CommonCmdFragment.class.getSimpleName().equals(this.getClass().getSimpleName())) {
            TextView header = ((TextView) getActivity().findViewById(R.id.header_center_name_text_view));
            TextView header_model_name = (TextView) getActivity().findViewById(R.id.header_model_name);
            getActivity().findViewById(R.id.im_actionbar_settings).setVisibility(View.INVISIBLE);

            header.setText(getResources().getString(R.string.goto_developer_options));
            header_model_name.setText("[ " + Constant.reName(mUHFMgr.getUHFDeviceModel()) +"-"+ UHFSDK.VERSION.VERSION_NAME+ " ]");
        }
        if (InventoryFragment.class.getSimpleName().equals(this.getClass().getSimpleName())) {

        }
    }


}
