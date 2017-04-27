package com.earthgee.library.pm.parser;

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

import com.earthgee.library.core.PluginDirHelper;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.util.ComponentNameComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhaoruixuan on 2017/4/17.
 * 通过反射系统PackageParser解析插件包并保存信息
 */
public class PluginPackageParser {

    private final File mPluginFile;
    private final PackageParser mParser;
    private final String mPackageName;
    private final Context mHostContext;
    private final PackageInfo mHostPackageInfo;

    private Map<ComponentName,Object> mActivityObjCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,Object> mServiceObjCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,Object> mProviderObjCache =new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,Object> mReceiverObjCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,Object> mInstrumentationObjCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,Object> mPermissionsObjCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,Object> mPermissionGroupObjCache=new TreeMap<>(new ComponentNameComparator());
    private ArrayList<String> mRequestPermissionsCache=new ArrayList<>();

    private Map<ComponentName,List<IntentFilter>> mActivityIntentFilterCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,List<IntentFilter>> mServiceIntentFilterCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,List<IntentFilter>> mProviderIntentFilterCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,List<IntentFilter>> mReceiverIntentFilterCache=new TreeMap<>(new ComponentNameComparator());

    private Map<ComponentName,ActivityInfo> mActivityInfoCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,ServiceInfo> mServiceInfoCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,ProviderInfo> mProviderInfoCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,ActivityInfo> mReceiversInfoCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,InstrumentationInfo> mInstrumentationInfoCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,PermissionGroupInfo> mPermissionGroupInfoCache=new TreeMap<>(new ComponentNameComparator());
    private Map<ComponentName,PermissionInfo> mPermissionsInfoCache=new TreeMap<>(new ComponentNameComparator());

