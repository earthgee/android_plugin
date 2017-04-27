package com.earthgee.library.pm;

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
import android.os.Binder;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.library.IApplicationCallback;
import com.earthgee.library.IPackageDataObserver;
import com.earthgee.library.IPluginManager;
import com.earthgee.library.am.BaseActivityManagerService;
import com.earthgee.library.am.MyActivityManagerService;
import com.earthgee.library.core.PluginDirHelper;
import com.earthgee.library.pm.parser.PluginPackageParser;
import com.earthgee.library.util.Utils;

import java.io.File;
import java.util.ArrayList;
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

    public void onCreate() {
        new Thread() {
            @Override
            public void run() {
                onCreateInner();
            }
        }.start();
    }

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
            for (File pluginFile : apkFiles) {
                try {
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
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<PackageInfo> getInstallPackages(int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {
        return null;
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws RemoteException {
        return null;
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException {
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

    }

    @Override
    public void clearApplicationUserData(String packageName, IPackageDataObserver
            observer) throws RemoteException {

    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws
            RemoteException {
        return null;
    }

    @Override
    public int installPackage(String filepath, int flags) throws RemoteException {
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
        return 0;
    }

    @Override
    public ActivityInfo selectStubActivityInfo(ActivityInfo targetInfo) throws RemoteException {
        return null;
    }

    @Override
    public ActivityInfo selectStubActivityInfoByIntent(Intent targetIntent) throws
            RemoteException {
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

    @Override
    public boolean killBackgroundProcesses(String packageName) throws RemoteException {
        return false;
    }

    @Override
    public boolean killApplicationProcess(String pluginPackageName) throws RemoteException {
        return false;
    }

    @Override
    public boolean forceStopPackage(String pluginPackageName) throws RemoteException {
        return false;
    }

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

}























