package com.earthgee.corelibrary.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class RunUtil {

    private static final int MESSAGE_RUN_ON_UITHREAD=0x1;

    private static Handler sHandler;

    public static void runOnUiThread(Runnable runnable){
        runOnUiThread(runnable,false);
    }

    public static void runOnUiThread(Runnable runnable,boolean waitUtilDone){
        if(Looper.myLooper()==Looper.getMainLooper()){
            runnable.run();
            return;
        }

        CountDownLatch countDownLatch=null;
        if(waitUtilDone){
            countDownLatch=new CountDownLatch(1);
        }
        Pair<Runnable,CountDownLatch> pair=new Pair<>(runnable,countDownLatch);
        getHandler().obtainMessage(MESSAGE_RUN_ON_UITHREAD,pair).sendToTarget();
        if(waitUtilDone){
            try{
                countDownLatch.await();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static Executor getThreadPool(){
        return AsyncTask.THREAD_POOL_EXECUTOR;
    }

    private static Handler getHandler(){
        synchronized (RunUtil.class){
            if(sHandler==null){
                sHandler=new InternalHandler();
            }
            return sHandler;
        }
    }

    private static class InternalHandler extends Handler{

        public InternalHandler(){
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MESSAGE_RUN_ON_UITHREAD){
                Pair<Runnable,CountDownLatch> pair= (Pair<Runnable, CountDownLatch>) msg.obj;
                Runnable runnable=pair.first;
                runnable.run();
                if(pair.second!=null){
                    pair.second.countDown();
                }
            }
        }
    }

}















































