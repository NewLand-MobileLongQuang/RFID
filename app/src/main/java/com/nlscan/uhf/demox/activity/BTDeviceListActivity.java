package com.nlscan.uhf.demox.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.util.Constant;

import java.util.Set;

/**
 * Bluetooth device list window,select device to connect
 */
public class BTDeviceListActivity extends Activity {

    private final int REQUEST_ENABLE_BT = 0x01;

    private Context mContext;

    private ListView lv_devices;
    private TextView btn_connect;
    private View empty_listview;

    private BtDeviceAdapter mAdapter;
    private BluetoothDevice[] mBTdevArray;

    private View mSelectItemView;

    private BluetoothDevice mSelectedBTDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_list);
        mContext = getApplicationContext();

        initView();
        searchBoundBTDevices();
    }

    private void initView()
    {
        initActionBar();
        lv_devices = (ListView) findViewById(R.id.lv_devices);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        empty_listview = findViewById(R.id.empty_listview);

        lv_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mBTdevArray != null && mBTdevArray.length > 0)
                {
                    mSelectedBTDevice = mBTdevArray[position];
                    if(mSelectItemView != view && mSelectItemView != null) {
                        mSelectItemView.setBackground(null);
                    }
                    mSelectItemView = view;
                    view.setBackgroundColor(Color.CYAN);
                }
            }
        });

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedBTDevice != null){
                    Intent mainIntent = new Intent(mContext,MainActivity.class);
                    mainIntent.putExtra(Constant.EXTRA_DEVICE_PATH_OR_MAC,mSelectedBTDevice.getAddress());
                    startActivity(mainIntent);
                    setResult(RESULT_OK);
                    finish();
                }else{
                    Toast.makeText(mContext, "No bluetooth device select.", Toast.LENGTH_SHORT).show();
                    searchBoundBTDevices();
                }
            }
        });
    }

    private void initActionBar()
    {
        TextView tv_title = (TextView) findViewById(R.id.header_center_name_text_view);
        TextView header_model_name = (TextView) findViewById(R.id.header_model_name);
        View im_actionbar_settings = findViewById(R.id.im_actionbar_settings);

        tv_title.setText(R.string.bt_device);
        header_model_name.setVisibility(View.INVISIBLE);
        im_actionbar_settings.setVisibility(View.INVISIBLE);
    }

    private void searchBoundBTDevices()
    {
        if(requestOpenBT())
        {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> boundDevSet = btAdapter.getBondedDevices();
            if(boundDevSet != null && boundDevSet.size() > 0)
            {
                mBTdevArray = new BluetoothDevice[boundDevSet.size()];
                boundDevSet.toArray(mBTdevArray);
                empty_listview.setVisibility(View.GONE);
            }else
                empty_listview.setVisibility(View.VISIBLE);

            mAdapter = new BtDeviceAdapter();
            lv_devices.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == RESULT_CANCELED) {

                Toast.makeText(mContext, "Bluetooth open failed.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(mContext, "Bluetooth open success.", Toast.LENGTH_SHORT).show();
                searchBoundBTDevices();
            }
        }
    }

    private boolean requestOpenBT()
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!btAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }

    //----------------------------------------------
    //Inner Class
    //----------------------------------------------

    private class BtDeviceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mBTdevArray == null ? 0 : mBTdevArray.length;
        }

        @Override
        public Object getItem(int position) {
            return mBTdevArray == null ? null : mBTdevArray[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BluetoothDevice btDev = mBTdevArray[position];
            if(convertView == null)
            {
                convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2,null);
            }

            TextView tv_name = (TextView) convertView.findViewById(android.R.id.text1);
            TextView tv_mac = (TextView) convertView.findViewById(android.R.id.text2);
            tv_name.setText(btDev.getName());
            tv_mac.setText(btDev.getAddress());
            return convertView;
        }

    }//End class BtDeviceAdapter
}
