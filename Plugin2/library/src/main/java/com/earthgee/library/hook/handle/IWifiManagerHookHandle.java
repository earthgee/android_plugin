package com.earthgee.library.hook.handle;

import android.content.Context;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IWifiManagerHookHandle extends BaseHookHandle{
    public IWifiManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("getScanResults",new getScanResults(mHostContext));
        sHookedMethodHandlers.put("getBatchedScanResults",new getBatchedScanResults(mHostContext));
        sHookedMethodHandlers.put("setWifiEnabled",new setWifiEnabled(mHostContext));
    }

    private class IWifiManagerHookedMethodHandler extends HookedMethodHandler{

        public IWifiManagerHookedMethodHandler(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            final int index=0;
            if(args!=null&&args.length>index&&args[index] instanceof String){
                String callingPackage = (String) args[index];
                if(!TextUtils.equals(callingPackage,mHostContext.getPackageName())){
                    args[index]=mHostContext.getPackageName();
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getScanResults extends IWifiManagerHookedMethodHandler{

        public getScanResults(Context hostContext) {
            super(hostContext);
        }
    }

    private class getBatchedScanResults extends IWifiManagerHookedMethodHandler{

        public getBatchedScanResults(Context hostContext) {
            super(hostContext);
        }
    }

    private class setWifiEnabled extends HookedMethodHandler{

        public setWifiEnabled(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null&&args.length>0){
                for(int i=0;i<args.length;i++){
                    Object arg=args[i];
                    if(arg!=null&&arg instanceof String){
                        String str= (String) arg;
                        if(!TextUtils.equals(str,mHostContext.getPackageName())&&
                                PluginManager.getInstance().isPluginPackage(str)){
                            args[i]=mHostContext.getPackageName();
                        }
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

}
