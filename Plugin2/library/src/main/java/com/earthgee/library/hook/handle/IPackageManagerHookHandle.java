package com.earthgee.library.hook.handle;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IPackageManagerHookHandle extends BaseHookHandle{
    public IPackageManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("getPackageInfo", new getPackageInfo(mHostContext));
        sHookedMethodHandlers.put("getPackageUid", new getPackageUid(mHostContext));
        sHookedMethodHandlers.put("getPackageGids", new getPackageGids(mHostContext));
        sHookedMethodHandlers.put("currentToCanonicalPackageNames", new currentToCanonicalPackageNames(mHostContext));
        sHookedMethodHandlers.put("canonicalToCurrentPackageNames", new canonicalToCurrentPackageNames(mHostContext));
        sHookedMethodHandlers.put("getPermissionInfo", new getPermissionInfo(mHostContext));
        sHookedMethodHandlers.put("queryPermissionsByGroup", new queryPermissionsByGroup(mHostContext));
        sHookedMethodHandlers.put("getPermissionGroupInfo", new getPermissionGroupInfo(mHostContext));
        sHookedMethodHandlers.put("getAllPermissionGroups", new getAllPermissionGroups(mHostContext));
        sHookedMethodHandlers.put("getApplicationInfo", new getApplicationInfo(mHostContext));
        sHookedMethodHandlers.put("getActivityInfo", new getActivityInfo(mHostContext));
        sHookedMethodHandlers.put("getReceiverInfo", new getReceiverInfo(mHostContext));
        sHookedMethodHandlers.put("getServiceInfo", new getServiceInfo(mHostContext));
        sHookedMethodHandlers.put("getProviderInfo", new getProviderInfo(mHostContext));
        sHookedMethodHandlers.put("checkPermission", new checkPermission(mHostContext));
        sHookedMethodHandlers.put("checkUidPermission", new checkUidPermission(mHostContext));
        sHookedMethodHandlers.put("addPermission", new addPermission(mHostContext));
        sHookedMethodHandlers.put("removePermission", new removePermission(mHostContext));
        sHookedMethodHandlers.put("grantPermission", new grantPermission(mHostContext));
        sHookedMethodHandlers.put("revokePermission", new revokePermission(mHostContext));
        sHookedMethodHandlers.put("checkSignatures", new checkSignatures(mHostContext));
        sHookedMethodHandlers.put("getPackagesForUid", new getPackagesForUid(mHostContext));
        sHookedMethodHandlers.put("getNameForUid", new getNameForUid(mHostContext));
        sHookedMethodHandlers.put("getUidForSharedUser", new getUidForSharedUser(mHostContext));
        sHookedMethodHandlers.put("getFlagsForUid", new getFlagsForUid(mHostContext));
        sHookedMethodHandlers.put("resolveIntent", new resolveIntent(mHostContext));
        sHookedMethodHandlers.put("queryIntentActivities", new queryIntentActivities(mHostContext));
        sHookedMethodHandlers.put("queryIntentActivityOptions", new queryIntentActivityOptions(mHostContext));
        sHookedMethodHandlers.put("queryIntentReceivers", new queryIntentReceivers(mHostContext));
        sHookedMethodHandlers.put("resolveService", new resolveService(mHostContext));
        sHookedMethodHandlers.put("queryIntentServices", new queryIntentServices(mHostContext));
        sHookedMethodHandlers.put("queryIntentContentProviders", new queryIntentContentProviders(mHostContext));
        sHookedMethodHandlers.put("getInstalledPackages", new getInstalledPackages(mHostContext));
        sHookedMethodHandlers.put("getPackagesHoldingPermissions", new getPackagesHoldingPermissions(mHostContext));
        sHookedMethodHandlers.put("getInstalledApplications", new getInstalledApplications(mHostContext));
        sHookedMethodHandlers.put("getPersistentApplications", new getPersistentApplications(mHostContext));
        sHookedMethodHandlers.put("resolveContentProvider", new resolveContentProvider(mHostContext));
        sHookedMethodHandlers.put("querySyncProviders", new querySyncProviders(mHostContext));
        sHookedMethodHandlers.put("queryContentProviders", new queryContentProviders(mHostContext));
        sHookedMethodHandlers.put("getInstrumentationInfo", new getInstrumentationInfo(mHostContext));
        sHookedMethodHandlers.put("queryInstrumentation", new queryInstrumentation(mHostContext));
        sHookedMethodHandlers.put("getInstallerPackageName", new getInstallerPackageName(mHostContext));
        sHookedMethodHandlers.put("addPackageToPreferred", new addPackageToPreferred(mHostContext));
        sHookedMethodHandlers.put("removePackageFromPreferred", new removePackageFromPreferred(mHostContext));
        sHookedMethodHandlers.put("getPreferredPackages", new getPreferredPackages(mHostContext));
        sHookedMethodHandlers.put("resetPreferredActivities", new resetPreferredActivities(mHostContext));
        sHookedMethodHandlers.put("getLastChosenActivity", new getLastChosenActivity(mHostContext));
        sHookedMethodHandlers.put("setLastChosenActivity", new setLastChosenActivity(mHostContext));
        sHookedMethodHandlers.put("addPreferredActivity", new addPreferredActivity(mHostContext));
        sHookedMethodHandlers.put("replacePreferredActivity", new replacePreferredActivity(mHostContext));
        sHookedMethodHandlers.put("clearPackagePreferredActivities", new clearPackagePreferredActivities(mHostContext));
        sHookedMethodHandlers.put("getPreferredActivities", new getPreferredActivities(mHostContext));
        sHookedMethodHandlers.put("getHomeActivities", new getHomeActivities(mHostContext));
        sHookedMethodHandlers.put("setComponentEnabledSetting", new setComponentEnabledSetting(mHostContext));
        sHookedMethodHandlers.put("getComponentEnabledSetting", new getComponentEnabledSetting(mHostContext));
        sHookedMethodHandlers.put("setApplicationEnabledSetting", new setApplicationEnabledSetting(mHostContext));
        sHookedMethodHandlers.put("getApplicationEnabledSetting", new getApplicationEnabledSetting(mHostContext));
        sHookedMethodHandlers.put("setPackageStoppedState", new setPackageStoppedState(mHostContext));
        sHookedMethodHandlers.put("deleteApplicationCacheFiles", new deleteApplicationCacheFiles(mHostContext));
        sHookedMethodHandlers.put("clearApplicationUserData", new clearApplicationUserData(mHostContext));
        sHookedMethodHandlers.put("getPackageSizeInfo", new getPackageSizeInfo(mHostContext));
        sHookedMethodHandlers.put("performDexOpt", new performDexOpt(mHostContext));
        sHookedMethodHandlers.put("movePackage", new movePackage(mHostContext));
    }

    private class getPackageInfo extends HookedMethodHandler{

        public getPackageInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                String packageName=null;
                if(args.length>index0){
                    if(args[index0]!=null&&args[index0] instanceof String){
                        packageName= (String) args[index0];
                    }
                }

                int flags=0;
                if(args.length>index1){
                    if(args[index1]!=null&&args[index1] instanceof Integer){
                        flags= (int) args[index1];
                    }
                }

                if(packageName!=null){
                    PackageInfo packageInfo=null;
                    try{
                        packageInfo= PluginManager.getInstance().getPackageInfo(packageName,flags);
                    }catch (Exception e){
                    }
                    if(packageInfo!=null){
                        setFakeResult(packageInfo);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getPackageUid extends HookedMethodHandler{

        public getPackageUid(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0;
                String packageName=null;
                if(args.length>index0){
                    if(args[index0]!=null&&args[index0] instanceof String){
                        packageName= (String) args[index0];
                    }
                    if(packageName!=null&&PluginManager.getInstance().isPluginPackage(packageName)){
                        args[index0]=mHostContext.getPackageName();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getPackageGids extends HookedMethodHandler{

        public getPackageGids(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0;
                String packageName=null;
                if(args.length>index0){
                    if(args[index0]!=null&&args[index0] instanceof String){
                        packageName= (String) args[index0];
                    }
                    if(packageName!=null&&PluginManager.getInstance().isPluginPackage(packageName)){
                        args[index0]=mHostContext.getPackageName();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class currentToCanonicalPackageNames extends HookedMethodHandler {
        public currentToCanonicalPackageNames(Context context) {
            super(context);
        }
        //API 2.3, 4.01_r1, 4.0.3_r1, 4.1.1_r1, 4.2_r1, 4.3_r1, 4.4_r1, 5.0.2_r1
        /*public String[] currentToCanonicalPackageNames(String[] names) throws RemoteException;*/
    }

    private class canonicalToCurrentPackageNames extends HookedMethodHandler {
        public canonicalToCurrentPackageNames(Context context) {
            super(context);
        }

        //API 2.3, 4.01_r1, 4.0.3_r1, 4.1.1_r1, 4.2_r1, 4.3_r1, 4.4_r1, 5.0.2_r1
        /*public String[] canonicalToCurrentPackageNames(String[] names) throws RemoteException;*/
    }

    private class getPermissionInfo extends HookedMethodHandler{

        public getPermissionInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                if(args.length>1&&args[index0] instanceof String&&args[index1] instanceof Integer){
                    String packageName= (String) args[index0];
                    int flags= (int) args[index1];
                    PermissionInfo permissionInfo=PluginManager.getInstance().getPermissionInfo(packageName,flags);
                    if(permissionInfo!=null){
                        setFakeResult(permissionInfo);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class queryPermissionsByGroup extends HookedMethodHandler{

        public queryPermissionsByGroup(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(args!=null&&invokeResult instanceof List){
                final int index0=0,index1=1;
                if(args.length>1
                        &&args[index0] instanceof String
                        &&args[index1] instanceof Integer){
                    String groups= (String) args[index0];
                    int flags= (int) args[index1];
                    List<PermissionInfo> infos=PluginManager.getInstance().queryPermissionsByGroup(groups,flags);
                    if(infos!=null&&infos.size()>0){
                        List old= (List) invokeResult;
                        old.addAll(infos);
                    }
                }
            }
            super.afterInvoke(receiver, method, args, invokeResult);
        }
    }

    private class getPermissionGroupInfo extends HookedMethodHandler{

        public getPermissionGroupInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                if(args.length>1&&args[index0] instanceof String&&args[index1] instanceof Integer){
                    String name= (String) args[index0];
                    int flags= (int) args[index1];
                    PermissionGroupInfo permissionGroupInfo=PluginManager.getInstance().getPermissionGroupInfo(name,flags);
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getAllPermissionGroups extends HookedMethodHandler{

        public getAllPermissionGroups(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(args!=null&&invokeResult instanceof List){
                final int index=0;
                if(args.length>index&&args[index] instanceof Integer){
                    int flags= (int) args[index];
                    List<PermissionGroupInfo> infos=PluginManager.getInstance().getAllPermissionGroups(flags);
                    if(infos!=null&&infos.size()>0){
                        List old= (List) invokeResult;
                        old.addAll(infos);
                    }
                }
            }
            super.afterInvoke(receiver, method, args, invokeResult);
        }
    }

    private class getApplicationInfo extends HookedMethodHandler{

        public getApplicationInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                if(args.length>=2&&args[index0] instanceof String&&args[index1] instanceof Integer){
                    String packageName= (String) args[index0];
                    int flags= (int) args[index1];
                    ApplicationInfo info=PluginManager.getInstance().getApplicationInfo(packageName,flags);
                    if(info!=null){
                        setFakeResult(info);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getActivityInfo extends HookedMethodHandler{

        public getActivityInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                if(args.length>=2&&args[index0] instanceof ComponentName
                        &&args[index1] instanceof Integer){
                    ComponentName className= (ComponentName) args[index0];
                    int flags= (int) args[index1];
                    ActivityInfo info=PluginManager.getInstance().getActivityInfo(className,flags);
                    if(info!=null){
                        setFakeResult(info);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getReceiverInfo extends HookedMethodHandler{

        public getReceiverInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                if(args.length>=2&&args[index0] instanceof ComponentName
                        &&args[index1] instanceof Integer){
                    ComponentName className= (ComponentName) args[index0];
                    int flags= (int) args[index1];
                    ActivityInfo info=PluginManager.getInstance().getReceiverInfo(className,flags);
                    if(info!=null){
                        setFakeResult(info);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getServiceInfo extends HookedMethodHandler{

        public getServiceInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                if(args.length>=2&&args[index0] instanceof ComponentName&&
                        args[index1] instanceof Integer){
                    ComponentName className= (ComponentName) args[index0];
                    int flags= (int) args[index1];
                    ServiceInfo info=PluginManager.getInstance().getServiceInfo(className,flags);
                    if(info!=null){
                        setFakeResult(info);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getProviderInfo extends HookedMethodHandler{

        public getProviderInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index0=0,index1=1;
                if(args.length>=2&&args[index0] instanceof ComponentName&&
                        args[index1] instanceof Integer){
                    ComponentName className= (ComponentName) args[index0];
                    int flags= (int) args[index1];
                    ProviderInfo info=PluginManager.getInstance().getProviderInfo(className,flags);
                    if(info!=null){
                        setFakeResult(info);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class checkPermission extends HookedMethodHandler{

        public checkPermission(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index=1;
                if(args.length>index&&args[index] instanceof String){
                    String packageName= (String) args[index];
                    if(PluginManager.getInstance().isPluginPackage(packageName)){
                        args[index]=mHostContext.getPackageName();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class checkUidPermission extends HookedMethodHandler{

        public checkUidPermission(Context hostContext) {
            super(hostContext);
        }
    }

    private class addPermission extends HookedMethodHandler{

        public addPermission(Context hostContext) {
            super(hostContext);
        }
    }

    private class remotePermission extends HookedMethodHandler{

        public remotePermission(Context hostContext) {
            super(hostContext);
        }
    }

    private class grantPermission extends HookedMethodHandler{

        public grantPermission(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if (args != null) {
                final int index = 0;
                if (args.length > index && args[index] instanceof String) {
                    String packageName = (String) args[index];
                    if (PluginManager.getInstance().isPluginPackage(packageName)) {
                        args[index] = mHostContext.getPackageName();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class revokePermission extends HookedMethodHandler {
        public revokePermission(Context context) {
            super(context);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            //API 2.3, 4.01, 4.0.3_r1
            //NO
            //API 4.1.1_r1, 4.2_r1, 4.3_r1, 4.4_r1, 5.0.2_r1
        /*public void revokePermission(String packageName, String permissionName) throws RemoteException;*/
            if (args != null) {
                final int index = 0;
                if (args.length > index && args[index] instanceof String) {
                    String packageName = (String) args[index];
                    if (PluginManager.getInstance().isPluginPackage(packageName)) {
                        args[index] = mHostContext.getPackageName();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class checkSignatures extends HookedMethodHandler{

        public checkSignatures(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            final int index0=0,index1=1;
            String pkg0=null,pkg1=null;
            if(args!=null&&args[index0]!=null&&args[index0] instanceof String){
                pkg0= (String) args[index0];
            }
            if(args!=null&&args[index1]!=null&&args[index1] instanceof String){
                pkg1= (String) args[index1];
            }
            if(!TextUtils.isEmpty(pkg0)&&!TextUtils.isEmpty(pkg1)){
                PluginManager instance=PluginManager.getInstance();
                if(instance.isPluginPackage(pkg0)&&instance.isPluginPackage(pkg1)){
                    int result=instance.checkSignatures(pkg0,pkg1);
                    setFakeResult(result);
                    return true;
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class getPackagesForUid extends HookedMethodHandler {
        public getPackagesForUid(Context context) {
            super(context);
        }
        //API 2.3, 4.01_r1, 4.0.3_r1, 4.1.1_r1, 4.2_r1, 4.3_r1, 4.4_r1, 5.0.2_r1
        /* public  String[] getPackagesForUid(int uid) throws RemoteException*/
    }

    private class getNameForUid extends HookedMethodHandler {
        public getNameForUid(Context context) {
            super(context);
        }
        //API 2.3, 4.01_r1, 4.0.3_r1, 4.1.1_r1, 4.2_r1, 4.3_r1, 4.4_r1, 5.0.2_r1
        /*public String getNameForUid(int uid) throws RemoteException;*/
    }

    private class getUidForSharedUser extends HookedMethodHandler {
        public getUidForSharedUser(Context context) {
            super(context);
        }
        //API 2.3, 4.01_r1, 4.0.3_r1, 4.1.1_r1, 4.2_r1, 4.3_r1, 4.4_r1, 5.0.2_r1
        /*  public int getUidForSharedUser(String sharedUserName) throws RemoteException;*/
    }

    private class getFlagsForUid extends HookedMethodHandler {
        public getFlagsForUid(Context context) {
            super(context);
        }
        //API 2.3, 4.01, 4.0.3_r1, 4.1.1_r1, 4.2_r1, 4.3_r1
//        NO
        //API 4.4_r1, 5.0.2_r1
        /*public int getFlagsForUid(int uid) throws android.os.RemoteException;*/
    }

}






































