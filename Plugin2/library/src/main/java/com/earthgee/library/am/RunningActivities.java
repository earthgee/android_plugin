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

}
































