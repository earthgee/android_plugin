package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.ISearchManagerHookHandle;
import com.earthgee.library.util.ISearchManagerCompat;

/**
 * Created by zhaoruixuan on 2017/4/12.
 * 对搜索服务进行binder hook
 */
public class ISearchManagerBinderHook extends BinderHook{

    private final static String SEARCH_MANAGER_SERVICE="search";

    public ISearchManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SEARCH_MANAGER_SERVICE;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SEARCH_MANAGER_SERVICE);
        return ISearchManagerCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ISearchManagerHookHandle(mHostContext);
    }

}
