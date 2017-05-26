package com.earthgee.libaray.pm.parser;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.text.TextUtils;

import com.earthgee.libaray.core.PluginDirHelper;
import com.earthgee.libaray.helper.ComponentNameComparator;
import com.earthgee.libaray.reflect.FieldUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhaoruixuan on 2017/5/25.
 */
public class PluginPackageParser {

    private final File mPluginFile;
    private final PackageParser mParser;
    private final String mPackageName;
    private final Context mHostContext;
    private final PackageInfo mHostPackgeInfo;

    private Map<ComponentName, Object> mActivityObjCache = new TreeMap<ComponentName, Object>(new ComponentNameComparator());
    private Map<ComponentName, Object> mServiceObjCache = new TreeMap<ComponentName, Object>(new ComponentNameComparator());
    private Map<ComponentName, Object> mProviderObjCache = new TreeMap<ComponentName, Object>(new ComponentNameComparator());
    private Map<ComponentName, Object> mReceiversObjCache = new TreeMap<ComponentName, Object>(new ComponentNameComparator());
    private Map<ComponentName, Object> mInstrumentationObjCache = new TreeMap<ComponentName, Object>(new ComponentNameComparator());
    private Map<ComponentName, Object> mPermissionsObjCache = new TreeMap<ComponentName, Object>(new ComponentNameComparator());
    private Map<ComponentName, Object> mPermissionGroupObjCache = new TreeMap<ComponentName, Object>(new ComponentNameComparator());
    private ArrayList<String> mRequestedPermissionsCache = new ArrayList<String>();


    private Map<ComponentName, List<IntentFilter>> mActivityIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(new ComponentNameComparator());
    private Map<ComponentName, List<IntentFilter>> mServiceIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(new ComponentNameComparator());
    private Map<ComponentName, List<IntentFilter>> mProviderIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(new ComponentNameComparator());
    private Map<ComponentName, List<IntentFilter>> mReceiverIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(new ComponentNameComparator());


    private Map<ComponentName, ActivityInfo> mActivityInfoCache = new TreeMap<ComponentName, ActivityInfo>(new ComponentNameComparator());
    private Map<ComponentName, ServiceInfo> mServiceInfoCache = new TreeMap<ComponentName, ServiceInfo>(new ComponentNameComparator());
    private Map<ComponentName, ProviderInfo> mProviderInfoCache = new TreeMap<ComponentName, ProviderInfo>(new ComponentNameComparator());
    private Map<ComponentName, ActivityInfo> mReceiversInfoCache = new TreeMap<ComponentName, ActivityInfo>(new ComponentNameComparator());
    private Map<ComponentName, InstrumentationInfo> mInstrumentationInfoCache = new TreeMap<ComponentName, InstrumentationInfo>(new ComponentNameComparator());
    private Map<ComponentName, PermissionGroupInfo> mPermissionGroupInfoCache = new TreeMap<ComponentName, PermissionGroupInfo>(new ComponentNameComparator());
    private Map<ComponentName, PermissionInfo> mPermissionsInfoCache = new TreeMap<ComponentName, PermissionInfo>(new ComponentNameComparator());

