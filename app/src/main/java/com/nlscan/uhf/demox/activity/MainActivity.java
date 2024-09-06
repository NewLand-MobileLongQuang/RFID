package com.nlscan.uhf.demox.activity;

import static com.nlscan.uhf.lib.UHFManager.PLUGIN_TYPE_NETWORK;

import static java.lang.String.format;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nlscan.uhf.demox.fragment.CommonCmdFragment;
import com.nlscan.uhf.demox.fragment.IndustryTestFragment;
import com.nlscan.uhf.demox.fragment.RapidInvFragment;
import com.nlscan.uhf.demox.util.PermissionUtils;
import com.nlscan.uhf.demox.util.constant.UHFSilionParams;
import com.nlscan.uhf.lib.UHFManager;
import com.nlscan.uhf.lib.UHFReader;
import com.nlscan.uhf.demox.AppApplication;
import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.fragment.BaseFragment;
import com.nlscan.uhf.demox.fragment.InventoryFragment;
import com.nlscan.uhf.demox.fragment.TagWriteFragment;
import com.nlscan.uhf.demox.fragment.UHFSettingsFragment;
import com.nlscan.uhf.demox.util.Constant;
import com.nlscan.uhf.lib.UHFSDK;

import java.util.Map;

public class MainActivity extends Activity {

    private final String SOCKET_DEV_ACTIVITY = "com.nlscan.uhf.demox.activity.SocketDevActivity";

    private final static int REQUEST_RUNTIME_PERMISSION = 0x10;
    private boolean mPermissionGranted = false;
    private Context mContext;
    private UHFManager mUHFMgr;
    private BaseFragment mInventoryFragment, mRapidInvFragment;
    private BaseFragment mSettingsFragment;
    private BaseFragment mTagFragment;
    private BaseFragment mCurFragment;
    private BaseFragment mIndustryFragment, mCommonCmdFragment;

    private TextView tv_inv_mode_label;
    private TextView tv_inventory;
    private TextView tv_settings;
    private TextView tv_tags;
    private TextView tv_industry, tv_common_cmd;
    private ImageView im_actionbar_settings, imRecycle;

    private PopupWindow mInvPolicyPopupWindow;
    private View mInvPolicyContentview;

    private String mDevPathOrMac, mDeviceModelUserSelected;

    private boolean mPaused = false;
    private ProgressDialog mLoadingPD = null;
    private Dialog mReLoadDialog = null;

    private final int MSG_LOAD_MODULE_COMPLETED = 0x01;
    private final int MSG_LOAD_MODULE = 0x02;
    private final int MSG_SHOW_RELOAD_WINDOW = 0x03;
    private final int MSG_UPDATE_ACTION_BAR = 0x04;

