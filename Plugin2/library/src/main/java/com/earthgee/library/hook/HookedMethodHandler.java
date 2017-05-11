package com.earthgee.library.hook;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/11.
 * 对应单个方法的hook处理器
 */
public class HookedMethodHandler {

    protected final Context mHostContext;

    private Object mFakeResult=null;
    private boolean mUseFakeResult=false;

    public HookedMethodHandler(Context hostContext){
        this.mHostContext=hostContext;
    }

    /**
     * 3步
     * 1.beforeInvoke的拦截可以不走真正的方法 而走hook的方法
     * 2.method.invoke 执行真正的方法
     * 3.afterInvoke 可以逆天改命 修改结果并覆盖
     *
     * @param receiver
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
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





















