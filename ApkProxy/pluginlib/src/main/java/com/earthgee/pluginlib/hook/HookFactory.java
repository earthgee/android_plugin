package com.earthgee.pluginlib.hook;

import android.content.Context;

import com.earthgee.pluginlib.helper.ProcessUtils;
import com.earthgee.pluginlib.hook.binder.IClipboardBinderHook;

import java.util.ArrayList;
import java.util.List;

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

    private List<Hook> mHookList=new ArrayList<>();

    public void installHook(Hook hook,ClassLoader cl){
        try{
            hook.onInstall(cl);
            synchronized (mHookList){
                mHookList.add(hook);
            }
        }catch (Exception e){

        }
    }

    public final void installHook(Context context,ClassLoader classLoader) throws Exception{
        if(ProcessUtils.isMainProcess(context)){
            //主进程
        }else{
            //插件进程
            installHook(new IClipboardBinderHook(context),classLoader);
            //其他服务
        }
    }

}












