package com.earthgee.pluginlib.hook;

import android.content.Context;

import java.lang.reflect.Method;

public class HookedMethodHandler {

    protected final Context mHostContext;

    private Object mFakedResult = null;
    private boolean mUseFakedResult = false;

    public HookedMethodHandler(Context hostContext) {
        this.mHostContext = hostContext;
    }


    /**
     * 在某个方法被调用之前执行，如果返回true，则不执行原始的方法，否则执行原始方法
     */
    protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
        return false;
    }

    protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
    }

    public void setFakedResult(Object fakedResult) {
        this.mFakedResult = fakedResult;
        mUseFakedResult = true;
    }

    public boolean isFakedResult() {
        return mUseFakedResult;
    }

    public Object getFakedResult() {
        return mFakedResult;
    }

}
