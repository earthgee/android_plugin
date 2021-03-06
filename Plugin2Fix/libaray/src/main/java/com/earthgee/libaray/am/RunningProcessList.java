package com.earthgee.libaray.am;

import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.libaray.pm.PluginManager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class RunningProcessList {

    private Context mHostContext;
    private static final Collator sCollator = Collator.getInstance();

    public void setContext(Context context) {
        this.mHostContext = context;
    }

    private static Comparator sComponentInfoComparator = new Comparator<ComponentInfo>() {
        @Override
        public int compare(ComponentInfo lhs, ComponentInfo rhs) {
            return sCollator.compare(lhs.name, rhs.name);
        }
    };

    private static Comparator sProviderInfoComparator = new Comparator<ProviderInfo>() {
        @Override
        public int compare(ProviderInfo lhs, ProviderInfo rhs) {
            return sCollator.compare(lhs.authority, rhs.authority);
        }
    };

    //正在运行的进程item
    private class ProcessItem{
        private String stubProcessName;
        private String targetProcessName;
        //进程pid
        private int pid;
        //进程uid
        private int uid;
        private long startTime;

        private List<String> pkgs=new ArrayList<>();

        private Map<String,ActivityInfo> targetActivityInfos=new HashMap<>();

        //正在运行的插件ProviderInfo
        //key=ProviderInfo.authority, value=插件的ProviderInfo
        private Map<String, ProviderInfo> targetProviderInfos = new HashMap<String, ProviderInfo>(1);

        //正在运行的插件ServiceInfo
        //key=ServiceInfo.name, value=插件的ServiceInfo
        private Map<String, ServiceInfo> targetServiceInfos = new HashMap<String, ServiceInfo>(1);


        //正在运行的插件ActivityInfo与代理ActivityInfo的映射
        //key=代理ActivityInfo.name, value=插件的ActivityInfo.name,
        private Map<String, Set<ActivityInfo>> activityInfosMap = new HashMap<String, Set<ActivityInfo>>(4);


        //正在运行的插件ProviderInfo与代理ProviderInfo的映射
        //key=代理ProviderInfo.authority, value=插件的ProviderInfo.authority,
        private Map<String, Set<ProviderInfo>> providerInfosMap = new HashMap<String, Set<ProviderInfo>>(4);

        //正在运行的插件ServiceInfo与代理ServiceInfo的映射
        //key=代理ServiceInfo.name, value=插件的ServiceInfo.name,
        private Map<String, Set<ServiceInfo>> serviceInfosMap = new HashMap<String, Set<ServiceInfo>>(4);

        private void updatePkgs() {
            ArrayList<String> newList = new ArrayList<String>();
            for (ActivityInfo info : targetActivityInfos.values()) {
                newList.add(info.packageName);
            }

            for (ServiceInfo info : targetServiceInfos.values()) {
                newList.add(info.packageName);
            }

            for (ProviderInfo info : targetProviderInfos.values()) {
                newList.add(info.packageName);
            }
            pkgs.clear();
            pkgs.addAll(newList);
        }

        private void addActivityInfo(String stubActivityName,ActivityInfo info){
            if(!targetActivityInfos.containsKey(info.name)){
                targetActivityInfos.put(info.name,info);
            }

            if(!pkgs.contains(info.packageName)){
                pkgs.add(info.packageName);
            }

            Set<ActivityInfo> list=activityInfosMap.get(stubActivityName);
            if(list==null){
                list=new TreeSet<>(sComponentInfoComparator);
                list.add(info);
                activityInfosMap.put(stubActivityName,list);
            }else{
                list.add(info);
            }
        }

        private void addServiceInfo(String stubServiceName,ServiceInfo info){
            if(!targetServiceInfos.containsKey(info.name)){
                targetServiceInfos.put(info.name,info);

                if(!pkgs.contains(info.packageName)){
                    pkgs.add(info.packageName);
                }

                Set<ServiceInfo> list=serviceInfosMap.get(stubServiceName);
                if(list==null){
                    list=new TreeSet<>(sComponentInfoComparator);
                    list.add(info);
                    serviceInfosMap.put(stubServiceName,list);
                }else{
                    list.add(info);
                }
            }
        }

        void removeServiceInfo(String stubServiceName,ServiceInfo targetInfo){
            targetServiceInfos.remove(targetInfo.name);
            if(stubServiceName==null){
                for(Set<ServiceInfo> set:serviceInfosMap.values()){
                    set.remove(targetInfo);
                }
            }else{
                Set<ServiceInfo> list=serviceInfosMap.get(stubServiceName);
                if(list!=null){
                    list.remove(targetInfo);
                }
            }
            updatePkgs();
        }

        private void addProviderInfo(String stubAuthority, ProviderInfo info) {
            if (!targetProviderInfos.containsKey(info.authority)) {
                targetProviderInfos.put(info.authority, info);

                if (!pkgs.contains(info.packageName)) {
                    pkgs.add(info.packageName);
                }

                //stub map to activity info
                Set<ProviderInfo> list = providerInfosMap.get(stubAuthority);
                if (list == null) {
                    list = new TreeSet<ProviderInfo>(sProviderInfoComparator);
                    list.add(info);
                    providerInfosMap.put(stubAuthority, list);
                } else {
                    list.add(info);
                }
            }
        }
    }

    //key=pid
    private Map<Integer,ProcessItem> items=new HashMap<>();

    public String getStubProcessByTarget(ComponentInfo targetInfo){
        for(ProcessItem processItem:items.values()){
            if(processItem.pkgs.contains(targetInfo.packageName)&& TextUtils.equals(processItem.targetProcessName,targetInfo.processName)){
                return processItem.stubProcessName;
            }else{
                try{
                    boolean signed=false;
                    for(String pkg:processItem.pkgs){
                        if(PluginManager.getInstance().checkSignatures(targetInfo.packageName,pkg)== PackageManager.SIGNATURE_MATCH){
                            signed=true;
                            break;
                        }
                    }
                    if(signed&&TextUtils.equals(processItem.targetProcessName,targetInfo.processName)){
                        return processItem.stubProcessName;
                    }
                }catch (Exception e){
                }
            }
        }
        return null;
    }

    void setTargetProcessName(ComponentInfo stubInfo,ComponentInfo targetInfo){
        for(ProcessItem item:items.values()){
            if(TextUtils.equals(item.stubProcessName,stubInfo.processName)){
                if(!item.pkgs.contains(targetInfo.packageName)){
                    item.pkgs.add(targetInfo.packageName);
                }
                item.targetProcessName=targetInfo.processName;
            }
        }
    }

    boolean isStubInfoUsed(ActivityInfo stubInfo,ActivityInfo targetInfo,String stubProcessName){
        for(Integer pid:items.keySet()){
            ProcessItem item=items.get(pid);
            if(TextUtils.equals(item.stubProcessName,stubProcessName)){
                Set<ActivityInfo> infos=item.activityInfosMap.get(stubInfo.name);
                if(infos!=null&&infos.size()>0){
                    for(ActivityInfo info:infos){
                        if(TextUtils.equals(info.name,targetInfo.name)&&
                                TextUtils.equals(info.packageName,targetInfo.packageName)){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    boolean isStubInfoUsed(ServiceInfo stubInfo){
        return false;
    }

    boolean isProcessRunning(String stubProcessName){
        for(ProcessItem processItem:items.values()){
            if(TextUtils.equals(stubProcessName,processItem.stubProcessName)){
                return true;
            }
        }

        return false;
    }

    boolean isPkgEmpty(String stubProcessName){
        for(ProcessItem item:items.values()){
            if(TextUtils.equals(stubProcessName,item.stubProcessName)){
                return item.pkgs.size()<=0;
            }
        }
        return true;
    }

    boolean isPkgCanRunInProcess(String packageName,String stubProcessName,String targetProcessName) throws RemoteException{
        for(ProcessItem item:items.values()){
            if(TextUtils.equals(stubProcessName,item.stubProcessName)){
                if(!TextUtils.isEmpty(item.targetProcessName)&&!TextUtils.equals(item.targetProcessName,targetProcessName)){
                    continue;
                }

                if(item.pkgs.contains(packageName)){
                    return true;
                }

                boolean signed=false;
                for(String pkg:item.pkgs){
                    if(PluginManager.getInstance().checkSignatures(packageName,pkg)==PackageManager.SIGNATURE_MATCH){
                        signed=true;
                        break;
                    }
                }
                if(signed){
                    return true;
                }
            }
        }
        return false;
    }

    boolean isPlugin(int pid){
        ProcessItem item=items.get(pid);
        if(item!=null){
            return !TextUtils.isEmpty(item.stubProcessName)&&!TextUtils.isEmpty(item.targetProcessName);
        }
        return false;
    }

    //是否是持久化的app
    public boolean isPersistentApplication(int pid){
        for(ProcessItem processItem:items.values()){
            if(processItem.pid==pid){
                if(processItem.targetActivityInfos!=null&&processItem.targetActivityInfos.size()>0){
                    for(ActivityInfo info:processItem.targetActivityInfos.values()){
                        if((info.applicationInfo.flags& ApplicationInfo.FLAG_PERSISTENT)!=0){
                            return true;
                        }
                    }
                }

                if (processItem.targetProviderInfos != null && processItem.targetProviderInfos.size() > 0) {
                    for (ProviderInfo info : processItem.targetProviderInfos.values()) {
                        if ((info.applicationInfo.flags & ApplicationInfo.FLAG_PERSISTENT) != 0) {
                            return true;
                        }
                    }
                }

                if (processItem.targetServiceInfos != null && processItem.targetServiceInfos.size() > 0) {
                    for (ServiceInfo info : processItem.targetServiceInfos.values()) {
                        if ((info.applicationInfo.flags & ApplicationInfo.FLAG_PERSISTENT) != 0) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    int getActivityCountByPid(int pid){
        ProcessItem item=items.get(pid);
        return item!=null?item.targetActivityInfos.size():0;
    }

    int getServiceCountByPid(int pid){
        ProcessItem item=items.get(pid);
        return item!=null?item.targetServiceInfos.size():0;
    }

    int getProviderCountByPid(int pid){
        ProcessItem item=items.get(pid);
        return item!=null?item.targetProviderInfos.size():0;
    }

    List<String> getStubServiceByPid(int pid){
        ProcessItem item=items.get(pid);
        if(item!=null&&item.serviceInfosMap!=null&&item.serviceInfosMap.size()>0){
            return new ArrayList<>(item.serviceInfosMap.keySet());
        }
        return null;
    }

    //每个进程接受到service bind成功会调到这里
    void addItem(int pid,int uid){
        ProcessItem item=items.get(pid);
        if(item==null){
            item=new ProcessItem();
            item.pid=pid;
            item.uid=uid;
            item.startTime=System.currentTimeMillis();
            items.put(pid,item);
        }else{
            item.pid=pid;
            item.uid=uid;
            item.startTime=System.currentTimeMillis();
        }
    }

    void setProcessName(int pid,String stubProcessName,String targetProcessName,String targetPkg){
        ProcessItem item=items.get(pid);
        if(item!=null){
            if(!item.pkgs.contains(targetPkg)){
                item.pkgs.add(targetPkg);
            }
            item.targetProcessName=targetProcessName;
            item.stubProcessName=stubProcessName;
        }
    }

    void addActivityInfo(int pid,int uid,ActivityInfo stubInfo,ActivityInfo targetInfo){
        ProcessItem item=items.get(pid);
        if (TextUtils.isEmpty(targetInfo.processName)) {
            targetInfo.processName=targetInfo.packageName;
        }
        if(item==null){
            item=new ProcessItem();
            item.pid=pid;
            item.uid=uid;
            items.put(pid,item);
        }
        item.stubProcessName=stubInfo.processName;
        if(!item.pkgs.contains(targetInfo.packageName)){
            item.pkgs.add(targetInfo.packageName);
        }
        item.targetProcessName=targetInfo.processName;
        item.addActivityInfo(stubInfo.name,targetInfo);
    }

    void addServiceInfo(int pid,int uid,ServiceInfo stubInfo,ServiceInfo targetInfo){
        ProcessItem item=items.get(pid);
        if(TextUtils.isEmpty(targetInfo.processName)){
            targetInfo.processName=targetInfo.packageName;
        }
        if(item==null){
            item=new ProcessItem();
            item.pid=pid;
            item.uid=uid;
            items.put(pid,item);
        }
        item.stubProcessName=stubInfo.processName;
        if(!item.pkgs.contains(targetInfo.packageName)){
            item.pkgs.add(targetInfo.packageName);
        }
        item.targetProcessName=targetInfo.processName;
        item.addServiceInfo(stubInfo.name,targetInfo);
    }

    void addProviderInfo(int pid,int uid,ProviderInfo stubInfo,ProviderInfo targetInfo){
        ProcessItem item=items.get(pid);
        if(TextUtils.isEmpty(targetInfo.processName)){
            targetInfo.processName=targetInfo.packageName;
        }
        if(item==null){
            item=new ProcessItem();
            item.pid=pid;
            item.uid=uid;
            items.put(pid,item);
        }
        item.stubProcessName=stubInfo.processName;
        if(!item.pkgs.contains(targetInfo.packageName)){
            item.pkgs.add(targetInfo.packageName);
        }
        item.targetProcessName=targetInfo.processName;
        item.addProviderInfo(stubInfo.authority,targetInfo);
    }

    void removeServiceInfo(int pid,int uid,ServiceInfo stubInfo,ServiceInfo targetInfo){
        ProcessItem item=items.get(pid);
        if(TextUtils.isEmpty(targetInfo.processName)){
            targetInfo.processName=targetInfo.packageName;
        }
        if(item!=null){
            if(stubInfo!=null){
                item.removeServiceInfo(stubInfo.name,targetInfo);
            }else{
                item.removeServiceInfo(null,targetInfo);
            }
        }
    }

    String getTargetProcessNameByPid(int pid){
        ProcessItem item=items.get(pid);
        return item!=null?item.targetProcessName:null;
    }

    boolean isStubInfoUsed(ProviderInfo stubInfo) {
        //TODO
        return false;
    }

}


