    public PluginPackageParser(Context hostContext, File pluginFile) throws Exception{
        mHostContext=hostContext;
        mPluginFile=pluginFile;
        mParser=PackageParser.newPluginParser(hostContext);
        mParser.parsePackage(pluginFile,0);
        mPackageName=mParser.getPackageName();
        mHostPackageInfo=mHostContext.getPackageManager().
                getPackageInfo(mHostContext.getPackageName(), 0);

        List datas=mParser.getActivities();
        for(Object data:datas){
            ComponentName componentName=new ComponentName(mPackageName,mParser.readNameFromComponent(data));
            synchronized (mActivityObjCache){
                mActivityObjCache.put(componentName,data);
            }

            synchronized (mActivityInfoCache){
                ActivityInfo value=mParser.generateActivityInfo(data,0);
                fixApplicationInfo(value.applicationInfo);
                if(TextUtils.isEmpty(value.processName)){
                    value.processName=value.packageName;
                }
                mActivityInfoCache.put(componentName,value);
            }

            List<IntentFilter> filters=mParser.readIntentFilterFromComponent(data);
            synchronized (mActivityIntentFilterCache){
                mActivityIntentFilterCache.remove(componentName);
                mActivityIntentFilterCache.put(componentName,new ArrayList<IntentFilter>(filters));
            }
        }

        datas=mParser.getServices();
        for(Object data:datas){
            ComponentName componentName=new ComponentName(mPackageName,mParser.readNameFromComponent(data));
            synchronized (mServiceObjCache){
                mServiceObjCache.put(componentName,data);
            }
            synchronized (mServiceInfoCache){
                ServiceInfo value=mParser.generateServiceInfo(data,0);
                fixApplicationInfo(value.applicationInfo);
                if(TextUtils.isEmpty(value.processName)){
                    value.processName=value.packageName;
                }
                mServiceInfoCache.put(componentName,value);
            }

            List<IntentFilter> filters=mParser.readIntentFilterFromComponent(data);
            synchronized (mServiceIntentFilterCache){
                mServiceIntentFilterCache.remove(componentName);
                mServiceIntentFilterCache.put(componentName,new ArrayList<IntentFilter>(filters));
            }
        }

        datas=mParser.getProviders();
        for(Object data:datas){
            ComponentName componentName=new ComponentName(mPackageName,mParser.readNameFromComponent(data));
            synchronized (mProviderObjCache){
                mProviderObjCache.put(componentName,data);
            }
            synchronized (mProviderInfoCache){
                ProviderInfo value=mParser.generateProviderInfo(data,0);
                fixApplicationInfo(value.applicationInfo);
                if(TextUtils.isEmpty(value.processName)){
                    value.processName=value.packageName;
                }
                mProviderInfoCache.put(componentName,value);
            }

            List<IntentFilter> filters=mParser.readIntentFilterFromComponent(data);
            synchronized (mProviderIntentFilterCache){
                mProviderIntentFilterCache.remove(componentName);
                mProviderIntentFilterCache.put(componentName,new ArrayList<IntentFilter>(filters));
            }
        }

        datas=mParser.getReceivers();
        for(Object data:datas){
            ComponentName componentName=new ComponentName(mPackageName,mParser.readNameFromComponent(data));
            synchronized (mReceiverObjCache){
                mReceiverObjCache.put(componentName,data);
            }
            synchronized (mReceiversInfoCache){
                ActivityInfo value=mParser.generateReceiverInfo(data,0);
                fixApplicationInfo(value.applicationInfo);
                if(TextUtils.isEmpty(value.processName)){
                    value.processName=value.packageName;
                }
                mReceiversInfoCache.put(componentName,value);
            }

            List<IntentFilter> filters=mParser.readIntentFilterFromComponent(data);
            synchronized (mReceiverIntentFilterCache){
                mReceiverIntentFilterCache.remove(componentName);
                mReceiverIntentFilterCache.put(componentName,new ArrayList<IntentFilter>(filters));
            }
        }

        datas=mParser.getInstrumentations();
        for(Object data:datas){
            ComponentName componentName=new ComponentName(mPackageName,mParser.readNameFromComponent(data));
            synchronized (mInstrumentationObjCache){
                mInstrumentationObjCache.put(componentName,data);
            }
        }

        datas=mParser.getPermissions();
        for(Object data:datas){
            String cls=mParser.readNameFromComponent(data);
            if(cls!=null){
                ComponentName componentName=new ComponentName(mPackageName,cls);
                synchronized(mPermissionsObjCache){
                    mPermissionsObjCache.put(componentName,data);
                }
                synchronized (mPermissionsInfoCache){
                    PermissionInfo value=mParser.generatePermissionInfo(data,0);
                    mPermissionsInfoCache.put(componentName,value);
                }
            }
        }

        datas=mParser.getPermissionGroups();
        for(Object data:datas){
            ComponentName componentName=new ComponentName(mPackageName,mParser.readNameFromComponent(data));
            synchronized (mPermissionGroupObjCache){
                mPermissionGroupObjCache.put(componentName,data);
            }
        }

        List<String> requestedPermissions=mParser.getRequestedPermissions();
        if(requestedPermissions!=null&&requestedPermissions.size()>0){
            synchronized (mRequestPermissionsCache){
                mRequestPermissionsCache.addAll(requestedPermissions);
            }
        }
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
                    FieldUtils.writeField(applicationInfo,"sacnSourceDir",applicationInfo.dataDir,true);
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

        applicationInfo.uid=mHostPackageInfo.applicationInfo.uid;

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.GINGERBREAD){
            if(applicationInfo.nativeLibraryDir==null){
                applicationInfo.nativeLibraryDir=PluginDirHelper.
                        getPluginNativeLibraryDir(mHostContext,applicationInfo.packageName);
            }
        }

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            if(applicationInfo.splitSourceDirs==null){
                applicationInfo.splitSourceDirs=new String[]{mPluginFile.getPath()};
            }
        }

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            if(applicationInfo.splitPublicSourceDirs==null){
                applicationInfo.splitPublicSourceDirs=new String[]{mPluginFile.getPath()};
            }
        }

        if(TextUtils.isEmpty(applicationInfo.processName)){
            applicationInfo.processName=applicationInfo.packageName;
        }
        return applicationInfo;
    }

    public String getPackageName() throws Exception{
        return mPackageName;
    }

    public void collectCertificates(int flags) throws Exception{
        mParser.collectCertificates(flags);
    }

    public PackageInfo getPackageInfo(int flags) throws Exception{
        PackageInfo packageInfo=
                mParser.generatePackageInfo
                        (mHostPackageInfo.gids,flags,mPluginFile.lastModified(),mPluginFile.lastModified(),new HashSet<String>(getRequestedPermissions()));
        fixPackageInfo(packageInfo);
        return packageInfo;
    }

    public List<String> getRequestedPermissions() throws Exception{
        synchronized (mRequestPermissionsCache){
            return new ArrayList<>(mRequestPermissionsCache);
        }
    }

    private PackageInfo fixPackageInfo(PackageInfo packageInfo){
        packageInfo.gids=mHostPackageInfo.gids;
        fixApplicationInfo(packageInfo.applicationInfo);
        return packageInfo;
    }

    public void writeSignature(Signature[] signatures) throws Exception{
        if(signatures!=null){
            mParser.writeSignature(signatures);
        }
    }

    public File getPluginFile() {
        return mPluginFile;
    }

    public ActivityInfo getActivityInfo(ComponentName className,int flags) throws Exception{
        Object data;
        synchronized (mActivityInfoCache){
            data=mActivityInfoCache.get(className);
        }
        if(data!=null){
            ActivityInfo activityInfo=mParser.generateActivityInfo(data,flags);
            fixApplicationInfo(activityInfo.applicationInfo);
            if(TextUtils.isEmpty(activityInfo.processName)){
                activityInfo.processName=activityInfo.packageName;
            }
            return activityInfo;
        }
        return null;
    }

    public List<ActivityInfo> getActivities() throws Exception{
        return new ArrayList<>(mActivityInfoCache.values());
    }

    public List<ServiceInfo> getServices() throws Exception{
        return new ArrayList<>(mServiceInfoCache.values());
    }

    public List<ProviderInfo> getProviders() throws Exception{
        return new ArrayList<>(mProviderInfoCache.values());
    }

    public List<ActivityInfo> getReceivers() throws Exception{
        return new ArrayList<>(mReceiversInfoCache.values());
    }

    public List<PermissionInfo> getPermissions() throws Exception{
        return new ArrayList<>(mPermissionsInfoCache.values());
    }

    public List<PermissionGroupInfo> getPermissionGroups() throws Exception{
        return new ArrayList<>(mPermissionGroupInfoCache.values());
    }

    public List<InstrumentationInfo> getInstrumentatiInfos(){
        return new ArrayList<>(mInstrumentationInfoCache.values());
    }

}




























