package com.earthgee.libaray.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

import com.earthgee.libaray.pm.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class RunningProcessList {

    private Context mHostContext;

    public void setContext(Context context) {
        this.mHostContext = context;
    }

    //正在运行的进程item
    private class ProcessItem{
        private String stubProcessName;
        private String targetProcessName;
        private int pid;
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
    }

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

}


























