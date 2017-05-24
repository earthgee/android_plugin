package com.earthgee.library.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.library.pm.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaoruixuan on 2017/5/3.
 * 除host进程外运行中的进程信息
 */
public class RunningProcessList {

    private Context mHostContext;

    public void setContext(Context context){
        this.mHostContext=context;
    }

    void clear(){
        items.clear();
    }

    private Map<Integer,ProcessItem> items=new HashMap<>();

    private class ProcessItem{
        private String stubProcessName;
        private String targetProcessName;
        private int pid;
        private int uid;
        private long startTime;

        private List<String> pkgs=new ArrayList<>();
        private Map<String,ActivityInfo> targetActivityInfo=new HashMap<>();
        private Map<String,ProviderInfo> targetProviderInfo=new HashMap<>();
        private Map<String,ServiceInfo> targetServiceInfo=new HashMap<>();
        private Map<String,Set<ActivityInfo>> activityInfosMap=new HashMap<>();
        private Map<String,Set<ProviderInfo>> providerInfosMap=new HashMap<>();
        private Map<String,Set<ServiceInfo>> serviceInfosMap=new HashMap<>();

    }

    public String getStubProcessByTarget(ComponentInfo targetInfo){
        for (ProcessItem processItem:items.values()){
            if(processItem.pkgs.contains(targetInfo.packageName)&&
                    TextUtils.equals(processItem.targetProcessName,targetInfo.processName)){
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

    boolean isProcessRunning(String stubProcessName){
        for(ProcessItem processItem:items.values()){
            if(TextUtils.equals(stubProcessName,processItem.stubProcessName)){
                return true;
            }
        }
        return false;
    }

    boolean isPkgEmpty(String stubProcessName){
        for (ProcessItem item:items.values()){
            if(TextUtils.equals(stubProcessName,item.stubProcessName)){
                return item.pkgs.size()<=0;
            }
        }
        return true;
    }

    boolean isPkgCanRunInProcess(String packagename,String stubProcessName,String targetProcessName) throws RemoteException{
        for(ProcessItem item:items.values()){
            if(TextUtils.equals(stubProcessName,item.stubProcessName)){
                if(!TextUtils.isEmpty(item.targetProcessName)&&!TextUtils.equals(item.targetProcessName,targetProcessName)){
                    continue;
                }

                if(item.pkgs.contains(packagename)){
                    return true;
                }

                boolean signed=false;
                for(String pkg:item.pkgs){
                    if(PluginManager.getInstance().checkSignatures(packagename,pkg)==PackageManager.SIGNATURE_MATCH){
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

    void setProcessName(int pid,String stubProcessName,String targetProcessName,String targetPkg){
        ProcessItem item=items.get(pid);
        if(item!=null){
            if(item.pkgs.contains(targetPkg)){
                item.pkgs.add(targetPkg);
            }
            item.targetProcessName=targetProcessName;
            item.stubProcessName=stubProcessName;
        }
    }

}





















