package com.earthgee.library.hook;

import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public abstract class Hook {

    private boolean mEnable=false;

    protected Context mHostContext;
    //hook某个方法对应的处理器
    protected BaseHookHandle mHookHandles;

    public void setEnable(boolean enable,boolean reInstallHook){
        this.mEnable=enable;
    }

    /**
     * @param enable 此hook运行与否
     */
    public final void setEnable(boolean enable){
        setEnable(enable,false);
    }

    public boolean isEnable(){
        return mEnable;
    }

    protected Hook(Context hostContext){
        mHostContext=hostContext;
        mHookHandles=createHookHandle();
    }

    protected abstract BaseHookHandle createHookHandle();

    protected abstract void onInstall(ClassLoader classLoader) throws Throwable;

    protected void onUnInstall(ClassLoader classLoader) throws Throwable{

    }

}
