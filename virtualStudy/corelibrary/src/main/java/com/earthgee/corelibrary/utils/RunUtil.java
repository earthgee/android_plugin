package com.earthgee.corelibrary.utils;

import android.os.AsyncTask;

import java.util.concurrent.Executor;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class RunUtil {

    public static void runOnUiThread(Runnable runnable){
        runOnUiThread(runnable,false);
    }

    public static void runOnUiThread(Runnable runnable,boolean waitUtilDone){

    }

    public static Executor getThreadPool(){
        return AsyncTask.THREAD_POOL_EXECUTOR;
    }

}