    public PluginPackageParser(Context hostContext, File pluginFile) throws Exception{
        mHostContext=hostContext;
        mPluginFile=pluginFile;
        mParser=PackageParser.newPluginParser(hostContext);
        mParser.parsePackage(pluginFile,0);
        mPackageName=mParser.getPackageName();
        mHostPackgeInfo=mHostContext.getPackageManager().getPackageInfo(mHostContext.getPackageName(),0);

        List datas = mParser.getActivities();
        for (Object data : datas) {
            ComponentName componentName = new ComponentName(mPackageName, mParser.readNameFromComponent(data));
            synchronized (mActivityObjCache) {
                mActivityObjCache.put(componentName, data);
            }
            synchronized (mActivityInfoCache) {
                ActivityInfo value = mParser.generateActivityInfo(data, 0);
                fixApplicationInfo(value.applicationInfo);
                if (TextUtils.isEmpty(value.processName)) {
                    value.processName = value.packageName;
                }
                mActivityInfoCache.put(componentName, value);
            }

            List<IntentFilter> filters = mParser.readIntentFilterFromComponent(data);
            synchronized (mActivityIntentFilterCache) {
                mActivityIntentFilterCache.remove(componentName);
                mActivityIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
            }
        }

        datas = mParser.getServices();
        for (Object data : datas) {
            ComponentName componentName = new ComponentName(mPackageName, mParser.readNameFromComponent(data));
            synchronized (mServiceObjCache) {
                mServiceObjCache.put(componentName, data);
            }
            synchronized (mServiceInfoCache) {
                ServiceInfo value = mParser.generateServiceInfo(data, 0);
                fixApplicationInfo(value.applicationInfo);
                if (TextUtils.isEmpty(value.processName)) {
                    value.processName = value.packageName;
                }
                mServiceInfoCache.put(componentName, value);
            }

            List<IntentFilter> filters = mParser.readIntentFilterFromComponent(data);
            synchronized (mServiceIntentFilterCache) {
                mServiceIntentFilterCache.remove(componentName);
                mServiceIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
            }
        }


        datas = mParser.getProviders();
        for (Object data : datas) {
            ComponentName componentName = new ComponentName(mPackageName, mParser.readNameFromComponent(data));
            synchronized (mProviderObjCache) {
                mProviderObjCache.put(componentName, data);
            }
            synchronized (mProviderInfoCache) {
                ProviderInfo value = mParser.generateProviderInfo(data, 0);
                fixApplicationInfo(value.applicationInfo);
                if (TextUtils.isEmpty(value.processName)) {
                    value.processName = value.packageName;
                }
                mProviderInfoCache.put(componentName, value);
            }

            List<IntentFilter> filters = mParser.readIntentFilterFromComponent(data);
            synchronized (mProviderIntentFilterCache) {
                mProviderIntentFilterCache.remove(componentName);
                mProviderIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
            }
        }


        datas = mParser.getReceivers();
        for (Object data : datas) {
            ComponentName componentName = new ComponentName(mPackageName, mParser.readNameFromComponent(data));
            synchronized (mReceiversObjCache) {
                mReceiversObjCache.put(componentName, data);
            }

            synchronized (mReceiversInfoCache) {
                ActivityInfo value = mParser.generateReceiverInfo(data, 0);
                fixApplicationInfo(value.applicationInfo);
                if (TextUtils.isEmpty(value.processName)) {
                    value.processName = value.packageName;
                }
                mReceiversInfoCache.put(componentName, value);
            }
            List<IntentFilter> filters = mParser.readIntentFilterFromComponent(data);
            synchronized (mReceiverIntentFilterCache) {
                mReceiverIntentFilterCache.remove(componentName);
                mReceiverIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
            }
        }

        datas = mParser.getInstrumentations();
        for (Object data : datas) {
            ComponentName componentName = new ComponentName(mPackageName, mParser.readNameFromComponent(data));
            synchronized (mInstrumentationObjCache) {
                mInstrumentationObjCache.put(componentName, data);
            }
        }

        datas = mParser.getPermissions();
        for (Object data : datas) {
            String cls = mParser.readNameFromComponent(data);
            if (cls != null) {
                ComponentName componentName = new ComponentName(mPackageName, cls);
                synchronized (mPermissionsObjCache) {
                    mPermissionsObjCache.put(componentName, data);
                }
                synchronized (mPermissionsInfoCache) {
                    PermissionInfo value = mParser.generatePermissionInfo(data, 0);
                    mPermissionsInfoCache.put(componentName, value);
                }
            }
        }

        datas = mParser.getPermissionGroups();
        for (Object data : datas) {
            ComponentName componentName = new ComponentName(mPackageName, mParser.readNameFromComponent(data));
            synchronized (mPermissionGroupObjCache) {
                mPermissionGroupObjCache.put(componentName, data);
            }
        }

        List<String> requestedPermissions = mParser.getRequestedPermissions();
        if (requestedPermissions != null && requestedPermissions.size() > 0) {
            synchronized (mRequestedPermissionsCache) {
                mRequestedPermissionsCache.addAll(requestedPermissions);
            }
        }
    }

