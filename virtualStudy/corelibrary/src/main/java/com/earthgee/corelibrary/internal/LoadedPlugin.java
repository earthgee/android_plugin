package com.earthgee.corelibrary.internal;

import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.utils.DexUtil;
import com.earthgee.corelibrary.utils.PackageParserCompat;
import com.earthgee.corelibrary.utils.PluginUtil;
import com.earthgee.corelibrary.utils.ReflectUtil;
import com.earthgee.corelibrary.utils.RunUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class LoadedPlugin {

    public static final String TAG="LoadedPlugin";

    //插件相关初始化
    public static LoadedPlugin create(PluginManager pluginManager,
                                      Context host, File apk) throws Exception{
        return new LoadedPlugin(pluginManager, host, apk);
    }

    private PluginManager mPluginManager;
    private Context mHostContext;
    private final String mLocation;
    private final PackageParser.Package mPackage;
    private final PackageInfo mPackageInfo;
    private PluginPackageManager mPackageManager;
    private Context mPluginContext;
    private final File mNativeLibDir;
    private AssetManager mAssets;
    private Resources mResources;
    private ClassLoader mClassLoader;

    private Map<ComponentName,ActivityInfo> mActivityInfos;
    private Map<ComponentName,ServiceInfo> mServiceInfos;
    private Map<ComponentName,ActivityInfo> mReceiverInfos;
    private Map<ComponentName,ProviderInfo> mProviderInfos;
    private Map<String,ProviderInfo> mProviders;
    private Map<ComponentName,InstrumentationInfo> mInstrumentationInfos;

    private Application mApplication;

    LoadedPlugin(PluginManager pluginManager,Context context,File apk) throws PackageParser.PackageParserException{
        this.mPluginManager=pluginManager;
        this.mHostContext=context;
        this.mLocation=apk.getAbsolutePath();
        this.mPackage= PackageParserCompat.parsePackage(context,apk,PackageParser.PARSE_MUST_BE_APK);
        this.mPackage.applicationInfo.metaData=this.mPackage.mAppMetaData;
        this.mPackageInfo=new PackageInfo();
        this.mPackageInfo.applicationInfo=this.mPackage.applicationInfo;
        this.mPackageInfo.applicationInfo.sourceDir=apk.getAbsolutePath();
        this.mPackageInfo.signatures=this.mPackage.mSignatures;
        this.mPackageInfo.packageName=this.mPackage.packageName;
//        if(pluginManager.getLoadedPlugin(mPackageInfo.packageName)!=null){
//            throw new RuntimeException("plugin has already been loaded : "+mPackageInfo.packageName);
//        }
        this.mPackageInfo.versionCode=this.mPackage.mVersionCode;
        this.mPackageInfo.versionName=this.mPackage.mVersionName;
        this.mPackageInfo.permissions=new PermissionInfo[0];
        this.mPackageManager=new PluginPackageManager();
        this.mPluginContext=new PluginContext(this);
        this.mNativeLibDir=context.getDir(Constants.NATIVE_DIR,Context.MODE_PRIVATE);
        this.mResources=createResources(context,apk);
        this.mAssets=this.mResources.getAssets();
        this.mClassLoader=createClassLoader(context,apk,this.mNativeLibDir,context.getClassLoader());

        tryToCopyNativeLib(apk);

        Map<ComponentName,InstrumentationInfo> instrumentations=new HashMap<>();
        for(PackageParser.Instrumentation instrumentation:mPackage.instrumentation){
            instrumentations.put(instrumentation.getComponentName(),instrumentation.info);
        }
        this.mInstrumentationInfos= Collections.unmodifiableMap(instrumentations);
        this.mPackageInfo.instrumentation=instrumentations.values().
                toArray(new InstrumentationInfo[instrumentations.size()]);

        Map<ComponentName,ActivityInfo> activityInfos=new HashMap<>();
        for(PackageParser.Activity activity:mPackage.activities){
            activityInfos.put(activity.getComponentName(),activity.info);
        }
        mActivityInfos=Collections.unmodifiableMap(activityInfos);
        mPackageInfo.activities=activityInfos.values().toArray(new ActivityInfo[activityInfos.size()]);

        Map<ComponentName,ServiceInfo> serviceInfos=new HashMap<>();
        for(PackageParser.Service service:mPackage.services){
            serviceInfos.put(service.getComponentName(),service.info);
        }
        mServiceInfos=Collections.unmodifiableMap(serviceInfos);
        mPackageInfo.services=serviceInfos.values().toArray(new ServiceInfo[serviceInfos.size()]);

        Map<String,ProviderInfo> providers=new HashMap<>();
        Map<ComponentName,ProviderInfo> providerInfos=new HashMap<>();
        for(PackageParser.Provider provider:mPackage.providers){
            providers.put(provider.info.authority,provider.info);
            providerInfos.put(provider.getComponentName(),provider.info);
        }
        mProviders=Collections.unmodifiableMap(providers);
        mProviderInfos=Collections.unmodifiableMap(providerInfos);
        mPackageInfo.providers=providers.values().toArray(new ProviderInfo[providerInfos.size()]);

        Map<ComponentName,ActivityInfo> receivers=new HashMap<>();
        for(PackageParser.Activity receiver:mPackage.receivers){
            receivers.put(receiver.getComponentName(),receiver.info);

            try{
                BroadcastReceiver br=BroadcastReceiver.class.cast(getClassLoader().loadClass(
                        receiver.getComponentName().getClassName()
                ).newInstance());
                for(PackageParser.ActivityIntentInfo aii:receiver.intents){
                    mHostContext.registerReceiver(br,aii);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        mReceiverInfos=Collections.unmodifiableMap(receivers);
        mPackageInfo.receivers=receivers.values().toArray(new ActivityInfo[receivers.size()]);
    }

    private static ClassLoader createClassLoader(Context context,File apk,File libsDir,ClassLoader parent){
        File dexOutputDir=context.getDir(Constants.OPTIMIZE_DIR,Context.MODE_PRIVATE);
        String dexOutputPath=dexOutputDir.getAbsolutePath();
        DexClassLoader loader=new DexClassLoader(apk.getAbsolutePath(),dexOutputPath,libsDir.getAbsolutePath(),
                parent);

        if(Constants.COMBINE_CLASSLOADER){
            try{
                DexUtil.insertDex(loader);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return loader;
    }

    private static AssetManager createAssetManager(Context context,File apk){
        try{
            AssetManager am=AssetManager.class.newInstance();
            ReflectUtil.invoke(AssetManager.class,am,"addAssetPath",apk.getAbsolutePath());
            return am;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static Resources createResources(Context context,File apk){
        if(Constants.COMBINE_RESOURCES){
            Resources resources=new ResourcesManager().createResources(context,apk.getAbsolutePath());
            ResourcesManager.hookResources(context,resources);
            return resources;
        }else{
            Resources hostResources=context.getResources();
            AssetManager assetManager=createAssetManager(context,apk);
            return new Resources(assetManager,hostResources.getDisplayMetrics(),hostResources.getConfiguration());
        }
    }

    private void tryToCopyNativeLib(File apk){
        Bundle metaData=this.mPackageInfo.applicationInfo.metaData;
        if(metaData!=null&&metaData.getBoolean("VA_IS_HAVE_LIB")){
            PluginUtil.copyNativeLib(apk,mHostContext,mPackageInfo,mNativeLibDir);
        }
    }

    private static ResolveInfo chooseBestActivity(Intent intent,String s,int flags,List<ResolveInfo> query){
        return query.get(0);
    }

    //插件处理请求
    public ResolveInfo resolveActivity(Intent intent,int flags){
        List<ResolveInfo> query=this.queryIntentActivities(intent,flags);
        if(null==query||query.isEmpty()){
            return null;
        }

        ContentResolver resolver=this.mPluginContext.getContentResolver();
        return chooseBestActivity(intent,intent.resolveTypeIfNeeded(resolver),flags,query);
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent,int flags){
        ComponentName component=intent.getComponent();
        List<ResolveInfo> resolveInfos=new ArrayList<>();
        ContentResolver resolver=this.mPluginContext.getContentResolver();

        for(PackageParser.Activity activity:this.mPackage.activities){
            if(activity.getComponentName().equals(component)){
                ResolveInfo resolveInfo=new ResolveInfo();
                resolveInfo.activityInfo=activity.info;
                resolveInfos.add(resolveInfo);
            }else if(component==null){
                for(PackageParser.ActivityIntentInfo intentInfo:activity.intents){
                    if(intentInfo.match(resolver,intent,true,TAG)>=0){
                        ResolveInfo resolveInfo=new ResolveInfo();
                        resolveInfo.activityInfo=activity.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }

    public ResolveInfo resolveService(Intent intent,int flags){
        List<ResolveInfo> query=this.queryIntentServices(intent,flags);
        if(null==query||query.isEmpty()){
            return null;
        }

        ContentResolver resolver=this.mPluginContext.getContentResolver();
        return chooseBestActivity(intent,intent.resolveTypeIfNeeded(resolver),flags,query);
    }

    public List<ResolveInfo> queryIntentServices(Intent intent,int flags){
        ComponentName component=intent.getComponent();
        List<ResolveInfo> resolveInfos=new ArrayList<>();
        ContentResolver resolver=this.mPluginContext.getContentResolver();

        for(PackageParser.Service service:this.mPackage.services){
            if(service.getComponentName().equals(component)){
                ResolveInfo resolveInfo=new ResolveInfo();
                resolveInfo.serviceInfo=service.info;
                resolveInfos.add(resolveInfo);
            }else if(component==null){
                for(PackageParser.ServiceIntentInfo intentInfo:service.intents){
                    if(intentInfo.match(resolver,intent,true,TAG)>=0){
                        ResolveInfo resolveInfo=new ResolveInfo();
                        resolveInfo.serviceInfo=service.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }

    public ProviderInfo resolveContentProvider(String name,int flags){
        return this.mProviders.get(name);
    }

    private class PluginPackageManager extends PackageManager{

        @Override
        public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public String[] currentToCanonicalPackageNames(String[] names) {
            return new String[0];
        }

        @Override
        public String[] canonicalToCurrentPackageNames(String[] names) {
            return new String[0];
        }

        @Override
        public Intent getLaunchIntentForPackage(String packageName) {
            return null;
        }

        @Override
        public Intent getLeanbackLaunchIntentForPackage(String packageName) {
            return null;
        }

        @Override
        public int[] getPackageGids(String packageName) throws NameNotFoundException {
            return new int[0];
        }

        @Override
        public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
            return null;
        }

        @Override
        public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public ServiceInfo getServiceInfo(ComponentName component, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public List<PackageInfo> getInstalledPackages(int flags) {
            return null;
        }

        @Override
        public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
            return null;
        }

        @Override
        public int checkPermission(String permName, String pkgName) {
            return 0;
        }

        @Override
        public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
            return false;
        }

        @Override
        public boolean addPermission(PermissionInfo info) {
            return false;
        }

        @Override
        public boolean addPermissionAsync(PermissionInfo info) {
            return false;
        }

        @Override
        public void removePermission(String name) {

        }

        @Override
        public int checkSignatures(String pkg1, String pkg2) {
            return 0;
        }

        @Override
        public int checkSignatures(int uid1, int uid2) {
            return 0;
        }

        @Nullable
        @Override
        public String[] getPackagesForUid(int uid) {
            return new String[0];
        }

        @Nullable
        @Override
        public String getNameForUid(int uid) {
            return null;
        }

        @Override
        public List<ApplicationInfo> getInstalledApplications(int flags) {
            return null;
        }

        @Override
        public String[] getSystemSharedLibraryNames() {
            return new String[0];
        }

        @Override
        public FeatureInfo[] getSystemAvailableFeatures() {
            return new FeatureInfo[0];
        }

        @Override
        public boolean hasSystemFeature(String name) {
            return false;
        }

        @Override
        public ResolveInfo resolveActivity(Intent intent, int flags) {
            return null;
        }

        @Override
        public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
            return null;
        }

        @Override
        public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
            return null;
        }

        @Override
        public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
            return null;
        }

        @Override
        public ResolveInfo resolveService(Intent intent, int flags) {
            return null;
        }

        @Override
        public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
            return null;
        }

        @Override
        public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
            return null;
        }

        @Override
        public ProviderInfo resolveContentProvider(String name, int flags) {
            return null;
        }

        @Override
        public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
            return null;
        }

        @Override
        public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
            return null;
        }

        @Override
        public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
            return null;
        }

        @Override
        public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
            return null;
        }

        @Override
        public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getDefaultActivityIcon() {
            return null;
        }

        @Override
        public Drawable getApplicationIcon(ApplicationInfo info) {
            return null;
        }

        @Override
        public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getApplicationBanner(ApplicationInfo info) {
            return null;
        }

        @Override
        public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getApplicationLogo(ApplicationInfo info) {
            return null;
        }

        @Override
        public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
            return null;
        }

        @Override
        public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
            return null;
        }

        @Override
        public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
            return null;
        }

        @Override
        public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
            return null;
        }

        @Override
        public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
            return null;
        }

        @Override
        public CharSequence getApplicationLabel(ApplicationInfo info) {
            return null;
        }

        @Override
        public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
            return null;
        }

        @Override
        public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
            return null;
        }

        @Override
        public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
            return null;
        }

        @Override
        public void verifyPendingInstall(int id, int verificationCode) {

        }

        @Override
        public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {

        }

        @Override
        public void setInstallerPackageName(String targetPackage, String installerPackageName) {

        }

        @Override
        public String getInstallerPackageName(String packageName) {
            return null;
        }

        @Override
        public void addPackageToPreferred(String packageName) {

        }

        @Override
        public void removePackageFromPreferred(String packageName) {

        }

        @Override
        public List<PackageInfo> getPreferredPackages(int flags) {
            return null;
        }

        @Override
        public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {

        }

        @Override
        public void clearPackagePreferredActivities(String packageName) {

        }

        @Override
        public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
            return 0;
        }

        @Override
        public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {

        }

        @Override
        public int getComponentEnabledSetting(ComponentName componentName) {
            return 0;
        }

        @Override
        public void setApplicationEnabledSetting(String packageName, int newState, int flags) {

        }

        @Override
        public int getApplicationEnabledSetting(String packageName) {
            return 0;
        }

        @Override
        public boolean isSafeMode() {
            return false;
        }

        @NonNull
        @Override
        public PackageInstaller getPackageInstaller() {
            return null;
        }
    }

    public void invokeApplication(){
        if(mApplication!=null){
            return;
        }

        RunUtil.runOnUiThread(new Runnable(){

            @Override
            public void run() {
                mApplication=makeApplication(false,mPluginManager.getInstrumentation());
            }
        },true);
    }

    private Application makeApplication(boolean forceDefaultAppClass, Instrumentation instrumentation){
        if(null!=this.mApplication){
            return mApplication;
        }

        String appClass=this.mPackage.applicationInfo.className;
        if(forceDefaultAppClass||null==appClass){
            appClass="android.app.Application";
        }

        try{
            mApplication=instrumentation.newApplication(mClassLoader,appClass,getPluginContext());
            instrumentation.callApplicationOnCreate(this.mApplication);
            return mApplication;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public PluginManager getPluginManager(){
        return this.mPluginManager;
    }

    public String getLocation(){
        return this.mLocation;
    }

    public ClassLoader getClassLoader(){
        return mClassLoader;
    }

    public String getPackageName(){
        return mPackage.packageName;
    }

    public Context getPluginContext(){
        return mPluginContext;
    }

    public ActivityInfo getActivityInfo(ComponentName componentName){
        return mActivityInfos.get(componentName);
    }

    public Resources getResources(){
        return mResources;
    }

    public Application getApplication(){
        return mApplication;
    }

    public Context getHostContext() {
        return mHostContext;
    }



}

















