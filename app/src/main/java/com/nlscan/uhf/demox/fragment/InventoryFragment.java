package com.nlscan.uhf.demox.fragment;

import static java.lang.String.format;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nlscan.uhf.demox.AppApplication;
import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.adapter.TableItemInfo;
import com.nlscan.uhf.demox.adapter.UhfDataListAdapter;
import com.nlscan.uhf.demox.util.Constant;
import com.nlscan.uhf.demox.util.constant.UHFSilionParams;
import com.nlscan.uhf.demox.util.view.TimerTextView;
import com.nlscan.uhf.lib.HexUtil;
import com.nlscan.uhf.lib.TagInfo;
import com.nlscan.uhf.lib.UHFConstants;
import com.nlscan.uhf.lib.UHFManager;
import com.nlscan.uhf.lib.UHFReader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryFragment extends BaseFragment {

    private final String TAG = InventoryFragment.class.getSimpleName();

    private Context mContext;
    private UHFManager mUHFMgr;
    private HorizontalScrollView mHorizontalScrollView;
    private ListView lv_data_list;
    private Button btn_start_inventory;
    private Button btn_stop_inventory;
    private Button btn_clean;
    private CheckBox cb_max_rssi;
    private CheckBox cb_mutip_tags;
    private CheckBox cb_tid;
    private CheckBox cb_rssi;
    private CheckBox cb_protocal;
    private CheckBox cb_frequence;
    //Total data count
    private TextView tv_total_count;
    //Total tag count
    private TextView tv_total_tags_count;
    //Inventory speed
    private TextView tv_speed;
    private EditText et_time, et_target_count;
    private TimerTextView tv_inv_span_time;
    //Battery Temperature
    private TextView tv_battery_temperature;
    private UhfDataListAdapter mAdapter;

    private List<TableItemInfo> mUhfDataList;//tag datas
    private Map<String, TableItemInfo> mTableItemMap = new HashMap<>();

    private int mListViewScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
    private Map<String, Integer> mTagCountMap;
    private int mMaxRssi = -1000;
    private long mStartReadingTime = 0;
    private int readTotalCount = 0;
    private int tagCount = 0;
    private int speed = 0;
    private boolean mActivityPause = false;
    private BroadcastReceiver mBatteryStateReceiver;
    //Sound and vibrate prompt
    private SoundPool soundPool;
    private Vibrator mVibrator;
    private boolean mPrompting = false;

    //Embed data enable
    private boolean mIsEmbedDataEnable = false;

    private final int MSG_UPDATE_VIEW = 0x01;
    private final int MSG_UPDATE_DATA_LIST_VIEW = 0x02;
    private final int MSG_STOP_INVENTORY = 0x03;
    private final int MSG_CLEAR_COMPLETE = 0x04;
    private final int MSG_TAGS_PUSH_TO_CACHE_AND_HANDLE = 0x05;
    private final int MSG_UPDATE_INVENTORY_PARAMS = 0x06;

    private boolean invStopping = false;

    private Handler mUIHandler;
    private Handler mTagsReceivedHandler;
    private HandlerThread mTagsReceivedHandlerThread;

    private HandlerThread mInvDataHandlerThread;
    private Handler mInvDataHandler;
    private final static int MSG_RUN_GET_TAGS_DATA = 0x01;
    private final static int MSG_START_CLEAR_DATA = 0x02;


    private boolean debugInvDataHandlerFirstTag = true;
    private int debugInvDataHandlerTagCount = 0;
    private long debugInvDataHandlerFirstTagTime = 0;

    private class InvDataHandler extends Handler {
        public InvDataHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "InvDataHandler handleMessage: " + msg.what);
            switch (msg.what) {//接收盘点数据
                case MSG_RUN_GET_TAGS_DATA:
                    Parcelable[] tagInfos = (Parcelable[]) msg.obj;
                    if (debugInvDataHandlerFirstTag) {
                        debugInvDataHandlerFirstTagTime = System.currentTimeMillis();
                        debugInvDataHandlerFirstTag = false;
                    }
                    long passT = System.currentTimeMillis() - debugInvDataHandlerFirstTagTime;

                    //Log.i(TAG, "handleMessage: "+String.format("TagCount=%d, firstTagTime=%d,passT=%d ",debugInvDataHandlerTagCount,debugInvDataHandlerFirstTagTime,passT));
                    runUpdateViewThread(tagInfos);
                    break;
                case MSG_START_CLEAR_DATA:
                    startClear();
                    break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mUHFMgr = UHFManager.getInstance(context);
        mUIHandler = new CustomHandler(Looper.getMainLooper());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTagCountMap = new ConcurrentHashMap<>();


        mInvDataHandlerThread = new HandlerThread("InvHandlerThread", Process.THREAD_PRIORITY_FOREGROUND);
        mInvDataHandlerThread.start();
        mInvDataHandler = new InvDataHandler(mInvDataHandlerThread.getLooper());

        mTagsReceivedHandlerThread = new HandlerThread("tags_receiver_handler", Process.THREAD_PRIORITY_FOREGROUND);
        mTagsReceivedHandlerThread.start();
        mTagsReceivedHandler = new CustomHandler(mTagsReceivedHandlerThread.getLooper());

        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool.load(mContext, R.raw.beep51, 1);
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mLayoutView = inflater.inflate(R.layout.layout_inv_main, null);
        return mLayoutView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUHFMgr.getUHFModuleInfo() != null && mUHFMgr.isPowerOn()) {
            mUIHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
        }
        registerResultReceiver();
        wakeupScreen();
        updateViewData();
        mActivityPause = false;
//        String sdkVersion = UHFSDK.VERSION.VERSION_NAME;
//        Log.i(TAG, "onResume: "+sdkVersion);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "--onPause()--");
        unRegisterResultReceiver();
        stopInventory();
        clearWakeup();
        mActivityPause = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onUhfPowerOning() {
        super.onUhfPowerOning();
    }

    @Override
    public void onUhfPowerOn() {
        super.onUhfPowerOn();
        mUIHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
    }

    @Override
    public void onUhfPowerOff() {
        super.onUhfPowerOff();
    }

    @Override
    public void onUhfStartInventory() {
        super.onUhfStartInventory();
        updateStateOnStartInventroy();
    }

    @Override
    public void onUhfStopInventory() {
        super.onUhfStopInventory();
        Log.d(TAG, "onUhfStopInventory()...");
        updateStateOnStoptInventroy();
    }

    private void wakeupScreen() {
        clearWakeup();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void clearWakeup() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initView() {
        mHorizontalScrollView = (HorizontalScrollView) mLayoutView.findViewById(R.id.horizontalScrollView1);
        lv_data_list = (ListView) mLayoutView.findViewById(R.id.lv_data_list);
        btn_start_inventory = (Button) mLayoutView.findViewById(R.id.btn_start_inventory);
        btn_stop_inventory = (Button) mLayoutView.findViewById(R.id.btn_stop_inventory);
        btn_clean = (Button) mLayoutView.findViewById(R.id.btn_clean);
        cb_max_rssi = (CheckBox) mLayoutView.findViewById(R.id.cb_max_rssi);
        cb_mutip_tags = (CheckBox) mLayoutView.findViewById(R.id.cb_mutip_tags);
        cb_tid = (CheckBox) mLayoutView.findViewById(R.id.cb_tid);
        cb_rssi = (CheckBox) mLayoutView.findViewById(R.id.cb_rssi);
        cb_frequence = (CheckBox) mLayoutView.findViewById(R.id.cb_frequence);
        cb_protocal = (CheckBox) mLayoutView.findViewById(R.id.cb_protocal);

        //Total data count
        tv_total_count = (TextView) mLayoutView.findViewById(R.id.tv_total_count);
        //Total tag count
        tv_total_tags_count = (TextView) mLayoutView.findViewById(R.id.tv_total_tags_count);
        et_target_count = (EditText) mLayoutView.findViewById(R.id.et_target_count);
        //Inventory speed
        tv_speed = (TextView) mLayoutView.findViewById(R.id.tv_speed);
        et_time = (EditText) mLayoutView.findViewById(R.id.et_time);
        tv_inv_span_time = (TimerTextView) mLayoutView.findViewById(R.id.tv_inv_span_time);
        tv_battery_temperature = (TextView) mLayoutView.findViewById(R.id.tv_battery_temperature);

        cb_mutip_tags.setChecked(true);
        cb_protocal.setVisibility(View.INVISIBLE);
        tv_total_count.setText(getString(R.string.uhf_total_count, ""));
        tv_total_tags_count.setText(getString(R.string.uhf_total_tag, ""));
        tv_speed.setText(getString(R.string.uhf_inv_speed, ""));
        tv_battery_temperature.setText(getString(R.string.battery_temperature, ""));

        mUhfDataList = new ArrayList<TableItemInfo>();
//        TableItemInfo headItemInfo = new TableItemInfo(mContext);
//        mUhfDataList.add(headItemInfo);
        mAdapter = new UhfDataListAdapter(mContext, mUhfDataList);
        lv_data_list.setAdapter(mAdapter);
        lv_data_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mListViewScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_start_inventory:
                        startInventory();
                        break;
                    case R.id.btn_stop_inventory:
                        stopInventory();
                        break;
                    case R.id.btn_clean:
                        mInvDataHandler.sendEmptyMessage(MSG_START_CLEAR_DATA);
                        break;
                }
            }
        };

        btn_start_inventory.setOnClickListener(onClickListener);
        btn_stop_inventory.setOnClickListener(onClickListener);
        btn_clean.setOnClickListener(onClickListener);

    }

    public void updateViewData() {
        if (mUHFMgr == null || !mUHFMgr.isPowerOn())
            return;

        //inventory button
        btn_start_inventory.setEnabled(!mUHFMgr.isInInventory());

        Map<String, Object> settings = mUHFMgr.getAllParams();

        //--MAX RSSI
        if (settings != null && settings.containsKey(UHFSilionParams.TAGDATA_RECORDHIGHESTRSSI.KEY)) {
            int[] hightrssi = (int[]) settings.get(UHFSilionParams.TAGDATA_RECORDHIGHESTRSSI.KEY);
            boolean bRecordRssi = hightrssi == null ? false : hightrssi[0] == 1;
            cb_max_rssi.setOnCheckedChangeListener(null);
            cb_max_rssi.setChecked(bRecordRssi);//Record hightest rssi
        }


        //--EMBDED DATA TID
        cb_tid.setOnCheckedChangeListener(null);
        cb_tid.setChecked(false);
        try {
            String sValue = (String) settings.get(UHFSilionParams.TAG_EMBEDEDDATA.KEY);
            JSONObject jsItem = new JSONObject(sValue);
            int bank = jsItem.optInt("bank");
            String sHexAccesspwd = jsItem.optString("accesspwd");
            int bytecnt = jsItem.optInt("bytecnt");
            int startaddr = jsItem.optInt("startaddr");
            if (bytecnt > 0 && bank == UHFReader.BANK_TYPE.TID.value())
                cb_tid.setChecked(true);
        } catch (Exception e) {
        }

        String sRssi = mUHFMgr.getParam(UHFSilionParams.INV_FIELD_RSSI.KEY,
                UHFSilionParams.INV_FIELD_RSSI.PARAM_INV_FIELD_RSSI,
                "0");
        String sFrequnce = mUHFMgr.getParam(UHFSilionParams.INV_FIELD_FREQUENCE.KEY,
                UHFSilionParams.INV_FIELD_FREQUENCE.PARAM_INV_FIELD_FREQUENCE,
                "0");
        String sProtocal = mUHFMgr.getParam(UHFSilionParams.INV_FIELD_PROTOCAL.KEY,
                UHFSilionParams.INV_FIELD_PROTOCAL.PARAM_INV_FIELD_PROTOCAL,
                "0");

        boolean isNormalMode = true;
        String modelName = mUHFMgr.getUHFDeviceModel();
        mModelName = mUHFMgr.getUHFDeviceModel();
        if (Constant.isUR90(modelName) //UR90[MODOULE_SLR1200]
                || Constant.isBU10_or_BU20(modelName)) //BU10/BU20[MODOULE_SLR1200]
        {
            int iQuickMode = settings.containsKey(UHFSilionParams.INV_QUICK_MODE.KEY) ? (int) settings.get(UHFSilionParams.INV_QUICK_MODE.KEY) : 0;
            int[] iGenSessions = settings.containsKey(UHFSilionParams.POTL_GEN2_SESSION.KEY) ? (int[]) settings.get(UHFSilionParams.POTL_GEN2_SESSION.KEY) : new int[]{-1};
            boolean q1enable1200 = (iQuickMode == 1 && iGenSessions[0] > 0);
            boolean q0enable1200 = (iQuickMode == 1 && iGenSessions[0] == 0);
            if (q1enable1200 || q0enable1200)
                isNormalMode = false;
        } else {
            int invPolicy = mContext.getResources().getInteger(R.integer.inv_policy_balance_value);
            if (null != settings && settings.containsKey(UHFSilionParams.INV_POLICY.KEY))
                invPolicy = (int) settings.get(UHFSilionParams.INV_POLICY.KEY);
            isNormalMode = (invPolicy == mContext.getResources().getInteger(R.integer.inv_policy_normal_value));
        }

        //Normal mode,disable rssi,frequence
        boolean brssi = (!"0".equals(sRssi)) || isNormalMode;
        boolean bfrequnce = (!"0".equals(sFrequnce)) || isNormalMode;
        boolean bprotocal = (!"0".equals(sProtocal)) || isNormalMode;

        if (Constant.isBU10_or_BU20(modelName))
            cb_tid.setVisibility(View.INVISIBLE);

        cb_rssi.setOnCheckedChangeListener(null);
        cb_frequence.setOnCheckedChangeListener(null);
        cb_protocal.setOnCheckedChangeListener(null);
        cb_rssi.setChecked(brssi);
        cb_frequence.setChecked(bfrequnce);
        cb_protocal.setChecked(bprotocal);

        if(Constant.isUR90_V2(modelName) || Constant.isUR90(modelName)){
            cb_rssi.setEnabled(isNormalMode ? false : true);
            cb_frequence.setEnabled(isNormalMode ? false : true);
            cb_protocal.setEnabled(isNormalMode ? false : true);
        }

        CompoundButton.OnCheckedChangeListener mOnCheck = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged: " + buttonView.getText());
                switch (buttonView.getId()) {
                    case R.id.cb_max_rssi:
                        mUHFMgr.setParam(UHFSilionParams.TAGDATA_RECORDHIGHESTRSSI.KEY, UHFSilionParams.TAGDATA_RECORDHIGHESTRSSI.PARAM_TAGDATA_RECORDHIGHESTRSSI, isChecked ? "1" : "0");
                        break;
                    case R.id.cb_tid:
                        enableTIDEmbeded(isChecked);
                        break;
                    case R.id.cb_rssi:
                        mUHFMgr.setParam(UHFSilionParams.INV_FIELD_RSSI.KEY, UHFSilionParams.INV_FIELD_RSSI.PARAM_INV_FIELD_RSSI, isChecked ? "1" : "0");
                        break;
                    case R.id.cb_frequence:
                        mUHFMgr.setParam(UHFSilionParams.INV_FIELD_FREQUENCE.KEY, UHFSilionParams.INV_FIELD_FREQUENCE.PARAM_INV_FIELD_FREQUENCE, isChecked ? "1" : "0");
                        break;
                    case R.id.cb_protocal:
                        mUHFMgr.setParam(UHFSilionParams.INV_FIELD_PROTOCAL.KEY, UHFSilionParams.INV_FIELD_PROTOCAL.PARAM_INV_FIELD_PROTOCAL, isChecked ? "1" : "0");
                        break;
                }
            }
        };

        cb_max_rssi.setOnCheckedChangeListener(mOnCheck);
        cb_tid.setOnCheckedChangeListener(mOnCheck);
        cb_rssi.setOnCheckedChangeListener(mOnCheck);
        cb_frequence.setOnCheckedChangeListener(mOnCheck);
        cb_protocal.setOnCheckedChangeListener(mOnCheck);
        if (Constant.isURM300(mModelName)
                || Constant.isURF520(mModelName)
                || Constant.isURM500(mModelName)
                || Constant.isURF100(mModelName)) {
            cb_max_rssi.setVisibility(View.GONE);
        }
    }

    private void enableTIDEmbeded(boolean enable) {
        Log.i(TAG, "enableTIDEmbeded: " + enable);
        int bank = UHFReader.BANK_TYPE.TID.value();//TID Bank
        int startaddr = 0;//start address(block count)
        int bytecnt = 12;
        try {
            JSONObject jsItem = new JSONObject();
            jsItem.put("bank", bank);
            jsItem.put("startaddr", startaddr);
            jsItem.put("bytecnt", bytecnt);
            String sValue = jsItem.toString();
            //set embeded datas
            if (enable) {
                mUHFMgr.setParam(UHFSilionParams.TAG_EMBEDEDDATA.KEY,
                        UHFSilionParams.TAG_EMBEDEDDATA.PARAM_TAG_EMBEDEDDATA,
                        sValue);
            } else {
                mUHFMgr.setParam(UHFSilionParams.TAG_EMBEDEDDATA.KEY,
                        UHFSilionParams.TAG_EMBEDEDDATA.PARAM_TAG_EMBEDEDDATA,
                        null);
            }


        } catch (Exception e) {

        }

    }

    private UHFReader.READER_STATE startInventory() {
        //region debug
        debugFirstTagReceived = true;
        debugFirstTagReceivedTime = 0;
        receivedTagCount = 0;

        debugInvDataHandlerFirstTag = true;
        debugInvDataHandlerFirstTagTime = 0;
        debugInvDataHandlerTagCount = 0;
        //endregion

        invStopping = false;
        if (!mUHFMgr.isPowerOn()) {
            Toast.makeText(mContext, getString(R.string.uhf_not_available), Toast.LENGTH_SHORT).show();
            return UHFReader.READER_STATE.UNKNOWN_READER_TYPE;
        }

        mIsEmbedDataEnable = isEmbedEnable();
        UHFReader.READER_STATE er = doStartInventory();
        //Keep screen on
        if (er == UHFReader.READER_STATE.OK_ERR) {
            updateStateOnStartInventroy();
        }
        return er;
    }

    private void updateStateOnStartInventroy() {
        mStartReadingTime = System.currentTimeMillis();
        Log.d("TAG", "--updateStateOnStartInventroy,time: " + mStartReadingTime);
        tv_inv_span_time.startTimeCounter();
        startTimeLimit();
        disableOrEnableViewOnInventory(false);
    }

    private void disableOrEnableViewOnInventory(boolean enable) {
        Activity activity = getActivity();
        if (activity != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_start_inventory.setEnabled(enable);
                    btn_clean.setEnabled(enable);
                    cb_max_rssi.setEnabled(enable);
                    cb_mutip_tags.setEnabled(enable);
                    cb_tid.setEnabled(enable);
                    et_time.setEnabled(enable);
                }
            });
        }

        Map<String, Object> settings = mUHFMgr.getAllParams();
        boolean isNormalMode = true;
        String modelName = mUHFMgr.getUHFDeviceModel();
        if (Constant.isUR90(modelName) //UR90[MODOULE_SLR1200]
                || Constant.isBU10_or_BU20(modelName)) //BU10/BU20[MODOULE_SLR1200]
        {
            int iQuickMode = settings.containsKey(UHFSilionParams.INV_QUICK_MODE.KEY) ? (int) settings.get(UHFSilionParams.INV_QUICK_MODE.KEY) : 0;
            int[] iGenSessions = settings.containsKey(UHFSilionParams.POTL_GEN2_SESSION.KEY) ? (int[]) settings.get(UHFSilionParams.POTL_GEN2_SESSION.KEY) : new int[]{-1};
            boolean q1enable1200 = (iQuickMode == 1 && iGenSessions[0] > 0);
            boolean q0enable1200 = (iQuickMode == 1 && iGenSessions[0] == 0);
            if (q1enable1200 || q0enable1200)
                isNormalMode = false;

            Log.d(TAG, "Normal mode: " + isNormalMode);

        } else {
            int invPolicy = mContext.getResources().getInteger(R.integer.inv_policy_balance_value);
            if (null != settings && settings.containsKey(UHFSilionParams.INV_POLICY.KEY))
                invPolicy = (int) settings.get(UHFSilionParams.INV_POLICY.KEY);
            isNormalMode = (invPolicy == mContext.getResources().getInteger(R.integer.inv_policy_normal_value));
        }

        if (isNormalMode && (Constant.isUR90(modelName) || Constant.isUR90_V2(modelName))) {
            cb_rssi.setEnabled(false);
            cb_protocal.setEnabled(false);
            cb_frequence.setEnabled(false);
        } else {
            cb_rssi.setEnabled(enable);
            cb_protocal.setEnabled(enable);
            cb_frequence.setEnabled(enable);
        }

    }

    private UHFReader.READER_STATE stopInventory() {
        Log.d(TAG, "--stopInventory...");
        invStopping = true;
        UHFReader.READER_STATE er = doStopInventory();
        if (er == UHFReader.READER_STATE.OK_ERR)
            updateStateOnStoptInventroy();
        return er;
    }

    private void updateStateOnStoptInventroy() {
        tv_inv_span_time.stopTimeCounter();
        disableOrEnableViewOnInventory(true);
    }


    private UHFReader.READER_STATE doStartInventory() {
        UHFReader.READER_STATE er = mUHFMgr.startTagInventory();
        return er;
    }

    private UHFReader.READER_STATE doStopInventory() {
        Log.d(TAG, "--doStopInventory...");
        return mUHFMgr.stopTagInventory();
    }