    public File getPluginFile(){
        return mPluginFile;
    }

    public ApplicationInfo getApplicationInfo(int flags) throws Exception{
        ApplicationInfo applicationInfo=mParser.generateApplicationInfo(flags);
        fixApplicationInfo(applicationInfo);
        if(TextUtils.isEmpty(applicationInfo.processName)){
            applicationInfo.processName=applicationInfo.packageName;
        }
        return applicationInfo;
    }

    public void collectCertificates(int flags) throws Exception{
        mParser.collectCertificates(flags);
    }

    public PackageInfo getPackageInfo(int flags) throws Exception{
        PackageInfo packageInfo=mParser.generatePackageInfo(mHostPackgeInfo.gids,flags,mPluginFile.lastModified(),
                mPluginFile.lastModified(),new HashSet<String>(getRequestedPermissions()));
        fixPackageInfo(packageInfo);
        return packageInfo;
    }

    public String getPackageName() throws Exception {
        return mPackageName;
    }

    private ApplicationInfo fixApplicationInfo(ApplicationInfo applicationInfo){
        if(applicationInfo.sourceDir==null){
            applicationInfo.sourceDir=mPluginFile.getPath();
        }
        if(applicationInfo.publicSourceDir==null){
            applicationInfo.publicSourceDir=mPluginFile.getPath();
        }

        if(applicationInfo.dataDir==null){
            applicationInfo.dataDir= PluginDirHelper.getPluginDataDir(mHostContext,applicationInfo.packageName);
        }

        try{
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                if(FieldUtils.readField(applicationInfo,"scanSourceDir",true)==null){
                    FieldUtils.writeField(applicationInfo,"scanSourceDir",applicationInfo.dataDir,true);
                }
            }
        }catch (Throwable e){
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (FieldUtils.readField(applicationInfo, "scanPublicSourceDir", true) == null) {
                    FieldUtils.writeField(applicationInfo, "scanPublicSourceDir", applicationInfo.dataDir, true);
                }
            }
        } catch (Throwable e) {
        }

        applicationInfo.uid=mHostPackgeInfo.applicationInfo.uid;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (applicationInfo.nativeLibraryDir == null) {
                applicationInfo.nativeLibraryDir = PluginDirHelper.getPluginNativeLibraryDir(mHostContext, applicationInfo.packageName);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (applicationInfo.splitSourceDirs == null) {
                applicationInfo.splitSourceDirs = new String[]{mPluginFile.getPath()};
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (applicationInfo.splitPublicSourceDirs == null) {
                applicationInfo.splitPublicSourceDirs = new String[]{mPluginFile.getPath()};
            }
        }

        if (TextUtils.isEmpty(applicationInfo.processName)) {
            applicationInfo.processName = applicationInfo.packageName;
        }
        return applicationInfo;
    }

    private PackageInfo fixPackageInfo(PackageInfo packageInfo){
        packageInfo.gids=mHostPackgeInfo.gids;
        fixApplicationInfo(packageInfo.applicationInfo);
        return packageInfo;
    }

    public List<String> getRequestedPermissions() throws Exception {
        synchronized (mRequestedPermissionsCache) {
            return new ArrayList<String>(mRequestedPermissionsCache);
        }
    }

    public void writeSignature(Signature[] signatures) throws Exception {
        if (signatures != null) {
            mParser.writeSignature(signatures);
        }
    }

    public List<ActivityInfo> getActivities(){
        return new ArrayList<>(mActivityInfoCache.values());
    }

    public List<ServiceInfo> getServices() throws Exception {
        return new ArrayList<ServiceInfo>(mServiceInfoCache.values());
    }

    public List<ProviderInfo> getProviders() throws Exception {
        return new ArrayList<ProviderInfo>(mProviderInfoCache.values());
    }

    public List<ActivityInfo> getReceivers() throws Exception {
        return new ArrayList<ActivityInfo>(mReceiversInfoCache.values());
    }

    public List<IntentFilter> getActivityIntentFilter(ComponentName className){
        synchronized (mActivityIntentFilterCache){
            return mActivityIntentFilterCache.get(className);
        }
    }

    public List<IntentFilter> getServiceIntentFilter(ComponentName className) {
        synchronized (mServiceIntentFilterCache) {
            return mServiceIntentFilterCache.get(className);
        }
    }

    public List<IntentFilter> getProviderIntentFilter(ComponentName className) {
        synchronized (mProviderObjCache) {
            return mProviderIntentFilterCache.get(className);
        }
    }

    public Map<ActivityInfo, List<IntentFilter>> getReceiverIntentFilter() {
        synchronized (mReceiverIntentFilterCache) {
            Map<ActivityInfo, List<IntentFilter>> map = new HashMap<ActivityInfo, List<IntentFilter>>();
            for (ComponentName componentName : mReceiverIntentFilterCache.keySet()) {
                map.put(mReceiversInfoCache.get(componentName), mReceiverIntentFilterCache.get(componentName));
            }
            return map;
        }
    }

    public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) {
        synchronized (mReceiverIntentFilterCache) {
            for (ComponentName componentName : mReceiverIntentFilterCache.keySet()) {
                if (TextUtils.equals(info.name, mReceiversInfoCache.get(componentName).name)) {
                    return mReceiverIntentFilterCache.get(componentName);
                }
            }
        }
        return null;
    }

    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws Exception {
        Object data;
        synchronized (mActivityObjCache) {
            data = mActivityObjCache.get(className);
        }
        if (data != null) {
            ActivityInfo activityInfo = mParser.generateActivityInfo(data, flags);
            fixApplicationInfo(activityInfo.applicationInfo);
            if (TextUtils.isEmpty(activityInfo.processName)) {
                activityInfo.processName = activityInfo.packageName;
            }
            return activityInfo;
        }
        return null;
    }

    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws Exception {
        Object data;
        synchronized (mServiceObjCache) {
            data = mServiceObjCache.get(className);
        }
        if (data != null) {
            ServiceInfo serviceInfo = mParser.generateServiceInfo(data, flags);
            fixApplicationInfo(serviceInfo.applicationInfo);
            if (TextUtils.isEmpty(serviceInfo.processName)) {
                serviceInfo.processName = serviceInfo.packageName;
            }
            return serviceInfo;
        }
        return null;
    }

    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws Exception {
        Object data;
        synchronized (mProviderObjCache) {
            data = mProviderObjCache.get(className);
        }
        if (data != null) {
            ProviderInfo providerInfo = mParser.generateProviderInfo(data, flags);
            fixApplicationInfo(providerInfo.applicationInfo);
            if (TextUtils.isEmpty(providerInfo.processName)) {
                providerInfo.processName = providerInfo.packageName;
            }
            return providerInfo;
        }
        return null;
    }

    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws Exception {
        Object data;
        synchronized (mReceiversObjCache) {
            data = mReceiversObjCache.get(className);
        }
        if (data != null) {
            ActivityInfo activityInfo = mParser.generateReceiverInfo(data, flags);
            fixApplicationInfo(activityInfo.applicationInfo);
            if (TextUtils.isEmpty(activityInfo.processName)) {
                activityInfo.processName = activityInfo.packageName;
            }
            return activityInfo;
        }
        return null;
    }

}













