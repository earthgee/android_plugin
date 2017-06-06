package com.earthgee.libaray.am;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

import com.earthgee.libaray.helper.AttributeCache;
import com.earthgee.libaray.stub.ActivityStub;
import com.earthgee.libaray.stub.ContentProviderStub;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class StaticProcessList {

    private Map<String, ProcessItem> items = new HashMap<>();
    private List<String> mOtherProcessNames = new ArrayList<>();

    private static final String CATEGORY_ACTIVITY_PROXY_STUB = "com.morgoo.droidplugin.category.PROXY_STUB";

    private class ProcessItem {

        private String name;

        //key=ActivityInfo.name,value=ActivityInfo
        private Map<String, ActivityInfo> activityInfos = new HashMap<String, ActivityInfo>(4);
        //key=ServiceInfo.name,value=ServiceInfo
        private Map<String, ServiceInfo> serviceInfos = new HashMap<String, ServiceInfo>(1);
        //key=ProviderInfo.authority,value=ProviderInfo
        private Map<String, ProviderInfo> providerInfos = new HashMap<String, ProviderInfo>(1);

        private void addActivityInfo(ActivityInfo info) {
            if (!activityInfos.containsKey(info.name)) {
                activityInfos.put(info.name, info);
            }
        }

        private void addServiceInfo(ServiceInfo info) {
            if (!serviceInfos.containsKey(info.name)) {
                serviceInfos.put(info.name, info);
            }
        }

        private void addProviderInfo(ProviderInfo info) {
            if (!providerInfos.containsKey(info.authority)) {
                providerInfos.put(info.authority, info);
            }
        }
    }

    void onCreate(Context mHostContext) throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(CATEGORY_ACTIVITY_PROXY_STUB);
        intent.setPackage(mHostContext.getPackageName());

        PackageManager pm = mHostContext.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        for (ResolveInfo activity : activities) {
            addActivityInfo(activity.activityInfo);
        }

        List<ResolveInfo> services = pm.queryIntentServices(intent, 0);
        for (ResolveInfo service : services) {
            addServiceInfo(service.serviceInfo);
        }

        PackageInfo packageInfo = pm.getPackageInfo(mHostContext.getPackageName(), PackageManager.GET_PROVIDERS);
        if (packageInfo.providers != null && packageInfo.providers.length > 0) {
            for (ProviderInfo providerInfo : packageInfo.providers) {
                if (providerInfo.name != null && providerInfo.name.startsWith(ContentProviderStub.class.getName())) {
                    addProviderInfo(providerInfo);
                }
            }
        }

        mOtherProcessNames.clear();
        PackageInfo packageInfo1 = pm.getPackageInfo(mHostContext.getPackageName(), PackageManager.GET_ACTIVITIES
                | PackageManager.GET_RECEIVERS
                | PackageManager.GET_PROVIDERS
                | PackageManager.GET_SERVICES);

        if (packageInfo1.activities != null) {
            for (ActivityInfo info : packageInfo1.activities) {
                if (!mOtherProcessNames.contains(info.processName) && !items.containsKey(info.processName)) {
                    mOtherProcessNames.add(info.processName);
                }
            }
        }

        if (packageInfo1.receivers != null) {
            for(ActivityInfo info:packageInfo1.receivers){
                if(mOtherProcessNames.contains(info.processName)&&!items.containsKey(info.processName)){
                    mOtherProcessNames.add(info.processName);
                }
            }
        }

        if(packageInfo1.providers!=null){
            for(ProviderInfo info:packageInfo1.providers){
                if(!mOtherProcessNames.contains(info.processName)&&!items.containsKey(info.processName)){
                    mOtherProcessNames.add(info.processName);
                }
            }
        }

        if(packageInfo1.services!=null){
            for(ServiceInfo info:packageInfo1.services){
                if(!mOtherProcessNames.contains(info.processName)&&!items.containsKey(info.processName)){
                    mOtherProcessNames.add(info.processName);
                }
            }
        }
    }

    public List<String> getOtherProcessNames() {
        return mOtherProcessNames;
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

    private void addServiceInfo(ServiceInfo info){
        if(TextUtils.isEmpty(info.processName)){
            info.processName=info.packageName;
        }
        ProcessItem item=items.get(info.processName);
        if(items==null){
            item=new ProcessItem();
            item.name=info.processName;
            items.put(info.processName,item);
        }
        item.addServiceInfo(info);
    }

    private void addProviderInfo(ProviderInfo info){
        if(TextUtils.isEmpty(info.processName)){
            info.processName=info.packageName;
        }
        ProcessItem item=items.get(info.processName);
        if(item==null){
            item=new ProcessItem();
            item.name=info.processName;
            items.put(info.processName,item);
        }
        item.addProviderInfo(info);
    }

    private static final Comparator<ComponentInfo> sComponentInfoComparator = new Comparator<ComponentInfo>() {
        @Override
        public int compare(ComponentInfo lhs, ComponentInfo rhs) {
            return Collator.getInstance().compare(lhs.name, rhs.name);
        }
    };

    List<ActivityInfo> getActivityInfoForProcessName(String processName, boolean dialogStyle) {
        ProcessItem item = items.get(processName);
        Collection<ActivityInfo> values = item.activityInfos.values();
        ArrayList<ActivityInfo> activityInfos = new ArrayList<ActivityInfo>();
        for (ActivityInfo info : values) {
            if (dialogStyle) {
                if (info.name.startsWith(ActivityStub.Dialog.class.getName())) {
                    activityInfos.add(info);
                }
            } else {
                if (!info.name.startsWith(ActivityStub.Dialog.class.getName())) {
                    activityInfos.add(info);
                }
            }
        }

        Collections.sort(activityInfos, sComponentInfoComparator);
        return activityInfos;
    }

    List<ServiceInfo> getServiceInfoForProcessName(String processName){
        ProcessItem item=items.get(processName);
        ArrayList<ServiceInfo> serviceInfos=new ArrayList<>(item.serviceInfos.values());
        Collections.sort(serviceInfos,sComponentInfoComparator);
        return serviceInfos;
    }

    List<String> getProcessNames(){
        return new ArrayList<>(items.keySet());
    }

}

















