    private Handler mUIHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: " + String.format("msg.what = %d", msg.what));
            switch (msg.what) {

                case MSG_LOAD_MODULE_COMPLETED:
                    if (mUHFMgr.getUHFModuleInfo() != null)
                        doPowerOn();
                    else
                        showReloadModuleWindow();
                    break;
                case MSG_LOAD_MODULE:
                    reConnect();
                    break;
                case MSG_SHOW_RELOAD_WINDOW:
                    showReloadModuleWindow();
                    break;
                case MSG_UPDATE_ACTION_BAR:
                    updateActionBarInfo();
                    break;

            }//end switch
        }

    };
    private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        mUHFMgr = UHFManager.getInstance(mContext);
        mDevPathOrMac = getIntent().getStringExtra(Constant.EXTRA_DEVICE_PATH_OR_MAC);
        mDeviceModelUserSelected = getIntent().getStringExtra(Constant.EXTRA_DEVICE_MODEL_KEY);

        initView();
        registerUHFStateReceiver();

        Log.d(TAG, "MainActivity.onCreate - isPowerOn()");
        if (mUHFMgr.isPowerOn()) {
            showInventoryFragment();
        }

        boolean needRequestPermission = PermissionUtils.checkRequestRuntimePermissions(mContext);
        if (needRequestPermission) {
            PermissionUtils.requestAllRuntimePermission(this, REQUEST_RUNTIME_PERMISSION);
        } else
            mPermissionGranted = true;


    }


    private long mLastKeyBackTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLastKeyBackTime == 0 || (System.currentTimeMillis() - mLastKeyBackTime) > 1000) {
                Toast.makeText(mContext, getString(R.string.exit_confirm_tip), Toast.LENGTH_SHORT).show();
                mLastKeyBackTime = System.currentTimeMillis();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPaused = false;
        Log.d(TAG, "MainActivity.onResume - isPowerOn()");
        if (!mUHFMgr.isConnected()) {
            if (mPermissionGranted) {
                reConnect();
                showLoadingWindow();
            }

        } else {
            updateActionBarInfo();
        }

        if (null == mCurFragment) {
            showInventoryFragment();
        }
        /*UHFModuleInfo module = mUHFMgr.getUHFModuleInfo();
        if(module == null) //Load rfid mode
            loadModule();
        else if(!mUHFMgr.isPowerOn()) //Do power on
            doPowerOn();
        else
            updateActionBarInfo();*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterUHFStateReceiver();
        //Clear datas of this inventory period
        AppApplication.getInstance().clearTagData();
        Log.d("TAG", "---onDestory---");
        mUHFMgr.powerOff();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RUNTIME_PERMISSION) {
            mPermissionGranted = true;
            reConnect();
            showLoadingWindow();
        }
    }

    private void initView() {
        tv_inventory = (TextView) findViewById(R.id.tv_inventory);
        tv_settings = (TextView) findViewById(R.id.tv_settings);
        tv_tags = (TextView) findViewById(R.id.tv_tags);
        tv_industry = (TextView) findViewById(R.id.tv_industry);
        tv_common_cmd = (TextView) findViewById(R.id.tv_common_cmd);
        im_actionbar_settings = (ImageView) findViewById(R.id.im_actionbar_settings);
        tv_inv_mode_label = (TextView) findViewById(R.id.tv_inv_mode_label);
        imRecycle = (ImageView) findViewById(R.id.im_recycle);

        View.OnClickListener mClick = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_inventory:
                        showInventoryFragment();
                        break;
                    case R.id.tv_settings:
                        showSettingsFragment();
                        break;
                    case R.id.tv_tags:
                        showTagsFragment();
                        break;
                    case R.id.im_actionbar_settings:
                        showInvPolicyPopWindow();
                        break;
                    case R.id.tv_industry:
                        showIndustryTestFragment();
                        break;
                    case R.id.im_recycle:
                        toggleRapidInvFragment();
                        break;
                    case R.id.tv_common_cmd:
                        showCommonCmdFragment();
                        break;
                }
            }
        };
        tv_inventory.setOnClickListener(mClick);
        tv_settings.setOnClickListener(mClick);
        tv_tags.setOnClickListener(mClick);
        im_actionbar_settings.setOnClickListener(mClick);
        tv_industry.setOnClickListener(mClick);
        imRecycle.setOnClickListener(mClick);
        tv_common_cmd.setOnClickListener(mClick);
    }

    private void toggleRapidInvFragment() {
        Log.i(TAG, "toggleRapidInvFragment: ");
        if (mCurFragment instanceof RapidInvFragment) {
            showInventoryFragment();
        } else {
            showRapidInventoryFragment();
        }
    }

    private void showRapidInventoryFragment() {
        if (mCurFragment instanceof RapidInvFragment)
            return;
        if (mRapidInvFragment == null)
            mRapidInvFragment = new RapidInvFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.body, mRapidInvFragment);
        transaction.commit();
        im_actionbar_settings.setVisibility(View.VISIBLE);
        imRecycle.setVisibility(View.VISIBLE);
        tv_inv_mode_label.setVisibility(View.VISIBLE);
        focusTab(R.id.tv_inventory);
        mCurFragment = mRapidInvFragment;
    }

    private void initViewURM300() {
        boolean isUrf520 = Constant.isURM300(mUHFMgr.getUHFDeviceModel());
        Log.i(TAG, "initView300: " + isUrf520 + "  " + tv_industry + "   " + mUHFMgr.getUHFDeviceModel() + "   " + mUHFMgr.getClass().getSimpleName());
        if (isUrf520 && tv_industry != null) {
            tv_industry.setVisibility(View.VISIBLE);
        }
    }

    private void initViewUnionCmd() {
        boolean isUnionCmd = Constant.isUnionCmd(mUHFMgr.getUHFDeviceModel());
        Log.i(TAG, "isUnionCmd: " + isUnionCmd + "  " + tv_common_cmd + "   " + mUHFMgr.getUHFDeviceModel() + "   " + mUHFMgr.getClass().getSimpleName());
        if (isUnionCmd && tv_common_cmd != null) {
            tv_common_cmd.setVisibility(View.VISIBLE);
        }
    }

    private void updateActionBarInfo() {
        //Device model name
        String devModelName = mUHFMgr.getUHFDeviceModel();
        String invModeName = null;
        //--UR90/BU10/BU20 part
        if (Constant.isUR90(devModelName) || Constant.isBU10_or_BU20(devModelName)) {
            int iQuickMode = getUHFIntSetting(UHFSilionParams.INV_QUICK_MODE.KEY, 0);
            int[] iGenSessions = getUHFIntArraySetting(UHFSilionParams.POTL_GEN2_SESSION.KEY);
            iGenSessions = iGenSessions == null ? new int[]{-1} : iGenSessions;
            boolean q1enable1200 = (iQuickMode == 1 && iGenSessions[0] > 0);
            boolean q0enable1200 = (iQuickMode == 1 && iGenSessions[0] == 0);
            if (q1enable1200)
                invModeName = getString(R.string.start_quick_mode_s1);
            else if (q0enable1200)
                invModeName = getString(R.string.start_quick_mode_s0);
            else
                invModeName = getString(R.string.uhf_inv_mode_normal);
        }

        //--SIM7100[UR90_V2.0] part
        if (Constant.isUR90_V2(devModelName)) {
            int curInvPolicy = getUHFIntSetting(UHFSilionParams.INV_POLICY.KEY, mContext.getResources().getInteger(R.integer.inv_policy_balance_value));
            int normalPolicy = mContext.getResources().getInteger(R.integer.inv_policy_normal_value);
            int balancePolicy = mContext.getResources().getInteger(R.integer.inv_policy_balance_value);
            int quickPolicy = mContext.getResources().getInteger(R.integer.inv_policy_quickly_value);

            if (curInvPolicy == normalPolicy)
                invModeName = getString(R.string.inv_policy_normal_label);
            else if (curInvPolicy == balancePolicy)
                invModeName = getString(R.string.inv_policy_balance_label);
            else if (curInvPolicy == quickPolicy)
                invModeName = getString(R.string.inv_policy_quickly_label);
            else
                invModeName = getString(R.string.inv_policy_normal_label);

        } else if (Constant.isUnionCmd(devModelName)) {
            int curInvPolicy = getUHFIntSetting(UHFSilionParams.INV_POLICY.KEY, mContext.getResources().getInteger(R.integer.inv_policy_balance_value));
            switch (curInvPolicy) {
                default:
                case 0://inv_policy_normal_value
                    invModeName = getString(R.string.inv_policy_normal_label);
                    break;
                case 1://inv_policy_mass_value
                    invModeName = getString(R.string.inv_policy_mass_label);
                    break;
                case 2://inv_policy_few_value
                    invModeName = getString(R.string.inv_policy_few_label);
                    break;
            }
        }

        invModeName = invModeName == null ? "" : invModeName;
        tv_inv_mode_label.setText(invModeName);

        //Device model name
        TextView header_model_name = (TextView) findViewById(R.id.header_model_name);

        header_model_name.setText("[ " + Constant.reName(devModelName) + "-" + UHFSDK.VERSION.VERSION_NAME + " ]");
    }

    private void focusTab(int id) {
        tv_inventory.setTextColor(Color.WHITE);
        tv_settings.setTextColor(Color.WHITE);
        tv_tags.setTextColor(Color.WHITE);
        tv_industry.setTextColor(Color.WHITE);
        tv_common_cmd.setTextColor(Color.WHITE);
        switch (id) {
            case R.id.tv_inventory:
                tv_inventory.setTextColor(getResources().getColor(R.color.dark_blue));
                break;
            case R.id.tv_settings:
                tv_settings.setTextColor(getResources().getColor(R.color.dark_blue));
                break;
            case R.id.tv_tags:
                tv_tags.setTextColor(getResources().getColor(R.color.dark_blue));
                break;
            case R.id.tv_industry:
                tv_industry.setTextColor(getResources().getColor(R.color.dark_blue));
                break;
            case R.id.tv_common_cmd:
                tv_common_cmd.setTextColor(getResources().getColor(R.color.dark_blue));
                break;
        }
    }

    private void showInventoryFragment() {
        if (mInventoryFragment == null)
            mInventoryFragment = new InventoryFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.body, mInventoryFragment);
        transaction.commit();
        im_actionbar_settings.setVisibility(View.VISIBLE);
        imRecycle.setVisibility(View.VISIBLE);
        tv_inv_mode_label.setVisibility(View.VISIBLE);
        focusTab(R.id.tv_inventory);
        mCurFragment = mInventoryFragment;

        updateActionBarInfo();
    }

    private void showSettingsFragment() {
        if (mSettingsFragment == null)
            mSettingsFragment = new UHFSettingsFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.body, mSettingsFragment);
        transaction.commit();
        im_actionbar_settings.setVisibility(View.INVISIBLE);
        imRecycle.setVisibility(View.INVISIBLE);
        tv_inv_mode_label.setVisibility(View.INVISIBLE);
        focusTab(R.id.tv_settings);
        mCurFragment = mSettingsFragment;
    }

    private void showTagsFragment() {
        if (mTagFragment == null)
            mTagFragment = new TagWriteFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.body, mTagFragment);
        transaction.commit();
        im_actionbar_settings.setVisibility(View.INVISIBLE);
        imRecycle.setVisibility(View.INVISIBLE);

        tv_inv_mode_label.setVisibility(View.INVISIBLE);
        focusTab(R.id.tv_tags);
        mCurFragment = mTagFragment;
    }

    private void showIndustryTestFragment() {
        if (mIndustryFragment == null)
            mIndustryFragment = new IndustryTestFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.body, mIndustryFragment);
        transaction.commit();
        im_actionbar_settings.setVisibility(View.INVISIBLE);
        imRecycle.setVisibility(View.INVISIBLE);
        tv_inv_mode_label.setVisibility(View.INVISIBLE);
        focusTab(R.id.tv_industry);
        mCurFragment = mIndustryFragment;
    }

    private void showCommonCmdFragment() {
        if (mCommonCmdFragment == null)
            mCommonCmdFragment = new CommonCmdFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.body, mCommonCmdFragment);
        transaction.commit();
        im_actionbar_settings.setVisibility(View.INVISIBLE);
        imRecycle.setVisibility(View.INVISIBLE);
        tv_inv_mode_label.setVisibility(View.INVISIBLE);
        focusTab(R.id.tv_common_cmd);
        mCurFragment = mCommonCmdFragment;
    }

    private void showInvPolicyPopWindow() {
        if (mUHFMgr.isInInventory()) {
            Toast.makeText(mContext, getString(R.string.stop_inventory_first), Toast.LENGTH_SHORT).show();
            return;
        }

        initInvModePopupView();
        if (mInvPolicyPopupWindow == null) {
            mInvPolicyPopupWindow = new PopupWindow(mInvPolicyContentview, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mInvPolicyPopupWindow.setBackgroundDrawable(mContext.getDrawable(android.R.drawable.spinner_dropdown_background));
            mInvPolicyPopupWindow.setOutsideTouchable(true);
            mInvPolicyPopupWindow.setFocusable(true);
        }

        mInvPolicyPopupWindow.showAsDropDown(im_actionbar_settings, -80, 0);
    }//

    private void initInvModePopupView() {
        if (mInvPolicyContentview == null)
            mInvPolicyContentview = getLayoutInflater().inflate(R.layout.layout_inv_policy, null);
        RadioGroup rg_inv_policy_slr1200 = (RadioGroup) mInvPolicyContentview.findViewById(R.id.rg_inv_policy_slr1200);
        RadioGroup rg_inv_policy_sim7100 = (RadioGroup) mInvPolicyContentview.findViewById(R.id.rg_inv_policy_sim7100);
        RadioGroup rg_inv_policy_urf520 = (RadioGroup) mInvPolicyContentview.findViewById(R.id.rg_inv_policy_urf520);

        String modelName = mUHFMgr.getUHFDeviceModel();
        Log.i(TAG, "initInvModePopupView: " + format("modelName=%s", modelName));
        boolean isUR90 = Constant.isUR90(modelName);
        boolean isBU_x0 = Constant.isBU10_or_BU20(modelName);
        boolean isUR90_V2 = Constant.isUR90_V2(mUHFMgr.getUHFDeviceModel());
        boolean isURM300 = Constant.isURM300(modelName);
        boolean isURF520 = Constant.isURF520(modelName);
        boolean isURM500 = Constant.isURM500(modelName);
        boolean isURF100 = Constant.isURF100(modelName);
        Log.i(TAG, "initInvModePopupView: " + format("isUR90=%s,isBU_x0=%s,isUR90_V2=%s,isURM300=%s,isURF520=%s,isURM500=%s", isUR90, isBU_x0, isUR90_V2, isURM300, isURF520, isURM500));

        rg_inv_policy_slr1200.setVisibility(isUR90 || isBU_x0 ? View.VISIBLE : View.GONE);
        rg_inv_policy_sim7100.setVisibility(isUR90_V2 || isURM300|| isURM500|| isURF520 || isURF100 ? View.VISIBLE : View.GONE);
        rg_inv_policy_urf520.setVisibility(View.GONE);

        if (isUR90 || isBU_x0)
            initPopuViewUR90();//--SLR1200[UR90]/BU10/BU20 part
        else if (isUR90_V2 || isURM300||isURF520 || isURM500 || isURF100)
            initPopuViewUR90_V2();//--SIM7100[UR90_V2.0] part

    }

    private void initPopuViewUR90() {
        //--SLR1200[UR90] part
        RadioGroup rg_inv_policy_slr1200 = (RadioGroup) mInvPolicyContentview.findViewById(R.id.rg_inv_policy_slr1200);
        int iQuickMode = getUHFIntSetting(UHFSilionParams.INV_QUICK_MODE.KEY, 0);
        int[] iGenSessions = getUHFIntArraySetting(UHFSilionParams.POTL_GEN2_SESSION.KEY);
        iGenSessions = iGenSessions == null ? new int[]{-1} : iGenSessions;
        boolean q1enable1200 = (iQuickMode == 1 && iGenSessions[0] > 0);
        boolean q0enable1200 = (iQuickMode == 1 && iGenSessions[0] == 0);
        if (q1enable1200)
            rg_inv_policy_slr1200.check(R.id.rb_item_inv_quickly_s1);
        else if (q0enable1200)
            rg_inv_policy_slr1200.check(R.id.rb_item_inv_quickly_s0);
        else
            rg_inv_policy_slr1200.check(R.id.rb_item_inv_quickly_normal);

        rg_inv_policy_slr1200.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectInvPolicy(checkedId);
                mInvPolicyPopupWindow.dismiss();
            }
        });
    }

    private void initPopuViewUR90_V2() {
        //--SIM7100[UR90_V2.0] part
        RadioGroup rg_inv_policy_sim7100 = (RadioGroup) mInvPolicyContentview.findViewById(R.id.rg_inv_policy_sim7100);
        RadioButton rb_item_inv_balance = (RadioButton) mInvPolicyContentview.findViewById(R.id.rb_item_inv_balance);
        RadioButton rb_item_inv_quickly = (RadioButton) mInvPolicyContentview.findViewById(R.id.rb_item_inv_quickly);
        if(Constant.isURF100(mUHFMgr.getUHFDeviceModel()))//URF100: only normal-mode
        {
            rb_item_inv_balance.setVisibility(View.GONE);
            rb_item_inv_quickly.setVisibility(View.GONE);
        }

        int curInvPolicy = getUHFIntSetting(UHFSilionParams.INV_POLICY.KEY, mContext.getResources().getInteger(R.integer.inv_policy_balance_value));
        int normalPolicy = mContext.getResources().getInteger(R.integer.inv_policy_normal_value);
        int balancePolicy = mContext.getResources().getInteger(R.integer.inv_policy_balance_value);
        int quickPolicy = mContext.getResources().getInteger(R.integer.inv_policy_quickly_value);

        if (curInvPolicy == normalPolicy)
            rg_inv_policy_sim7100.check(R.id.rb_item_inv_normal);
        else if (curInvPolicy == balancePolicy)
            rg_inv_policy_sim7100.check(R.id.rb_item_inv_balance);
        else if (curInvPolicy == quickPolicy)
            rg_inv_policy_sim7100.check(R.id.rb_item_inv_quickly);
        else
            rg_inv_policy_sim7100.check(R.id.rb_item_inv_normal);

        rg_inv_policy_sim7100.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectInvPolicy(checkedId);
                mInvPolicyPopupWindow.dismiss();
            }
        });

    }//End initPopuViewSIM7100

    /**
     * urf 520 support normal mass few inv policy which are not the same as ur90v2 or urm300
     */
    private void initPopuViewURF520() {
        //--SIM7100[UR90_V2.0] part
        RadioGroup rg_inv_policy_urf520 = (RadioGroup) mInvPolicyContentview.findViewById(R.id.rg_inv_policy_urf520);
        int curInvPolicy = getUHFIntSetting(UHFSilionParams.INV_POLICY.KEY, mContext.getResources().getInteger(R.integer.inv_policy_normal_value));
        int normalPolicy = mContext.getResources().getInteger(R.integer.inv_policy_normal_value);
        int massPolicy = mContext.getResources().getInteger(R.integer.inv_policy_mass_value);
        int fewPolicy = mContext.getResources().getInteger(R.integer.inv_policy_few_value);


        if (curInvPolicy == normalPolicy)
            rg_inv_policy_urf520.check(R.id.rb_item_inv_urf520_normal);
        else if (curInvPolicy == massPolicy)
            rg_inv_policy_urf520.check(R.id.rb_item_inv_urf520_mass);
        else if (curInvPolicy == fewPolicy)
            rg_inv_policy_urf520.check(R.id.rb_item_inv_urf520_few);
        else
            rg_inv_policy_urf520.check(R.id.rb_item_inv_urf520_normal);

        rg_inv_policy_urf520.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectInvPolicy(checkedId);
                mInvPolicyPopupWindow.dismiss();
            }
        });
    }


    //Select inventory strategy
    private void selectInvPolicy(int vId) {
        UHFReader.READER_STATE er = UHFReader.READER_STATE.CMD_FAILED_ERR;
        //--SLR1200[UR90] part
        if (vId == R.id.rb_item_inv_quickly_normal ||
                vId == R.id.rb_item_inv_quickly_s1 ||
                vId == R.id.rb_item_inv_quickly_s0) {
            boolean enableQuickMode = (vId == R.id.rb_item_inv_quickly_s1 || vId == R.id.rb_item_inv_quickly_s0);
            er = mUHFMgr.setParam(UHFSilionParams.INV_QUICK_MODE.KEY, UHFSilionParams.INV_QUICK_MODE.PARAM_INV_QUICK_MODE, enableQuickMode ? "1" : "0");
            if (enableQuickMode && er == UHFReader.READER_STATE.OK_ERR) {
                String session = vId == R.id.rb_item_inv_quickly_s1 ? "1" : "0";
                er = mUHFMgr.setParam(UHFSilionParams.POTL_GEN2_SESSION.KEY, UHFSilionParams.POTL_GEN2_SESSION.PARAM_POTL_GEN2_SESSION, session);
            }
            if (er != UHFReader.READER_STATE.OK_ERR)
                Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
        }

        //--SIM7100[UR90_V2.0] part
        if (vId == R.id.rb_item_inv_normal ||
                vId == R.id.rb_item_inv_balance ||
                vId == R.id.rb_item_inv_quickly) {
            int iValue = mContext.getResources().getInteger(R.integer.inv_policy_balance_value);
            if (vId == R.id.rb_item_inv_normal)
                iValue = mContext.getResources().getInteger(R.integer.inv_policy_normal_value);
            if (vId == R.id.rb_item_inv_quickly)
                iValue = mContext.getResources().getInteger(R.integer.inv_policy_quickly_value);
            er = mUHFMgr.setParam(UHFSilionParams.INV_POLICY.KEY, UHFSilionParams.INV_POLICY.PARAM_INV_POLICY, String.valueOf(iValue));
            if (er != UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext, getString(R.string.setting_fail) + ",err: " + er.name(), Toast.LENGTH_SHORT).show();
            }
        }
        //--[URF520] part
        if (vId == R.id.rb_item_inv_urf520_normal ||
                vId == R.id.rb_item_inv_urf520_few ||
                vId == R.id.rb_item_inv_urf520_mass) {
            int iValue = mContext.getResources().getInteger(R.integer.inv_policy_mass_value);
            if (vId == R.id.rb_item_inv_urf520_normal) {
                iValue = mContext.getResources().getInteger(R.integer.inv_policy_normal_value);

            }
            if (vId == R.id.rb_item_inv_urf520_few)
                iValue = mContext.getResources().getInteger(R.integer.inv_policy_few_value);
            Log.i(TAG, "selectInvPolicy: " + format("inv value : %d ", iValue));
            er = mUHFMgr.setParam(UHFSilionParams.INV_POLICY.KEY, UHFSilionParams.INV_POLICY.PARAM_INV_POLICY, String.valueOf(iValue));
            if (er != UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext, getString(R.string.setting_fail) + ",err: " + er.name(), Toast.LENGTH_SHORT).show();
            }
        }

        //Update action bar's information
        if (er == UHFReader.READER_STATE.OK_ERR)
            updateActionBarInfo();

        //Notify inventory to update view state
        if (mCurFragment instanceof InventoryFragment)
            ((InventoryFragment) mCurFragment).updateViewData();
    }

    protected void registerUHFStateReceiver() {
        IntentFilter iFilter = new IntentFilter(UHFManager.ACTOIN_UHF_STATE_CHANGE);
        mContext.registerReceiver(mUHFStateReceiver, iFilter);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryStateReceiver, batteryFilter);

        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenStateReceiver, screenFilter);

    }

    protected void unRegisterUHFStateReceiver() {
        try {
            mContext.unregisterReceiver(mUHFStateReceiver);
            mContext.unregisterReceiver(mBatteryStateReceiver);
            mContext.unregisterReceiver(mScreenStateReceiver);
        } catch (Exception e) {
        }
    }

    private void doPowerOn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UHFReader.READER_STATE er = mUHFMgr.powerOn();
                if (er != UHFReader.READER_STATE.OK_ERR)
                    mUIHandler.sendEmptyMessage(MSG_SHOW_RELOAD_WINDOW);
            }
        }).start();
    }

    protected void showLoadingWindow() {
        if (mPaused)
            return;

        if (mReLoadDialog != null)
            mReLoadDialog.dismiss();

        if (mLoadingPD != null && mLoadingPD.isShowing())
            return;
        else if (mLoadingPD != null)
            mLoadingPD.dismiss();

        mLoadingPD = new ProgressDialog(MainActivity.this);
        mLoadingPD.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingPD.setCancelable(true);
        mLoadingPD.setCanceledOnTouchOutside(false);
        mLoadingPD.setMessage(getString(R.string.power_oning));
        mLoadingPD.show();
    }

    //Reload module information
    private void reConnect() {
        synchronized (this) {
            if (mPaused)
                return;

            //mUHFMgr.asyncConnect("/dev/ttyS0:921600",0,null,null);
            //mUHFMgr.asyncConnect("C1:EC:5B:37:3F:44",0,null,null);

            Log.i(TAG, "reConnect mDevPathOrMac : " + mDevPathOrMac);
            mUHFMgr.asyncConnect(
                    mDevPathOrMac,
                    SOCKET_DEV_ACTIVITY.equals(getCallingActivity() == null ? "" : getCallingActivity().getClassName()) ? PLUGIN_TYPE_NETWORK : 0,
                    mDeviceModelUserSelected,
                    null);
            showLoadingWindow();
        }
    }

    //Module not exists , show window
    private void showReloadModuleWindow() {
        synchronized (this) {

            if (mLoadingPD != null)
                mLoadingPD.dismiss();

            if (mReLoadDialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.common_tip);
                builder.setMessage(getString(R.string.uhf_module_unavailable));
                builder.setPositiveButton(R.string.search_again, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mUIHandler.sendEmptyMessageDelayed(MSG_LOAD_MODULE, 50);
                    }
                }).setNegativeButton(R.string.common_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

                mReLoadDialog = builder.create();
                mReLoadDialog.setCanceledOnTouchOutside(false);
            }

            if (!mPaused)
                mReLoadDialog.show();
            else {
                mReLoadDialog.dismiss();
                mReLoadDialog = null;
            }

            Log.d("TAG", "----show Reload-dialog window.");
        }
    }


    //Power oning
    protected void onUhfPowerOning() {
        Log.d("TAG", "---onUhfPowerOning()---");
        showLoadingWindow();
        if (mCurFragment != null)
            mCurFragment.onUhfPowerOning();
    }

    /**
     * Power on complete
     */
    protected void onUhfPowerOn() {
        Log.d("TAG", "---onUhfPowerOn()---");
        if (mLoadingPD != null)
            mLoadingPD.dismiss();

        if (mReLoadDialog != null)
            mReLoadDialog.dismiss();

        if (mCurFragment != null)
            mCurFragment.onUhfPowerOn();
        else {
            showInventoryFragment();
        }

        updateActionBarInfo();

        String modelName = mUHFMgr.getUHFDeviceModel();
        if (Constant.isBU10_or_BU20(modelName)) //BU10/BU20 not support tag writing
            tv_tags.setVisibility(View.GONE);
        else
            tv_tags.setVisibility(View.VISIBLE);
        initViewURM300();
//        initViewUnionCmd();// move to dev mode page
    }

    /**
     * Power off complete
     */
    protected void onUhfPowerOff() {
        Log.d("TAG", "---onUhfPowerOff()---");
        if (mCurFragment != null)
            mCurFragment.onUhfPowerOff();

        mUIHandler.sendEmptyMessage(MSG_SHOW_RELOAD_WINDOW);
    }

    /**
     * It's starting inventory
     */
    public void onUhfStartInventory() {
        if (mCurFragment != null)
            mCurFragment.onUhfStartInventory();
    }

    /**
     * It's stoping inventory
     */
    public void onUhfStopInventory() {
        if (mCurFragment != null)
            mCurFragment.onUhfStopInventory();
    }

    private int getUHFIntSetting(String key, int defaultValue) {
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        int result = defaultValue;
        if (settingsMap != null && settingsMap.get(key) != null) {
            result = (Integer) settingsMap.get(key);
            Log.d("ITAG", "Get int setting: " + key + ", value: " + result);
        }
        return result;
    }

    private int[] getUHFIntArraySetting(String key) {
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        int[] result = null;
        if (settingsMap != null && settingsMap.get(key) != null)
            result = (int[]) settingsMap.get(key);
        return result;
    }

    //---------------------------------------------------------
    // Inner Class
    //---------------------------------------------------------
    //RFID module state change(eg: Power on,Power off)
    private BroadcastReceiver mUHFStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null)
                return;
            if (UHFManager.ACTOIN_UHF_STATE_CHANGE.equals(intent.getAction())) {
                int uhf_state = intent.getIntExtra(UHFManager.EXTRA_UHF_STATE, -1);
                switch (uhf_state) {
                    case UHFSilionParams.UHF_STATE_CONNECTING:
                        onUhfPowerOning();
                        break;
                    case UHFSilionParams.UHF_STATE_CONNECTED:
                        onUhfPowerOn();
                        break;
                    case UHFSilionParams.UHF_STATE_DISCONNECTED:
                        onUhfPowerOff();
                        break;
                    case UHFSilionParams.UHF_STATE_START_INVENTORY:
                        onUhfStartInventory();
                        break;
                    case UHFSilionParams.UHF_STATE_STOP_INVENTORY:
                        onUhfStopInventory();
                        break;
                    case UHFSilionParams.UHF_STATE_CONNECT_FAIL:
                        onUhfPowerOff();
                        break;
                }
            }
        }
    };


    //Battery capcity change
    private BroadcastReceiver mBatteryStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int btemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                float fbtemperature = (float) btemperature / 10;

                String sHighestTemper = mUHFMgr.getParam(UHFSilionParams.HIGH_TEMPERATURE.KEY,
                        UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_VALUE,
                        String.valueOf(UHFSilionParams.HIGH_TEMPERATURE.DEFAULT_TEMPERATURE_VALUE));

                int highestTemper = 100;
                if (sHighestTemper != null && TextUtils.isDigitsOnly(sHighestTemper))
                    highestTemper = Integer.parseInt(sHighestTemper);

                if (fbtemperature > highestTemper) //Higher than settings most temperature
                {
                    mUIHandler.sendEmptyMessageDelayed(MSG_UPDATE_ACTION_BAR, 500);
                }
            }
        }
    };//End BatteryStateReceiver


    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        private boolean mIsConnectBeforeScreenOff = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.isNewlandPDA()) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    mIsConnectBeforeScreenOff = mUHFMgr.isConnected();
                    Log.d(TAG, "Screen off, mIsConnectBeforeScreenOff: " + mIsConnectBeforeScreenOff);
                    mUHFMgr.disconnect();
                } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.d(TAG, "Screen on, mIsConnectBeforeScreenOff: " + mIsConnectBeforeScreenOff);
                    if (mIsConnectBeforeScreenOff) {
                        reConnect();
                    }
                }
            }
        }

    };//End ScreenStateReceiver

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


}
