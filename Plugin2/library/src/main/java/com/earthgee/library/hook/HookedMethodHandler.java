package com.earthgee.library.hook;

import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class HookedMethodHandler {

    protected final Context mHostContext;

    private Object mFakeResult=null;
    private boolean mUseFakeResult=false;

    public HookedMethodHandler(Context hostContext){
        this.mHostContext=hostContext;
    }



}
