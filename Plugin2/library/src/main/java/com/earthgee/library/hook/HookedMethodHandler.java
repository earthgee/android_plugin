package com.earthgee.library.hook;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

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

    public synchronized Object doHookInner(Object receiver, Method method,Object[] args) throws Throwable{
        long b=System.currentTimeMillis();
        try{
            mUseFakeResult=false;
            mFakeResult=null;
            boolean suc=beforeInvoke(receiver,method,args);
            Object invokeResult=null;
            if(!suc){
                invokeResult=method.invoke(receiver,args);
            }
            afterInvoke(receiver,method,args,invokeResult);
            if(mUseFakeResult){
                return mFakeResult;
            }else{
                return invokeResult;
            }
        }finally {
            long time=System.currentTimeMillis()-b;
            if(time>5){
            }
        }
    }

    protected boolean beforeInvoke(Object receiver, Method method,Object[] args) throws Throwable{
        return false;
    }

    protected void afterInvoke(Object receiver,Method method,
                               Object[] args,Object invokeResult) throws Throwable{

    }

    public boolean isFakeResult(){
        return mUseFakeResult;
    }

    public Object getFakeResult(){
        return mFakeResult;
    }

    public void setFakeResult(Object fakedResult){
        this.mFakeResult=fakedResult;
        mUseFakeResult=true;
    }

}





















