package com.earthgee.bundle.runtime;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;

/**
 * Created by zhaoruixuan on 2018/4/26.
 */
public class ContextImplHook extends ContextWrapper{

    static final Logger log;

    static {
        log= LoggerFactory.getLogcatLogger("ContextImplHook");
    }

    public ContextImplHook(Context base) {
        super(base);
    }

    @Override
    public Resources getResources() {
        log.log("getResources is invoke",Logger.LogLevel.INFO);
        return RuntimeArgs.delegateResources;
    }

    public AssetManager getAssets(){
        log.log("getAssets is invoke", Logger.LogLevel.INFO);
        return RuntimeArgs.delegateResources.getAssets();
    }


}
