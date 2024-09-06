package com.nlscan.uhf.demox.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nlscan.uhf.demox.R;
import com.nlscan.uhf.demox.util.Constant;
import com.nlscan.uhf.demox.view.RapidInvItem;
import com.nlscan.uhf.lib.TagInfo;

import java.util.Arrays;
import java.util.HashSet;

public class RapidInvFragment extends BaseFragment implements View.OnClickListener {

    private RapidInvItem total, unique, readTime, readRate, countDown;
    private Button start, stop, clear;
    private HashSet<TagInfoEquals> mTags = new HashSet<>();
    private SoundPool mSoundPool;
    private Vibrator mVibrator;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mLayoutView = inflater.inflate(R.layout.layout_inv_rapid, null);
        return mLayoutView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        initEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerResultReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterResultReceiver();
    }

    private void initViews() {
        total = mLayoutView.findViewById(R.id.total_reads);
        unique = mLayoutView.findViewById(R.id.unique_tags);
        readTime = mLayoutView.findViewById(R.id.read_time);
        readRate = mLayoutView.findViewById(R.id.read_rate);
        countDown = mLayoutView.findViewById(R.id.count_down);
        //
        start = mLayoutView.findViewById(R.id.btn_start_inventory);
        stop = mLayoutView.findViewById(R.id.btn_stop_inventory);
        clear = mLayoutView.findViewById(R.id.btn_clean);
    }

    private void initEvents() {
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        clear.setOnClickListener(this);
    }

    private int receivedTagCount = 0;
    private String TAG = RapidInvFragment.class.getSimpleName();
    private BroadcastReceiver mUhfBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!Constant.ACTION_UHF_RESULT_SEND.equals(action))
                return;

            play3DEffect();

            Parcelable[] tagInfos = intent.getParcelableArrayExtra(Constant.EXTRA_TAG_INFO);
            for (Parcelable tagInfo : tagInfos) {
                TagInfoEquals tagInfo1 = new TagInfoEquals((TagInfo) tagInfo);
                if (!mTags.contains(tagInfo1)) {
                    mTags.add(tagInfo1);
                }
            }
            receivedTagCount++;
            Log.i(TAG, "onReceive: " + String.format("receivedTagCount:%d,tag size : %d", receivedTagCount, mTags.size()));
            total.setSubText(receivedTagCount + "");
            unique.setSubText(mTags.size() + "");
            readRate.setSubText(receivedTagCount / (elapsedTime <= 0 ? 1 : elapsedTime) + "");
        }
    };

    private void registerResultReceiver() {
        try {
            IntentFilter iFilter = new IntentFilter(Constant.ACTION_UHF_RESULT_SEND);
            mContext.registerReceiver(mUhfBR, iFilter);
        } catch (Exception e) {
        }
    }

    private void unRegisterResultReceiver() {
        try {
            mContext.unregisterReceiver(mUhfBR);
        } catch (Exception e) {
        }
    }

    private void startInv() {
        clearInv();
        mUHFMgr.startTagInventory();
        startTimer();
    }

    private void stopInv() {
        mUHFMgr.stopTagInventory();
        handler.removeCallbacks(runnable);
    }

    private void clearInv() {
        total.setSubText("0");
        unique.setSubText("0");
        readTime.setSubText("0");
        readRate.setSubText("0");
        mTags.clear();
        elapsedTime = 0;
        receivedTagCount = 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_inventory:
                startInv();
                break;
            case R.id.btn_stop_inventory:
                stopInv();
                break;
            case R.id.btn_clean:
                clearInv();
                break;
        }
    }


    class TagInfoEquals extends TagInfo {
        public TagInfoEquals(byte pAntennaID, int pFrequency, int pTimeStamp, short pEmbededDatalen, byte[] pEmbededData, byte[] pRes, short pEpclen, byte[] pPC, byte[] pCRC, byte[] pEpcId, int pPhase, SL_TagProtocol pprotocol, int pReadCnt, int pRSSI) {
            super(pAntennaID, pFrequency, pTimeStamp, pEmbededDatalen, pEmbededData, pRes, pEpclen, pPC, pCRC, pEpcId, pPhase, pprotocol, pReadCnt, pRSSI);
        }

        public TagInfoEquals(Parcel dest) {
            super(dest);
        }

        public TagInfoEquals(TagInfo tagInfo) {
            this(tagInfo.AntennaID, tagInfo.Frequency, tagInfo.TimeStamp, tagInfo.EmbededDatalen, tagInfo.EmbededData, tagInfo.Res, tagInfo.Epclen, tagInfo.PC, tagInfo.CRC, tagInfo.EpcId, tagInfo.Phase, tagInfo.protocol, tagInfo.ReadCnt, tagInfo.RSSI);
        }

        @Override
        public boolean equals(@Nullable Object obj) {//标签是否相同的判断依据为EpcId
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            TagInfoEquals other = (TagInfoEquals) obj;
            return Arrays.equals(EpcId, other.EpcId);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(EpcId);
        }

        private boolean isArrayEquals(byte[] a, byte[] b) {
            if (a == null || b == null)
                return false;
            if (a.length != b.length)
                return false;
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i])
                    return false;
            }
            return true;
        }
    }

    //计时器
    private int elapsedTime = 0; // 已经过去的时间，单位为秒
    private int interval = 1000; // 更新间隔，单位为毫秒

    private void startTimer() {
        handler.postDelayed(runnable, interval);
    }

    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime++;
            readTime.setSubText(elapsedTime + "");
            if (elapsedTime >= Integer.parseInt(countDown.getSubText())) {
                stopInv();
                return;
            }
            handler.postDelayed(this, interval);
        }
    };


    private void playSound() {
        mSoundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        // 获取最大音量值
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        // 不断获取当前的音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_RING);
        //最终影响音量
        float volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        mSoundPool.play(1, volumnRatio, volumnRatio, 0, 0, 1);
    }

    private void playVibrator() {
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(80);
    }

    private void play3DEffect() {
        playSound();
        playVibrator();
    }


}
