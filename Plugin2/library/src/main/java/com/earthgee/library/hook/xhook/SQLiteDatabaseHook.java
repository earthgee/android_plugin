package com.earthgee.library.hook.xhook;

import android.content.Context;
import android.os.Environment;

import com.earthgee.library.core.PluginDirHelper;
import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;

import java.io.File;

/**
 * Created by zhaoruixuan on 2017/5/9.
 */
public class SQLiteDatabaseHook extends Hook{

    private final String mDataDir;
    private final String mHostDataDir;
    private final String mHostPkg;

    public SQLiteDatabaseHook(Context hostContext) {
        super(hostContext);
        mDataDir=new File(Environment.getDataDirectory(),"data/").getPath();
        mHostDataDir= PluginDirHelper.getContextDataDir(hostContext);
        mHostPkg=hostContext.getPackageName();
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {

    }
}
