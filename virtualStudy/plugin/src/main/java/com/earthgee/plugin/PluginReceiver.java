package com.earthgee.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by zhaoruixuan on 2017/8/7.
 */
public class PluginReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("earthgee2","plugin broadcast receiver received");
    }

}
