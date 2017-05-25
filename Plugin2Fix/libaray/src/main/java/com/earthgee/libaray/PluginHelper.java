package com.earthgee.libaray;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by zhaoruixuan on 2017/5/25.
 */
public class PluginHelper implements ServiceConnection{

    private static PluginHelper sInstance = null;

    private PluginHelper() {
    }

    public static final PluginHelper getInstance() {
        if (sInstance == null) {
            sInstance = new PluginHelper();
        }
        return sInstance;
    }

    public void applicationOnCreate(final Context baseContext) {
        mContext = baseContext;
        initPlugin(baseContext);
    }

    private Context mContext;

    public void applicationAttachBaseContext(Context baseContext) {
        MyCrashHandler.getInstance().register(baseContext);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
