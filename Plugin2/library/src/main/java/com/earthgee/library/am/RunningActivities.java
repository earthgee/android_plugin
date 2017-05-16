package com.earthgee.library.am;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import com.earthgee.library.pm.PluginManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/5/2.
 * 运行中插件activity管理,manifest插桩导致每个进程可运行的activity数量有限
 */
public class RunningActivities {

    private static Map<Activity,RunningActivityRecord> mRunningActivityList=new HashMap<>();
    private static Map<Integer,RunningActivityRecord> mRunningSingleStandardActivityList=new HashMap<>();
    private static Map<Integer,RunningActivityRecord> mRunningSingleTopActivityList=new HashMap<>();
    private static Map<Integer,RunningActivityRecord> mRunningSingleTaskActivityList=new HashMap<>();
    private static Map<Integer,RunningActivityRecord> mRunningSingleInstanceActivityList=new HashMap<>();

    private static class RunningActivityRecord{
        private final Activity activity;
        private final ActivityInfo targetActivityInfo;
        private final ActivityInfo stubActivityInfo;
        private int index=0;

        public RunningActivityRecord(Activity activity, ActivityInfo targetActivityInfo, ActivityInfo stubActivityInfo, int index) {
            this.activity = activity;
            this.targetActivityInfo = targetActivityInfo;
            this.stubActivityInfo = stubActivityInfo;
            this.index = index;
        }
    }

    //在插件activity启动之前调用
    public static void beforeStartActivity(){
        synchronized (mRunningActivityList){
            for(RunningActivityRecord record:mRunningActivityList.values()){
                if(record.stubActivityInfo.launchMode==ActivityInfo.LAUNCH_MULTIPLE){
                    continue;
                }else if(record.stubActivityInfo.launchMode==ActivityInfo.LAUNCH_SINGLE_TOP){
                    doFinshIt(mRunningSingleTopActivityList);
                }else if(record.stubActivityInfo.launchMode==ActivityInfo.LAUNCH_SINGLE_TASK){
                    doFinshIt(mRunningSingleTaskActivityList);
                }else if(record.stubActivityInfo.launchMode==ActivityInfo.LAUNCH_SINGLE_INSTANCE){
                    doFinshIt(mRunningSingleInstanceActivityList);
                }
            }
        }
    }

    //销毁选择，根据索引选择
    private static final Comparator<RunningActivityRecord> sRunningActivityRecordComparator = new Comparator<RunningActivityRecord>() {
        @Override
        public int compare(RunningActivityRecord lhs, RunningActivityRecord rhs) {
            if (lhs != null && rhs != null) {
                if (lhs.index > rhs.index) {
                    return 1;
                } else if (lhs.index < rhs.index) {
                    return -1;
                } else {
                    return 0;
                }
            } else if (lhs != null && rhs == null) {
                return 1;
            } else if (lhs == null && rhs != null) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    //当带flag的activity超过一个进程可容纳最大容量时，销毁前面的acitivty
    private static void doFinshIt(Map<Integer,RunningActivityRecord> runningActivityRecordMap){
        if(runningActivityRecordMap!=null
                &&runningActivityRecordMap.size()>= PluginManager.STUB_NO_ACTIVITY_MAX_NUM-1){
            List<RunningActivityRecord> activities=new ArrayList<>(runningActivityRecordMap.size());
            activities.addAll(runningActivityRecordMap.values());
            Collections.sort(activities,sRunningActivityRecordComparator);
            RunningActivityRecord record=activities.get(0);
            if(record.activity!=null&&!record.activity.isFinishing()){
                record.activity.finish();
            }
        }
    }

    public static void onActivityCreate(Activity activity,ActivityInfo targetActivityInfo,ActivityInfo stubActivityInfo){
        synchronized (mRunningActivityList){
            RunningActivityRecord value=new RunningActivityRecord
                    (activity,targetActivityInfo,stubActivityInfo,findMaxIndex()+1);
            mRunningActivityList.put(activity,value);
            if(targetActivityInfo.launchMode==ActivityInfo.LAUNCH_MULTIPLE){
                mRunningSingleStandardActivityList.put(value.index,value);
            }else if(targetActivityInfo.launchMode==ActivityInfo.LAUNCH_SINGLE_TOP){
                mRunningSingleTopActivityList.put(value.index,value);
            }else if(targetActivityInfo.launchMode==ActivityInfo.LAUNCH_SINGLE_TASK){
                mRunningSingleTaskActivityList.put(value.index,value);
            }
        }
    }

    private static int findMaxIndex(){
        int max=0;
        synchronized (mRunningActivityList){
            for(RunningActivityRecord record:mRunningActivityList.values()){
                if(max<record.index){
                    max=record.index;
                }
            }
        }
        return max;
    }

}

































