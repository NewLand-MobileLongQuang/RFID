package com.nlscan.uhf.demox.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nlscan.uhf.demox.R;

public class RapidInvItem extends ConstraintLayout {
    private EditText st = null;
    private TextView ht;

    public RapidInvItem(@NonNull Context context) {
        super(context);
    }

    public RapidInvItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_rapid_inv_item, this, true);

        // 获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RapidInvItem);
        String htt = typedArray.getString(R.styleable.RapidInvItem_headText);
        String stt = typedArray.getString(R.styleable.RapidInvItem_subText);
        typedArray.recycle();

        // 设置按钮文本和图标
        ht = findViewById(R.id.head_text);
        st = findViewById(R.id.sub_text);
        ht.setText(htt);
        st.setText(stt);

        // 进行其他自定义的初始化操作
        // 例如设置样式、设置点击事件等
    }

    public void setSubText(String s) {
        st = findViewById(R.id.sub_text);
        st.setText(s);
    }
    public String getSubText() {
        st = findViewById(R.id.sub_text);
        return st.getText().toString();
    }
}
