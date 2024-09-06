package com.nlscan.uhf.demox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import com.nlscan.uhf.lib.UHFConstants;
import com.nlscan.uhf.lib.UHFManager;
import com.nlscan.uhf.lib.adapters.UHFConfigInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Uncatch exception
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";  
    private static CrashHandler INSTANCE = new CrashHandler();  
    private Context mContext;  
    private CrashHandler() {  
    }  
  
    public static CrashHandler getInstance() {  
        return INSTANCE;  
    }  
  
    public void init(Context ctx) {  
        mContext = ctx;  
        Thread.setDefaultUncaughtExceptionHandler(this);  
    }  
  
    @Override  
    public void uncaughtException(Thread thread, Throwable ex) {  

    	handleException(ex);
        new Thread() {
            @Override
            public void run() {
               Looper.prepare();
               AlertDialog dialog=new AlertDialog.Builder(mContext)
           				.setTitle("Wanning")
           				.setCancelable(false)
                        .setMessage("Something crashed.")
                        .setPositiveButton("Confirm", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            	dialog.dismiss();
                            }
                        }).create();

               dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
               dialog.show();
                Looper.loop();
            }
        }.start();
    }
  
    /** 
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     * @param ex 
     * @return true:如果处理了该异常信息;否则返回false 
     */  
    private boolean handleException(Throwable ex) {  
    	
        Log.e(TAG, "Crash exception!", ex);
        UHFManager uhfMgr = UHFManager.getInstance(mContext);
        uhfMgr.disconnect();
        powerDriver(false);
        saveCrashInfo2File(ex);
        
        return true;  
    } 
    
    /** 
     * 收集设备参数信息 
     * @param ctx 
     */  
    public Map<String, String> collectDeviceInfo(Context ctx) {
    	
    	Map<String,String> infos=new HashMap<String, String>();
        try {  
            PackageManager pm = ctx.getPackageManager();  
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
            if (pi != null) {  
                String versionName = pi.versionName == null ? "null" : pi.versionName;  
                String versionCode = pi.versionCode + "";  
                infos.put("versionName", versionName);  
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {  
            Log.w(TAG, "an error occured when collect package info", e);  
        }  
        Field[] fields = Build.class.getDeclaredFields();  
        for (Field field : fields) {  
            try {  
                field.setAccessible(true);  
                infos.put(field.getName(), field.get(null).toString());  
                //Log.d(TAG, field.getName() + " : " + field.get(null));  
            } catch (Exception e) {  
                Log.w(TAG, "an error occured when collect crash info", e);  
            }  
        }  
        
        return infos;
        
    }//collectDeviceInfo
    
    private String saveCrashInfo2File(Throwable ex) 
    {  
        SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMddHHmm"); 
        String time = formatter.format(new Date());  
    	Map<String, String> infos=collectDeviceInfo(mContext);
        StringBuffer sb = new StringBuffer();  
        sb.append("time:").append(time).append("\n");
        sb.append("========================================").append("\n");
        
        for (Map.Entry<String, String> entry : infos.entrySet()) {  
            String key = entry.getKey();  
            String value = entry.getValue();  
            sb.append(key + "=" + value + "\n");  
        }  
          
        Writer writer = new StringWriter();  
        PrintWriter printWriter = new PrintWriter(writer);  
        ex.printStackTrace(printWriter);  
        Throwable cause = ex.getCause();  
        while (cause != null) {  
            cause.printStackTrace(printWriter);  
            cause = cause.getCause();  
        }  
        printWriter.close();  
        String result = writer.toString();  
        sb.append(result);  
        try {  
            
            String fileName = "crash_log"+time+".txt";
            String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NL_RFID-SDK_Demo/";
            File dir = new File(dirPath);  
            if (!dir.exists())
                dir.mkdirs();  
            File logFile = new File(dirPath,fileName);
            logFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(logFile);
            fos.write(sb.toString().getBytes());  
            fos.close();

            return fileName;  
            
        } catch (Exception e) {  
            Log.w(TAG, "an error occured while writing file...", e);  
        }  
        return null;  
    }

    private boolean powerDriver(boolean powerOn)
    {
        Log.d(TAG, "Enter Power driver, : "+powerOn);
        String moduleDriverPath = "/sys/devices/platform/droi_gpio_common/rfid_vbat_en";
        String serialDriverPath = "/sys/devices/platform/droi_gpio_common/rfid_ldo_en";
        File powerFile = new File(moduleDriverPath);
        if(powerFile.exists())
        {
            try {
                //上电
                FileWriter fw58 = new FileWriter(powerFile);//写文件
                fw58.write(powerOn?"1":"0");
                fw58.close();
                Log.d(TAG, "Power driver: "+powerFile.getAbsolutePath()+", Power state: "+(powerOn?"POWER ON.":"POWER DOWN."));
                fw58 = new FileWriter(new File(serialDriverPath));//写文件
                fw58.write(powerOn?"1":"0");
                Log.d(TAG, "Power driver: "+serialDriverPath+", Power state: "+(powerOn?"POWER ON.":"POWER DOWN."));
                return true;
            }catch (Exception e){
                Log.w(TAG, "Power driver, write power on data error.",e);
            }
            return false;
        }

        try {
            UHFConfigInfo uhfConfig = UHFConstants.getNewlandUHFConfigs().get(0);
            //上电
            FileWriter fw58 = new FileWriter(uhfConfig.power_driver);//写文件
            fw58.write(powerOn?"1":"0");
            fw58.close();
            Log.d(TAG, "Power driver, Power state: "+(powerOn?"POWER ON.":"POWER DOWN."));
            Thread.sleep(10);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Power driver, write power on data error.",e);
        }

        try {
            ///sys/bus/platform/devices/sys_switch.0/nls_serial 文件。写入 "txrx=gpio" 表示设置为GPIO脚。写入 "txrx=serial"表示设置为串口功能脚
            FileWriter fw58 = new FileWriter("/sys/bus/platform/devices/sys_switch.0/nls_serial");
            if(powerOn){
                fw58.write("txrx=serial");
                Log.d(TAG, "Power driver, nls_serial : "+(powerOn?"txrx=serial":"txrx=gpio"));
            }
            //fw58.write(powerOn?"txrx=serial":"txrx=gpio");
            fw58.close();

            Thread.sleep(50);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Power driver, write gpio to serial error.",e);
        }

        return false;

    }//End powerDriver
}
