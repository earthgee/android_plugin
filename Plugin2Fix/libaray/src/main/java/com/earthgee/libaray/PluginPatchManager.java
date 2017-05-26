package com.earthgee.libaray;

import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class PluginPatchManager {

    private Context mContext;

    private static PluginPatchManager s_inst = new PluginPatchManager();
    public static PluginPatchManager getInstance() {
        return s_inst;
    }

    public void init(Context context){
        mContext = context;
    }

}
