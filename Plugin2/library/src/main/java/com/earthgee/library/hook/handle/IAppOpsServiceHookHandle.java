package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.util.IAppOpsServiceCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 替换包名
 */
public class IAppOpsServiceHookHandle extends BaseHookHandle{
    public IAppOpsServiceHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("checkOperation",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("noteOperation",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("startOperation",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("finishOperation",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("startWatchingMode",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("stopWatchingMode",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getToken",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("permissionToOpCode",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("noteProxyOperation",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("checkPackage",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getPackagesForOps",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getOpsForPackage",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setUidMode",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setMode",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("resetAllModes",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("checkAudioOperation",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setAudioRestriction",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setUserRestrictions",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("removeUser",new MyBaseHandler(mHostContext));
        addAllMethodFromHookedClass();
    }

    @Override
    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return IAppOpsServiceCompat.Class();
    }

    @Override
    protected HookedMethodHandler newBaseHandler() throws ClassNotFoundException {
        return new MyBaseHandler(mHostContext);
    }

    private static class MyBaseHandler extends ReplaceCallingPackageHookedMethodHandler {
        public MyBaseHandler(Context context) {
            super(context);
        }
    }
}
