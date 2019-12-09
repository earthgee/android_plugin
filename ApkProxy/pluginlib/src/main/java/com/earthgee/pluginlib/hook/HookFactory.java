package com.earthgee.pluginlib.hook;

import android.content.Context;

import com.earthgee.pluginlib.helper.ProcessUtils;

public class HookFactory {

    private static HookFactory sInstance = null;

    private HookFactory() {
    }

    public static HookFactory getInstance() {
        synchronized (HookFactory.class) {
            if (sInstance == null) {
                sInstance = new HookFactory();
            }
        }
        return sInstance;
    }

    public final void installHook(Context context,ClassLoader classLoader) throws Exception{
        if(ProcessUtils.isMainProcess(context)){

        }else{

        }
    }

}












