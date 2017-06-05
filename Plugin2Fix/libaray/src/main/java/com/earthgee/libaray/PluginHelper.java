package com.earthgee.libaray;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.earthgee.libaray.core.PluginProcessManager;
import com.earthgee.libaray.pm.PluginManager;

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

    private void initPlugin(Context baseContext){
        try{
            PluginPatchManager.getInstance().init(baseContext);
            PluginProcessManager.installHook(baseContext);
        }catch (Exception e){
        }

        //插件程序启动后pluginservicemanager已启动,可以直接启动hook
        try{
            if(PluginProcessManager.isPluginProcess(baseContext)){
                PluginProcessManager.setHookEnable(true);
            }else{
                PluginProcessManager.setHookEnable(false);
            }
        }catch (Exception e){
        }

        try {
            PluginManager.getInstance().addServiceConnection(PluginHelper.this);
            PluginManager.getInstance().init(baseContext);
        } catch (Throwable e) {
        }

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        PluginProcessManager.setHookEnable(true, true);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
