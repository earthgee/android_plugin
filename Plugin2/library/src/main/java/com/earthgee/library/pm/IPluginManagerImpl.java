package com.earthgee.library.pm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.library.IApplicationCallback;
import com.earthgee.library.IPackageDataObserver;
import com.earthgee.library.IPluginManager;
import com.earthgee.library.am.BaseActivityManagerService;
import com.earthgee.library.am.MyActivityManagerService;
import com.earthgee.library.core.PluginClassLoader;
import com.earthgee.library.core.PluginDirHelper;
import com.earthgee.library.pm.parser.IntentMatcher;
import com.earthgee.library.pm.parser.PackageParser;
import com.earthgee.library.pm.parser.PluginPackageParser;
import com.earthgee.library.util.NativeLibraryHelperCompat;
import com.earthgee.library.util.PackageManagerCompat;
import com.earthgee.library.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhaoruixuan on 2017/4/14.
 */
public class IPluginManagerImpl extends IPluginManager.Stub {

    private Context mContext;
    private BaseActivityManagerService mActivityManagerService;

    private final Object mLock = new Object();
    private AtomicBoolean mHasLoadedOk = new AtomicBoolean(false);

    //插件对应解析器信息
    private Map<String, PluginPackageParser> mPluginCache = Collections.synchronizedMap(new HashMap<String, PluginPackageParser>(20));
    //插件对应签名信息
    private Map<String, Signature[]> mSignatureCache = new HashMap<>();
    private Set<String> mHostRequestedPermission = new HashSet<>(10);

    public IPluginManagerImpl(Context context) {
        mContext = context;
        mActivityManagerService = new MyActivityManagerService(mContext);
    }

    //由于此服务和宿主进程在一个进程内
    public void onCreate() {
        new Thread() {
            @Override
            public void run() {
                onCreateInner();
            }
        }.start();
    }

    //加载插件信息
    private void onCreateInner() {
        loadAllPlugin(mContext);
        loadHostRequestPermission();
        try {
            mHasLoadedOk.set(true);
            synchronized (mLock) {
                mLock.notifyAll();
            }
        } catch (Exception e) {
        }
    }

