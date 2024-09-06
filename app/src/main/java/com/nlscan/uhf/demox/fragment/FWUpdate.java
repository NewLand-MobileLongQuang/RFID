package com.nlscan.uhf.demox.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nlscan.uhf.demox.R;

public class FWUpdate extends BaseFragment {
    private static final int PICKFILE_RESULT_CODE = 110;
    private TextView tvFw;
    private EditText edFw;
    private Button btnFw;
    private String fwPath = "";


    private String TAG = InventoryFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initEvent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mLayoutView = inflater.inflate(R.layout.layout_industry_test_fragment, null);
        initView(mLayoutView);
        return mLayoutView;
    }

    private void initView(View v) {
        tvFw = ((TextView) v.findViewById(R.id.tv_fw));
        edFw = ((EditText) v.findViewById(R.id.ed_fw));
        btnFw = ((Button) v.findViewById(R.id.btn_fw_choose_file));
    }

    private void initEvent() {
        btnFw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fwPath = edFw.getText().toString();
                chooseFile();
            }
        });
    }

    private void chooseFile() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

}
