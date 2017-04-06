package com.earthgee.library;

import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/4/6.
 */
public class PluginHelper {

    private static PluginHelper sInstance=null;

    private PluginHelper(){

    }

    public static final PluginHelper getInstance(){
        if(sInstance==null){
            sInstance=new PluginHelper();
        }
        return sInstance;
    }

    public void applicationAttachBaseContext(Context baseContext){
        MyCrashHandler.getInstance().register(baseContext);
    }

}
