package com.earthgee.library;

import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/4/11.
 * 异常情况
 */
public class PluginPatchManager {

    private static PluginPatchManager instance=new PluginPatchManager();

    public static PluginPatchManager getInstance(){
        return instance;
    }

    private Context mContext;

    public void init(Context context){
        mContext=context;
    }



}
