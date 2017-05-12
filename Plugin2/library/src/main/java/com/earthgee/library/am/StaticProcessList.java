package com.earthgee.library.am;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

import com.earthgee.library.stub.ActivityStub;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/5/3.
 */
public class StaticProcessList {

    private static final String CATEGORY_ACTIVITY_PROXY_STUB="com.morgoo.droidplugin.category.PROXY_STUB";

    private Map<String,ProcessItem> items=new HashMap<>();

    private List<String> mOtherProcessNames=new ArrayList<>();

    private class ProcessItem{
        private String name;
        private Map<String,ActivityInfo> activityInfos=new HashMap<>();
        private Map<String,ServiceInfo> serviceInfos=new HashMap<>();
        private Map<String,ProviderInfo> providerInfos=new HashMap<>();

        private void addActivityInfo(ActivityInfo info){
            if(!activityInfos.containsKey(info.name)){
                activityInfos.put(info.name,info);
            }
        }
    }

    void onCreate(Context mHostContext) throws PackageManager.NameNotFoundException{
        //宿主进程application信息
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(CATEGORY_ACTIVITY_PROXY_STUB);
        intent.setPackage(mHostContext.getPackageName());

        PackageManager pm=mHostContext.getPackageManager();
        List<ResolveInfo> activities=pm.queryIntentActivities(intent,PackageManager.GET_META_DATA);
        for(ResolveInfo activity:activities){
            addActivityInfo(activity.activityInfo);
        }

        //service...provider... todo
    }

    private void addActivityInfo(ActivityInfo info){
        if(TextUtils.isEmpty(info.processName)){
            info.processName=info.packageName;
        }
        ProcessItem item=items.get(info.processName);
        if(item==null){
            item=new ProcessItem();
            item.name=info.processName;
            items.put(info.processName,item);
        }
        item.addActivityInfo(info);
    }

    List<ActivityInfo> getActivityInfoForProcessName(String processName){
        ProcessItem item=items.get(processName);
        ArrayList<ActivityInfo> activityInfos=new ArrayList<>(item.activityInfos.values());
        Collections.sort(activityInfos,sComponentInfoComparator);
        return activityInfos;
    }

    List<ActivityInfo> getActivityInfoForProcessName(String processName,boolean dialogStyle){
        ProcessItem item=items.get(processName);
        Collection<ActivityInfo> values=item.activityInfos.values();
        ArrayList<ActivityInfo> activityInfos=new ArrayList<>();
        for(ActivityInfo info:values){
            if(dialogStyle){
                if(info.name.startsWith(ActivityStub.Dialog.class.getName())){
                    activityInfos.add(info);
                }
            }else{
                if(!info.name.startsWith(ActivityStub.Dialog.class.getName())){
                    activityInfos.add(info);
                }
            }
        }

        Collections.sort(activityInfos,sComponentInfoComparator);
        return activityInfos;
    }

    void clear(){
        items.clear();
    }

    private static final Comparator<ComponentInfo> sComponentInfoComparator=new Comparator<ComponentInfo>() {
        @Override
        public int compare(ComponentInfo lhs, ComponentInfo rhs) {
            return Collator.getInstance().compare(lhs.name,rhs.name);
        }
    };

    List<String> getProcessNames(){
        return new ArrayList<>(items.keySet());
    }

}































