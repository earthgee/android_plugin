package com.earthgee.library.am;

import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/5/3.
 */
public class RunningProcessList {

    private Context mHostContext;

    public void setContext(Context context){
        this.mHostContext=context;
    }

    void clear(){
        items.clear();
    }

    private class ProcessItem{
        private String stubProcessName;
        private String targetProcessName;
        private int pid;
        private int uid;
        private long startTime;
    }

}





