    //将所有host进程请求的权限保存下来
    private void loadHostRequestPermission() {
        try {
            mHostRequestedPermission.clear();
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pms = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (pms != null && pms.requestedPermissions != null && pms.requestedPermissions.length > 0) {
                for (String requestedPermission : pms.requestedPermissions) {
                    mHostRequestedPermission.add(requestedPermission);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 加载所有插件并将信息保存在内存中
     * @param context
     */
    private void loadAllPlugin(Context context) {
        //找到所有插件apk文件
        ArrayList<File> apkFiles = null;
        try {
            apkFiles = new ArrayList<>();
            File baseDir = new File(PluginDirHelper.getBaseDir(context));
            File[] dirs = baseDir.listFiles();
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    File file = new File(dir, "apk/base-1.apk");
                    if (file.exists()) {
                        apkFiles.add(file);
                    }
                }
            }
        } catch (Exception e) {
        }

        if (apkFiles != null && apkFiles.size() > 0) {
            //针对单个apk文件
            for (File pluginFile : apkFiles) {
                try {
                    //在这个类中缓存插件组件信息
                    PluginPackageParser pluginPackageParser = new PluginPackageParser(mContext, pluginFile);
                    //插件签名目录下的签名构建
                    Signature[] signatures = readSignatures(pluginPackageParser.getPackageName());
                    if (signatures == null || signatures.length <= 0) {
                        //使用系统的签名
                        pluginPackageParser.collectCertificates(0);
                        PackageInfo info = pluginPackageParser.getPackageInfo(PackageManager.GET_SIGNATURES);
                        saveSignatures(info);
                    } else {
                        mSignatureCache.put(pluginPackageParser.getPackageName(), signatures);
                        pluginPackageParser.writeSignature(signatures);
                    }
                    if (!mPluginCache.containsKey(pluginPackageParser.getPackageName())) {
                        mPluginCache.put(pluginPackageParser.getPackageName(), pluginPackageParser);
                    }
                } catch (Throwable e) {
                } finally {
                }

                try {
                    mActivityManagerService.onCreate(IPluginManagerImpl.this);
                } catch (Throwable e) {
                }
            }
        }
    }

    //读取签名文件并生成签名字节流
    private Signature[] readSignatures(String packageName) {
        List<String> files = PluginDirHelper.getPluginSignatureFiles(mContext, packageName);
        List<Signature> signatures = new ArrayList<>(files.size());
        int i = 0;
        for (String file : files) {
            try {
                byte[] data = Utils.readFromFile(new File(file));
                if (data != null) {
                    Signature sin = new Signature(data);
                    signatures.add(sin);
                } else {
                    return null;
                }
                i++;
            } catch (Exception e) {
                return null;
            }
        }
        return signatures.toArray(new Signature[signatures.size()]);
    }

    //将从插件中解析出的签名信息写到对应的文件中
    private void saveSignatures(PackageInfo pkgInfo) {
        if (pkgInfo != null && pkgInfo.signatures != null) {
            int i = 0;
            for (Signature signature : pkgInfo.signatures) {
                File file = new File(PluginDirHelper.getPluginSignatureFile(mContext, pkgInfo.packageName, i));
                try {
                    Utils.writeToFile(file, signature.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                    file.delete();
                    Utils.deleteDir(PluginDirHelper.getPluginSignatureDir(mContext, pkgInfo.packageName));
                    break;
                }
                i++;
            }
        }
    }

    public void onDestroy() {
        mActivityManagerService.onDestroy();
    }

    @Override
    public boolean waitForReady() throws RemoteException {
        waitForReadyInner();
        return true;
    }

    private void waitForReadyInner() {
        if (!mHasLoadedOk.get()) {
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    //根据插件包名获得PackageInfo
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws RemoteException {
        waitForReady();
        try{
            String pkg=packageName;
            if(pkg!=null&&!TextUtils.equals(packageName,mContext.getPackageName())){
                enforcePluginFileExists();
                PluginPackageParser parser=mPluginCache.get(pkg);
                if(parser!=null){
                    PackageInfo packageInfo=parser.getPackageInfo(flags);
                    if(packageInfo!=null&&(flags&PackageManager.GET_SIGNATURES)!=0&&packageInfo.signatures==null){
                        packageInfo.signatures=mSignatureCache.get(packageName);
                    }
                    return packageInfo;
                }
            }
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    @Override
    public boolean isPluginPackage(String packageName) throws RemoteException {
        waitForReady();
        enforcePluginFileExists();
        return mPluginCache.containsKey(packageName);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws RemoteException {
        waitForReady();
        try {
            String pkg = className.getPackageName();
            if (pkg != null) {
                enforcePluginFileExists();
                PluginPackageParser packageParser = mPluginCache.get(className.getPackageName());
                if (packageParser != null) {
                    return packageParser.getActivityInfo(className, flags);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolveType, int flags) throws RemoteException {
        waitForReadyInner();
        try{
            enforcePluginFileExists();
            List<ResolveInfo> infos= IntentMatcher.resolveIntent(mContext,mPluginCache,intent,
                    resolveType,flags);
            if(infos!=null&&infos.size()>0){
                return IntentMatcher.findBest(infos);
            }
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try{
            enforcePluginFileExists();
            return IntentMatcher.resolveActivityIntent(mContext,mPluginCache,intent,resolvedType,flags);
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            return IntentMatcher.resolveReceiverIntent(mContext, mPluginCache, intent, resolvedType, flags);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            List<ResolveInfo> infos = IntentMatcher.resolveServiceIntent(mContext, mPluginCache, intent, resolvedType, flags);
            if (infos != null && infos.size() > 0) {
                return IntentMatcher.findBest(infos);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            return IntentMatcher.resolveServiceIntent(mContext, mPluginCache, intent, resolvedType, flags);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            return IntentMatcher.resolveProviderIntent(mContext, mPluginCache, intent, resolvedType, flags);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    //获得所有插件包PackageInfo
    @Override
    public List<PackageInfo> getInstallPackages(int flags) throws RemoteException {
        waitForReadyInner();
        try{
            enforcePluginFileExists();
            List<PackageInfo> infos=new ArrayList<>(mPluginCache.size());
            for(PluginPackageParser pluginPackageParser:mPluginCache.values()){
                infos.add(pluginPackageParser.getPackageInfo(flags));
            }
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            List<ApplicationInfo> infos = new ArrayList<ApplicationInfo>(mPluginCache.size());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    infos.add(pluginPackageParser.getApplicationInfo(flags));
                }
            return infos;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws RemoteException {
        waitForReadyInner();
        try{
            enforcePluginFileExists();
            List<PermissionInfo> list=new ArrayList<>();
            for(PluginPackageParser pluginPackageParser:mPluginCache.values()){
                List<PermissionInfo> permissionInfos=pluginPackageParser.getPermissions();
                for(PermissionInfo permissionInfo:permissionInfos){
                    if(TextUtils.equals(permissionInfo.group,group)&&!list.contains(permissionInfo)){
                        list.add(permissionInfo);
                    }
                }
            }
            return list;
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
        waitForReadyInner();
        try{
            enforcePluginFileExists();
            for(PluginPackageParser pluginPackageParser:mPluginCache.values()){
                List<PermissionGroupInfo> permissionGroupInfos=pluginPackageParser.getPermissionGroups();
                for(PermissionGroupInfo permissionGroupInfo:permissionGroupInfos){
                    if(TextUtils.equals(permissionGroupInfo.name,name)){
                        return permissionGroupInfo;
                    }
                }
            }
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException {
        waitForReadyInner();
        try{
            enforcePluginFileExists();
            List<PermissionGroupInfo> list=new ArrayList<>();
            for(PluginPackageParser pluginPackageParser:mPluginCache.values()){
                List<PermissionGroupInfo> permissionGroupInfos=pluginPackageParser.getPermissionGroups();
                for(PermissionGroupInfo permissionGroupInfo:permissionGroupInfos){
                    if(!list.contains(permissionGroupInfo)){
                        list.add(permissionGroupInfo);
                    }
                }
            }
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) throws RemoteException {
        waitForReady();
        try {
            enforcePluginFileExists();
            for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                List<ProviderInfo> providerInfos = pluginPackageParser.getProviders();
                for (ProviderInfo providerInfo : providerInfos) {
                    if (TextUtils.equals(providerInfo.authority, name)) {
                        return providerInfo;
                    }
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver
            observer) throws RemoteException {
        boolean success=false;
        try{
            if(TextUtils.isEmpty(packageName)){
                return;
            }

            PluginPackageParser parser=mPluginCache.get(packageName);
            if(parser==null){
                return;
            }
            ApplicationInfo applicationInfo=parser.getApplicationInfo(0);
            Utils.deleteDir(new File(applicationInfo.dataDir,"caches").getName());
            success=true;
        }catch (Exception e){
            handleException(e);
        }finally {
            if(observer!=null){
                observer.onRemoveCompleted(packageName,success);
            }
        }
    }

    @Override
    public void clearApplicationUserData(String packageName, IPackageDataObserver
            observer) throws RemoteException {

    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws
            RemoteException {
        waitForReadyInner();
        try{
            if(TextUtils.equals(packageName,mContext.getPackageName())){
                return null;
            }
            PluginPackageParser parser=mPluginCache.get(packageName);
            if(parser!=null){
                return parser.getApplicationInfo(flags);
            }
        }catch (Exception e){
            handleException(e);
        }
        return null;
    }

    //从sd卡上安装插件
    @Override
    public int installPackage(String filepath, int flags) throws RemoteException {
        String apkfile=null;
        try{
            //获得插件package info
            PackageManager pm=mContext.getPackageManager();
            PackageInfo info=pm.getPackageArchiveInfo(filepath,0);
            if(info==null){
                return PackageManagerCompat.INSTALL_FAILED_INVALID_APK;
            }

            //获得插件apk的寄宿在host的目录
            apkfile=PluginDirHelper.getPluginApkFile(mContext,info.packageName);
            //允许覆盖替换
            if((flags&PackageManagerCompat.INSTALL_REPLACE_EXISTING)!=0){
                //停止运行插件进程
                forceStopPackage(info.packageName);
                //清除插件缓存目录
                if(mPluginCache.containsKey(info.packageName)){
                    deleteApplicationCacheFiles(info.packageName,null);
                }
                //删掉插件apk文件
                new File(apkfile).delete();
                //新文件copy
                Utils.copyFile(filepath,apkfile);
                //解析插件文件
                PluginPackageParser parser=new PluginPackageParser(mContext,new File(apkfile));
                //签名信息保存
                parser.collectCertificates(0);
                PackageInfo pkgInfo=parser.getPackageInfo(PackageManager.GET_PERMISSIONS|PackageManager.GET_SIGNATURES);
                if(pkgInfo!=null&&pkgInfo.requestedPermissions!=null&&pkgInfo.requestedPermissions.length>0){
                    for(String requestedPermission:pkgInfo.requestedPermissions){
                        boolean b=false;
                        try{
                            b=pm.getPermissionInfo(requestedPermission,0)!=null;
                        }catch (PackageManager.NameNotFoundException e){
                        }
                        if(!mHostRequestedPermission.contains(requestedPermission)&&b){
                            new File(apkfile).delete();
                            return PackageManagerCompat.INSTALL_FAILED_NO_REQUESTEDPERMISSION;
                        }
                    }
                }
                saveSignatures(pkgInfo);
                //移动so库
                if(copyNativeLibs(mContext,apkfile,parser.getApplicationInfo(0))<0){
                    new File(apkfile).delete();
                    return PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI;
                }

                //移动dex包(no)
                dexOpt(mContext,apkfile,parser);
                mPluginCache.put(parser.getPackageName(),parser);
                sendInstalledBroadcast(info.packageName);
                return PackageManagerCompat.INSTALL_SUCCEEDED;
            }else{
                if(mPluginCache.containsKey(info.packageName)){
                    return PackageManagerCompat.INSTALL_FAILED_ALREADY_EXISTS;
                }else{
                    forceStopPackage(info.packageName);
                    new File(apkfile).delete();
                    Utils.copyFile(filepath,apkfile);
                    PluginPackageParser parser=new PluginPackageParser(mContext,new File(apkfile));
                    parser.collectCertificates(0);
                    PackageInfo pkgInfo=parser.getPackageInfo(PackageManager.GET_PERMISSIONS|PackageManager.GET_SIGNATURES);
                    if(pkgInfo!=null&&pkgInfo.requestedPermissions!=null&&pkgInfo.requestedPermissions.length>0){
                        for(String requestedPermission:pkgInfo.requestedPermissions){
                            boolean b=false;
                            try{
                                b=pm.getPermissionInfo(requestedPermission,0)!=null;
                            }catch (PackageManager.NameNotFoundException e){
                            }
                            if(!mHostRequestedPermission.contains(requestedPermission)&&b){
                                new File(apkfile).delete();
                                return PackageManagerCompat.INSTALL_FAILED_NO_REQUESTEDPERMISSION;
                            }
                        }
                    }
                    saveSignatures(pkgInfo);
                    if(copyNativeLibs(mContext,apkfile,parser.getApplicationInfo(0))<0){
                        new File(apkfile).delete();
                        return PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI;
                    }
                    dexOpt(mContext,apkfile,parser);
                    mPluginCache.put(parser.getPackageName(),parser);
                    sendInstalledBroadcast(info.packageName);
                    return PackageManagerCompat.INSTALL_SUCCEEDED;
                }
            }
        }catch (Exception e){
            if(apkfile!=null){
                new File(apkfile).delete();
            }
            handleException(e);
            return PackageManagerCompat.INSTALL_FAILED_INTERNAL_ERROR;
        }
        return 0;
    }

    @Override
    public int deletePackage(String packageName, int flags) throws RemoteException {
        return 0;
    }

    @Override
    public List<ActivityInfo> getReceivers(String packageName, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) throws RemoteException {
        return null;
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) throws RemoteException {
        PackageManager pm=mContext.getPackageManager();
        Signature[] signatures1=new Signature[0];
        try{
            signatures1=getSignature(pkg1,pm);
        }catch (PackageManager.NameNotFoundException e){
            return PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
        }
        Signature[] signatures2=new Signature[0];
        try{
            signatures2=getSignature(pkg2,pm);
        }catch (PackageManager.NameNotFoundException e){
            return PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
        }

        boolean pkg1Signed=signatures1!=null&&signatures1.length>0;
        boolean pkg2Signed=signatures2!=null&&signatures2.length>0;

        if(!pkg1Signed&&!pkg2Signed){
            return PackageManager.SIGNATURE_NEITHER_SIGNED;
        }else if(!pkg1Signed&&pkg2Signed){
            return PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
        }else if(pkg1Signed&&!pkg2Signed){
            return PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
        }else {
            if(signatures1.length==signatures2.length) {
                for (int i = 0; i < signatures1.length; i++) {
                    Signature s1 = signatures1[i];
                    Signature s2 = signatures2[i];
                    if (!Arrays.equals(s1.toByteArray(), s2.toByteArray())) {
                        return PackageManager.SIGNATURE_NO_MATCH;
                    }
                    return PackageManager.SIGNATURE_MATCH;
                }
            }else{
                return PackageManager.SIGNATURE_NO_MATCH;
            }
        }

        return 0;
    }

    private Signature[] getSignature(String pkg,PackageManager pm) throws RemoteException,PackageManager.NameNotFoundException{
        PackageInfo info=getPackageInfo(pkg,PackageManager.GET_SIGNATURES);
        if(info==null){
            info=pm.getPackageInfo(pkg,PackageManager.GET_SIGNATURES);
        }
        if(info==null){
            throw new PackageManager.NameNotFoundException();
        }
        return info.signatures;
    }

    @Override
    public ActivityInfo selectStubActivityInfo(ActivityInfo targetInfo) throws RemoteException {
        return mActivityManagerService.selectStubActivityInfo(Binder.getCallingPid(),Binder.getCallingUid(),targetInfo);
    }

    @Override
    public ActivityInfo selectStubActivityInfoByIntent(Intent targetIntent) throws
            RemoteException {
        ActivityInfo ai=null;
        if(targetIntent.getComponent()!=null){
            ai=getActivityInfo(targetIntent.getComponent(),0);
        }else {
            ResolveInfo resolveInfo=resolveIntent(targetIntent,targetIntent.
                    resolveTypeIfNeeded(mContext.getContentResolver()),0);
            if(resolveInfo!=null&&resolveInfo.activityInfo!=null){
                ai=resolveInfo.activityInfo;
            }
        }

        if(ai!=null){
            return selectStubActivityInfo(ai);
        }
        return null;
    }

    @Override
    public ServiceInfo selectStubServiceInfo(ServiceInfo targetInfo) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo selectStubServiceInfoByIntent(Intent targetIntent) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo getTargetServiceInfo(ServiceInfo stubInfo) throws RemoteException {
        return null;
    }

    @Override
    public ProviderInfo selectStubProviderInfo(String name) throws RemoteException {
        ProviderInfo targetInfo=resolveContentProvider(name,0);
        return mActivityManagerService.selectStubProviderInfo(Binder.getCallingPid(),Binder.getCallingUid(),targetInfo);
    }

    @Override
    public List<String> getPackageNameByPid(int pid) throws RemoteException {
        return null;
    }

    @Override
    public String getProcessNameByPid(int pid) throws RemoteException {
        return null;
    }

    //杀掉有查找包名组件的对应进程
    @Override
    public boolean killBackgroundProcesses(String packageName) throws RemoteException {
        ActivityManager am= (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos=am.getRunningAppProcesses();
        boolean success=false;
        for(ActivityManager.RunningAppProcessInfo info:infos){
            if(info.pkgList!=null){
                String[] pkgListCopy=Arrays.copyOf(info.pkgList,info.pkgList.length);
                Arrays.sort(pkgListCopy);
                if(Arrays.binarySearch(pkgListCopy,packageName)>=0&&info.pid!=android.os.Process.myPid()){
                    android.os.Process.killProcess(info.pid);
                    success=true;
                }
            }
        }
        return success;
    }

    @Override
    public boolean killApplicationProcess(String pluginPackageName) throws RemoteException {
        return false;
    }

    @Override
    public boolean forceStopPackage(String pluginPackageName) throws RemoteException {
        return killBackgroundProcesses(pluginPackageName);
    }

    //注册回调
    @Override
    public boolean registerApplicationCallback(IApplicationCallback callback) throws
            RemoteException {
        return mActivityManagerService.registerApplicationCallback(Binder.getCallingPid(), Binder.getCallingUid(), callback);
    }

    @Override
    public boolean unregisterApplicationCallback(IApplicationCallback callback) throws
            RemoteException {
        return false;
    }

    @Override
    public void onActivityCreated(ActivityInfo stubInfo, ActivityInfo targetInfo) throws
            RemoteException {

    }

    @Override
    public void onActivityDestroy(ActivityInfo stubInfo, ActivityInfo targetInfo) throws
            RemoteException {

    }

    @Override
    public void onServiceCreated(ServiceInfo stubInfo, ServiceInfo targetInfo) throws
            RemoteException {

    }

    @Override
    public void onServiceDestroy(ServiceInfo stubInfo, ServiceInfo targetInfo) throws
            RemoteException {

    }

    @Override
    public void onProviderCreated(ProviderInfo stubInfo, ProviderInfo targetInfo) throws
            RemoteException {

    }

    @Override
    public void reportMyProcessName(String stubProcessName, String targetProcessName, String
            targetPkg) throws RemoteException {
        mActivityManagerService.onReportMyProcessName(Binder.getCallingPid(),Binder.getCallingUid(),stubProcessName,
                targetProcessName,targetPkg);
    }

    @Override
    public void onActivityOnNewIntent(ActivityInfo stubInfo, ActivityInfo targetInfo, Intent
            intent) throws RemoteException {

    }

    @Override
    public int getMyPid() throws RemoteException {
        return 0;
    }

    private void enforcePluginFileExists() throws RemoteException {
        List<String> removedPkg = new ArrayList<>();
        for (String pkg : mPluginCache.keySet()) {
            PluginPackageParser parser = mPluginCache.get(pkg);
            File pluginFile = parser.getPluginFile();
            if (pluginFile != null && pluginFile.exists()) {
            } else {
                removedPkg.add(pkg);
            }
        }
        for (String pkg : removedPkg) {
            deletePackage(pkg, 0);
        }
    }

    private void handleException(Exception e) throws RemoteException {
        RemoteException remoteException;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            remoteException = new RemoteException(e.getMessage());
            remoteException.initCause(e);
            remoteException.setStackTrace(e.getStackTrace());
        } else {
            remoteException = new RemoteException();
            remoteException.initCause(e);
            remoteException.setStackTrace(e.getStackTrace());
        }
        throw remoteException;
    }

    private int copyNativeLibs(Context context,String apkfile,ApplicationInfo applicationInfo) throws Exception{
        String nativeLibraryDir=PluginDirHelper.getPluginNativeLibraryDir(context,applicationInfo.packageName);
        return NativeLibraryHelperCompat.copyNativeBinaries(new File(apkfile),new File(nativeLibraryDir));
    }

    private void dexOpt(Context hostContext,String apkFile,PluginPackageParser parser) throws Exception{
        String packageName=parser.getPackageName();
        String optimizedDirectory=PluginDirHelper.getPluginDalvikCacheDir(hostContext,packageName);
        String libraryPath=PluginDirHelper.getPluginNativeLibraryDir(hostContext,packageName);
        ClassLoader classLoader=new PluginClassLoader(apkFile,optimizedDirectory,libraryPath,hostContext.getClassLoader());
    }

    private void sendInstalledBroadcast(String packageName){
        Intent intent=new Intent(PluginManager.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package://"+packageName));
        mContext.sendBroadcast(intent);
    }

}























