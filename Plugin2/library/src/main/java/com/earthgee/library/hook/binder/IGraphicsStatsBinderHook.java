package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IGraphicsStatsHookHandle;
import com.earthgee.library.util.IGraphicsStatsCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IGraphicsStatsBinderHook extends BinderHook{

    private final static String SERVICE_NAME="graphicsstats";

    public IGraphicsStatsBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IGraphicsStatsCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IGraphicsStatsHookHandle(mHostContext);
    }
}
