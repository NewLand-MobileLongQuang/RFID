package com.nlscan.uhf.demox.util;

import android.text.TextUtils;


import com.nlscan.uhf.lib.UHFManager;
import com.nlscan.uhf.lib.UHFModuleInfo;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zhouyi on 2019/2/26.
 */

public class Constant {
    public static final int UNKOWNERR = -1;
    public static final int NO_UHF = 0; //没有检测到UHF模块
    public static final int HAVE_UHF = 1; //检测到UHF模块
    public static final int UHF_POWERON = 2; //UHF上电成功
    public static final int UHF_POWERON_ERR = 3; //UHF上电失败

    public static final int EPC_INPUT_BY_WRITE_OR_SCAN_ONE = 4;//用户扫描或手动输入一个EPC
    public static final int EPC_INPUT_BY_SCAN_MORE = 5;//用户扫描输入多个EPC

    public static final int GET_UHF_TAG_FROM_BROADCAST = 6;//获得uhf标签

    public static final int WRITE_DATA_TO_XLS_SUCCESS = 7;//数据写入xls成功

    public static final int WRITE_DATA_TO_XLS_FAILED = 8;//数据写入xls失败

    public static final int GET_SCAN_DATA = 9; //获得扫描结果

    public final static String SCAN_RESULT_1 = "scan_result_1";
    public final static String SCAN_RESULT_2 = "scan_result_2";
    public final static String SCAN_BROADTYPE = "scan_board";


    /**
     * 读码结果发送的广播action
     */
    public final static String ACTION_UHF_RESULT_SEND = "nlscan.intent.action.uhf.ACTION_RESULT_EX";
    /**
     * 读码结果发送的广播Extra
     */
    public final static String EXTRA_TAG_INFO = "tag_info";

    /**
     * Extra: parameter of select device's serial path or bt mac
     */
    public final static String EXTRA_DEVICE_PATH_OR_MAC = "path_or_mac";
    public final static String EXTRA_DEVICE_PLUGIN_TYPE = "EXTRA_DEVICE_PLUGIN_TYPE";
    //连接指定型号UHF的参数
    public final static String EXTRA_DEVICE_MODEL_KEY = "EXTRA_DEVICE_MODEL_KEY";

    //UR90[芯联创：MODOULE_SLR1200]
    public final static String NEWLAND_MODULE_NAME_UR90 = "UR90";
    public final static String NEWLAND_MODULE_SERIAL_TYPE = "MODULE_TYPE_SERIAL";
    public final static String NEWLAND_MODULE_NET_TYPE = "MODULE_TYPE_NET";
    //[芯联创：MODOULE_SLR1200]
    public final static String SILION_MODULE_NAME_SLR1200 = "MODOULE_SLR1200";

    //UR90_V2.0[芯联创：MODOULE_SIM7100]
    public final static String NEWLAND_MODULE_NAME_UR90_V2_0 = "UR90_V2.0";
    //[芯联创：MODOULE_SIM7100]
    public final static String SILION_MODULE_NAME_SIM7100 = "MODOULE_SIM7100";

    //Model: BU10/BU20
    public final static String NEWLAND_MODULE_NAME_BU10_OR_BU20 = "BU10_BU20";
    //Model :ST
    public final static String NEWLAND_MODULE_NAME_URM300 = "URM300";
    public final static String NEWLAND_MODULE_NAME_URF520 = "URF520";
    public final static String NEWLAND_MODULE_NAME_URM500 = "URM500";
    public final static String NEWLAND_MODULE_NAME_NLS_URM500 = "NLS-URM500";
    public final static String NEWLAND_MODULE_NAME_URM500_ORIGIN = "URM500";
    public final static String NEWLAND_MODULE_NAME_URF520_SERIAL = "URF520_SERIAL";
    public final static String NEWLAND_MODULE_NAME_URF100 = "URF100";
    //==============================

    //当前模块名称（芯联创的型号）
    public static String mCurSilionModuleName = SILION_MODULE_NAME_SLR1200;

    //是否是UR90_V2.0[芯联创：MODOULE_SIM7100]
    public static boolean isSIM7100(String moduleName) {
        return SILION_MODULE_NAME_SIM7100.equals(moduleName) || NEWLAND_MODULE_NAME_UR90_V2_0.equals(moduleName);
    }

    //是否是UR90[Silion：MODOULE_SLR1200]
    public static boolean isSLR1200(String moduleName) {
        return SILION_MODULE_NAME_SLR1200.equals(moduleName) || NEWLAND_MODULE_NAME_UR90.equals(moduleName);
    }

    //Is it UR90_V2.0[AS：MODOULE_SIM7100]
    public static boolean isUR90_V2(String moduleName) {
        return SILION_MODULE_NAME_SIM7100.equals(moduleName) || NEWLAND_MODULE_NAME_UR90_V2_0.equals(moduleName);
    }

