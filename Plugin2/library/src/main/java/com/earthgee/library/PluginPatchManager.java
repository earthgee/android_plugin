package com.earthgee.library;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.earthgee.library.pm.PluginManager;

/**
 * Created by zhaoruixuan on 2017/4/11.
 * 异常情况,pms掉线时重新拉起
 */
public class PluginPatchManager {

    private static final int CHECK_DAEAMON_INTERVAL=300;
    private static final int MAX_WAIT_DAEAMON_TIME=5000;

    private Intent mDelayIntent;

    private static PluginPatchManager instance=new PluginPatchManager();
    private long lStartTime=0;
    private Runnable mDelayRunnable;
    private Handler MainThreadHandler;

    public static PluginPatchManager getInstance(){
        return instance;
    }

    private Context mContext;

    public void init(Context context){
        mContext=context;
    }

    public boolean canStartPluginActivity(Intent intent){
        if(intent==null|| PluginManager.getInstance().isConnected()){
            return true;
        }

        ComponentName name=intent.getComponent();
        if(name!=null&&mContext!=null&&!name.getPackageName().equals(mContext.getPackageName())){
            return false;
        }
        return true;
    }

    public boolean startPluginActivity(Intent intent){
        if(intent==null){
            return false;
        }

        if(PluginManager.getInstance().isConnected()){
            mDelayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(mDelayIntent);
            return true;
        }

        PluginManager.getInstance().connectToService();
        initInner();
        mDelayIntent=intent;
        lStartTime=System.currentTimeMillis();
        MainThreadHandler.postDelayed(mDelayRunnable,CHECK_DAEAMON_INTERVAL);
        return true;
    }

    private void postDelayImpl(){
        if(PluginManager.getInstance().isConnected()){
            mDelayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(mDelayIntent);
        }else{
            if(System.currentTimeMillis()-lStartTime<MAX_WAIT_DAEAMON_TIME){
                MainThreadHandler.postDelayed(mDelayRunnable,CHECK_DAEAMON_INTERVAL);
            }
        }
    }

    private void initInner(){
        if(MainThreadHandler==null){
            MainThreadHandler=new Handler(Looper.getMainLooper());
        }

        if(mDelayRunnable==null){
            mDelayRunnable=new Runnable() {
                @Override
                public void run() {
                    postDelayImpl();
                }
            };
        }
    }

}






















