package com.nlscan.uhf.demox.fragment;

import static com.nlscan.uhf.lib.UHFConstants.EXTRA_TAG_INFO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.util.Constant;
import com.nlscan.uhf.demox.view.IndustryTextSpinnerView;
import com.nlscan.uhf.lib.UHFConstants;
import com.nlscan.uhf.lib.adapters.newland.analysis.SWRParcelable;
import com.nlscan.uhf.lib.adapters.newland.analysis.TagParcelable;
import com.nlscan.uhf.lib.adapters.silion.UHFSilionParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 通用指令问答界面
 */
public class CommonCmdFragment extends BaseFragment implements View.OnClickListener, TimeOutListener {
    private static final String TAG = CommonCmdFragment.class.getSimpleName();
    private String cmdResult, swrAppend;
    private Button send, clear, sendInv, sendSWR, switchBaud, sendSWRInbox;
    private EditText cmdSendEt, timeout;//, cmdResultEt
    private ToggleButton isAsc;
    private TextView tvSwr,moduleInfo;
    private List<SWRParcelable> swrList = new ArrayList<>(60);
    private int count = 0;

    //swr
    private IndustryTextSpinnerView antSpinner, regionSpinner, powerSpinner;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mLayoutView = inflater.inflate(R.layout.layout_common_cmd_fragment, null);
        return mLayoutView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        initEvents();
        getModuleInfo();
    }

    private void initViews() {
        send = (Button) mLayoutView.findViewById(R.id.cmd_send);
        clear = (Button) mLayoutView.findViewById(R.id.cmd_clear);
        cmdSendEt = (EditText) mLayoutView.findViewById(R.id.cmd_input);
//        cmdResultEt = (EditText) mLayoutView.findViewById(R.id.cmd_output);
        timeout = (EditText) mLayoutView.findViewById(R.id.timeout_inv);
        sendInv = (Button) mLayoutView.findViewById(R.id.cmd_send_inv);
        isAsc = (ToggleButton) mLayoutView.findViewById(R.id.is_ascii);
        sendSWR = (Button) mLayoutView.findViewById(R.id.btn_swr);
        tvSwr = (TextView) mLayoutView.findViewById(R.id.txt_swr);
        switchBaud = (Button) mLayoutView.findViewById(R.id.switch_baud);
        sendSWRInbox = (Button) mLayoutView.findViewById(R.id.btn_swr_send);
        //swr
        antSpinner = (IndustryTextSpinnerView) mLayoutView.findViewById(R.id.swr_antenna);
        regionSpinner = (IndustryTextSpinnerView) mLayoutView.findViewById(R.id.swr_region);
        powerSpinner = (IndustryTextSpinnerView) mLayoutView.findViewById(R.id.swr_power);
        // module info
        moduleInfo = (TextView) mLayoutView.findViewById(R.id.tv_module_info);
    }

    private void initEvents() {
        send.setOnClickListener(this);
        clear.setOnClickListener(this);
        sendInv.setOnClickListener(this);
        isAsc.setOnClickListener(this);
        sendSWR.setOnClickListener(this);
        switchBaud.setOnClickListener(this);
        sendSWRInbox.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cmd_send:
                sendCmd(UHFSilionParams.COMMON_CMD.KEY, genCmd(cmdSendEt.getText().toString()), "failed");
                break;
            case R.id.cmd_clear:
                cmdSendEt.setText("");
//                cmdResultEt.setText("");
                tvSwr.setText("");
                swrAppend = "";
//                timeout.setText("");
                break;
            case R.id.cmd_send_inv:
                swrList.clear();
                tvSwr.setText("");
                swrAppend = "";
                count = 0;
                sendCmd(UHFSilionParams.COMMON_CMD_INV_Start.KEY, genCmd(cmdSendEt.getText().toString()), "");
                int time = Integer.parseInt((timeout == null || timeout.getText() == null || timeout.getText().equals("")) ? "1" : timeout.getText().toString());
                countDown(time, this);
                break;
            case R.id.is_ascii:
                Log.i(TAG, "onClick: " + String.format("isAsc:%s", isAsc.isChecked()));
                if (isAsc.isChecked()) {
                    isAsc.setText("USING ASCII(TAG-SUBTAG-DATA)");
                } else {
                    isAsc.setText("USING HEX(Full byte command)");
                }
                break;
            case R.id.btn_swr:
                sendSwr(1, 0, 30);
                break;

            case R.id.switch_baud:
                String s = switchBaud();
                tvSwr.setText(s);
                break;

            case R.id.btn_swr_send:
                sendSwr(antSpinner.getSelectedValue(), regionSpinner.getSelectedValue(), powerSpinner.getSelectedValue());
                break;
        }
    }

    private void getModuleInfo() {
        String moduleInfo = mUHFMgr.getParam(UHFSilionParams.MODULE_INFO.KEY,  "","");
        Log.i(TAG, "getModuleInfo: "+moduleInfo);
        this.moduleInfo.setText(moduleInfo);
    }

    private void sendSwr(int ant, int region, int power) {
        swrList.clear();
        tvSwr.setText("");
        swrAppend = "";
        count = 0;
        sendCmd(UHFSilionParams.COMMON_CMD_INV_Start.KEY, "7E0130303030" + "23" + stringToHex("URFSWR" + ant + "A" + region + "R" + power + "P") + "3B03", "failed");
        int time2 = Integer.parseInt((timeout == null || timeout.getText() == null || timeout.getText().equals("")) ? "1" : timeout.getText().toString());
        countDown(time2, this);
    }

    private String switchBaud() {
        return mUHFMgr.getParam(UHFSilionParams.URM_BAUD_SWITCH.KEY, "", "failed");
    }

    private void sendCmd(String type, String cmdSend, String defRst) {
        if (cmdSend != null && !cmdSend.equals("")) {
            Log.i(TAG, "onClick: " + String.format("cmdSend:%s", cmdSend));
            cmdResult = mUHFMgr.getParam(type, cmdSend, defRst);
            tvSwr.setText(hexToString(cmdResult) + System.lineSeparator() + cmdResult);
        }
    }

    private void countDown(int time, TimeOutListener listener) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int i = time;

            @Override
            public void run() {
                if (i == 0) {
                    timer.cancel();
                    listener.timeOut();
                } else {
                    i--;
                }
            }
        }, 1000, 1000);
    }


    @Override
    public void timeOut() {
        mUHFMgr.getParam(UHFSilionParams.COMMON_CMD_INV_STOP.KEY, "", "");
    }

    private String genCmd(String cmd) {
        final String store = "23";//40 or 23
        String rst = "";
        if (!isAsc.isChecked()) {
            rst = cmd;
        } else {
            rst = "7E0130303030" + store + stringToHex(cmd) + "3B03";
        }
        Log.i(TAG, "genCmd: " + String.format("cmd:%s", rst));
        return rst;
    }

    public String stringToHex(String ascString) {
        StringBuilder hexBuilder = new StringBuilder();
        for (int i = 0; i < ascString.length(); i++) {
            char ch = ascString.charAt(i);
            String hex = Integer.toHexString((int) ch);
            hexBuilder.append(hex);
        }
        return hexBuilder.toString();
    }

    public String hexToString(String hexString) {
        StringBuilder ascBuilder = new StringBuilder();
        for (int i = 0; i < hexString.length() - 1; i += 2) {
            String hex = hexString.substring(i, i + 2);
            int ch = Integer.parseInt(hex, 16);
            ascBuilder.append((char) ch);
        }
        return ascBuilder.toString();
    }


    private void registerResultReceiver() {
        try {
            IntentFilter iFilter = new IntentFilter(UHFConstants.ACTION_UHF_RESULT_SEND_SWR);
            mContext.registerReceiver(mSWR, iFilter);

        } catch (Exception e) {
        }

    }

    private void unRegisterResultReceiver() {
        try {
            mContext.unregisterReceiver(mSWR);
        } catch (Exception e) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        registerResultReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterResultReceiver();
    }

    private BroadcastReceiver mSWR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + String.format("action:%s", intent == null ? "null" : intent.getAction()));
            if (UHFConstants.ACTION_UHF_RESULT_SEND_SWR.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(EXTRA_TAG_INFO);
                SWRParcelable swr = (SWRParcelable) parcelableExtra;
                if (swr != null && swr.getvSwr() != null) {
                    swrList.add(swr);
                    swrAppend += (++count) + "、" + swr.toString() + System.lineSeparator() + System.lineSeparator();
                    tvSwr.setText(swrAppend);
                }
                String s = swr.toString();
                Log.i(TAG, "onReceive: " + String.format("swr:%s", s));
            }
        }
    };
}

interface TimeOutListener {
    void timeOut();
}
