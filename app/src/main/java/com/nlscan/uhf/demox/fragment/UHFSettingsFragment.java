package com.nlscan.uhf.demox.fragment;

import static java.lang.String.format;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nlscan.uhf.demox.AppApplication;
import com.nlscan.uhf.demox.activity.DeveloperOptionActivity;
import com.nlscan.uhf.lib.UHFCommonParams;
import com.nlscan.uhf.lib.UHFConstants;
import com.nlscan.uhf.lib.UHFManager;
import com.nlscan.uhf.lib.UHFReader;
import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.util.Constant;
import com.nlscan.uhf.demox.util.constant.UHFSilionParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UHFSettingsFragment extends BaseFragment {

    private static final String TAG = UHFSettingsFragment.class.getSimpleName();

    private View mLayout;

    //---Protocal part---
    private CheckBox cb_6c;
    private CheckBox cb_6b;
    private CheckBox cb_national;

    //---Ant power part---
    private Spinner spinner_read_power_1,
            spinner_read_power_2,
            spinner_read_power_3,
            spinner_read_power_4,
            spinner_read_power_urm;
    private Spinner spinner_write_power_1,
            spinner_write_power_2,
            spinner_write_power_3,
            spinner_write_power_4,
            spinner_write_power_urm;
    private Button btn_get_ant_power, btn_get_ant_power_urm;
    private Button btn_set_ant_power, btn_set_ant_power_urm;
    private ArrayAdapter<String> adapter_ant_power, adapter_ant_power_urm_tx, adapter_ant_power_urm_rx;
    String[] spipow = {"500", "600", "700", "800", "900", "1000", "1100",
            "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900",
            "2000", "2100", "2200", "2300", "2400", "2500", "2600", "2700",
            "2800", "2900", "3000"};

    String[] rxSensitivitysURM = {"-17", "-14", "-11", "-9", "-8", "-6", "-5", "-3", "-2", "0", "1", "3", "4", "6", "7", "9", "10", "13", "16", "19"};
    String[] txOutputLevelsURM = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};

    //---Region part---
    private Spinner spinner_region;
    private Button btn_get_region;
    private Button btn_set_region;
    private ArrayAdapter<String> adapter_regions;

    //---Frequence part---
    private TextView spinner_frequence;
    private Button btn_get_frequence;
    private Button btn_set_frequence;
    private ArrayAdapter<String> adapter_frequence;
    private String[] mFrequences;// All the frequences supported by the module
    private Set<String> mSelectedFrequences = new HashSet<>();// Selected by the user but not yet set in the module

    //---Session part---
    private Spinner spinner_session;
    private Button btn_get_session;
    private Button btn_set_session;
    String[] sessions = {"S0", "S1", "S2", "S3"};
    private ArrayAdapter<String> adapter_session;

    //---Target part---
    private Spinner spinner_target;
    private Button btn_get_target;
    private Button btn_set_target;
    String[] targets = {"A", "B", "A-B", "B-A"};
    private ArrayAdapter<String> adapter_target;

    //---Reader encode part---
    private Spinner spinner_encode;
    private Button btn_get_encode;
    private Button btn_set_encode;
    private ArrayAdapter<String> adapter_tag_encode;
    String[] tagEncodes = {"FM0", "M2", "M4", "M8"};

    //---Q value part---
    private Spinner spinner_q_value;
    private Button btn_get_q_value;
    private Button btn_set_q_value;
    private ArrayAdapter<String> adapter_q_value;
    String[] qValues = {"Auto", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "10", "11", "12", "13", "14", "15"};

    //---Ant checking part---
    private CheckBox cb_ant_check;

    //---Power of low battery level part---
    private CheckBox cb_enable_low_battery_monitor;
    private Spinner spinner_battery_level;
    private Spinner spinner_low_battery_antpower;
    private Button btn_get_low_battery_power;
    private Button btn_set_low_battery_power;
    private List<String> mLowBatterys = Arrays.asList("0", "10", "15", "20", "25", "30", "35", "40", "45", "50", "60");
    private final String DEFUALT_LOW_BATTERY = "20";
    private final String DEFUALT_LOW_BATTERY_POWER = "2000";
    private ArrayAdapter<String> adapter_lower_battery;
    private ArrayAdapter<String> adapter_lower_battery_power;

    //---Low battery warning part---
    private CheckBox cb_enable_battery_warning;
    private Spinner spinner_battery_warning_1;
    private Spinner spinner_battery_warning_2;
    private Button btn_get_battery_monitor;
    private Button btn_set_battery_monitor;
    private TextView tv_power_monitor_tips;
    private List<String> mWarnBatterys_1 = Arrays.asList("50", "45", "40", "35", "30", "25", "20");
    private List<String> mWarnBatterys_2 = Arrays.asList("15", "10", "5");
    private final String DEFUALT_WARN_BATTERY_1 = "20";
    private final String DEFUALT_WARN_BATTERY_2 = "15";
    private ArrayAdapter<String> adapter_warn_battery_1;
    private ArrayAdapter<String> adapter_warn_battery_2;

    //---Inventory policy part---
    private View content_SLR1200;//UR90[MODOULE_SLR1200] setings
    private View content_SIM7100;//UR90_V2.0[MODOULE_SIM7100] settings
    private CheckBox checkbox_q1enable1200,
            checkbox_q2enable1200;
    private Spinner spinner_inv_policy;
    private Button btn_get_inv_policy;
    private Button btn_set_inv_policy;
    private ArrayAdapter<String> adapter_inv_policy;
    private String[] mInvPolicyLabels;
    private int[] mInvPolicyValues;

    //---Extended parameters
    private CheckBox cb_fast_id;

    //---High temperature policy part---
    private CheckBox cb_enable_high_temperature_monitor;
    private Button btn_get_high_temperature_policy;
    private Button btn_set_high_temperature_policy;
    private Spinner spinner_battery_temperature;
    private Spinner spinner_high_temperature_power;
    private Spinner spinner_high_temp_inv_strategy;
    private ArrayAdapter<String> adapter_high_temperature;
    private ArrayAdapter<String> adapter_read_power;
    private ArrayAdapter<String> adapter_high_temp_inv_policy;
    private String[] labels_inv_policy_high_temper;


    //--Special output mode part---
    private Spinner spinner_output_mode;
    private Spinner spinner_special_keys;
    private ArrayAdapter<String> adapter_output_mode;
    private ArrayAdapter<String> adapter_special_key;
    private Button btn_get_output_datas;
    private Button btn_set_output_datas;

    //---Other part---
    private CheckBox cb_trigger_gun;
    private CheckBox cb_inv_prompt_sound;
    private CheckBox cb_inv_prompt_vibrate;
    private Button btnGoToDevMode;

    //---Ant enable/disable part---
    private View content_ant_enable_setting;

    private View ant_power_group_input_1;
    private View ant_power_group_input_2;
    private View ant_power_group_input_3;
    private View ant_power_group_input_4;

    private CheckBox cb_ant_1;
    private CheckBox cb_ant_2;
    private CheckBox cb_ant_3;
    private CheckBox cb_ant_4;

    FrequenceAdapter adapter;
    CheckBox cb_select_all;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.layout_settings_all, null);
        return mainView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLayout = view;
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        updateViewDatas();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initView() {
        initProtocalView();
        initAntPowerView();
        initAntPowerViewURM();
        initRegionView();
        initFrequenceView();
        initSessionView();
        initTargetView();
        initEncodeView();
        initQvalueView();
        initAntCheckView();
        initInventoryPolicyView();
        initExtendParamsView();
        initLowBatteryPowerView();
        initBatteryWarningView();
        initHighTemperatureView();
        initSpecialOutputMode();
        initOtherView();
        //Just hide views which are not supported.
        hideNotSupportViews();

        //Disable all views when it is in inventory;
        enableOrDisableAllViews(!mUHFMgr.isInInventory());
        Log.d(TAG,"----Init views complete.----");
    }

    //Hide not supported views
    private void hideNotSupportViews() {
        //Not support by bu10/bu20 uhf device
        if (Constant.isBU10_or_BU20(mModelName)) {
            View content_ant_check = mLayout.findViewById(R.id.content_ant_check);
            content_ant_check.setVisibility(View.GONE);

            if (Constant.isBU10_or_BU20(mModelName)) {
                View content_extended_params = mLayout.findViewById(R.id.content_extended_params);
                content_extended_params.setVisibility(View.GONE);
            }
        }//

        //Not support on non-newland PDA
        if (true)//(!UHFConstants.isNewlandPDA())
        {
            View content_low_power = mLayout.findViewById(R.id.content_low_power);
            content_low_power.setVisibility(View.GONE);

            View content_battery_monitor = mLayout.findViewById(R.id.content_battery_monitor);
            content_battery_monitor.setVisibility(View.GONE);

            View content_high_temperature = mLayout.findViewById(R.id.content_high_temperature);
            content_high_temperature.setVisibility(View.GONE);

            View content_output_mode = mLayout.findViewById(R.id.content_output_mode);
            content_output_mode.setVisibility(View.GONE);

            cb_trigger_gun.setVisibility(View.GONE);
        }

    }

    /**
     * Init protocal views
     */
    private void initProtocalView() {
        cb_6c = (CheckBox) mLayout.findViewById(R.id.cb_6c);
        cb_6b = (CheckBox) mLayout.findViewById(R.id.cb_6b);
        cb_national = (CheckBox) mLayout.findViewById(R.id.cb_national);
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int id = buttonView.getId();
                switch (id) {
                    case R.id.cb_6c:
                    case R.id.cb_6b:
                    case R.id.cb_national:
                        setProtocalData();
                        break;
                }
            }
        };
        cb_6c.setOnCheckedChangeListener(null);
        cb_6b.setOnCheckedChangeListener(null);
        cb_national.setOnCheckedChangeListener(null);
        updateProtocalData();
        cb_6c.setOnCheckedChangeListener(onCheckedChangeListener);
        cb_6b.setOnCheckedChangeListener(onCheckedChangeListener);
        cb_national.setOnCheckedChangeListener(onCheckedChangeListener);

        //Not support,Disabled
        cb_6c.setEnabled(false);
        cb_6b.setEnabled(false);
        cb_national.setEnabled(false);
    }

    /**
     * Init ant power views
     */
    private void initAntPowerView() {
        //Ant power
        Map<String, Object> settings = mUHFMgr.getAllParams();
        int[] maxpowerArr = settings == null ? null : ((int[]) settings.get(UHFSilionParams.RF_MAXPOWER.KEY));
        int maxpower = (maxpowerArr == null || maxpowerArr.length < 1) ? 3000 : maxpowerArr[0];//set default value if read from config failed
        if (Constant.isUR90_V2(mModelName) && maxpower > 3000) { //UR90_V2.0[SIM7100] support 33db
            spipow = new String[]{"500", "600", "700", "800", "900", "1000", "1100",
                    "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900",
                    "2000", "2100", "2200", "2300", "2400", "2500", "2600", "2700",
                    "2800", "2900", "3000", "3100", "3200", "3300"};
        } else if (Constant.isURF520(mModelName) || Constant.isURM500(mModelName)) { //URF520 support 33db
            spipow = new String[35];//support 34db
            for (int i = 0; i < spipow.length; i++) {
                spipow[i] = i + "";
            }
        }else if(Constant.isURF100(mModelName)){
            spipow = new String[31];//support 30db
            for (int i = 0; i < spipow.length; i++) {
                spipow[i] = i + "";
            }
        }

        ant_power_group_input_1 = mLayout.findViewById(R.id.ant_power_group_input_1);
        ant_power_group_input_2 = mLayout.findViewById(R.id.ant_power_group_input_2);
        ant_power_group_input_3 = mLayout.findViewById(R.id.ant_power_group_input_3);
        ant_power_group_input_4 = mLayout.findViewById(R.id.ant_power_group_input_4);
        //Ant's count
        int antCount = getAntportCount();
        if(antCount <= 1) //Single ant, hide others
        {
            ant_power_group_input_2.setVisibility(View.GONE);
            ant_power_group_input_3.setVisibility(View.GONE);
            ant_power_group_input_4.setVisibility(View.GONE);
        }

        cb_ant_1 = (CheckBox) mLayout.findViewById(R.id.cb_ant_1);
        cb_ant_2 = (CheckBox) mLayout.findViewById(R.id.cb_ant_2);
        cb_ant_3 = (CheckBox) mLayout.findViewById(R.id.cb_ant_3);
        cb_ant_4 = (CheckBox) mLayout.findViewById(R.id.cb_ant_4);

        spinner_read_power_1 = (Spinner) mLayout.findViewById(R.id.spinner_read_power_1);
        spinner_write_power_1 = (Spinner) mLayout.findViewById(R.id.spinner_write_power_1);

        spinner_read_power_2 = (Spinner) mLayout.findViewById(R.id.spinner_read_power_2);
        spinner_write_power_2 = (Spinner) mLayout.findViewById(R.id.spinner_write_power_2);

        spinner_read_power_3 = (Spinner) mLayout.findViewById(R.id.spinner_read_power_3);
        spinner_write_power_3 = (Spinner) mLayout.findViewById(R.id.spinner_write_power_3);

        spinner_read_power_4 = (Spinner) mLayout.findViewById(R.id.spinner_read_power_4);
        spinner_write_power_4 = (Spinner) mLayout.findViewById(R.id.spinner_write_power_4);

        btn_get_ant_power = (Button) mLayout.findViewById(R.id.btn_get_ant_power);
        btn_set_ant_power = (Button) mLayout.findViewById(R.id.btn_set_ant_power);



        adapter_ant_power = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, spipow);
        adapter_ant_power.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_read_power_1.setAdapter(adapter_ant_power);
        spinner_write_power_1.setAdapter(adapter_ant_power);
        spinner_read_power_2.setAdapter(adapter_ant_power);
        spinner_write_power_2.setAdapter(adapter_ant_power);
        spinner_read_power_3.setAdapter(adapter_ant_power);
        spinner_write_power_3.setAdapter(adapter_ant_power);
        spinner_read_power_4.setAdapter(adapter_ant_power);
        spinner_write_power_4.setAdapter(adapter_ant_power);

        btn_get_ant_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAntPowerData();
            }
        });

        btn_set_ant_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAntPowerData();
            }
        });


    }

    private void initAntPowerViewURM() {
        Log.i(TAG, "initAntPowerViewURM: " + format("isurm:%b,mName:%s", Constant.isURM300(mModelName), mModelName));
        if (Constant.isURM300(mModelName)) {
            mLayout.findViewById(R.id.ant_power_group_input_1).setVisibility(View.GONE);
            mLayout.findViewById(R.id.ant_power_group_btn).setVisibility(View.GONE);
            mLayout.findViewById(R.id.ant_power_group_input_urm).setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.ant_power_group_btn_urm).setVisibility(View.VISIBLE);
            //
            spinner_read_power_urm = (Spinner) mLayout.findViewById(R.id.spinner_read_power_urm);
            spinner_write_power_urm = (Spinner) mLayout.findViewById(R.id.spinner_write_power_urm);
            adapter_ant_power_urm_tx = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, txOutputLevelsURM);
            adapter_ant_power_urm_rx = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, rxSensitivitysURM);
            spinner_read_power_urm.setAdapter(adapter_ant_power_urm_rx);
            spinner_write_power_urm.setAdapter(adapter_ant_power_urm_tx);
            //

            btn_get_ant_power_urm = (Button) mLayout.findViewById(R.id.btn_get_ant_power_urm);
            btn_set_ant_power_urm = (Button) mLayout.findViewById(R.id.btn_set_ant_power_urm);
            btn_get_ant_power_urm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateAntPowerDataURM();
