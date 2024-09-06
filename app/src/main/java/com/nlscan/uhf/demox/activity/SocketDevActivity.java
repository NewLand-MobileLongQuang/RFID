package com.nlscan.uhf.demox.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.util.Constant;

public class SocketDevActivity extends Activity {

    private Context mContext;
    private Button mBtnConnect;
    private EditText mEtPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_device);
        mContext = getApplicationContext();
        initV();
        initEvent();
    }

    private void initV() {
        ((TextView) findViewById(R.id.tv_dev_type)).setText(getResources().getString(R.string.socket_device));
        mEtPath=((EditText) findViewById(R.id.et_dev_path));
        mEtPath.setText("192.168.1.139:8058");
        mBtnConnect = ((Button) findViewById(R.id.btn_connect));
    }

    private void initEvent() {
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    private void connect() {
        Intent mainIntent = new Intent(mContext,MainActivity.class);
        mainIntent.putExtra(Constant.EXTRA_DEVICE_PATH_OR_MAC,mEtPath.getText().toString());
//        startActivity(mainIntent);
        startActivityForResult(mainIntent,120);
        setResult(RESULT_OK);
        finish();
    }

}




