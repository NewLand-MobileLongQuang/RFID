package com.nlscan.uhf.demox.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.util.Constant;
import com.nlscan.uhf.demox.util.PermissionUtils;
import com.nlscan.uhf.lib.UHFManager;


/**
 * Select the device which to connect.
 */
public class SelectDeviceActivity extends Activity {

    private final static int REQUEST_RUNTIME_PERMISSION = 0x10;
    private boolean mPermissionGranted = false;

    private Context mContext;
    private ListView lv_devices;
    private Button btn_select;
    private TextView tv_device;

    private ArrayAdapter<String> mAdapater;
    private String mSelectedDevice;
    private View mSelectItemView;

//    private String[] mDevices = new String[]{
//            Constant.NEWLAND_MODULE_NAME_UR90,
//            Constant.NEWLAND_MODULE_NAME_UR90_V2_0,
//            Constant.NEWLAND_MODULE_NAME_BU10_OR_BU20,
//            Constant.NEWLAND_MODULE_NAME_URM300,
//            Constant.NEWLAND_MODULE_NAME_URM500,
//            Constant.NEWLAND_MODULE_NAME_URF520
//    };
    private String[] mDeviceTypes ;
    private String[] mDeviceTypeNames ;

    private final int REQUEST_CONNECT = 0x01;

    private UHFManager mUHFMgr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);
        mContext = getApplicationContext();
        mUHFMgr = UHFManager.getInstance(mContext);
        mDeviceTypes = new String[]{
                Constant.NEWLAND_MODULE_SERIAL_TYPE,
                Constant.NEWLAND_MODULE_NAME_BU10_OR_BU20,
                Constant.NEWLAND_MODULE_NET_TYPE
        };
        mDeviceTypeNames = new String[]{
                mContext.getString(R.string.module_type_serial),
                mContext.getString(R.string.module_type_bluetooth),
                mContext.getString(R.string.module_type_network)
        };
        initView();

        boolean needRequestPermission = PermissionUtils.checkRequestRuntimePermissions(mContext);
        if (needRequestPermission) {
            PermissionUtils.requestAllRuntimePermission(this, REQUEST_RUNTIME_PERMISSION);
        } else {
            mPermissionGranted = true;
            if(!Constant.isSupportSelectDevice())
            {
                goToMainActivity();
                finish();
            }else if(mUHFMgr.isConnected()){
                goToMainActivity();
                finish();
            }
//            else if(Constant.isMT95L()){
//                String devPath = Constant.getDevPathOnNewlandPDA();
//                if(!TextUtils.isEmpty(devPath)) {
//                    goToMainActivity(devPath, null);
//                    finish();
//                }
//            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RUNTIME_PERMISSION) {
            mPermissionGranted = true;
            if(!Constant.isSupportSelectDevice())
            {
                goToMainActivity();
                finish();
            }else if(mUHFMgr.isConnected()){
                goToMainActivity();
                finish();
            }
//            else if(Constant.isMT95L()){
//                String devPath = Constant.getDevPathOnNewlandPDA();
//                if(!TextUtils.isEmpty(devPath)) {
//                    goToMainActivity(devPath, null);
//                    finish();
//                }
//            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONNECT) {
            if (resultCode == RESULT_OK)
                finish();
        }
    }

    private void initView() {
        initActionBar();
        lv_devices = (ListView) findViewById(R.id.lv_devices);
        btn_select = (Button) findViewById(R.id.btn_select);
        tv_device = (TextView) findViewById(R.id.tv_device);

        mAdapater = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mDeviceTypeNames);
        lv_devices.setAdapter(mAdapater);
        lv_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectDevTypeName = mDeviceTypeNames[position];
                mSelectedDevice = mDeviceTypes[position];
                tv_device.setText(selectDevTypeName);

                if (mSelectItemView != view && mSelectItemView != null) {
                    mSelectItemView.setBackground(null);
                }
                mSelectItemView = view;
                view.setBackgroundColor(Color.CYAN);
            }
        });

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDevice();
            }
        });
    }

    private void initActionBar() {
        TextView tv_title = (TextView) findViewById(R.id.header_center_name_text_view);
        TextView header_model_name = (TextView) findViewById(R.id.header_model_name);
        View im_actionbar_settings = findViewById(R.id.im_actionbar_settings);

        tv_title.setText(R.string.device_select);
        header_model_name.setVisibility(View.INVISIBLE);
        im_actionbar_settings.setVisibility(View.INVISIBLE);
    }

    private void connectDevice() {
        if (TextUtils.isEmpty(mSelectedDevice))
            Toast.makeText(mContext, "Please select target device first.", Toast.LENGTH_SHORT).show();
        else {

            Intent it = new Intent();

            switch (mSelectedDevice) {
                case Constant.NEWLAND_MODULE_NAME_UR90:
                case Constant.NEWLAND_MODULE_NAME_UR90_V2_0:
                case Constant.NEWLAND_MODULE_NAME_URM300:
                case Constant.NEWLAND_MODULE_NAME_URM500:
                case Constant.NEWLAND_MODULE_SERIAL_TYPE:
                        it = new Intent(mContext, SerialDevActivity.class);
                    break;
                case Constant.NEWLAND_MODULE_NAME_BU10_OR_BU20:
                    it = new Intent(mContext, BTDeviceListActivity.class);
                    break;
                case Constant.NEWLAND_MODULE_NAME_URF520:
                    case Constant.NEWLAND_MODULE_NET_TYPE:
                    it = new Intent(mContext, SocketDevActivity.class);
                    break;
            }
            it.putExtra(Constant.EXTRA_DEVICE_MODEL_KEY, mSelectedDevice);
            startActivityForResult(it, REQUEST_CONNECT);
        }
    }

    private void goToMainActivity()
    {
        Intent mainIntent = new Intent(mContext, MainActivity.class);
        //mainIntent.putExtra(Constant.EXTRA_DEVICE_PATH_OR_MAC, "");
        //mainIntent.putExtra(Constant.EXTRA_DEVICE_MODEL_KEY, "");
        startActivity(mainIntent);
    }

    private void goToMainActivity(String devPathOrMac,String moduleName)
    {
        Intent mainIntent = new Intent(mContext, MainActivity.class);
        mainIntent.putExtra(Constant.EXTRA_DEVICE_PATH_OR_MAC, devPathOrMac);
        mainIntent.putExtra(Constant.EXTRA_DEVICE_MODEL_KEY, moduleName);
        startActivity(mainIntent);
    }
}