//                    updateAntPowerData();
                }
            });

            btn_set_ant_power_urm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAntPowerDataURM();
                }
            });
        }
    }

    /**
     * Init region views
     */
    private void initRegionView() {
        spinner_region = (Spinner) mLayout.findViewById(R.id.spinner_region);
        btn_get_region = (Button) mLayout.findViewById(R.id.btn_get_region);
        btn_set_region = (Button) mLayout.findViewById(R.id.btn_set_region);

        String[] spireg = Constant.isURM300(mModelName)
                ? mContext.getResources().getStringArray(R.array.region_item_labels_URM300)
                : (Constant.isURF520(mModelName) || Constant.isURM500(mModelName) ? mContext.getResources().getStringArray(R.array.region_item_labels_URF520)
                : mContext.getResources().getStringArray(R.array.region_item_labels));
        adapter_regions = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, spireg);
        adapter_regions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_region.setAdapter(adapter_regions);
        btn_get_region.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRegionData();
            }
        });
        btn_set_region.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRegionData();
                initFrequenceView();
            }
        });
    }

    /**
     * Init region views
     */
    private void initFrequenceView() {
        spinner_frequence = (TextView) mLayout.findViewById(R.id.spinner_frequence);
        btn_get_frequence = (Button) mLayout.findViewById(R.id.btn_get_frequence);
        btn_set_frequence = (Button) mLayout.findViewById(R.id.btn_set_frequence);

        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        Object obj = settingsMap.get(UHFSilionParams.FREQUENCY_HOPTABLE.KEY);
        int[] htb = null;
        if (obj != null) {
            htb = (int[]) obj;
        }

        int[] tablefre = null;
        if (htb != null) {

            tablefre = Sort(htb, htb.length);
            String[] ssf = new String[htb.length];
            for (int i = 0; i < htb.length; i++) {
                ssf[i] = String.valueOf(tablefre[i]);
            }
            mFrequences = ssf;
        }

        /*if(mFrequences != null) {
            adapter_frequence = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mFrequences);
            adapter_frequence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_frequence.setAdapter(adapter_frequence);
        }*/

        spinner_frequence.setText(mFrequences == null ? "" : mFrequences[0]);
        spinner_frequence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFrequencePopupWindow();
            }
        });

        btn_get_frequence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFrequenceData();
            }
        });
        btn_set_frequence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFrequenceData();
            }
        });
    }

    /**
     * Init session views
     */
    private void initSessionView() {
        spinner_session = (Spinner) mLayout.findViewById(R.id.spinner_session);
        btn_get_session = (Button) mLayout.findViewById(R.id.btn_get_session);
        btn_set_session = (Button) mLayout.findViewById(R.id.btn_set_session);

        adapter_session = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, sessions);
        adapter_session.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_session.setAdapter(adapter_session);
        btn_get_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSessionData();
            }
        });
        btn_set_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSessionData();
            }
        });
    }

    /**
     * Init target views
     */
    private void initTargetView() {
        spinner_target = (Spinner) mLayout.findViewById(R.id.spinner_target);
        btn_get_target = (Button) mLayout.findViewById(R.id.btn_get_target);
        btn_set_target = (Button) mLayout.findViewById(R.id.btn_set_target);

        adapter_target = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, targets);
        adapter_target.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_target.setAdapter(adapter_target);
        btn_get_target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTargetData();
            }
        });
        btn_set_target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTargetData();
            }
        });

    }

    /**
     * Init reader encode views
     */
    private void initEncodeView() {
        spinner_encode = (Spinner) mLayout.findViewById(R.id.spinner_encode);
        btn_get_encode = (Button) mLayout.findViewById(R.id.btn_get_encode);
        btn_set_encode = (Button) mLayout.findViewById(R.id.btn_set_encode);

        if (Constant.isUR90_V2(mModelName)) {
            tagEncodes = new String[]{"FM0", "M2", "M4", "M8", "PROF0",
                    "PROF1",
                    "PROF2",
                    "PROF3",
                    "PROF4",
                    "PROF5",
                    "RFM_1",
                    "RFM_3",
                    "RFM_5",
                    "RFM_7",
                    "RFM_11",
                    "RFM_12",
                    "RFM_13",
                    "RFM_15",

            };
        } else if (Constant.isURM300(mModelName)) {
            tagEncodes = new String[]{
                    "PROF1",
                    "PROF2",
                    "PROF3",
                    "PROF4",
                    "RFM_0"
            };
        } else if (Constant.isURF520(mModelName) || Constant.isURM500(mModelName)) {
            tagEncodes = new String[]{
                    "MODE_103",
                    "MODE_302",
                    "MODE_120",
                    "MODE_323",
                    "MODE_202",
                    "MODE_345",
                    "MODE_344",
                    "MODE_223",
                    "MODE_222",
                    "MODE_241",
                    "MODE_244",
                    "MODE_285"
            };
        }else if (Constant.isURF100(mModelName)) {
            tagEncodes = new String[]{
                    "MODE_223",
                    "MODE_222",
                    "MODE_241",
                    "MODE_244",
                    "MODE_285"
            };
        }
        adapter_tag_encode = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, tagEncodes);
        adapter_tag_encode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_encode.setAdapter(adapter_tag_encode);
        btn_get_encode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Constant.isURF100(mModelName)) {
                    updateEncodeDataURF100();
                }if (Constant.isURF520(mModelName) || Constant.isURM500(mModelName)) {
                    updateEncodeDataURF520();
                } else {
                    updateEncodeData();
                }
            }
        });
        btn_set_encode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Constant.isURF100(mModelName)) {
                    setEncodeDataURF100();
                }else if (Constant.isURF520(mModelName) || Constant.isURM500(mModelName)) {
                    setEncodeDataURF520();
                } else {
                    setEncodeData();
                }
            }
        });
    }

    /**
     * Init Q value views
     */
    private void initQvalueView() {
        spinner_q_value = (Spinner) mLayout.findViewById(R.id.spinner_q_value);
        btn_get_q_value = (Button) mLayout.findViewById(R.id.btn_get_q_value);
        btn_set_q_value = (Button) mLayout.findViewById(R.id.btn_set_q_value);

        adapter_q_value = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, qValues);
        adapter_q_value.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_q_value.setAdapter(adapter_q_value);
        btn_get_q_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateQvalueData();
            }
        });
        btn_set_q_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setQvalueData();
            }
        });
    }

    /**
     * Init Ant-checking views
     */
    private void initAntCheckView() {

        if (cb_ant_check == null)
            cb_ant_check = (CheckBox) mLayout.findViewById(R.id.cb_ant_check);

        cb_ant_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAntCheck();
            }
        });


    }

    /**
     * Init power on low battery level views
     */
    private void initLowBatteryPowerView() {
        cb_enable_low_battery_monitor = (CheckBox) mLayout.findViewById(R.id.cb_enable_low_battery_monitor);
        spinner_battery_level = (Spinner) mLayout.findViewById(R.id.spinner_battery_level);
        spinner_low_battery_antpower = (Spinner) mLayout.findViewById(R.id.spinner_low_battery_power);
        btn_get_low_battery_power = (Button) mLayout.findViewById(R.id.btn_get_low_battery_power);
        btn_set_low_battery_power = (Button) mLayout.findViewById(R.id.btn_set_low_battery_power);

        adapter_lower_battery = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mLowBatterys);
        adapter_lower_battery.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_battery_level.setAdapter(adapter_lower_battery);

        adapter_lower_battery_power = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, spipow);
        adapter_lower_battery_power.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_low_battery_antpower.setAdapter(adapter_lower_battery_power);


        btn_get_low_battery_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePowerOnLowBatteryData();
            }
        });
        btn_set_low_battery_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPowerOnLowBatteryData();
            }
        });

    }

    /**
     * Init battery listener views
     */
    private void initBatteryWarningView() {
        cb_enable_battery_warning = (CheckBox) mLayout.findViewById(R.id.cb_enable_battery_warning);
        spinner_battery_warning_1 = (Spinner) mLayout.findViewById(R.id.spinner_battery_warning_1);
        spinner_battery_warning_2 = (Spinner) mLayout.findViewById(R.id.spinner_battery_warning_2);
        btn_get_battery_monitor = (Button) mLayout.findViewById(R.id.btn_get_battery_monitor);
        btn_set_battery_monitor = (Button) mLayout.findViewById(R.id.btn_set_battery_monitor);
        tv_power_monitor_tips = (TextView) mLayout.findViewById(R.id.tv_power_monitor_tips);

        adapter_warn_battery_1 = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mWarnBatterys_1);
        adapter_warn_battery_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_battery_warning_1.setAdapter(adapter_warn_battery_1);

        adapter_warn_battery_2 = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mWarnBatterys_2);
        adapter_warn_battery_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_battery_warning_2.setAdapter(adapter_warn_battery_2);

        btn_get_battery_monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBatteryWarningData();
            }
        });
        btn_set_battery_monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBatteryWarningData();
            }
        });

    }

    /**
     * Init inventory policy views
     */
    private void initInventoryPolicyView() {
        //Device different,show difference settings
        //SLR1200: UR90
        //SIM7100: UR90_V2.0
        content_SLR1200 = mLayout.findViewById(R.id.content_SLR1200);
        content_SIM7100 = mLayout.findViewById(R.id.content_SIM7100);
        content_SLR1200.setVisibility(Constant.isUR90(mModelName) || Constant.isBU10_or_BU20(mModelName) ? View.VISIBLE : View.GONE);

        content_SIM7100.setVisibility(Constant.isUR90_V2(mModelName) || Constant.isUnionCmd(mModelName) ? View.VISIBLE : View.GONE);

        if (Constant.isUR90(mModelName) || Constant.isBU10_or_BU20(mModelName))//UR90/BU10/BU20
            initInventoryPolicyUR90View();
        else
            initInventoryPolicyUR90_V2View();

    }

    private void initInventoryPolicyUR90View() {
        checkbox_q1enable1200 = (CheckBox) mLayout.findViewById(R.id.checkbox_q1enable1200);
        checkbox_q2enable1200 = (CheckBox) mLayout.findViewById(R.id.checkbox_q2enable1200);
    }

    private void initInventoryPolicyUR90_V2View() {
        spinner_inv_policy = (Spinner) mLayout.findViewById(R.id.spinner_inv_policy);
        btn_get_inv_policy = (Button) mLayout.findViewById(R.id.btn_get_inv_policy);
        btn_set_inv_policy = (Button) mLayout.findViewById(R.id.btn_set_inv_policy);

        mInvPolicyLabels = mContext.getResources().getStringArray(R.array.inv_policy_labels);
        mInvPolicyValues = mContext.getResources().getIntArray(R.array.inv_policy_values);
        if(Constant.isUnionCmd(mModelName)){
            mInvPolicyLabels = new String[]{
                    mContext.getString(R.string.inv_policy_normal_label),
                    mContext.getString(R.string.inv_policy_mass_label),
                    mContext.getString(R.string.inv_policy_few_label),
            };
            mInvPolicyValues = new int[]{
                    mContext.getResources().getInteger(R.integer.inv_policy_normal_value),
                    mContext.getResources().getInteger(R.integer.inv_policy_mass_value),
                    mContext.getResources().getInteger(R.integer.inv_policy_few_value),
            };
        }
        if(Constant.isURF100(mModelName)){
            mInvPolicyLabels = new String[]{
                    mContext.getString(R.string.inv_policy_normal_label)
            };
            mInvPolicyValues = new int[]{
                    mContext.getResources().getInteger(R.integer.inv_policy_normal_value)
            };
        }

        adapter_inv_policy = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mInvPolicyLabels);
        adapter_inv_policy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_inv_policy.setAdapter(adapter_inv_policy);

        btn_get_inv_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInventoryPolicyData();
            }
        });

        btn_set_inv_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInventoryPolicyData();
            }
        });
    }

    /**
     * Init extended parameters
     */
    private void initExtendParamsView() {

        cb_fast_id = (CheckBox) mLayout.findViewById(R.id.cb_fast_id);
        cb_fast_id.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setFastIdData();
            }
        });

        if (Constant.isURM300(mModelName)) {
            View viewById = mLayout.findViewById(R.id.content_extended_params);
            viewById.setVisibility(View.GONE);
        }
    }

    private void initHighTemperatureView() {
        cb_enable_high_temperature_monitor = (CheckBox) mLayout.findViewById(R.id.cb_enable_high_temperature_monitor);
        btn_get_high_temperature_policy = (Button) mLayout.findViewById(R.id.btn_get_high_temperature_policy);
        btn_set_high_temperature_policy = (Button) mLayout.findViewById(R.id.btn_set_high_temperature_policy);
        spinner_battery_temperature = (Spinner) mLayout.findViewById(R.id.spinner_battery_temperature);
        spinner_high_temperature_power = (Spinner) mLayout.findViewById(R.id.spinner_high_temperature_power);
        spinner_high_temp_inv_strategy = (Spinner) mLayout.findViewById(R.id.spinner_high_temp_inv_strategy);

        //Temperature spinner datas
        String[] labels_temperature = mContext.getResources().getStringArray(R.array.high_temperature_labels);
        adapter_high_temperature = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, labels_temperature);
        adapter_high_temperature.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_battery_temperature.setAdapter(adapter_high_temperature);

        //Read power spinner datas
        Map<String, Object> settings = mUHFMgr.getAllParams();
        int[] maxpowerArr = (int[]) settings.get(UHFSilionParams.RF_MAXPOWER.KEY);
        int maxpower = maxpowerArr[0];
        if (Constant.isUR90_V2(mModelName) && maxpower > 3000) { //UR90_V2.0[SIM7100] support 33db
            spipow = new String[]{"500", "600", "700", "800", "900", "1000", "1100",
                    "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900",
                    "2000", "2100", "2200", "2300", "2400", "2500", "2600", "2700",
                    "2800", "2900", "3000", "3100", "3200", "3300"};
        }

        String[] labels_read_power = spipow;
        adapter_read_power = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, labels_read_power);
        adapter_read_power.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_high_temperature_power.setAdapter(adapter_read_power);

        //Inventory strategy spinner datas
        //UR90
        labels_inv_policy_high_temper = new String[]{mContext.getString(R.string.uhf_inv_mode_normal),
                mContext.getString(R.string.start_quick_mode_s1),
                mContext.getString(R.string.start_quick_mode_s0)};
        //UR90/BU10/BU20
        if (Constant.isUR90(mModelName) || Constant.isBU10_or_BU20(mModelName)) {
            labels_inv_policy_high_temper = new String[]{mContext.getString(R.string.uhf_inv_mode_normal),
                    mContext.getString(R.string.start_quick_mode_s1),
                    mContext.getString(R.string.start_quick_mode_s0)};
        } else {//UR90_V2.0
            labels_inv_policy_high_temper = mContext.getResources().getStringArray(R.array.inv_policy_labels);
        }
        adapter_high_temp_inv_policy = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, labels_inv_policy_high_temper);
        adapter_high_temp_inv_policy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_high_temp_inv_strategy.setAdapter(adapter_high_temp_inv_policy);

        btn_get_high_temperature_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateHighTemperatureData();
            }
        });

        btn_set_high_temperature_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHightTemperatureDatas();
            }
        });
    }

    /**
     * Init special output mode
     */
    private void initSpecialOutputMode() {
        spinner_output_mode = (Spinner) mLayout.findViewById(R.id.spinner_output_mode);
        spinner_special_keys = (Spinner) mLayout.findViewById(R.id.spinner_special_keys);
        btn_get_output_datas = (Button) mLayout.findViewById(R.id.btn_get_output_datas);
        btn_set_output_datas = (Button) mLayout.findViewById(R.id.btn_set_output_datas);

        String[] labels_output_mode = mContext.getResources().getStringArray(R.array.labels_output_mode);
        adapter_output_mode = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, labels_output_mode);
        adapter_output_mode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_output_mode.setAdapter(adapter_output_mode);

        String[] labels_special_key = mContext.getResources().getStringArray(R.array.labels_special_key);
        adapter_special_key = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, labels_special_key);
        adapter_special_key.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_special_keys.setAdapter(adapter_special_key);

        btn_get_output_datas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOutputModeDatas();
            }
        });

        btn_set_output_datas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOutputModeDatas();
            }
        });

    }

    /**
     * Init other views
     */
    private void initOtherView() {
        cb_trigger_gun = (CheckBox) mLayout.findViewById(R.id.cb_trigger_gun);
        cb_inv_prompt_sound = (CheckBox) mLayout.findViewById(R.id.cb_inv_prompt_sound);
        cb_inv_prompt_vibrate = (CheckBox) mLayout.findViewById(R.id.cb_inv_prompt_vibrate);

        btnGoToDevMode = (Button) mLayout.findViewById(R.id.btn_dev_mode);
        if (Constant.isURM500(mModelName)) {
            btnGoToDevMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DeveloperOptionActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            btnGoToDevMode.setVisibility(View.GONE);
        }

    }

    /**
     * Update all views's data
     */
    private void updateViewDatas() {
        Log.d(TAG, "---Start updateViewDatas---");
        //updateProtocalData();
        initProtocalView();
        updateAntPowerData();
        updateAntPowerDataURM();
        updateRegionData();
        updateFrequenceData();
        updateSessionData();
        updateTargetData();
        if (Constant.isURF520(mModelName) || Constant.isURM500(mModelName)) {
            updateEncodeDataURF520();
        } else if(Constant.isURF100(mModelName)){
            updateEncodeDataURF100();
        } else
            updateEncodeData();
        updateQvalueData();
        updateAntCheck();
        updateInventoryPolicyData();
        updatePowerOnLowBatteryData();
        updateBatteryWarningData();
        updateHighTemperatureData();
        updateOutputModeDatas();
        updateFastIdData();
        updateOtherData();
        Log.d(TAG,"----Update view's data complete.----");
    }

    private void enableOrDisableAllViews(boolean enable) {
        List<View> viewList = findAllViews(mLayout);
        for (View child : viewList) {
            child.setEnabled(enable);
        }

        //Not support,Disabled
        cb_6c.setEnabled(false);
        cb_6b.setEnabled(false);
        cb_national.setEnabled(false);
    }

    private List<View> findAllViews(View target) {
        List<View> viewList = new ArrayList<>();
        if (!(target instanceof ViewGroup))
            viewList.add(target);
        else {
            ViewGroup viewGroup = (ViewGroup) target;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = viewGroup.getChildAt(i);
                List<View> childViewList = findAllViews(child);
                viewList.addAll(childViewList);
            }
        }

        return viewList;
    }

    /**
     * Update protocal datas
     */
    private void updateProtocalData() {
        cb_6c.setChecked(true);
    }

    /**
     * Set protocal datas
     */
    private void setProtocalData() {

    }

    /**
     * Update ant power datas
     */
    private void updateAntPowerData() {
        try {

            //Ant power, JSONArray string like: [{"antid":1,"readPower":2600,"writePower":2700},...]
            Map<String, Object> settingsMap = mUHFMgr.getAllParams();
            String sValue = (String) settingsMap.get(UHFSilionParams.RF_ANTPOWER.KEY);
            if (sValue != null) {
                Log.d(TAG, "Ant power svalue: " + sValue + "");
                JSONArray jsArray = new JSONArray(sValue);
                int len = jsArray.length();

                if (len > 0) { //Ant id = 1
                    JSONObject jobj_1 = jsArray.optJSONObject(0);
                    if(jobj_1 != null)
                    {
                        int antid = jobj_1.optInt("antid");//Ant id
                        short readPower = (short) jobj_1.optInt("readPower");//read power
                        short writePower = (short) jobj_1.optInt("writePower");//write power
                        boolean antEnable = jobj_1.optBoolean("enable");
                        int readIndex = readPower >= 100 ? (readPower - 500) / 100 : readPower;
                        int writeIndex = writePower >= 100 ? (writePower - 500) / 100 : writePower;
                        spinner_read_power_1.setSelection(readIndex);
                        spinner_write_power_1.setSelection(writeIndex);
                        cb_ant_1.setChecked(antEnable);
                    }
                }

                if (len > 1) {//Ant id = 2
                    JSONObject jobj_2 = jsArray.optJSONObject(1);
                    if(jobj_2 != null)
                    {
                        int antid = jobj_2.optInt("antid");//Ant id
                        short readPower = (short) jobj_2.optInt("readPower");//read power
                        short writePower = (short) jobj_2.optInt("writePower");//write power
                        boolean antEnable = jobj_2.optBoolean("enable");
                        int readIndex = readPower >= 100 ? (readPower - 500) / 100 : readPower;
                        int writeIndex = writePower >= 100 ? (writePower - 500) / 100 : writePower;
                        spinner_read_power_2.setSelection(readIndex);
                        spinner_write_power_2.setSelection(writeIndex);
                        cb_ant_2.setChecked(antEnable);
                    }
                }

                if (len > 2) {//Ant id = 3
                    JSONObject jobj_3 = jsArray.optJSONObject(2);
                    if(jobj_3 != null)
                    {
                        int antid = jobj_3.optInt("antid");//Ant id
                        short readPower = (short) jobj_3.optInt("readPower");//read power
                        short writePower = (short) jobj_3.optInt("writePower");//write power
                        int readIndex = readPower >= 100 ? (readPower - 500) / 100 : readPower;
                        int writeIndex = writePower >= 100 ? (writePower - 500) / 100 : writePower;
                        boolean antEnable = jobj_3.optBoolean("enable");
                        spinner_read_power_3.setSelection(readIndex);
                        spinner_write_power_3.setSelection(writeIndex);
                        cb_ant_3.setChecked(antEnable);
                    }
                }

                if (len > 3) {//Ant id = 4
                    JSONObject jobj_4 = jsArray.optJSONObject(3);
                    if(jobj_4 != null)
                    {
                        int antid = jobj_4.optInt("antid");//Ant id
                        short readPower = (short) jobj_4.optInt("readPower");//read power
                        short writePower = (short) jobj_4.optInt("writePower");//write power
                        int readIndex = readPower >= 100 ? (readPower - 500) / 100 : readPower;
                        int writeIndex = writePower >= 100 ? (writePower - 500) / 100 : writePower;
                        boolean antEnable = jobj_4.optBoolean("enable");
                        spinner_read_power_4.setSelection(readIndex);
                        spinner_write_power_4.setSelection(writeIndex);
                        cb_ant_4.setChecked(antEnable);
                    }
                }

            } else
                Toast.makeText(mContext, "AntPowerData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                            "Exception:" + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Update ant power datas
     */
    private void updateAntPowerDataURM() {
        if (Constant.isURM300(mModelName)) {
            try {
                //Ant power, JSONArray string like: [{"antid":1,"readPower":2600,"writePower":2700},...]
                Map<String, Object> settingsMap = mUHFMgr.getAllParams();
                String sValue = (String) settingsMap.get(UHFSilionParams.RF_ANTPOWER.KEY);
                if (sValue != null) {
                    JSONArray jsArray = new JSONArray(sValue);
                    int len = jsArray.length();
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            JSONObject jobj = jsArray.optJSONObject(i);
                            int antid = jobj.optInt("antid");//天线ID
                            short readPower = (short) jobj.optInt("readPower");//读功率
                            short writePower = (short) jobj.optInt("writePower");//写功率
                            Log.i(TAG, "updateAntPowerDataURM: " + format("rP:%d,wP:%d", readPower, writePower));

                            if (i == 0) {
//                            spinner_read_power.setSelection(0);
//                            spinner_write_power.setSelection(0);

                                for (int j = 0; j < rxSensitivitysURM.length; j++) {
                                    Log.i(TAG, "updateAntPowerDataURM: " + format("rP:%d,rPs:%s,bool:%b", readPower, Short.parseShort(rxSensitivitysURM[j]), readPower == (Short.parseShort(rxSensitivitysURM[j]))));
                                    if (readPower == (Short.parseShort(rxSensitivitysURM[j]))) {
                                        spinner_read_power_urm.setSelection(j);
                                    }
                                }

                                for (int k = 0; k < txOutputLevelsURM.length; k++) {
                                    Log.i(TAG, "updateAntPowerDataURM: " + format("wP:%d,wPs:%s", writePower, Short.parseShort(txOutputLevelsURM[k])));
                                    if (writePower == (Short.parseShort(txOutputLevelsURM[k]))) {
                                        spinner_write_power_urm.setSelection(k);
                                    }
                                }
                            }
                        }
                    }

                } else
                    Toast.makeText(mContext, "AntPowerData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext,
                                "Exception:" + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Set ant power datas
     */
    private void setAntPowerData() {
        int antportc = getAntportCount();
        Log.d(TAG,"--setAntPowerData, ant ports:="+antportc);
        JSONArray jsItemArray = new JSONArray();
        try{
            boolean isAllAntDisable = !cb_ant_1.isChecked() && !cb_ant_2.isChecked() && !cb_ant_3.isChecked() && !cb_ant_4.isChecked();

            int antid = 1;
            int rIndex = spinner_read_power_1.getSelectedItemPosition();
            int wIndex = spinner_write_power_1.getSelectedItemPosition();
            int rPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * rIndex : rIndex;
            int wPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * wIndex : wIndex;
            JSONObject jsItem_1 = new JSONObject();
            jsItem_1.put("antid", antid);
            jsItem_1.put("readPower", rPower);
            jsItem_1.put("writePower", wPower);
            jsItem_1.put("enable",isAllAntDisable || antportc <= 1 ? true : cb_ant_1.isChecked()); //if disable all,ant 1 cannot be disabled.
            jsItemArray.put(jsItem_1);

            if(antportc > 1)
            {
                antid = 2;
                rIndex = spinner_read_power_2.getSelectedItemPosition();
                wIndex = spinner_write_power_2.getSelectedItemPosition();
                rPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * rIndex : rIndex;
                wPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * wIndex : wIndex;
                JSONObject jsItem_2 = new JSONObject();
                jsItem_2.put("antid", antid);
                jsItem_2.put("readPower", rPower);
                jsItem_2.put("writePower", wPower);
                jsItem_2.put("enable",cb_ant_2.isChecked());
                jsItemArray.put(jsItem_2);
            }

            if(antportc > 2)
            {
                antid = 3;
                rIndex = spinner_read_power_3.getSelectedItemPosition();
                wIndex = spinner_write_power_3.getSelectedItemPosition();
                rPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * rIndex : rIndex;
                wPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * wIndex : wIndex;
                JSONObject jsItem_3 = new JSONObject();
                jsItem_3.put("antid", antid);
                jsItem_3.put("readPower", rPower);
                jsItem_3.put("writePower", wPower);
                jsItem_3.put("enable",cb_ant_3.isChecked());
                jsItemArray.put(jsItem_3);
            }

            if(antportc > 3)
            {
                antid = 4;
                rIndex = spinner_read_power_4.getSelectedItemPosition();
                wIndex = spinner_write_power_4.getSelectedItemPosition();
                rPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * rIndex : rIndex;
                wPower = Constant.isUR90(mModelName) || Constant.isUR90_V2(mModelName) ? 500 + 100 * wIndex : wIndex;
                JSONObject jsItem_4 = new JSONObject();
                jsItem_4.put("antid", antid);
                jsItem_4.put("readPower", rPower);
                jsItem_4.put("writePower", wPower);
                jsItem_4.put("enable",cb_ant_4.isChecked());
                jsItemArray.put(jsItem_4);
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        Log.i(TAG, "setAntPowerData: " + format("json:= %s", jsItemArray.toString()));
        UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.RF_ANTPOWER.KEY, UHFSilionParams.RF_ANTPOWER.PARAM_RF_ANTPOWER, jsItemArray.toString());

        if (er == UHFReader.READER_STATE.OK_ERR) {
            Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
        updateAntPowerData();
    }

    /**
     * Set ant power datas
     */
    private void setAntPowerDataURM() {
        Log.i(TAG, "setAntPowerDataURM: ");
        int antportc = getAntportCount();
        int[] rpower = new int[antportc];
        int[] wpower = new int[antportc];
        for (int i = 0; i < antportc; i++) {
            rpower[i] = Integer.parseInt(rxSensitivitysURM[spinner_read_power_urm.getSelectedItemPosition()]);
            wpower[i] = Integer.parseInt(txOutputLevelsURM[spinner_write_power_urm.getSelectedItemPosition()]);
        }

        try {
            JSONArray jsItemArray = new JSONArray();
            for (int i = 0; i < antportc; i++) {
                int antid = i + 1;
                int readPower = (short) (rpower[i]);
                int writePower = (short) (wpower[i]);
                JSONObject jsItem = new JSONObject();
                jsItem.put("antid", antid);
                jsItem.put("readPower", readPower);
                jsItem.put("writePower", writePower);
                jsItemArray.put(jsItem);
            }
            Log.i(TAG, "setAntPowerDataURM: " + format("json:%s", jsItemArray.toString()));
            UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.RF_ANTPOWER.KEY, UHFSilionParams.RF_ANTPOWER.PARAM_RF_ANTPOWER, jsItemArray.toString());

            if (er == UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                    "Exception:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * Update region datas
     */
    private void updateRegionData() {
        spinner_region.setSelection(-1);
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        Object obj = settingsMap.get(UHFSilionParams.FREQUENCY_REGION.KEY);
        int region = -1;
        if (obj != null) {
            region = (Integer) obj;
        }

        if (region != -1) {
            UHFSilionParams.Region_Conf regionEnum = UHFSilionParams.Region_Conf.valueOf(region);

            String[] regions = Constant.isURM300(mModelName)
                    ? mContext.getResources().getStringArray(R.array.region_item_labels_URM300)
                    : (Constant.isURF520(mModelName) || Constant.isURM500(mModelName) ? mContext.getResources().getStringArray(R.array.region_item_labels_URF520)
                    : mContext.getResources().getStringArray(R.array.region_item_labels));


            List<String> listRegion = Arrays.asList(regions);
            int index = 0;
            switch (regionEnum) {
                case RG_NA: //North america
                    index = listRegion.indexOf(mContext.getString(R.string.region_item_north_america_label));
                    break;
                case RG_EU3: //Europe3
                    index = listRegion.indexOf(mContext.getString(R.string.region_item_euro3_label));
                    break;
                case RG_PRC: //China
                    index = listRegion.indexOf(mContext.getString(R.string.region_item_china_label));
                    break;
                case RG_PRC2: //China２
                    index = listRegion.indexOf(mContext.getString(R.string.region_item_china2_label));
                    break;
                case RG_IN: //India
                    index = listRegion.indexOf(mContext.getString(R.string.region_item_india_label));
                    break;
                case RG_KR: //Korea
                    index = listRegion.indexOf(mContext.getString(R.string.region_item_korea_label));
                    break;
                case RG_OPEN://All
                    index = listRegion.indexOf(mContext.getString(R.string.region_item_all_label));
                    break;
            }
            spinner_region.setSelection(index);

            if (settingsMap.containsKey(UHFSilionParams.REGION_CERTIFICATION.KEY)) {
                int regionCert = (int) settingsMap.get(UHFSilionParams.REGION_CERTIFICATION.KEY);
                UHFSilionParams.Region_Conf regionc = UHFSilionParams.Region_Conf.valueOf(regionCert);
                Log.d("TAG", "Region certification: " + regionc.name());
                if (regionc != UHFSilionParams.Region_Conf.RG_PRC && regionc != UHFSilionParams.Region_Conf.RG_PRC2)
                    spinner_region.setEnabled(false);
            }

        } else
            Toast.makeText(mContext, "RegionData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();

    }

    /**
     * Set region datas
     */
    private void setRegionData() {
        UHFSilionParams.Region_Conf rre;


        String[] regions = Constant.isURM300(mModelName)
                ? mContext.getResources().getStringArray(R.array.region_item_labels_URM300)
                : (Constant.isURF520(mModelName) || Constant.isURM500(mModelName) ? mContext.getResources().getStringArray(R.array.region_item_labels_URF520)
                : mContext.getResources().getStringArray(R.array.region_item_labels));

        List<String> listRegion = Arrays.asList(regions);
        final int china = listRegion.indexOf(mContext.getString(R.string.region_item_china_label));
        final int china2 = listRegion.indexOf(mContext.getString(R.string.region_item_china2_label));
        final int northAmerica = listRegion.indexOf(mContext.getString(R.string.region_item_north_america_label));
        final int europe3 = listRegion.indexOf(mContext.getString(R.string.region_item_euro3_label));
        final int india = listRegion.indexOf(mContext.getString(R.string.region_item_india_label));
        final int korea = listRegion.indexOf(mContext.getString(R.string.region_item_korea_label));
        final int all = listRegion.indexOf(mContext.getString(R.string.region_item_all_label));

        int index = spinner_region.getSelectedItemPosition();
        if (index == china) {
            rre = UHFSilionParams.Region_Conf.RG_PRC;//China
        } else if (index == china2) {
            rre = UHFSilionParams.Region_Conf.RG_PRC2;//China２
        } else if (index == northAmerica) {
            rre = UHFSilionParams.Region_Conf.RG_NA;//North america
        } else if (index == europe3) {
            rre = UHFSilionParams.Region_Conf.RG_EU3;//Europe3
        } else if (index == india) {
            rre = UHFSilionParams.Region_Conf.RG_IN;//India
        } else if (index == korea) {
            rre = UHFSilionParams.Region_Conf.RG_KR;//Korea
        } else if (index == all) {
            rre = UHFSilionParams.Region_Conf.RG_OPEN;//All
        } else
            rre = UHFSilionParams.Region_Conf.RG_NONE;

        if (rre == UHFSilionParams.Region_Conf.RG_NONE) {
            Toast.makeText(mContext, R.string.unsupport_region, Toast.LENGTH_SHORT).show();
            return;
        }

        int iRegion = rre.value();
        Log.i(TAG, "setRegionData: " + format("iRegion : %d", iRegion));
        UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.FREQUENCY_REGION.KEY, UHFSilionParams.FREQUENCY_REGION.PARAM_FREQUENCY_REGION, String.valueOf(iRegion));
        if (er == UHFReader.READER_STATE.OK_ERR) {
            updateFrequenceData();
            Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Update frequence datas
     */
    private void updateFrequenceData() {
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        Object obj = settingsMap.get(UHFSilionParams.FREQUENCY_HOPTABLE.KEY);
        Log.d(TAG, "Frequence hotable: " + obj);
        int[] htb = null;
        if (obj != null) {
            htb = (int[]) obj;
        }

        int[] tablefre = null;
        if (htb != null) {

            mSelectedFrequences.clear();

            tablefre = Sort(htb, htb.length);
            String[] ssf = new String[htb.length];
            for (int i = 0; i < htb.length; i++) {
                ssf[i] = String.valueOf(tablefre[i]);
            }
            mFrequences = ssf;
            spinner_frequence.setText(mFrequences == null ? "" : mFrequences[0]);
            //adapter_frequence.notifyDataSetChanged();
        } else
            Toast.makeText(mContext, "FrequenceData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();
    }

    private void showFrequencePopupWindow() {
        View freqListView = LayoutInflater.from(mContext).inflate(R.layout.layout_frequence_list, null);
        cb_select_all = (CheckBox) freqListView.findViewById(R.id.cb_select_all);
        Button btn_confirm = (Button) freqListView.findViewById(R.id.btn_confirm);
        ListView lv_frequnce_list = (ListView) freqListView.findViewById(R.id.lv_frequnce_list);
        if (mSelectedFrequences == null)
            mSelectedFrequences = new HashSet<>();
        if (mSelectedFrequences.size() == mFrequences.length)
            cb_select_all.setChecked(true);

        Log.i(TAG, "showFrequencePopupWindow: " + (mSelectedFrequences == null ? 0 : mSelectedFrequences.size()));
        adapter = new FrequenceAdapter(mContext, mFrequences, mSelectedFrequences);
        lv_frequnce_list.setAdapter(adapter);
        lv_frequnce_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: ");
                String freq = mFrequences[position];

                if (mSelectedFrequences.contains(freq))
                    mSelectedFrequences.remove(freq);
                else
                    mSelectedFrequences.add(freq);
                adapter.notifyDataSetChanged();
                if (mSelectedFrequences.size() >= mFrequences.length)
                    cb_select_all.setChecked(true);
                else
                    cb_select_all.setChecked(false);
            }
        });

        cb_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "cb_select_all onClick: ");
                //cb_select_all.setChecked(!cb_select_all.isChecked());
                boolean isChecked = cb_select_all.isChecked();
                mSelectedFrequences.clear();
                if (isChecked)
                    mSelectedFrequences.addAll(Arrays.asList(mFrequences));
                adapter.notifyDataSetChanged();
            }
        });

        int spinner_view_width = spinner_frequence.getMeasuredWidth();
        final PopupWindow popup = new PopupWindow(freqListView, spinner_view_width + (spinner_view_width / 2), ViewGroup.LayoutParams.WRAP_CONTENT);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        popup.setContentView(freqListView);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.showAsDropDown(spinner_frequence, -80, 0);
    }

    /**
     * Set frequence datas
     */
    private void setFrequenceData() {
        Log.i(TAG, "setFrequenceData: " + format("mSelectedFrequences size :%s", mSelectedFrequences == null ? "null" : mSelectedFrequences.size()));
        if (mSelectedFrequences != null && mSelectedFrequences.size() > 0) {
            String[] sFreqArr = new String[mSelectedFrequences.size()];
            mSelectedFrequences.toArray(sFreqArr);
            int[] vls = new int[sFreqArr.length];
            for (int i = 0; i < sFreqArr.length; i++) {
                String sFreq = sFreqArr[i];
                vls[i] = Integer.parseInt(sFreq);
            }

            int[] htb = vls;
            String sValue = converToString(htb);
            UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.FREQUENCY_HOPTABLE.KEY, UHFSilionParams.FREQUENCY_HOPTABLE.PARAM_HTB, sValue);
            if (er == UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
                updateFrequenceData();
            } else
                Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update session datas
     */
    private void updateSessionData() {
        try {

            Map<String, Object> settingsMap = mUHFMgr.getAllParams();
            Object obj = settingsMap.get(UHFSilionParams.POTL_GEN2_SESSION.KEY);
            int[] val2 = obj == null ? new int[]{-1} : (int[]) obj;

            if (val2 != null && val2.length > 0) {
                if (val2[0] < spinner_session.getCount())
                    spinner_session.setSelection(val2[0]);
            } else
                Toast.makeText(mContext, "SessionData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Exception:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set session datas
     */
    private void setSessionData() {
        try {
            int[] val = new int[]{-1};
            val[0] = spinner_session.getSelectedItemPosition();

            UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.POTL_GEN2_SESSION.KEY, UHFSilionParams.POTL_GEN2_SESSION.PARAM_POTL_GEN2_SESSION, String.valueOf(val[0]));

            if (er == UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Exception:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * Update target datas
     */
    private void updateTargetData() {
        try {

            Map<String, Object> settingsMap = mUHFMgr.getAllParams();
            Object obj = settingsMap.get(UHFSilionParams.POTL_GEN2_TARGET.KEY);
            int[] val = (int[]) obj;

            if (val != null && val.length > 0) {
                if (val[0] < spinner_target.getCount())
                    spinner_target.setSelection(val[0]);
            } else
                Toast.makeText(mContext, "TargetData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(mContext,
                            "Exception:" + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    /**
     * Set target datas
     */
    private void setTargetData() {
        int[] val = new int[]{spinner_target.getSelectedItemPosition()};

        String sValue = converToString(val);
        UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.POTL_GEN2_TARGET.KEY, UHFSilionParams.POTL_GEN2_TARGET.PARAM_POTL_GEN2_TARGET, sValue);

        if (er == UHFReader.READER_STATE.OK_ERR) {
            Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Update encode datas
     */
    private void updateEncodeData() {
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        Object obj = settingsMap.get(UHFSilionParams.POTL_GEN2_TAGENCODING.KEY);
        int[] val = (int[]) obj;

        if (val != null && val.length > 0) {

            if (val[0] <= 3)
                spinner_encode.setSelection(val[0]);
            else if (val[0] > 100) {
                if (val[0] == 101)
                    spinner_encode.setSelection(10);
                if (val[0] == 103)
                    spinner_encode.setSelection(11);
                if (val[0] == 105)
                    spinner_encode.setSelection(12);
                if (val[0] == 107)
                    spinner_encode.setSelection(13);
                if (val[0] == 111)
                    spinner_encode.setSelection(14);
                if (val[0] == 112)
                    spinner_encode.setSelection(15);
                if (val[0] == 113)
                    spinner_encode.setSelection(16);
                if (val[0] == 115)
                    spinner_encode.setSelection(17);
            } else
                spinner_encode.setSelection(4 + val[0] - 0x10);

        } else
            Toast.makeText(mContext, "EncodeData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();
    }

    /**
     * Update encode datas
     */
    private void updateEncodeDataURF520() {
        if (Constant.isURF520(mModelName) || Constant.isURM500(mModelName)) {
            Map<String, Object> settingsMap = mUHFMgr.getAllParams();
            Object obj = settingsMap.get(UHFSilionParams.POTL_GEN2_TAGENCODING.KEY);
            int[] val = (int[]) obj;

            if (val != null && val.length > 0) {
                int i = val[0];
                List<String> strings = Arrays.asList(tagEncodes);
                int i1 = strings.indexOf(i + "");
                spinner_encode.setSelection(i);
            } else
                Toast.makeText(mContext, "EncodeData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update encode datas
     */
    private void updateEncodeDataURF100() {
        if (Constant.isURF100(mModelName)) {
            Map<String, Object> settingsMap = mUHFMgr.getAllParams();
            Object obj = settingsMap.get(UHFSilionParams.POTL_GEN2_TAGENCODING.KEY);
            int[] val = (int[]) obj;
            Log.d(TAG,"---Get encode data:="+Arrays.toString(val));
            if (val != null && val.length > 0) {
                int i = val[0];
                final int OFFSET=7;
                int index = i - OFFSET;
                index = index < 0 ? 0 : index;
                spinner_encode.setSelection(index);
            } else
                Toast.makeText(mContext, "EncodeData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set encode datas
     */
    private void setEncodeData() {
        int[] val = new int[]{spinner_encode.getSelectedItemPosition()};
        Log.i(TAG, "setEncodeData: " + val[0]);
        if (val[0] > 3 && val[0] <= 9) {
            val[0] = 0x10 + val[0] - 4;
        } else if (val[0] > 9) {
            if (val[0] == 10)
                val[0] = 101;
            else if (val[0] == 11)
                val[0] = 103;
            else if (val[0] == 12)
                val[0] = 105;
            else if (val[0] == 13)
                val[0] = 107;
            else if (val[0] == 14)
                val[0] = 111;
            else if (val[0] == 15)
                val[0] = 112;
            else if (val[0] == 16)
                val[0] = 113;
            else if (val[0] == 17)
                val[0] = 115;
        }
        int[] realVal = {val[0]};
        setToModule(realVal);
    }

    private void setEncodeDataURF520() {
        int[] realVal = new int[]{spinner_encode.getSelectedItemPosition()};
        setToModule(realVal);
    }

    private void setEncodeDataURF100() {
        final int OFFSET = 7;
        int index = spinner_encode.getSelectedItemPosition();
        int value = index + OFFSET;
        int[] realVal = new int[]{value};
        setToModule(realVal);
    }

    private void setToModule(int[] realVal) {
        String sValue = converToString(realVal);
        UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.POTL_GEN2_TAGENCODING.KEY, UHFSilionParams.POTL_GEN2_TAGENCODING.PARAM_POTL_GEN2_TAGENCODING, sValue);
        if (er == UHFReader.READER_STATE.OK_ERR) {
            Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Update Q-value datas
     */
    private void updateQvalueData() {
        try {
            Map<String, Object> settingsMap = mUHFMgr.getAllParams();
            Object obj = settingsMap.get(UHFSilionParams.POTL_GEN2_Q.KEY);
            int[] val = (int[]) obj;

            if (val != null && val.length > 0) {
                spinner_q_value.setSelection(val[0] + 1);
            } else
                Toast.makeText(mContext, "QvalueData: " + getString(R.string.no_data), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                            "Exception:" + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Set Q-value datas
     */
    private void setQvalueData() {
        try {
            int[] val = new int[]{-1};
            val[0] = spinner_q_value.getSelectedItemPosition() - 1;
            String sValue = converToString(val);
            UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.POTL_GEN2_Q.KEY, UHFSilionParams.POTL_GEN2_Q.PARAM_POTL_GEN2_Q, sValue);
            if (er == UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                            "Exception:" + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
    }

    /**
     * Update ant check datas
     */
    private void updateAntCheck() {
        try {
            int[] val2 = new int[]{-1};
            Map<String, Object> settingsMap = mUHFMgr.getAllParams();
            val2 = (int[]) settingsMap.get(UHFSilionParams.READER_IS_CHK_ANT.KEY);

            if (val2 != null) {
                cb_ant_check.setOnCheckedChangeListener(null);
                cb_ant_check.setChecked(val2[0] == 0 ? false : true);
                cb_ant_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setAntCheck();
                    }
                });
            }

        } catch (Exception e) {
            Toast.makeText(mContext,
                    "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set ant check datas
     */
    private void setAntCheck() {
        try {

            UHFReader.READER_STATE er;
            er = mUHFMgr.setParam(UHFSilionParams.READER_IS_CHK_ANT.KEY, UHFSilionParams.READER_IS_CHK_ANT.PARAM_READER_IS_CHK_ANT, cb_ant_check.isChecked() ? String.valueOf(1) : String.valueOf(0));
            if (er != UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext,
                                getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT)
                        .show();
                updateAntCheck();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                            "Exception:" + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
    }

    /**
     * Update ant power on lowe battery level datas
     */
    private void updatePowerOnLowBatteryData() {
        Map<String, Object> settings = mUHFMgr.getAllParams();
        int iLowerpowerEnable = settings.containsKey(UHFSilionParams.LOWER_POWER.PARAM_LOWER_POWER_DM_ENABLE) ? (Integer) settings.get(UHFSilionParams.LOWER_POWER.PARAM_LOWER_POWER_DM_ENABLE) : 0;
        cb_enable_low_battery_monitor.setChecked(iLowerpowerEnable == 1);

        Object obj = settings.get(UHFSilionParams.LOWER_POWER.PARAM_LOWER_POWER_LEVEL);
        int batterLevel = 15;
        if (obj != null)
            batterLevel = (Integer) obj;

        String sbatterLevel = String.valueOf(batterLevel);
        int index = mLowBatterys.indexOf(sbatterLevel);
        spinner_battery_level.setSelection(index);

        obj = settings.get(UHFSilionParams.LOWER_POWER.PARAM_LOWER_POWER_READ_DBM);
        int antpower = 2700;
        if (obj != null)
            antpower = (Integer) obj;

        spinner_low_battery_antpower.setSelection((antpower - 500) / 100);
    }

    /**
     * Set ant power on lowe battery level datas
     */
    private void setPowerOnLowBatteryData() {

        int iLowerpowerEnable = cb_enable_low_battery_monitor.isChecked() ? 1 : 0;
        String slevel = mLowBatterys.get(spinner_battery_level.getSelectedItemPosition());
        int level = Integer.parseInt(slevel);
        String sPower = spipow[spinner_low_battery_antpower.getSelectedItemPosition()];

        try {
            UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.LOWER_POWER.KEY, UHFSilionParams.LOWER_POWER.PARAM_LOWER_POWER_DM_ENABLE, String.valueOf(iLowerpowerEnable));
            if (er == UHFReader.READER_STATE.OK_ERR)
                er = mUHFMgr.setParam(UHFSilionParams.LOWER_POWER.KEY, UHFSilionParams.LOWER_POWER.PARAM_LOWER_POWER_LEVEL, String.valueOf(level));
            if (er == UHFReader.READER_STATE.OK_ERR)
                er = mUHFMgr.setParam(UHFSilionParams.LOWER_POWER.KEY, UHFSilionParams.LOWER_POWER.PARAM_LOWER_POWER_READ_DBM, String.valueOf(sPower));

            if (er == UHFReader.READER_STATE.OK_ERR) {
                Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                    "Exception:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * Update battery compacity listener datas
     */
    private void updateBatteryWarningData() {
        Map<String, Object> settings = mUHFMgr.getAllParams();
        String sWarnVal1 = mUHFMgr.getParam(UHFSilionParams.BATTERY_WARNING.KEY, UHFSilionParams.BATTERY_WARNING.PARAM_BATTERY_WARNING_1, "20");
        String sWarnVal2 = mUHFMgr.getParam(UHFSilionParams.BATTERY_WARNING.KEY, UHFSilionParams.BATTERY_WARNING.PARAM_BATTERY_WARNING_2, "15");
        String sEnable = mUHFMgr.getParam(UHFSilionParams.BATTERY_WARNING.KEY, UHFSilionParams.BATTERY_WARNING.PARAM_BATTERY_WARNING_ENABLE, "1");

        boolean bEnable = "1".equals(sEnable);

        int index_1 = mWarnBatterys_1.indexOf(sWarnVal1);
        int index_2 = mWarnBatterys_2.indexOf(sWarnVal2);
        spinner_battery_warning_1.setSelection(index_1);
        spinner_battery_warning_2.setSelection(index_2);
        cb_enable_battery_warning.setChecked(bEnable);
    }

    /**
     * Set battery compacity listener datas
     */
    private void setBatteryWarningData() {

        boolean ifMonitor = cb_enable_battery_warning.isChecked();
        String sWarnVal1 = mWarnBatterys_1.get(spinner_battery_warning_1.getSelectedItemPosition());
        String sWarnVal2 = mWarnBatterys_2.get(spinner_battery_warning_2.getSelectedItemPosition());

        UHFReader.READER_STATE er = UHFReader.READER_STATE.CMD_FAILED_ERR;
        er = mUHFMgr.setParam(UHFSilionParams.BATTERY_WARNING.KEY, UHFSilionParams.BATTERY_WARNING.PARAM_BATTERY_WARNING_ENABLE, ifMonitor ? "1" : "0");
        if (er == UHFReader.READER_STATE.OK_ERR)
            er = mUHFMgr.setParam(UHFSilionParams.BATTERY_WARNING.KEY, UHFSilionParams.BATTERY_WARNING.PARAM_BATTERY_WARNING_1, sWarnVal1);
        if (er == UHFReader.READER_STATE.OK_ERR)
            er = mUHFMgr.setParam(UHFSilionParams.BATTERY_WARNING.KEY, UHFSilionParams.BATTERY_WARNING.PARAM_BATTERY_WARNING_2, sWarnVal2);

        /*final String BROAD_BATTERY_MONITOR = "com.nlscan.uhf.silion.action.BATTERY_MONITOR";
        final String EXTRA_STRING_MONITOR = "if monitor";
        final String EXTRA_STRING_WARN_ONE = "warn value 1";
        final String EXTRA_STRING_WARN_TWO = "warn value 2";
        Intent batteryIntent = new Intent();
        batteryIntent.setAction(BROAD_BATTERY_MONITOR);
        batteryIntent.putExtra(EXTRA_STRING_MONITOR,ifMonitor);
        batteryIntent.putExtra(EXTRA_STRING_WARN_ONE,warnVal1);
        batteryIntent.putExtra(EXTRA_STRING_WARN_TWO,warnVal2);
        mContext.sendBroadcast(batteryIntent);*/

        if (er == UHFReader.READER_STATE.OK_ERR) {
            String tipStr = "";
            tipStr += ifMonitor ? getString(R.string.tips_para_on) : getString(R.string.tips_para_off);
            tipStr += "\n";
            tipStr += getString(R.string.low_power) + " " + sWarnVal1 + " " + getString(R.string.tips_para_warn1) + "\n";
            tipStr += getString(R.string.low_power) + " " + sWarnVal2 + " " + getString(R.string.tips_para_warn2);
            tv_power_monitor_tips.setText(tipStr);

            Toast.makeText(mContext, R.string.setting_success, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(mContext, R.string.setting_fail, Toast.LENGTH_SHORT).show();

    }

    /**
     * Update inventory compose policy datas
     */
    private void updateInventoryPolicyData() {
        boolean isUR90_V2 = Constant.isUR90_V2(mModelName);
        boolean isUR90 = Constant.isUR90(mModelName);
        boolean isBU_x0 = Constant.isBU10_or_BU20(mModelName);
        if (isUR90 || isBU_x0) {
            updateUR90InventoryPolicyData();
        } else
            updateUR90_V2InventoryPolicyData();

    }

    private void updateUR90InventoryPolicyData() {
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        Object oValue = settingsMap.get(UHFSilionParams.INV_QUICK_MODE.KEY);
        int iQuickMode = oValue == null ? 0 : (Integer) oValue;
        oValue = settingsMap.get(UHFSilionParams.POTL_GEN2_SESSION.KEY);
        int[] iGenSessions = (int[]) oValue;
        iGenSessions = iGenSessions == null ? new int[]{-1} : iGenSessions;
        boolean q1enable1200 = (iQuickMode == 1 && iGenSessions[0] > 0);
        boolean q0enable1200 = (iQuickMode == 1 && iGenSessions[0] == 0);
        checkbox_q1enable1200.setOnCheckedChangeListener(null);
        checkbox_q2enable1200.setOnCheckedChangeListener(null);
        checkbox_q1enable1200.setChecked(q1enable1200);
        checkbox_q2enable1200.setChecked(q0enable1200);
        checkbox_q1enable1200.setEnabled(!q0enable1200);
        checkbox_q2enable1200.setEnabled(!q1enable1200);

        //Enable(max power,S1,interval:0)
        checkbox_q1enable1200.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.INV_QUICK_MODE.KEY, UHFSilionParams.INV_QUICK_MODE.PARAM_INV_QUICK_MODE, isChecked ? "1" : "0");
                if (er == UHFReader.READER_STATE.OK_ERR && isChecked) {
                    er = mUHFMgr.setParam(UHFSilionParams.POTL_GEN2_SESSION.KEY, UHFSilionParams.POTL_GEN2_SESSION.PARAM_POTL_GEN2_SESSION, "1");
                }

                if (er == UHFReader.READER_STATE.OK_ERR) {
                    if (isChecked)
                        checkbox_q2enable1200.setEnabled(false);
                    else
                        checkbox_q2enable1200.setEnabled(true);
                } else
                    Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();


            }
        });


        //Enable(max power,S0,interval:0)
        checkbox_q2enable1200.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.INV_QUICK_MODE.KEY, UHFSilionParams.INV_QUICK_MODE.PARAM_INV_QUICK_MODE, isChecked ? "1" : "0");
                if (er == UHFReader.READER_STATE.OK_ERR && isChecked) {
                    er = mUHFMgr.setParam(UHFSilionParams.POTL_GEN2_SESSION.KEY, UHFSilionParams.POTL_GEN2_SESSION.PARAM_POTL_GEN2_SESSION, "0");
                }

                if (er == UHFReader.READER_STATE.OK_ERR) {
                    if (isChecked)
                        checkbox_q1enable1200.setEnabled(false);
                    else
                        checkbox_q1enable1200.setEnabled(true);
                } else
                    Toast.makeText(mContext, getString(R.string.setting_fail) + " : " + er.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUR90_V2InventoryPolicyData() {
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        int curInvPolicy = mContext.getResources().getInteger(R.integer.inv_policy_normal_value);
        if (settingsMap != null && settingsMap.get(UHFSilionParams.INV_POLICY.KEY) != null) {
            curInvPolicy = (Integer) settingsMap.get(UHFSilionParams.INV_POLICY.KEY);
            Log.d(TAG, "curInvPolicy: " + curInvPolicy);
        }
        int[] allInvPolicy = mInvPolicyValues;
        int invPoclicyIndex = 0;
        for (int i = 0; i < allInvPolicy.length; i++) {
            if (allInvPolicy[i] == curInvPolicy) {
                invPoclicyIndex = i;
                break;
            }
        }
        spinner_inv_policy.setSelection(invPoclicyIndex);
    }

    /**
     * Set inventory compose policy datas
     */
    private void setInventoryPolicyData() {
        boolean slr1200 = Constant.isSLR1200(mModelName);
        if (!slr1200) {
            UHFReader.READER_STATE er = UHFReader.READER_STATE.INVALID_PARA;
            int index = spinner_inv_policy.getSelectedItemPosition();
            int[] policyValues = mInvPolicyValues;
            if (policyValues != null && index >= 0 && index < policyValues.length) {
                int iValue = policyValues[index];
                er = mUHFMgr.setParam(UHFSilionParams.INV_POLICY.KEY, UHFSilionParams.INV_POLICY.PARAM_INV_POLICY, String.valueOf(iValue));
            } else
                er = UHFReader.READER_STATE.INVALID_PARA;
            String msg = er == UHFReader.READER_STATE.OK_ERR ? getString(R.string.setting_success) : getString(R.string.setting_fail) + ",err: " + er.value();
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update fastId data
     */
    private void updateFastIdData() {
        String sValue = mUHFMgr.getParam(UHFSilionParams.FAST_ID.KEY, UHFSilionParams.FAST_ID.PARAM_FAST_ID, "0");
        cb_fast_id.setChecked("1".equals(sValue));
    }

    /**
     * Set fasetId data
     */
    private void setFastIdData() {
        String sValue = cb_fast_id.isChecked() ? "1" : "0";
        UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.FAST_ID.KEY, UHFSilionParams.FAST_ID.PARAM_FAST_ID, sValue);
        String msg = er == UHFReader.READER_STATE.OK_ERR ? getString(R.string.setting_success) : getString(R.string.setting_fail) + ",err: " + er.value();
//        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Update high temperature view's data
     */
    private void updateHighTemperatureData() {
        String sEnable = mUHFMgr.getParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_MONITOR_ENABLE, "1");
        String sTemperature = mUHFMgr.getParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_VALUE, String.valueOf(UHFSilionParams.HIGH_TEMPERATURE.DEFAULT_TEMPERATURE_VALUE));
        String sAntPower = mUHFMgr.getParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_ANT_POWER, String.valueOf(UHFSilionParams.HIGH_TEMPERATURE.DEFAULT_ANT_POWER_VALUE));
        String defaultInv = Constant.isUR90(mModelName) || Constant.isBU10_or_BU20(mModelName) ? UHFSilionParams.HIGH_TEMPERATURE.VALUE_SLR1200_INV_POLICY_NORMAL : String.valueOf(mContext.getResources().getInteger(R.integer.inv_policy_normal_value));
        String sInvPolicy = mUHFMgr.getParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_INV_STRATEGY, defaultInv);//normal

        cb_enable_high_temperature_monitor.setChecked("1".equals(sEnable));
        //Temperature spinner datas
        String[] labels_temperature = mContext.getResources().getStringArray(R.array.high_temperature_labels);
        List<String> itemList = Arrays.asList(labels_temperature);
        int itemIndex = itemList.indexOf(sTemperature);
        spinner_battery_temperature.setSelection(itemIndex);

        //Ant power datas
        String[] labels_read_power = spipow;
        itemList = Arrays.asList(labels_read_power);
        itemIndex = itemList.indexOf(sAntPower);
        spinner_high_temperature_power.setSelection(itemIndex);

        //Inventory strategy spinner datas
        itemIndex = 0;
        Log.i(TAG, format("invPolicy : %s", sInvPolicy));
        if (Constant.isUR90(mModelName) || Constant.isBU10_or_BU20(mModelName)) {//UR90,BU10,BU20
            if (UHFSilionParams.HIGH_TEMPERATURE.VALUE_SLR1200_INV_POLICY_NORMAL.equals(sInvPolicy)) {
                sInvPolicy = mContext.getString(R.string.uhf_inv_mode_normal);
            } else if (UHFSilionParams.HIGH_TEMPERATURE.VALUE_SLR1200_INV_POLICY_QUICK_S1.equals(sInvPolicy)) {
                sInvPolicy = mContext.getString(R.string.start_quick_mode_s1);
            } else if (UHFSilionParams.HIGH_TEMPERATURE.VALUE_SLR1200_INV_POLICY_QUICK_S0.equals(sInvPolicy)) {
                sInvPolicy = mContext.getString(R.string.start_quick_mode_s0);
            }
            itemList = Arrays.asList(labels_inv_policy_high_temper);
            itemIndex = itemList.indexOf(sInvPolicy);
        } else {//UR90_V2.0
            int[] inv_policy_values = mContext.getResources().getIntArray(R.array.inv_policy_values);
            int invPolicy = Integer.parseInt(sInvPolicy);
            for (int i = 0; i < inv_policy_values.length; i++) {
                if (invPolicy == inv_policy_values[i]) {
                    itemIndex = i;
                    break;
                }
            }
        }
        spinner_high_temp_inv_strategy.setSelection(itemIndex);

    }

    private void setHightTemperatureDatas() {
        String sEnable = cb_enable_high_temperature_monitor.isChecked() ? "1" : "0";
        String sTemperature = (String) spinner_battery_temperature.getSelectedItem();
        String[] labels_read_power = spipow;
        String sAntPower = labels_read_power[spinner_high_temperature_power.getSelectedItemPosition()];
        String sInvPolicy = null;
        if (Constant.isUR90(mModelName) || Constant.isBU10_or_BU20(mModelName))//UR90/BU10/BU20
        {
            String select = (String) spinner_high_temp_inv_strategy.getSelectedItem();
            if (mContext.getString(R.string.uhf_inv_mode_normal).equals(select))
                sInvPolicy = UHFSilionParams.HIGH_TEMPERATURE.VALUE_SLR1200_INV_POLICY_NORMAL;
            else if (mContext.getString(R.string.start_quick_mode_s1).equals(select))
                sInvPolicy = UHFSilionParams.HIGH_TEMPERATURE.VALUE_SLR1200_INV_POLICY_QUICK_S1;
            else if (mContext.getString(R.string.start_quick_mode_s0).equals(select))
                sInvPolicy = UHFSilionParams.HIGH_TEMPERATURE.VALUE_SLR1200_INV_POLICY_QUICK_S0;

        } else {
            int[] inv_policy_values = mContext.getResources().getIntArray(R.array.inv_policy_values);
            sInvPolicy = String.valueOf(inv_policy_values[spinner_high_temp_inv_strategy.getSelectedItemPosition()]);
        }

        UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_MONITOR_ENABLE, sEnable);
        if (er == UHFReader.READER_STATE.OK_ERR)
            er = mUHFMgr.setParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_VALUE, sTemperature);
        if (er == UHFReader.READER_STATE.OK_ERR)
            er = mUHFMgr.setParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_ANT_POWER, sAntPower);
        if (er == UHFReader.READER_STATE.OK_ERR && sInvPolicy != null)
            er = mUHFMgr.setParam(UHFSilionParams.HIGH_TEMPERATURE.KEY, UHFSilionParams.HIGH_TEMPERATURE.PARAM_HIGH_TEMPERATURE_INV_STRATEGY, sInvPolicy);
        else
            er = UHFReader.READER_STATE.INVALID_PARA;

        String msg = er == UHFReader.READER_STATE.OK_ERR ? getString(R.string.setting_success) : getString(R.string.setting_fail) + ",err: " + er.value();
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Update output mode datas
     */
    private void updateOutputModeDatas() {
        Map<String, Object> settingsMap = mUHFMgr.getAllParams();
        int outputMode = settingsMap.containsKey(UHFSilionParams.EXTEND_OUTPUT_MODE.PARAM_EXTEND_OUTPUT_MODE) ? (Integer) settingsMap.get(UHFSilionParams.EXTEND_OUTPUT_MODE.PARAM_EXTEND_OUTPUT_MODE) : UHFSilionParams.EXTEND_OUTPUT_MODE.VALUE_OUTPUT_MODE_NONE;
        int keycode = settingsMap.containsKey(UHFSilionParams.OUTPUT_CUSTOM_EMULATE_KEY.KEY) ? (Integer) settingsMap.get(UHFSilionParams.OUTPUT_CUSTOM_EMULATE_KEY.KEY) : UHFSilionParams.OUTPUT_CUSTOM_EMULATE_KEY.VALUE_EMULATE_KEYCODE_NONE;

        int[] outputModeValues = mContext.getResources().getIntArray(R.array.values_output_mode);
        String[] keycodeNames = mContext.getResources().getStringArray(R.array.values_special_key);
        int outputModeIndex = 0;
        for (int i = 0; i < outputModeValues.length; i++) {
            if (outputMode == outputModeValues[i]) {
                outputModeIndex = i;
                break;
            }
        }
        spinner_output_mode.setSelection(outputModeIndex);

        int keycodeIndex = 0;
        for (int i = 0; i < keycodeNames.length; i++) {
            String keycodeName = keycodeNames[i];
            int tKeycode = KeyEvent.keyCodeFromString(keycodeName);
            if (keycode == tKeycode) {
                keycodeIndex = i;
                break;
            }
        }
        spinner_special_keys.setSelection(keycodeIndex);
    }

    private void setOutputModeDatas() {
        int[] outputModeValues = mContext.getResources().getIntArray(R.array.values_output_mode);
        String[] keycodeNames = mContext.getResources().getStringArray(R.array.values_special_key);
        int outputModeIndex = spinner_output_mode.getSelectedItemPosition();
        int keycodeNameIndex = spinner_special_keys.getSelectedItemPosition();

        int outputMode = outputModeValues[outputModeIndex];

        String keyName = keycodeNames[keycodeNameIndex];
        int keycode = KeyEvent.keyCodeFromString(keyName);

        UHFReader.READER_STATE er = mUHFMgr.setParam(UHFSilionParams.EXTEND_OUTPUT_MODE.KEY, UHFSilionParams.EXTEND_OUTPUT_MODE.PARAM_EXTEND_OUTPUT_MODE, String.valueOf(outputMode));
        if (er == UHFReader.READER_STATE.OK_ERR)
            er = mUHFMgr.setParam(UHFSilionParams.OUTPUT_CUSTOM_EMULATE_KEY.KEY, UHFSilionParams.OUTPUT_CUSTOM_EMULATE_KEY.PARAM_OUTPUT_CUSTOM_EMULATE_KEY, String.valueOf(keycode));

        String msg = er == UHFReader.READER_STATE.OK_ERR ? getString(R.string.setting_success) : getString(R.string.setting_fail) + ",err: " + er.value();
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Update other datas
     */
    private void updateOtherData() {


        boolean gunTriggerEnable = false;
        boolean soundEnable = false;
        boolean vibrateEnable = false;

        if (!UHFConstants.isNewlandPDA()) {
            AppApplication app = (AppApplication) getActivity().getApplication();
            soundEnable = app.mSound;
            vibrateEnable = app.mVibrate;
        } else {
            gunTriggerEnable = mUHFMgr.isTriggerOn(UHFCommonParams.TRIGGER_MODE.TRIGGER_MODE_BACK);
            soundEnable = mUHFMgr.isPromptSoundEnable();
            vibrateEnable = mUHFMgr.isPromptVibrateEnable();
        }

        cb_trigger_gun.setOnCheckedChangeListener(null);
        cb_inv_prompt_sound.setOnCheckedChangeListener(null);
        cb_inv_prompt_vibrate.setOnCheckedChangeListener(null);

        cb_trigger_gun.setChecked(gunTriggerEnable);
        cb_inv_prompt_sound.setChecked(soundEnable);
        cb_inv_prompt_vibrate.setChecked(vibrateEnable);

        CompoundButton.OnCheckedChangeListener mCheckChange = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged: " + format("bV:%s,bool:%b", buttonView.getText(), isChecked));
                setOtherData();
            }
        };

        cb_trigger_gun.setOnCheckedChangeListener(mCheckChange);
        cb_inv_prompt_sound.setOnCheckedChangeListener(mCheckChange);
        cb_inv_prompt_vibrate.setOnCheckedChangeListener(mCheckChange);

    }

    /**
     * Set other datas
     */
    private void setOtherData() {

        boolean gunTriggerEnable = cb_trigger_gun.isChecked();
        boolean soundEnable = cb_inv_prompt_sound.isChecked();
        boolean vibrateEnable = cb_inv_prompt_vibrate.isChecked();
        if (!UHFConstants.isNewlandPDA()) {
            AppApplication app = (AppApplication) getActivity().getApplication();
            app.mSound = soundEnable;
            app.mVibrate = vibrateEnable;
        } else {
            boolean suc = mUHFMgr.setTrigger(UHFCommonParams.TRIGGER_MODE.TRIGGER_MODE_BACK, gunTriggerEnable);
            suc = mUHFMgr.setPromptSoundEnable(soundEnable);
            suc = mUHFMgr.setPromptVibrateEnable(vibrateEnable);
        }

    }

    @Override
    public void onUhfPowerOning() {
        super.onUhfPowerOning();
    }

    @Override
    public void onUhfPowerOn() {
        super.onUhfPowerOn();
    }

    @Override
    public void onUhfPowerOff() {
        super.onUhfPowerOff();
    }

    public int[] Sort(int[] array, int len) {
        int tmpIntValue = 0;
        for (int xIndex = 0; xIndex < len; xIndex++) {
            for (int yIndex = 0; yIndex < len; yIndex++) {
                if (array[xIndex] < array[yIndex]) {
                    tmpIntValue = (Integer) array[xIndex];
                    array[xIndex] = array[yIndex];
                    array[yIndex] = tmpIntValue;
                }
            }
        }

        return array;
    }

    @Override
    public void onUhfStartInventory() {
        enableOrDisableAllViews(false);
    }

    @Override
    public void onUhfStopInventory() {
        enableOrDisableAllViews(true);
    }


    /**
     * Get ant count
     *
     * @return
     */
    private int getAntportCount() {
        String sValue = mUHFMgr.getParam(UHFSilionParams.ANTS.KEY,UHFSilionParams.ANTS.PARAM_MAX_ANTS_COUNT,"1");
        if(TextUtils.isEmpty(sValue))
            return 1 ;
        else if(TextUtils.isDigitsOnly(sValue))
            return Integer.parseInt(sValue);
        return 1;
    }

    /**
     * Int[] to "int,int..." String
     *
     * @param intArray
     * @return
     */
    private String converToString(int[] intArray) {
        if (intArray != null && intArray.length > 0) {
            String line = "";
            for (int i = 0; i < intArray.length; i++) {
                line += String.valueOf(intArray[i]);
                if (i < intArray.length - 1)
                    line += ",";
            }
            return line;
        }

        return null;
    }

    //-------------------------------------------------------------
    // Inner class
    //-------------------------------------------------------------
    private class FrequenceAdapter extends BaseAdapter {
        private String[] mListItems;
        private Set<String> mSelectedItems;
        private Context ctx;

        public FrequenceAdapter(Context context, String[] items, Set<String> selectedItems) {
            ctx = context;
            mListItems = items;
            mSelectedItems = selectedItems;
        }

        @Override
        public int getCount() {
            return mListItems == null ? 0 : mListItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mListItems[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            String item = mListItems[position];
            CheckBox cb_select;
            if (convertView == null) {
                convertView = LayoutInflater.from(ctx).inflate(R.layout.layout_list_view_item_frequence, null);
                cb_select = (CheckBox) convertView.findViewById(R.id.cb_select);
                convertView.setTag(cb_select);
            } else
                cb_select = (CheckBox) convertView.getTag();

            boolean isSelected = mSelectedItems.contains(item);
            cb_select.setChecked(isSelected);
            cb_select.setText(item);
            return convertView;
        }
    }//End FrequenceAdapter


}
