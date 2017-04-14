// IPluginManager.aidl
package com.earthgee.library;

// Declare any non-default types here with import statements
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import com.earthgee.library.IApplicationCallback;
import com.earthgee.library.IPackageDataObserver;
import java.util.List;

interface IPluginManager {

    boolean waitForReady();

    PackageInfo getPackageInfo(String packageName,int flags);

    boolean isPluginPackage(String packageName);

    ActivityInfo getActivityInfo(in ComponentName className,int flags);

    ActivityInfo getReceiverInfo(in ComponentName className,int flags);

    ServiceInfo getServiceInfo(in ComponentName className,int flags);

    ProviderInfo getProviderInfo(in ComponentName className,int flags);

    ResolveInfo resolveIntent(in Intent intent,String resolveType,int flags);

    List<ResolveInfo> queryIntentActivities(in Intent intent,String resolvedType,int flags);

    List<ResolveInfo> queryIntentReceivers(in Intent intent,String resolvedType,int flags);

    ResolveInfo resolveService(in Intent intent,String resolvedType,int flags);

    List<ResolveInfo> queryIntentServices(in Intent intent,String resolvedType,int flags);

    List<ResolveInfo> queryIntentContentProviders(in Intent intent,String resolvedType,int flags);

    List<PackageInfo> getInstallPackages(int flags);

    List<ApplicationInfo> getInstalledApplications(int flags);

    PermissionInfo getPermissionInfo(String name,int flags);

    List<PermissionInfo> queryPermissionsByGroup(String group,int flags);

    PermissionGroupInfo getPermissionGroupInfo(String name,int flags);

    List<PermissionGroupInfo> getAllPermissionGroups(int flags);

    ProviderInfo resolveContentProvider(String name,int flags);

    void deleteApplicationCacheFiles(String packageName,IPackageDataObserver observer);

    void clearApplicationUserData(String packageName,IPackageDataObserver observer);

    ApplicationInfo getApplicationInfo(String packageName,int flags);

    int installPackage(String filepath,int flags);

    int deletePackage(String packageName,int flags);

    List<ActivityInfo> getReceivers(String packageName,int flags);

    List<IntentFilter> getReceiverIntentFilter(in ActivityInfo info);

    int checkSignatures(String pkg1,String pkg2);

    ActivityInfo selectStubActivityInfo(in ActivityInfo targetInfo);

    ActivityInfo selectStubActivityInfoByIntent(in Intent targetIntent);

    ServiceInfo selectStubServiceInfo(in ServiceInfo targetInfo);

    ServiceInfo selectStubServiceInfoByIntent(in Intent targetIntent);

    ServiceInfo getTargetServiceInfo(in ServiceInfo stubInfo);

    ProviderInfo selectStubProviderInfo(String name);

    List<String> getPackageNameByPid(int pid);

    String getProcessNameByPid(int pid);

    boolean killBackgroundProcesses(String packageName);

    boolean killApplicationProcess(String pluginPackageName);

    boolean forceStopPackage(String pluginPackageName);

    boolean registerApplicationCallback(IApplicationCallback callback);

    boolean unregisterApplicationCallback(IApplicationCallback callback);

    void onActivityCreated(in ActivityInfo stubInfo,in ActivityInfo targetInfo);

    void onActivityDestroy(in ActivityInfo stubInfo,in ActivityInfo targetInfo);

    void onServiceCreated(in ServiceInfo stubInfo,in ServiceInfo targetInfo);

    void onServiceDestroy(in ServiceInfo stubInfo,in ServiceInfo targetInfo);

    void onProviderCreated(in ProviderInfo stubInfo,in ProviderInfo targetInfo);

    void reportMyProcessName(String stubProcessName,String targetProcessName,String targetPkg);

    void onActivityOnNewIntent(in ActivityInfo stubInfo,in ActivityInfo targetInfo,in Intent intent);

    int getMyPid();

}

