    //Is it UR90[Silion：MODOULE_SLR1200]
    public static boolean isUR90(String moduleName) {
        return SILION_MODULE_NAME_SLR1200.equals(moduleName) || NEWLAND_MODULE_NAME_UR90.equals(moduleName);
    }


    //Is it BU10/BU20
    public static boolean isBU10_or_BU20(String moduleName) {
        if (moduleName == null)
            return false;
        return NEWLAND_MODULE_NAME_BU10_OR_BU20.contains(moduleName);
    }

    public static boolean isURM300(String moduleName) {
        return NEWLAND_MODULE_NAME_URM300.equalsIgnoreCase(moduleName);
    }

    public static boolean isURF520(String moduleName) {
        if (moduleName != null&&moduleName.contains("520")) {
            return true;
        }
        return false;
    }

    public static boolean isURM500(String moduleName) {
        return NEWLAND_MODULE_NAME_URM500.equalsIgnoreCase(moduleName) || NEWLAND_MODULE_NAME_NLS_URM500.equalsIgnoreCase(moduleName);
    }

    public static boolean isURF100(String moduleName){
        return NEWLAND_MODULE_NAME_URF100.equals(moduleName);
    }

    /**
     * Is Newland pda device
     *
     * @return
     */
    public static boolean isNewlandPDA() {
        try {
            Class<?> scanMgrCls = Class.forName("com.nlscan.android.scan.ScanManager");
            return scanMgrCls != null;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isUnionCmd(String moduleName) {
        return isURF520(moduleName) || isURM500(moduleName) || isURF100(moduleName);
    }

    public static int[] stringToIntArray(String sValue, String splitc) {
        if (TextUtils.isEmpty(sValue))
            return null;
        String[] sArray = sValue.split(splitc);
        int[] res = new int[sArray.length];
        for (int i = 0; i < sArray.length; i++) {
            res[i] = Integer.parseInt(sArray[i]);
        }

        return res;
    }

    /**
     * 是否支持选择设备进行连接
     *
     * @return true/false
     */
    public static boolean isSupportSelectDevice() {
        try {
            Class<?> cls = UHFManager.class;
            Method method = cls.getMethod("isConnected");
            return method != null;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * MT95L platform
     *
     * @return
     */
    public static boolean isMT95L() {
        return "MT95L_DROI_SDK30".equals(getSystemProperty("persist.sys.type", ""))
                || "MT95L_MT87XX_Bird_SDK33".equals(getSystemProperty("persist.sys.type", ""))
                || getSystemProperty("persist.sys.type", "").contains("MT95L");
    }

    public static String getSystemProperty(String property, String defvalue) {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method getPropertyMethod_1 = cls.getMethod("get", String.class);
            Method getPropertyMethod_2 = cls.getMethod("get", String.class, String.class);
            if (defvalue == null)
                return (String) getPropertyMethod_1.invoke(cls, property);
            else
                return (String) getPropertyMethod_2.invoke(cls, property, defvalue);
        } catch (Exception e) {
        }

        return defvalue;
    }

    /**
     * load serial path on Newland pda
     */
    public static String getDevPathOnNewlandPDA() {
        if (Constant.isNewlandPDA()) {
            //uhf配置文件（配置的上电路径，串口，型号名字，UHF服务的action等）
            final String settingsFilePath = "uhf/uhf_module_config.xml";
            final String unrecoverableFilePath = "/unrecoverable/"+settingsFilePath;
            final String persistFilePath = "/persist/"+settingsFilePath;
            final String systemFilePath = "/system/usr/"+settingsFilePath;
            final String systemExtFilePath = "/system_ext/usr/"+settingsFilePath;
            final String newlandFilePath = "/newland/usr/"+settingsFilePath;
            File configFile = new File(unrecoverableFilePath); //unrecoverable/uhf/uhf_module_config.xml
            if(!configFile.exists())
                configFile = new File(persistFilePath); //persist/uhf/uhf_module_config.xml

            if(!configFile.exists())
                configFile = new File(systemFilePath); //system/usr/uhf/uhf_module_config.xml

            if(!configFile.exists())
                configFile = new File(systemExtFilePath); //system_ext/usr/uhf/uhf_module_config.xml

            if(!configFile.exists())
                configFile = new File(newlandFilePath); //newland/usr/uhf/uhf_module_config.xml

            String configFilePath = configFile.getAbsolutePath();

            List<UHFModuleInfo> infoList = UHFModuleInfo.parseUHFModuleInfo(configFilePath);
            if (infoList != null && infoList.size() > 0) {
                UHFModuleInfo info = infoList.get(0);
                String devpath = info.serial_path;
                return devpath;
            }
        }
        return "";
    }

    public static String reName(String origin) {
        if (NEWLAND_MODULE_NAME_URM500.equals(origin)) {
            return NEWLAND_MODULE_NAME_URM500_ORIGIN;
        }
        return origin;
    }
}
