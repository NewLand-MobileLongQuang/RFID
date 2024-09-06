package com.nlscan.uhf.demox.fragment;

import static com.nlscan.uhf.lib.HexUtil.hexStr2int;

import static java.lang.String.*;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.activity.MainActivity;
import com.nlscan.uhf.demox.util.ResourceValueParser;
import com.nlscan.uhf.demox.view.IndustryTextSpinnerView;
import com.nlscan.uhf.lib.UHFReader;
import com.nlscan.uhf.lib.adapters.newland.NLSerialPortUHFAdapter;
import com.nlscan.uhf.lib.adapters.silion.UHFSilionParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class IndustryTestFragment extends BaseFragment implements View.OnClickListener {

    HashMap<String, Byte> tariMap, blfMap, codingMap, trextMap;
    IndustryTextSpinnerView tariView, blfView, codingView, trextView;
    Button getBtn, setBtn;
    //固件升级
    private static final int PICKFILE_RESULT_CODE = 110;
    private TextView tvFw, tv_module_info, tv_update_succ, tv_update_failed;
    private EditText edFw;
    private Button btnFw, btnStart;
    private String fwPath = "";


    private String TAG = InventoryFragment.class.getSimpleName();

    private BroadcastReceiver mHomeKeyEventReceiver = null;
    private BroadcastReceiver mMenuKeyEventReceiver = null;
    private BroadcastReceiver mBackKeyEventReceiver = null;
    private ProgressDialog mLoadingPD = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mLayoutView = inflater.inflate(R.layout.layout_industry_test_fragment, null);
        initView(mLayoutView);
        initViewFwUpdate(mLayoutView);
        initEventFwUpdate();

        initFwData();
        return mLayoutView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProtocolCfgSettingsSpinner();
    }

    private void initData() {
        tariMap = ResourceValueParser.parseGen2ProtocolCfgSetting(getActivity(), R.array.urm_tari);
        blfMap = ResourceValueParser.parseGen2ProtocolCfgSetting(getActivity(), R.array.urm_blf);
        codingMap = ResourceValueParser.parseGen2ProtocolCfgSetting(getActivity(), R.array.urm_coding);
        trextMap = ResourceValueParser.parseGen2ProtocolCfgSetting(getActivity(), R.array.urm_trext);
        logHashMap(tariMap);
        logHashMap(blfMap);
        logHashMap(codingMap);
        logHashMap(trextMap);
    }

    private void initView(View v) {
        tariView = ((IndustryTextSpinnerView) v.findViewById(R.id.tari));
        blfView = ((IndustryTextSpinnerView) v.findViewById(R.id.blf));
        codingView = ((IndustryTextSpinnerView) v.findViewById(R.id.coding));
        trextView = ((IndustryTextSpinnerView) v.findViewById(R.id.trext));
        setBtn = ((Button) v.findViewById(R.id.btn_set));
        setBtn.setOnClickListener(this);
        getBtn = ((Button) v.findViewById(R.id.btn_get));
        getBtn.setOnClickListener(this);
        //
        tariView.setText("tari");
        blfView.setText("blf");
        codingView.setText("coding");
        trextView.setText("trext");
        //
        tariView.setMap(tariMap);
        blfView.setMap(blfMap);
        codingView.setMap(codingMap);
        trextView.setMap(trextMap);

//        updateProtocolCfgSettingsSpinner();//do on onResume()
    }


    private void logHashMap(HashMap<String, Byte> map) {
        for (String k : map.keySet()) {
            Log.i(TAG, "logHashMap: " + format("k:%s,v:%s", k, map.get(k)));
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set:
                int tari = tariView.getSelectedValue();
                int blf = blfView.getSelectedValue();
                int coding = codingView.getSelectedValue();
                int trext = trextView.getSelectedValue();
                setGen2(tari, blf, coding, trext);
                break;
            case R.id.btn_get:
                updateProtocolCfgSettingsSpinner();
                break;
        }
    }

    private void setGen2(int t, int b, int c, int tr) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("tari", t);
            jo.put("blf", b);
            jo.put("coding", c);
            jo.put("trext", tr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setParamWithToast(UHFSilionParams.URM_ProtocolCfg_Gen2.KEY, UHFSilionParams.URM_ProtocolCfg_Gen2.PARAM_URM_ProtocolCfg_Gen2, jo.toString());

    }

    private void updateProtocolCfgSettingsSpinner() {
        String cfg = mUHFMgr.getParam(UHFSilionParams.URM_ProtocolCfg_Gen2.KEY, UHFSilionParams.URM_ProtocolCfg_Gen2.PARAM_URM_ProtocolCfg_Gen2, "");
        Log.i(TAG, "updateProtocolCfgSettings: " + cfg);
        try {
            JSONObject jo = new JSONObject(cfg);
            int tari = hexStr2int(jo.optString("tari"));
            int blf = hexStr2int(jo.optString("blf"));
            int coding = hexStr2int(jo.optString("coding"));
            int trext = hexStr2int(jo.optString("trext"));

            tariView.updateSpinnerSelected(tari);
            blfView.updateSpinnerSelected(blf);
            codingView.updateSpinnerSelected(coding);
            trextView.updateSpinnerSelected(trext);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //固件升级 分割线-------------------------------------------------------------------
    private void initViewFwUpdate(View v) {
        tvFw = ((TextView) v.findViewById(R.id.tv_fw));
        edFw = ((EditText) v.findViewById(R.id.ed_fw));
        btnFw = ((Button) v.findViewById(R.id.btn_fw_choose_file));
        btnStart = ((Button) v.findViewById(R.id.btn_fw_start_update));
        tv_module_info = ((TextView) v.findViewById(R.id.tv_module_info));
        tv_update_succ = ((TextView) v.findViewById(R.id.tv_fw_rst_succ));
        tv_update_failed = ((TextView) v.findViewById(R.id.tv_fw_rst_failed));
    }

    private void initFwData() {
        String param = mUHFMgr.getParam(UHFSilionParams.MODULE_INFO.KEY, UHFSilionParams.MODULE_INFO.PARAM_MODULE_INFO, "");
        Log.i(TAG, "initFwData: " + param);
        tv_module_info.setText(param);
    }

    private void initEventFwUpdate() {
        btnFw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fwPath = edFw.getText().toString();
                chooseFile();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUpdate();
            }
        });
    }

    private void chooseFile() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }


    private String formatFilePath(Intent data) {
        Uri uri = data.getData();
        String filePath = "";
        if (uri.getScheme().equals("file")) {
            // 如果是 file:// URI，直接获取路径
            filePath = uri.getPath();
        } else {
            // 如果是 content:// URI，使用 ContentResolver 获取路径
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            Log.i(TAG, "formatFilePath: "+ format("index:%d", index));
            if (index>-1) {
                filePath = cursor.getString(index);
            }
            cursor.close();
        }
        return filePath;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (data != null && data.getData() != null && data.getData().getPath() != null) {
//            updatePathUI(data.getData().getPath());
//        }
        if (data != null) {
            updatePathUI(formatFilePath(data));
        }
        //rqc:110,rstc:-1,data:/storage/emulated/0/Z11.bin
//        Log.i(TAG, "onActivityResult: "+String.format("rqc:%d,rstc:%d,data:%s",requestCode,resultCode,data!=null?data.getData().getPath().toString():"null"));
    }

    private void updatePathUI(String path) {
        fwPath = path;
        edFw.setText(path);
    }

    class FwHandle extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                showLoadingDialog();
            }
            if (msg.what == 1) {
                dismissLoadingDialog();
                tv_update_succ.setVisibility(View.VISIBLE);
                tv_update_failed.setVisibility(View.GONE);
            }
            if (msg.what == 2) {
                //完成升级
                dismissLoadingDialog();
                tv_update_succ.setVisibility(View.GONE);
                tv_update_failed.setVisibility(View.VISIBLE);
            }

        }
    }

    FwHandle fwHandle = new FwHandle();

    private void doUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                fwHandle.sendEmptyMessage(0);

                UHFReader.READER_STATE reader_state = mUHFMgr.setParam(UHFSilionParams.FIRMWARE_UPDATE.KEY, UHFSilionParams.FIRMWARE_UPDATE.PARAM_FIRMWARE_UPDATE, fwPath);

                fwHandle.sendEmptyMessage(reader_state.value() == UHFReader.READER_STATE.OK_ERR.value() ? 1 : 2);

                Log.i(TAG, "doUpdate: " + format("state:%d,path:%s", reader_state.value(), fwPath));
            }
        }).start();

    }


    //dialog
    private void showLoadingDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            mLoadingPD = new ProgressDialog(getActivity());
            mLoadingPD.setMessage(getResources().getString(R.string.updating));
            mLoadingPD.setCancelable(false);
            mLoadingPD.show();
        }
    }

    //dialog
    private void dismissLoadingDialog() {
        if (mLoadingPD != null) {
            mLoadingPD.dismiss();
        }
    }


}

