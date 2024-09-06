package com.nlscan.uhf.demox.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.nlscan.uhf.lib.TagInfo;
import com.nlscan.uhf.lib.UHFReader;
import com.nlscan.uhf.demox.R;

public class TableItemInfo {

    public TagInfo tagInfo;
    public TableHeaderItemInfo headerInfo;
    public TableItemInfo(Context context)
    {
        headerInfo = new TableHeaderItemInfo(context);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof TableItemInfo)
        {
            TableItemInfo target = (TableItemInfo) obj;
            if(target.tagInfo != null && this.tagInfo != null)
            {

                try{
                    String targetEpcId = UHFReader.bytes_Hexstr(target.tagInfo.EpcId);
                    String thisEpcId = UHFReader.bytes_Hexstr(this.tagInfo.EpcId);
                    return targetEpcId.equals(thisEpcId);
                }catch (Exception e){
                }
            }

        }
        return super.equals(obj);
    }

    public static class TableHeaderItemInfo{
        public String mNumStr;
        public String mEpcStr;
        public String mCountStr;
        public String mAntStr;
        public String mProtocolStr;
        public String mRSSIStr;
        public String mFreqStr;
        public String mEmbedDataStr;
        public TableHeaderItemInfo(Context context)
        {
            mNumStr = context.getString(R.string.uhf_num);
            mEpcStr = context.getString(R.string.uhf_epc_data);
            mCountStr = context.getString(R.string.uhf_count);
            mAntStr = context.getString(R.string.uhf_ant);
            mProtocolStr = context.getString(R.string.uhf_protocol);
            mRSSIStr = context.getString(R.string.uhf_rssi);
            mFreqStr = context.getString(R.string.uhf_freq);
            mEmbedDataStr = context.getString(R.string.uhf_embeddata);
        }
    }
}
