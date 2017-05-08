package com.earthgee.library.hook;

import android.content.Context;
import android.os.Build;

import com.earthgee.library.hook.binder.IAppOpsServiceBinderHook;
import com.earthgee.library.hook.binder.IAudioServiceBinderHook;
import com.earthgee.library.hook.binder.IClipboardBinderHook;
import com.earthgee.library.hook.binder.IContentServiceBinderHook;
import com.earthgee.library.hook.binder.IGraphicsStatsBinderHook;
import com.earthgee.library.hook.binder.IInputMethodManagerBinderHook;
import com.earthgee.library.hook.binder.ILocationManagerBinderHook;
import com.earthgee.library.hook.binder.IMediaRouterServiceBinderHook;
import com.earthgee.library.hook.binder.IMmsBinderHook;
import com.earthgee.library.hook.binder.IMountServiceBinderHook;
import com.earthgee.library.hook.binder.INotificationManagerBinderHook;
import com.earthgee.library.hook.binder.IPhoneSubInfoBinderHook;
import com.earthgee.library.hook.binder.ISearchManagerBinderHook;
import com.earthgee.library.hook.binder.ISessionManagerBinderHook;
import com.earthgee.library.hook.binder.ISmsBinderHook;
import com.earthgee.library.hook.binder.ISubBinderHook;
import com.earthgee.library.hook.binder.ITelephonyBinderHook;
import com.earthgee.library.hook.binder.ITelephonyRegistryBinderHook;
import com.earthgee.library.hook.binder.IWifiManagerBinderHook;
import com.earthgee.library.hook.binder.IWindowManagerBinderHook;
import com.earthgee.library.hook.proxy.IActivityManagerHook;
import com.earthgee.library.hook.proxy.IPackageManagerHook;
import com.earthgee.library.hook.proxy.InstrumentationHook;
import com.earthgee.library.hook.proxy.PluginCallbackHook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class HookFactory {

    private static HookFactory instance=null;
    private HookFactory(){
    }
    public static HookFactory getInstance(){
        synchronized (HookFactory.class){
            if(instance==null){
                instance=new HookFactory();
            }
        }
        return instance;
    }

    private List<Hook> mHookList=new ArrayList<>(3);

    public void setHookEnable(boolean enable){
        synchronized (mHookList){
            for(Hook hook:mHookList){
                hook.setEnable(enable);
            }
        }
    }

    public void setHookEnable(boolean enable,boolean reinstallHook){
        synchronized (mHookList){
            for (Hook hook:mHookList){
                hook.setEnable(enable,reinstallHook);
            }
        }
    }

    public void installHook(Hook hook,ClassLoader cl){
        try{
            hook.onInstall(cl);
            synchronized (mHookList){
                mHookList.add(hook);
            }
        }catch (Throwable throwable){
        }
    }

    public final void installHook(Context context,
                                  ClassLoader classLoader) throws Exception{
        installHook(new IClipboardBinderHook(context),classLoader);
        installHook(new ISearchManagerBinderHook(context),classLoader);
        installHook(new INotificationManagerBinderHook(context),classLoader);
        installHook(new IMountServiceBinderHook(context),classLoader);
        installHook(new IAudioServiceBinderHook(context),classLoader);
        installHook(new IContentServiceBinderHook(context),classLoader);
        installHook(new IWindowManagerBinderHook(context),classLoader);
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1){
            installHook(new IGraphicsStatsBinderHook(context),classLoader);
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
            installHook(new IMediaRouterServiceBinderHook(context),classLoader);
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            installHook(new ISessionManagerBinderHook(context),classLoader);
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR2){
            installHook(new IWifiManagerBinderHook(context),classLoader);
        }
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR2){
            installHook(new IInputMethodManagerBinderHook(context),classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            installHook(new ILocationManagerBinderHook(context), classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            installHook(new ITelephonyRegistryBinderHook(context), classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            installHook(new ISubBinderHook(context), classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            installHook(new IPhoneSubInfoBinderHook(context), classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            installHook(new ITelephonyBinderHook(context), classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            installHook(new ISmsBinderHook(context), classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            installHook(new IMmsBinderHook(context), classLoader);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            installHook(new IAppOpsServiceBinderHook(context), classLoader);
        }
        installHook(new IPackageManagerHook(context),classLoader);
        installHook(new IActivityManagerHook(context),classLoader);
        installHook(new PluginCallbackHook(context),classLoader);
        installHook(new InstrumentationHook(context),classLoader);


    }

}

















