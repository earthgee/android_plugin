package com.earthgee.library.hook.handle;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.library.PluginPatchManager;
import com.earthgee.library.am.RunningActivities;
import com.earthgee.library.core.Env;
import com.earthgee.library.core.PluginProcessManager;
import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.reflect.FieldUtils;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/5/2.
 * AMS hook
 */
public class IActivityManagerHookHandle extends BaseHookHandle{
    public IActivityManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("startActivity", new startActivity(mHostContext));
        sHookedMethodHandlers.put("startActivityAsUser", new startActivityAsUser(mHostContext));
        sHookedMethodHandlers.put("startActivityAsCaller", new startActivityAsCaller(mHostContext));
        sHookedMethodHandlers.put("startActivityAndWait", new startActivityAndWait(mHostContext));
        sHookedMethodHandlers.put("startActivityWithConfig", new startActivityWithConfig(mHostContext));
        sHookedMethodHandlers.put("startActivityIntentSender", new startActivityIntentSender(mHostContext));
        sHookedMethodHandlers.put("startVoiceActivity", new startVoiceActivity(mHostContext));
        sHookedMethodHandlers.put("startNextMatchingActivity", new startNextMatchingActivity(mHostContext));
        sHookedMethodHandlers.put("startActivityFromRecents", new startActivityFromRecents(mHostContext));
        sHookedMethodHandlers.put("finishActivity", new finishActivity(mHostContext));
        sHookedMethodHandlers.put("registerReceiver", new registerReceiver(mHostContext));
        sHookedMethodHandlers.put("broadcastIntent", new broadcastIntent(mHostContext));
        sHookedMethodHandlers.put("unbroadcastIntent", new unbroadcastIntent(mHostContext));
        sHookedMethodHandlers.put("getCallingPackage", new getCallingPackage(mHostContext));
        sHookedMethodHandlers.put("getCallingActivity", new getCallingActivity(mHostContext));
        sHookedMethodHandlers.put("getAppTasks", new getAppTasks(mHostContext));
        sHookedMethodHandlers.put("addAppTask", new addAppTask(mHostContext));
        sHookedMethodHandlers.put("getTasks", new getTasks(mHostContext));
        sHookedMethodHandlers.put("getServices", new getServices(mHostContext));
        sHookedMethodHandlers.put("getProcessesInErrorState", new getProcessesInErrorState(mHostContext));
        sHookedMethodHandlers.put("getContentProvider", new getContentProvider(mHostContext));
        sHookedMethodHandlers.put("getContentProviderExternal", new getContentProviderExternal(mHostContext));
        sHookedMethodHandlers.put("removeContentProviderExternal", new removeContentProviderExternal(mHostContext));
        sHookedMethodHandlers.put("publishContentProviders", new publishContentProviders(mHostContext));
        sHookedMethodHandlers.put("getRunningServiceControlPanel", new getRunningServiceControlPanel(mHostContext));
        sHookedMethodHandlers.put("startService", new startService(mHostContext));
        sHookedMethodHandlers.put("stopService", new stopService(mHostContext));
        sHookedMethodHandlers.put("stopServiceToken", new stopServiceToken(mHostContext));
        sHookedMethodHandlers.put("setServiceForeground", new setServiceForeground(mHostContext));
        sHookedMethodHandlers.put("bindService", new bindService(mHostContext));
        sHookedMethodHandlers.put("publishService", new publishService(mHostContext));
        sHookedMethodHandlers.put("unbindFinished", new unbindFinished(mHostContext));
        sHookedMethodHandlers.put("peekService", new peekService(mHostContext));
        sHookedMethodHandlers.put("bindBackupAgent", new bindBackupAgent(mHostContext));
        sHookedMethodHandlers.put("backupAgentCreated", new backupAgentCreated(mHostContext));
        sHookedMethodHandlers.put("unbindBackupAgent", new unbindBackupAgent(mHostContext));
        sHookedMethodHandlers.put("killApplicationProcess", new killApplicationProcess(mHostContext));
        sHookedMethodHandlers.put("startInstrumentation", new startInstrumentation(mHostContext));
        sHookedMethodHandlers.put("getActivityClassForToken", new getActivityClassForToken(mHostContext));
        sHookedMethodHandlers.put("getPackageForToken", new getPackageForToken(mHostContext));
        sHookedMethodHandlers.put("getIntentSender", new getIntentSender(mHostContext));
        sHookedMethodHandlers.put("clearApplicationUserData", new clearApplicationUserData(mHostContext));
        sHookedMethodHandlers.put("handleIncomingUser", new handleIncomingUser(mHostContext));
        sHookedMethodHandlers.put("grantUriPermission", new grantUriPermission(mHostContext));
        sHookedMethodHandlers.put("getPersistedUriPermissions", new getPersistedUriPermissions(mHostContext));
        sHookedMethodHandlers.put("killBackgroundProcesses", new killBackgroundProcesses(mHostContext));
        sHookedMethodHandlers.put("forceStopPackage", new forceStopPackage(mHostContext));
        sHookedMethodHandlers.put("getRunningAppProcesses", new getRunningAppProcesses(mHostContext));
        sHookedMethodHandlers.put("getRunningExternalApplications", new getRunningExternalApplications(mHostContext));
        sHookedMethodHandlers.put("getMyMemoryState", new getMyMemoryState(mHostContext));
        sHookedMethodHandlers.put("crashApplication", new crashApplication(mHostContext));
        sHookedMethodHandlers.put("grantUriPermissionFromOwner", new grantUriPermissionFromOwner(mHostContext));
        sHookedMethodHandlers.put("checkGrantUriPermission", new checkGrantUriPermission(mHostContext));
        sHookedMethodHandlers.put("startActivities", new startActivities(mHostContext));
        sHookedMethodHandlers.put("getPackageScreenCompatMode", new getPackageScreenCompatMode(mHostContext));
        sHookedMethodHandlers.put("setPackageScreenCompatMode", new setPackageScreenCompatMode(mHostContext));
        sHookedMethodHandlers.put("getPackageAskScreenCompat", new getPackageAskScreenCompat(mHostContext));
        sHookedMethodHandlers.put("setPackageAskScreenCompat", new setPackageAskScreenCompat(mHostContext));
        sHookedMethodHandlers.put("navigateUpTo", new navigateUpTo(mHostContext));
        sHookedMethodHandlers.put("serviceDoneExecuting", new serviceDoneExecuting(mHostContext));
    }

    //对startActivity方法进行hook
    private static class startActivity extends ReplaceCallingPackageHookedMethodHandler{

        public startActivity(Context hostContext) {
            super(hostContext);
        }

        //设置替换intent的classloader
        private void setIntentClassLoader(Intent intent,ClassLoader classLoader){
            try{
                Bundle mExtras=intent.getExtras();
                if(mExtras!=null){
                    mExtras.setClassLoader(classLoader);
                }else{
                    Bundle value=new Bundle();
                    value.setClassLoader(classLoader);
                    FieldUtils.writeField(intent,"mExtras",value);
                }
            }catch (Exception e){
            }finally {
                intent.setExtrasClassLoader(classLoader);
            }
        }

        //对intent中的Component做替换,替换插件的为本地的
        protected boolean doReplaceIntentForStartActivityAPILow(Object[] args) throws RemoteException{
            int intentOfArgIndex=findFirstIntentInArgs(args);
            if(args!=null&&args.length>1&&intentOfArgIndex>=0){
                Intent intent= (Intent) args[intentOfArgIndex];
                ActivityInfo activityInfo=resolveActivity(intent);
                //如果是插件中的activity
                if(activityInfo!=null&&isPackagePlugin(activityInfo.packageName)){
                    //选择代替的本地插桩activity
                    ComponentName component=selectProxyActivity(intent);
                    if(component!=null){
                        Intent newIntent=new Intent();
                        newIntent.setComponent(component);
                        newIntent.putExtra(Env.EXTRA_TARGET_INTENT,intent);
                        newIntent.setFlags(intent.getFlags());
                        if(TextUtils.equals(mHostContext.getPackageName(),activityInfo.packageName)){
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        args[intentOfArgIndex]=newIntent;
                    }
                }
            }
            return true;
        }

        protected boolean doReplaceIntentForStartActivityAPIHigh(Object[] args) throws RemoteException{
            int intentOfArgIndex=findFirstIntentInArgs(args);
            if(args!=null&&args.length>1&&intentOfArgIndex>=0){
                Intent intent= (Intent) args[intentOfArgIndex];
                if(!PluginPatchManager.getInstance().canStartPluginActivity(intent)){
                    PluginPatchManager.getInstance().startPluginActivity(intent);
                    return false;
                }
                ActivityInfo activityInfo=resolveActivity(intent);
                if(activityInfo!=null&&isPackagePlugin(activityInfo.packageName)){
                    ComponentName component=selectProxyActivity(intent);
                    if(component!=null){
                        Intent newIntent=new Intent();
                        try{
                            ClassLoader pluginClassLoader= PluginProcessManager.getPluginClassLoader(component.getPackageName());
                            setIntentClassLoader(newIntent,pluginClassLoader);
                        }catch (Exception e){
                        }
                        newIntent.setComponent(component);
                        newIntent.putExtra(Env.EXTRA_TARGET_INTENT,intent);
                        newIntent.setFlags(intent.getFlags());

                        String callingPackage= (String) args[1];
                        if(TextUtils.equals(mHostContext.getPackageName(),callingPackage)){
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        args[intentOfArgIndex]=newIntent;
                        args[1]=mHostContext.getPackageName();
                    }
                }
            }
            return true;
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            RunningActivities.beforeStartActivity();
            boolean bRet=true;
            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.JELLY_BEAN_MR2){
                bRet=doReplaceIntentForStartActivityAPILow(args);
            }else{
                bRet=doReplaceIntentForStartActivityAPIHigh(args);
            }
            if(!bRet){
                setFakeResult(Activity.RESULT_CANCELED);
                return true;
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    //同startActivity
    private static class startActivityAsUser extends startActivity {

        public startActivityAsUser(Context hostContext) {
            super(hostContext);
        }

        //API 17
         /* public int startActivityAsUser(IApplicationThread caller,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, String profileFile,
            ParcelFileDescriptor profileFd, Bundle options, int userId) throws RemoteException;*/
        //API 18,19
        /* public int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, String profileFile,
            ParcelFileDescriptor profileFd, Bundle options, int userId) throws RemoteException;*/

        //API 21
        /* public int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, ProfilerInfo profilerInfo,
            Bundle options, int userId) throws RemoteException;*/
    }

    //同startActivity
    private static class startActivityAsCaller extends startActivity {

        public startActivityAsCaller(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            //API 21
             /* public int startActivityAsCaller(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int flags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException;*/
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private static class startActivityAndWait extends startActivity {

        public startActivityAndWait(Context hostContext) {
            super(hostContext);
        }

        //API 2.3
        /*public WaitResult startActivityAndWait(IApplicationThread caller,
            Intent intent, String resolvedType, Uri[] grantedUriPermissions,
            int grantedMode, IBinder resultTo, String resultWho, int requestCode,
            boolean onlyIfNeeded, boolean debug) throws RemoteException;*/

        //API 15
        /* public WaitResult startActivityAndWait(IApplicationThread caller,
            Intent intent, String resolvedType, Uri[] grantedUriPermissions,
            int grantedMode, IBinder resultTo, String resultWho, int requestCode,
            boolean onlyIfNeeded, boolean debug, String profileFile,
            ParcelFileDescriptor profileFd, boolean autoStopProfiler) throws RemoteException;*/


        //API 16
        /*  public WaitResult startActivityAndWait(IApplicationThread caller,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, String profileFile,
            ParcelFileDescriptor profileFd, Bundle options) throws RemoteException;*/

        //API 17
        /*  public WaitResult startActivityAndWait(IApplicationThread caller,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, String profileFile,
            ParcelFileDescriptor profileFd, Bundle options, int userId) throws RemoteException;*/

        //API 18,19
        /*  public WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, String profileFile,
            ParcelFileDescriptor profileFd, Bundle options, int userId) throws RemoteException;*/

        //API 21
        /* public WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, ProfilerInfo profilerInfo,
            Bundle options, int userId) throws RemoteException;*/
    }


    private static class startActivityWithConfig extends startActivity {

        public startActivityWithConfig(Context hostContext) {
            super(hostContext);
        }

        //API 2.3,15
        /*  public int startActivityWithConfig(IApplicationThread caller,
            Intent intent, String resolvedType, Uri[] grantedUriPermissions,
            int grantedMode, IBinder resultTo, String resultWho, int requestCode,
            boolean onlyIfNeeded, boolean debug, Configuration newConfig) throws RemoteException;*/


        //API 16
        /* public int startActivityWithConfig(IApplicationThread caller,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int startFlags, Configuration newConfig,
            Bundle options) throws RemoteException;*/

        //API 17
        /* public int startActivityWithConfig(IApplicationThread caller,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int startFlags, Configuration newConfig,
            Bundle options, int userId) throws RemoteException;*/
        //API 18,19,21
        /*  public int startActivityWithConfig(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int startFlags, Configuration newConfig,
            Bundle options, int userId) throws RemoteException;*/
    }

    private static class startActivityIntentSender extends ReplaceCallingPackageHookedMethodHandler {

        public startActivityIntentSender(Context hostContext) {
            super(hostContext);
        }

        //API 2.3,15
        /* public int startActivityIntentSender(IApplicationThread caller,
            IntentSender intent, Intent fillInIntent, String resolvedType,
            IBinder resultTo, String resultWho, int requestCode,
            int flagsMask, int flagsValues) throws RemoteException;*/

        //API 16,17,18,19,21
        /*  public int startActivityIntentSender(IApplicationThread caller,
            IntentSender intent, Intent fillInIntent, String resolvedType,
            IBinder resultTo, String resultWho, int requestCode,
            int flagsMask, int flagsValues, Bundle options) throws RemoteException;*/
        //DO NOTHING
    }

    private static class startVoiceActivity extends startActivity {

        public startVoiceActivity(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            //API 21
        /*   public int startVoiceActivity(String callingPackage, int callingPid, int callingUid,
            Intent intent, String resolvedType, IVoiceInteractionSession session,
            IVoiceInteractor interactor, int flags, ProfilerInfo profilerInfo, Bundle options,
            int userId) throws RemoteException;*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final int index = 0;
                if (args != null && args.length > index) {
                    if (args[index] != null && args[index] instanceof String) {
                        String targetPkg = (String) args[index];
                        if (isPackagePlugin(targetPkg)) {
                            args[index] = mHostContext.getPackageName();
                        }
                    }
                }
                doReplaceIntentForStartActivityAPIHigh(args);
            }
            return false;
        }
    }

    private static class startNextMatchingActivity extends startActivity {

        public startNextMatchingActivity(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            //API 2.3,15
        /*  public boolean startNextMatchingActivity(IBinder callingActivity,
            Intent intent) throws RemoteException;*/

            //API 16,17,17,19,21
        /* public boolean startNextMatchingActivity(IBinder callingActivity,
            Intent intent, Bundle options) throws RemoteException;*/
            doReplaceIntentForStartActivityAPILow(args);
            return false;
        }
    }

    private static class startActivityFromRecents extends ReplaceCallingPackageHookedMethodHandler {

        public startActivityFromRecents(Context hostContext) {
            super(hostContext);
        }

        //API 21
        /*public int startActivityFromRecents(int taskId, Bundle options) throws RemoteException;*/
        //DO NOTHING
    }

    private static class finishActivity extends ReplaceCallingPackageHookedMethodHandler {

        public finishActivity(Context hostContext) {
            super(hostContext);
        }

        //API 2.3,15,16,17,18,19
        /* public boolean finishActivity(IBinder token, int code, Intent data)
            throws RemoteException;*/
        //API 21
        /*public boolean finishActivity(IBinder token, int code, Intent data, boolean finishTask)
            throws RemoteException;*/
        //FIXME 先不修改。
    }

    //在方法参数中找到intent
    private static int findFirstIntentInArgs(Object[] args){
        if(args!=null&&args.length>0){
            int i=0;
            for(Object arg:args){
                if(arg!=null&&arg instanceof Intent){
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    private static class registerReceiver extends ReplaceCallingPackageHookedMethodHandler{

        public registerReceiver(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class broadcastIntent extends ReplaceCallingPackageHookedMethodHandler{

        public broadcastIntent(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class unbroadcastIntent extends ReplaceCallingPackageHookedMethodHandler{

        public unbroadcastIntent(Context hostContext) {
            super(hostContext);
        }

        //todo
    }



    private static ActivityInfo resolveActivity(Intent intent) throws RemoteException{
        return PluginManager.getInstance().resolveActivityInfo(intent,0);
    }

    private static boolean isPackagePlugin(String packageName) throws RemoteException{
        return PluginManager.getInstance().isPluginPackage(packageName);
    }

    private static ComponentName selectProxyActivity(Intent intent){
        try{
            if(intent!=null){
                ActivityInfo proxyInfo=PluginManager.getInstance().selectStubActivityInfo(intent);
                if(proxyInfo!=null){
                    return new ComponentName(proxyInfo.packageName,proxyInfo.name);
                }
            }
        }catch (Exception e){
        }
        return null;
    }

    private static class getCallingPackage extends ReplaceCallingPackageHookedMethodHandler {

        public getCallingPackage(Context hostContext) {
            super(hostContext);
        }

        //API 2.3,15,16,17,18,19,21
        /* public String getCallingPackage(IBinder token) throws RemoteException;*/
        //FIXME  I don't know what function of this,just hook it.
    }

    private static class getCallingActivity extends ReplaceCallingPackageHookedMethodHandler {

        public getCallingActivity(Context hostContext) {
            super(hostContext);
        }

        //API  2.3,15,16,17,18,19, 21
        /*  public ComponentName getCallingActivity(IBinder token) throws RemoteException;*/
        //FIXME I don't know what function of this,just hook it.
        //也不知道这个是干嘛的。是返回此Activity是由谁调起的么？
    }

    private static class getAppTasks extends ReplaceCallingPackageHookedMethodHandler {

        public getAppTasks(Context hostContext) {
            super(hostContext);
        }
        // API 21
        /* public List<IAppTask> getAppTasks(String callingPackage) throws RemoteException;*/
        //FIXME I don't know what function of this,just hook it.
    }

    private static class addAppTask extends ReplaceCallingPackageHookedMethodHandler {

        public addAppTask(Context hostContext) {
            super(hostContext);
        }

        //API 21
        /* public int addAppTask(IBinder activityToken, Intent intent,
            ActivityManager.TaskDescription description, Bitmap thumbnail) throws RemoteException;*/
        //FIXME api21的不知道干嘛的，先不修改吧。
    }

    private static class getTasks extends ReplaceCallingPackageHookedMethodHandler {

        public getTasks(Context hostContext) {
            super(hostContext);
        }

        //API 2.3,15,16,17,18
        /*  public List getTasks(int maxNum, int flags,
                         IThumbnailReceiver receiver) throws RemoteException;*/
        //API 19
        /*public List<RunningTaskInfo> getTasks(int maxNum, int flags,
                         IThumbnailReceiver receiver) throws RemoteException;*/
        //API 21
        /* public List<RunningTaskInfo> getTasks(int maxNum, int flags) throws RemoteException;*/
        //FIXME 这里需要把原来函数返回的 List<RunningTaskInfo>中关于代理activity修改成插件自己的。

//        @Override
//        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
//            if (invokeResult instanceof List) {
//                List runningTaskInfo = (List) invokeResult;
//                if (runningTaskInfo.size() > 0) {
//                    for (Object obj : runningTaskInfo) {
//                        RunningTaskInfo info = (RunningTaskInfo) obj;
//                        info.baseActivity =;
//                        info.topActivity =;
//                    }
//                }
//            }
//            super.afterInvoke(receiver, method, args, invokeResult);
//        }
    }

    private static class startInstrumentation extends ReplaceCallingPackageHookedMethodHandler {

        public startInstrumentation(Context hostContext) {
            super(hostContext);
        }

        //API 2.3,15,16
        /*    public boolean startInstrumentation(ComponentName className, String profileFile,
            int flags, Bundle arguments, IInstrumentationWatcher watcher)
            throws RemoteException;*/
        //API 17
       /*    public boolean startInstrumentation(ComponentName className, String profileFile,
            int flags, Bundle arguments, IInstrumentationWatcher watcher, int userId)
            throws RemoteException;*/
        //API 18,19
        /*    public boolean startInstrumentation(ComponentName className, String profileFile,
            int flags, Bundle arguments, IInstrumentationWatcher watcher,
            IUiAutomationConnection connection, int userId) throws RemoteException;*/
        //API 21
        /* public boolean startInstrumentation(ComponentName className, String profileFile,
            int flags, Bundle arguments, IInstrumentationWatcher watcher,
            IUiAutomationConnection connection, int userId,
            String abiOverride) throws RemoteException;*/

        //FIXME 单元测试用的。这个就不改了。
    }

    private static class getActivityClassForToken extends ReplaceCallingPackageHookedMethodHandler {

        public getActivityClassForToken(Context hostContext) {
            super(hostContext);
        }

        //API  2.3,15,16,17,18,19, 21
       /* public ComponentName getActivityClassForToken(IBinder token) throws RemoteException;*/
        //FIXME I don't know what function of this,just hook it.
        //通过token拿Activity？搞不懂，不改。
    }

    private static class getPackageForToken extends ReplaceCallingPackageHookedMethodHandler {

        public getPackageForToken(Context hostContext) {
            super(hostContext);
        }

        //API  2.3,15,16,17,18,19, 21
        /* public String getPackageForToken(IBinder token) throws RemoteException;*/
        //FIXME I don't know what function of this,just hook it.
        //通过token拿包名？搞不懂，不改。
    }

    private static class getServices extends ReplaceCallingPackageHookedMethodHandler{

        public getServices(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class getProcessesInErrorState extends ReplaceCallingPackageHookedMethodHandler{

        public getProcessesInErrorState(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class getContentProvider extends ReplaceCallingPackageHookedMethodHandler{

        public getContentProvider(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class getContentProviderExternal extends getContentProvider{

        public getContentProviderExternal(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class removeContentProviderExternal extends ReplaceCallingPackageHookedMethodHandler{

        public removeContentProviderExternal(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class publishContentProviders extends ReplaceCallingPackageHookedMethodHandler{

        public publishContentProviders(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class getRunningServiceControlPanel extends ReplaceCallingPackageHookedMethodHandler{

        public getRunningServiceControlPanel(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class startService extends ReplaceCallingPackageHookedMethodHandler{

        public startService(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class stopService extends ReplaceCallingPackageHookedMethodHandler{

        public stopService(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class stopServiceToken extends ReplaceCallingPackageHookedMethodHandler{

        public stopServiceToken(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class setServiceForeground extends ReplaceCallingPackageHookedMethodHandler{

        public setServiceForeground(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class bindService extends ReplaceCallingPackageHookedMethodHandler{

        public bindService(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class publishService extends ReplaceCallingPackageHookedMethodHandler{

        public publishService(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class unbindFinished extends ReplaceCallingPackageHookedMethodHandler{

        public unbindFinished(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class peekService extends ReplaceCallingPackageHookedMethodHandler{

        public peekService(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class bindBackupAgent extends ReplaceCallingPackageHookedMethodHandler{

        public bindBackupAgent(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class backupAgentCreated extends ReplaceCallingPackageHookedMethodHandler{

        public backupAgentCreated(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class unbindBackupAgent extends ReplaceCallingPackageHookedMethodHandler{

        public unbindBackupAgent(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class killApplicationProcess extends ReplaceCallingPackageHookedMethodHandler{

        public killApplicationProcess(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    public static class getIntentSender extends ReplaceCallingPackageHookedMethodHandler{

        public getIntentSender(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class clearApplicationUserData extends ReplaceCallingPackageHookedMethodHandler{

        public clearApplicationUserData(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class handleIncomingUser extends ReplaceCallingPackageHookedMethodHandler{

        public handleIncomingUser(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class grantUriPermission extends ReplaceCallingPackageHookedMethodHandler{

        public grantUriPermission(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class getPersistedUriPermissions extends ReplaceCallingPackageHookedMethodHandler{

        public getPersistedUriPermissions(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class killBackgroundProcesses extends ReplaceCallingPackageHookedMethodHandler{

        public killBackgroundProcesses(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class forceStopPackage extends ReplaceCallingPackageHookedMethodHandler{

        public forceStopPackage(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

    private static class getRunningAppProcesses extends ReplaceCallingPackageHookedMethodHandler{

        public getRunningAppProcesses(Context hostContext) {
            super(hostContext);
        }
    }

    private static class getRunningExternalApplications extends ReplaceCallingPackageHookedMethodHandler{

        public getRunningExternalApplications(Context hostContext) {
            super(hostContext);
        }
    }

    private static class getMyMemoryState extends ReplaceCallingPackageHookedMethodHandler{

        public getMyMemoryState(Context hostContext) {
            super(hostContext);
        }
    }

    private static class crashApplication extends ReplaceCallingPackageHookedMethodHandler{

        public crashApplication(Context hostContext) {
            super(hostContext);
        }
    }

    private static class grantUriPermissionFromOwner extends ReplaceCallingPackageHookedMethodHandler{

        public grantUriPermissionFromOwner(Context hostContext) {
            super(hostContext);
        }
    }

    private static class checkGrantUriPermission extends ReplaceCallingPackageHookedMethodHandler{

        public checkGrantUriPermission(Context hostContext) {
            super(hostContext);
        }
    }

    private static class startActivities extends ReplaceCallingPackageHookedMethodHandler{

        public startActivities(Context hostContext) {
            super(hostContext);
        }
    }

    private static class getPackageScreenCompatMode extends ReplaceCallingPackageHookedMethodHandler{

        public getPackageScreenCompatMode(Context hostContext) {
            super(hostContext);
        }
    }

    private static class setPackageScreenCompatMode extends ReplaceCallingPackageHookedMethodHandler{

        public setPackageScreenCompatMode(Context hostContext) {
            super(hostContext);
        }
    }

    private static class getPackageAskScreenCompat extends ReplaceCallingPackageHookedMethodHandler{

        public getPackageAskScreenCompat(Context hostContext) {
            super(hostContext);
        }
    }

    private static class setPackageAskScreenCompat extends ReplaceCallingPackageHookedMethodHandler{

        public setPackageAskScreenCompat(Context hostContext) {
            super(hostContext);
        }
    }

    private static class navigateUpTo extends ReplaceCallingPackageHookedMethodHandler{

        public navigateUpTo(Context hostContext) {
            super(hostContext);
        }
    }

    private class serviceDoneExecuting extends ReplaceCallingPackageHookedMethodHandler{

        public serviceDoneExecuting(Context hostContext) {
            super(hostContext);
        }

        //todo
    }

}
























