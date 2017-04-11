package com.earthgee.library.hook;

import android.content.Context;

import com.earthgee.library.hook.binder.IClipboardBinderHook;

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

    }

}

