//    private void updateListView()
//    {
//        /**第一个可见的位置**/
//        int firstVisiblePosition = lv_data_list.getFirstVisiblePosition();
//        /**最后一个可见的位置**/
//        int lastVisiblePosition = firstVisiblePosition + 15 ;
//        if(mUhfDataList.size() < lastVisiblePosition -1)
//            lastVisiblePosition = mUhfDataList.size() - 1;
//
//        for(int i = firstVisiblePosition; i < lastVisiblePosition + 1; i++)
//        {
//            int position = i;
//            View convertView = lv_data_list.getChildAt(position);
//            boolean needadd = convertView == null;
//            if(needadd)
//                mAdapter.addList();
//            else
//                mAdapter.getView(position,convertView,lv_data_list);
//        }
//
//    }

    private void runUpdateViewThread(Parcelable[] tagInfos) {


        if (tagInfos == null)
            return;

        try {
            boolean handleComplete = false;
            while (!handleComplete && !mActivityPause) {
                //It's scrolling,don't update view imediatly
                if (mListViewScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    long startTime = System.currentTimeMillis();
                    getDataFromList(tagInfos);
                    long endTime = System.currentTimeMillis();
                    Log.i(TAG, "runUpdateViewThread: " + String.format("getDataFromList cost time:%d ms", endTime - startTime));

                    mUIHandler.sendEmptyMessage(MSG_UPDATE_DATA_LIST_VIEW);
                    updateTagCountViewDatas(null);
                    handleComplete = true;
                }
                if (!handleComplete)
                    Thread.sleep(10);
            }

        } catch (Exception e) {
            Log.w(TAG, "err:", e);
        }
    }

    /**
     * 盘点数据会到达
     *
     * @param tagInfos
     */
    private void getDataFromList(Parcelable[] tagInfos) {
        if (tagInfos == null) {
            return;
        } else {

            boolean isMultipTag = cb_mutip_tags.isChecked();
            for (Parcelable parcel : tagInfos) {
                TagInfo tagInfo = (TagInfo) parcel;
                tagInfo.Epclen = tagInfo.Epclen == 0 ? (short) tagInfo.EpcId.length : tagInfo.Epclen;
                if (tagInfo != null) {
                    Log.i(TAG, "getDataFromList: " + format("EPC:%s,ReadCnt:%d,Epclen:%d,rssi:%d"
                            , HexUtil.bytesToHexString(tagInfo.EpcId), tagInfo.ReadCnt, tagInfo.Epclen, tagInfo.RSSI));
                }
                if (!isMultipTag) {
                    int rssi = tagInfo.RSSI;
                    if (rssi < mMaxRssi)
                        continue;
                    else {
                        mMaxRssi = rssi;
                        mUhfDataList.clear();
                    }
                }

                //unavailable tag
                String indexKey = getTagKey(tagInfo);
                Log.i(TAG, "getDataFromList: " + format("indexKey:%s", indexKey));
                if (TextUtils.isEmpty(indexKey))
                    continue;

                //Remote repeatitive EPC
                boolean tagExist = mTagCountMap.containsKey(indexKey);
                int readcnt = tagExist ? mTagCountMap.get(indexKey) : 0;
                readcnt += tagInfo.ReadCnt;
                boolean isUrm300 = Constant.isURM300(mUHFMgr.getUHFDeviceModel());
                boolean isURM500 = Constant.isURM500(mUHFMgr.getUHFDeviceModel());
                Log.i(TAG, "getDataFromList: isUrm300 " + isUrm300);
                if (!isUrm300 && !Constant.isURF520(mUHFMgr.getUHFDeviceModel()) && !isURM500) {//urf520 && urm300 && urm500 tagCount was added in sdk
                    tagInfo.ReadCnt = readcnt;
                }
                mTagCountMap.put(indexKey, tagInfo.ReadCnt);

                if (tagExist) //Already exist
                {
                    TableItemInfo oldItemInfo = mTableItemMap.get(indexKey);
                    TagInfo oldTagInfo = oldItemInfo.tagInfo;
                    TagInfo newTagInfo = tagInfo;
                    oldTagInfo.AntennaID = newTagInfo.AntennaID;
                    oldTagInfo.Frequency = newTagInfo.Frequency;
                    oldTagInfo.TimeStamp = newTagInfo.TimeStamp;
                    //oldTagInfo.EmbededDatalen = newTagInfo.EmbededDatalen;
                    //oldTagInfo.EmbededData = newTagInfo.EmbededData;
                    oldTagInfo.Res = newTagInfo.Res;
                    oldTagInfo.Epclen = newTagInfo.Epclen;
                    oldTagInfo.PC = newTagInfo.PC;
                    oldTagInfo.CRC = newTagInfo.CRC;
                    oldTagInfo.EpcId = newTagInfo.EpcId;
                    oldTagInfo.Phase = newTagInfo.Phase;
                    oldTagInfo.protocol = newTagInfo.protocol;
                    oldTagInfo.ReadCnt = newTagInfo.ReadCnt;
                    oldTagInfo.RSSI = newTagInfo.RSSI;


                    if (newTagInfo.EmbededDatalen > 0) {
                        oldTagInfo.EmbededDatalen = newTagInfo.EmbededDatalen;
                        oldTagInfo.EmbededData = newTagInfo.EmbededData;
                    }

                    //mUhfDataList.remove(index);
                    //mUhfDataList.add(index,itemInfo);
                } else {
                    final TableItemInfo itemInfo = new TableItemInfo(mContext);
                    itemInfo.tagInfo = tagInfo;
                    mTableItemMap.put(indexKey, itemInfo);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.i(TAG, "mUhfDataList-currThread Name" + Thread.currentThread().getName());
                            mUhfDataList.add(itemInfo);
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                    //Add data to global cache
                    byte[] epcIdBytes = Arrays.copyOfRange(tagInfo.EpcId, 0, tagInfo.Epclen);
                    String epcIdHex = UHFReader.bytes_Hexstr(epcIdBytes);
                    AppApplication.getInstance().addTagData(epcIdHex);
                }
            }
        }
    }

    private String getTagKey(TagInfo tagInfo) {
        if (tagInfo == null) {
            Log.i(TAG, "getTagKey: " + "tagInfo is null");
            return null;
        } else {
            Log.i(TAG, "getTagKey: " + format("mIsEmbedDataEnable:=%s, EmbededDatalen:=%d", mIsEmbedDataEnable, tagInfo.EmbededDatalen));
        }
        if (mIsEmbedDataEnable && tagInfo.EmbededDatalen == 0)
            return null;

        byte[] epcIdBytes = Arrays.copyOfRange(tagInfo.EpcId, 0, tagInfo.Epclen);
        byte[] embedDataBytes = tagInfo.EmbededDatalen > 0 ? Arrays.copyOfRange(tagInfo.EmbededData, 0, tagInfo.EmbededDatalen) : null;

        String epcIdHex = UHFReader.bytes_Hexstr(epcIdBytes);
        String embededDataHex = UHFReader.bytes_Hexstr(embedDataBytes);
        Log.i(TAG, "getTagKey: " + format("epcIdHex:=%s, embededDataHex:=%s", epcIdHex, embededDataHex));
        epcIdHex = epcIdHex == null ? "" : epcIdHex.trim();
        embededDataHex = embededDataHex == null ? "" : embededDataHex.trim();
        return epcIdHex + embededDataHex;
    }

    /**
     * Is embeded data enable
     *
     * @return true - yes / false - no
     */
    private boolean isEmbedEnable() {
        try {
            Map<String, Object> mSettingsMap = mUHFMgr.getAllParams();
            String sValue = (String) mSettingsMap.get(UHFSilionParams.TAG_EMBEDEDDATA.KEY);
            JSONObject jsItem = new JSONObject(sValue);
            int bank = jsItem.optInt("bank");
            String sHexAccesspwd = jsItem.optString("accesspwd");
            int bytecnt = jsItem.optInt("bytecnt");
            int startaddr = jsItem.optInt("startaddr");
            if (bytecnt > 0)
                return true;
        } catch (Exception e) {
        }

        return false;
    }

    private void startClear() {

        while (mListViewScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }

        mTableItemMap.clear();
        mTagCountMap.clear();
        mAdapter.clearAll();
        readTotalCount = 0;
        tagCount = 0;
        speed = 0;
        mMaxRssi = -1000;
        //Clear tag's cache(must be invoked)
        mUHFMgr.setParam(UHFSilionParams.INV_CLEAR_CACHE.KEY, UHFSilionParams.INV_CLEAR_CACHE.PARAM_INV_CLEAR_CACHE, "1");
        mUIHandler.sendEmptyMessage(MSG_CLEAR_COMPLETE);
        try {
            Thread.sleep(50);
        } catch (Exception e) {

        }
    }

    private void startTimeLimit() {
        String sTimeLimit = et_time.getText().toString();
        long lTimeSec = 0;
        if (!TextUtils.isEmpty(sTimeLimit) && TextUtils.isDigitsOnly(sTimeLimit)) {
            int maxLen = String.valueOf(Long.MAX_VALUE).length();
            if (sTimeLimit.length() > maxLen)
                sTimeLimit = sTimeLimit.substring(0, maxLen);
            lTimeSec = Long.parseLong(sTimeLimit);
        }

        if (lTimeSec <= 0)
            return;

        final long fLtime = lTimeSec * 1000;

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    long passTime = tv_inv_span_time.getPassTime();
                    while (true) {
                        Thread.sleep(100);
                        if (passTime > fLtime || invStopping) {
                            break;
                        } else {
                            passTime = tv_inv_span_time.getPassTime();
                        }
                    }

                    if (mUHFMgr.isInInventory())
                        mUIHandler.sendEmptyMessage(MSG_STOP_INVENTORY);

                } catch (Exception e) {
                }
            }
        }).start();
    }

    private void registerResultReceiver() {
        try {
            IntentFilter iFilter = new IntentFilter(Constant.ACTION_UHF_RESULT_SEND);
            mContext.registerReceiver(mUhfBR, iFilter);

            mBatteryStateReceiver = new BatteryStateReceiver();
            IntentFilter iFilter_battery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            mContext.registerReceiver(mBatteryStateReceiver, iFilter_battery);
        } catch (Exception e) {
        }

    }

    private void unRegisterResultReceiver() {
        try {
            mContext.unregisterReceiver(mUhfBR);
            if (mBatteryStateReceiver != null)
                mContext.unregisterReceiver(mBatteryStateReceiver);
        } catch (Exception e) {
        }
    }


    private void updateInventoryParams() {
        if (getActivity() == null)
            return;

        Log.d(TAG,"readTotalCount:="+readTotalCount);
        tv_total_count.setText(getString(R.string.uhf_total_count, " " + this.readTotalCount));
        tv_total_tags_count.setText(getString(R.string.uhf_total_tag, " " + this.tagCount));

        if (!TextUtils.isEmpty(et_target_count.getText().toString())) {
//            Log.d(TAG, "updateInventoryParams: "+String.format("tagCount:%d,targetCount:%d",this.tagCount,Integer.parseInt(et_target_count.getText().toString())));
            if (this.tagCount >= Integer.parseInt(et_target_count.getText().toString())) {
                if (mUHFMgr.isInInventory()) {
                    mUIHandler.sendEmptyMessage(MSG_STOP_INVENTORY);
                }
            }
        }

        //Log.i(TAG, "updateInventoryParams: " + format("mName:%s,isurm:%b,t:%s,spT:%s", mModelName, Constant.isURM300(mModelName), this.readTotalCount, tv_inv_span_time.getPassTime()));
        tv_speed.setText(getString(R.string.uhf_inv_speed, " "
                + speed + "/S"));
    }

    /**
     * Push recevied tag's informations to cache,for handler later
     */
    private void pushTagsToCacheAndHandle(Parcelable[] tagInfos) {
        synchronized (this) {


            //It's stoped, do nothing
            if (mActivityPause)
                return;

            if (tagInfos == null || tagInfos.length == 0)
                return;

            performPrompt();

            //Update tags count
            updateTagCountViewDatas(tagInfos);

            debugInvDataHandlerTagCount++;
            Log.i(TAG, "handleMessage: " + String.format("TagCount=%d, ", debugInvDataHandlerTagCount));
            //Cached first
            //runUpdateViewThread(tagInfos);
            Message msg = Message.obtain(mInvDataHandler, MSG_RUN_GET_TAGS_DATA, tagInfos);
            msg.sendToTarget();
        }
    }

    /**
     * Inventory prompt
     */
    private void performPrompt() {

        if (mPrompting)
            return;
        mPrompting = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean soundEnable = false;
                boolean vibrateEnable = false;
                if (UHFConstants.isNewlandPDA()) {
                    soundEnable = mUHFMgr.isPromptSoundEnable();
                    vibrateEnable = mUHFMgr.isPromptVibrateEnable();
                } else {
                    AppApplication app = (AppApplication) getActivity().getApplication();
                    soundEnable = app.mSound;
                    vibrateEnable = app.mVibrate;
                }

                if (soundEnable) {
                    AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    // 获取最大音量值
                    float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_RING);
                    // 不断获取当前的音量值
                    float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_RING);
                    //最终影响音量
                    float volumnRatio = audioCurrentVolumn / audioMaxVolumn;
                    long begin = System.currentTimeMillis();
                    while((System.currentTimeMillis() - begin) < 1000L)
                    {
                        soundPool.play(1, volumnRatio, volumnRatio, 0, 0, 1);
                    }

                }

                if (vibrateEnable) {
                    mVibrator.vibrate(80); // 震动
                }
                mPrompting = false;
            }
        }).start();

    }//End performPrompt

    private void updateTagCountViewDatas(Parcelable[] tagInfos) {

        String modelName = mUHFMgr.getUHFDeviceModel();
        Map<String, Integer> copyMap = new HashMap<>();
        copyMap.putAll(mTagCountMap);

        if (tagInfos != null) {
            for (Parcelable parcel : tagInfos) {
                TagInfo tag = (TagInfo) parcel;
                String indexKey = getTagKey(tag);
                if (TextUtils.isEmpty(indexKey))
                    continue;

                int curTagCnt = tag.ReadCnt == 0 ? 1 : tag.ReadCnt;
                //Log.d(TAG,"curTagCnt:="+curTagCnt);
                if (copyMap.containsKey(indexKey)) {
                    int cnt = copyMap.get(indexKey) + curTagCnt;
                    copyMap.put(indexKey, cnt);
                } else {
                    copyMap.put(indexKey, curTagCnt);
                }
            }
        }

        tagCount = copyMap.size();
        int tempTotal = 0;
        Collection<Integer> values = copyMap.values();
        for (Integer cnt : values) {
            tempTotal += cnt;
        }
        readTotalCount = tempTotal;

        if (mStartReadingTime != 0) {
            long readTime = tv_inv_span_time.getPassTime(); //System.currentTimeMillis() - mStartReadingTime;
            //Log.d("TAG", "total:" + readTotalCount + ", Read time: " + (readTime));
            speed = readTotalCount / (int) (readTime / 1000 == 0 ? 1 : readTime / 1000);
        }

        //Update inventory's parameters
        mUIHandler.sendEmptyMessage(MSG_UPDATE_INVENTORY_PARAMS);


    }

    //--------------------------------------------------------------------------------
    // Inner Class
    //--------------------------------------------------------------------------------

    private boolean debugFirstTagReceived = true;
    private long debugFirstTagReceivedTime = 0;
    private int receivedTagCount = 0;
    /**
     * The broadcast receiver of uhf inventory result
     */
    private BroadcastReceiver mUhfBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            long receivedTime = System.currentTimeMillis();
            String action = intent.getAction();
            if (!Constant.ACTION_UHF_RESULT_SEND.equals(action))
                return;

            Parcelable[] tagInfos = intent.getParcelableArrayExtra(Constant.EXTRA_TAG_INFO);
            if (debugFirstTagReceived) {
                debugFirstTagReceivedTime = System.currentTimeMillis();
                debugFirstTagReceived = false;
            }
            receivedTagCount++;
            long startReadingTime = intent.getLongExtra("extra_start_reading_time", 0l);
            mStartReadingTime = startReadingTime == 0 ? mStartReadingTime : startReadingTime;
            if (mStartReadingTime == 0)
                mStartReadingTime = System.currentTimeMillis();

            Log.d(TAG, "onReceive: mStartReadingTime: " + mStartReadingTime);
            Log.i(TAG, "onReceive: " + String.format("TagCount=%d, receivedTime=%d", receivedTagCount, receivedTime));
            Message msg = Message.obtain(mTagsReceivedHandler, MSG_TAGS_PUSH_TO_CACHE_AND_HANDLE, tagInfos);
            msg.sendToTarget();
        }
    };

    public class BatteryStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int mainlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int percent = (mainlevel * 100) / scale; //eg: 56%

                int btemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                float fbtemperature = (float) btemperature / 10;
                String sbtemperature = format("%3.1f", fbtemperature);
                tv_battery_temperature.setText(getString(R.string.battery_temperature, sbtemperature));
            }
        }
    }//End BatteryStateReceiver

    private class CustomHandler extends Handler {
        public CustomHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            //UI handler part
            switch (msg.what) {
                case MSG_UPDATE_VIEW:
                    updateViewData();
                    disableOrEnableViewOnInventory(!mUHFMgr.isInInventory());
                    break;
                case MSG_UPDATE_DATA_LIST_VIEW:
                    mAdapter.addList();
                    break;
                case MSG_UPDATE_INVENTORY_PARAMS:
                    updateInventoryParams();
                    break;
                case MSG_STOP_INVENTORY:
                    stopInventory();
                    break;
                case MSG_CLEAR_COMPLETE:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    tv_total_count.setText(getString(R.string.uhf_total_count, ""));
                    tv_total_tags_count.setText(getString(R.string.uhf_total_tag, ""));
                    tv_speed.setText(getString(R.string.uhf_inv_speed, ""));
                    tv_inv_span_time.setText("");
                    break;
            }//End switch

            //Third thread handler part
            switch (msg.what) {
                case MSG_TAGS_PUSH_TO_CACHE_AND_HANDLE:
                    Parcelable[] tagInfos = (Parcelable[]) msg.obj;
                    pushTagsToCacheAndHandle(tagInfos);
                    break;
            }//End switch

        }//end handleMessage
    }//End class CustomHandler
}
