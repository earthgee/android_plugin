package com.earthgee.libaray.hook.handle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.earthgee.libaray.PluginPatchManager;
import com.earthgee.libaray.am.RunningActivities;
import com.earthgee.libaray.core.Env;
import com.earthgee.libaray.core.PluginProcessManager;
import com.earthgee.libaray.helper.ContentProviderHolderCompat;
import com.earthgee.libaray.helper.MyProxy;
import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.HookedMethodHandler;
import com.earthgee.libaray.hook.proxy.IContentProviderHook;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.FieldUtils;
import com.earthgee.libaray.reflect.MethodUtils;
import com.earthgee.libaray.reflect.Utils;
import com.earthgee.libaray.stub.ServicesManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class IActivityManagerHookHandle extends BaseHookHandle{
    public IActivityManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("startActivity",new startActivity(mHostContext));
        sHookedMethodHandlers.put("getRunningAppProcesses",new getRunningAppProcesses(mHostContext));
        sHookedMethodHandlers.put("startService",new startService(mHostContext));
        sHookedMethodHandlers.put("stopService",new stopService(mHostContext));
        sHookedMethodHandlers.put("bindService",new bindService(mHostContext));
        sHookedMethodHandlers.put("registerReceiver",new registerReceiver(mHostContext));
        sHookedMethodHandlers.put("getContentProvider",new getContentProvider(mHostContext));
    }

    //todo replace pacakge name
    private static class startActivity extends HookedMethodHandler{

        public startActivity(Context hostContext) {
            super(hostContext);
        }

        protected boolean doReplaceIntentForStartActivityAPILow(Object[] args) throws RemoteException{
            int intentOfArgIndex = findFirstIntentIndexInArgs(args);
            if(args!=null&&args.length>1&&intentOfArgIndex>=0){
                Intent intent= (Intent) args[intentOfArgIndex];
                ActivityInfo activityInfo=resolveActivity(intent);
                if(activityInfo!=null&&isPackagePlugin(activityInfo.packageName)){
                    ComponentName component=selectProxyActivity(intent);
                    if(component!=null){
                        Intent newIntent=new Intent();
                        newIntent.setComponent(component);
                        newIntent.putExtra(Env.EXTRA_TARGET_INTENT,intent);
                        newIntent.setFlags(intent.getFlags());
                        if (TextUtils.equals(mHostContext.getPackageName(), activityInfo.packageName)) {
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        args[intentOfArgIndex] = newIntent;
                    }
                }
            }
            return true;
        }

        private void setIntentClassLoader(Intent intent,ClassLoader classLoader){
            try{
                Bundle mExtras= (Bundle) FieldUtils.readField(intent,"mExtras");
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

        protected boolean doReplaceIntentForStartActivityAPIHigh(Object[] args) throws RemoteException{
            int intentOfArgIndex=findFirstIntentIndexInArgs(args);
            if(args!=null&&args.length>1&&intentOfArgIndex>=0){
                Intent intent= (Intent) args[intentOfArgIndex];
                if(!PluginPatchManager.getInstance().canStartPluginActivity(intent)){
                    PluginPatchManager.getInstance().startPluginActivity(intent);
                    return false;
                }

                ActivityInfo activityInfo=resolveActivity(intent);
                //是插件的组件进行hook
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
                setFakedResult(Activity.RESULT_CANCELED);
                return true;
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private static class getRunningAppProcesses extends HookedMethodHandler{

        public getRunningAppProcesses(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(invokeResult!=null&&invokeResult instanceof List){
                List<Object> infos= (List<Object>) invokeResult;
                if(infos.size()>0){
                    for(Object info:infos){
                        if(info instanceof ActivityManager.RunningAppProcessInfo){
                            ActivityManager.RunningAppProcessInfo myInfo= (ActivityManager.RunningAppProcessInfo) info;
                            if(myInfo.uid!= Process.myUid()){
                                continue;
                            }

                            List<String> pkgs=PluginManager.getInstance().getPackagesNameByPid(myInfo.pid);
                            String processName=PluginManager.getInstance().getProcessNameByPid(myInfo.pid);
                            if(processName!=null){
                                myInfo.processName=processName;
                            }
                            if(pkgs!=null&&pkgs.size()>0){
                                ArrayList<String> ls=new ArrayList<>();
                                if(myInfo.pkgList!=null){
                                    for(String s:myInfo.pkgList){
                                        if(!ls.contains(s)){
                                            ls.add(s);
                                        }
                                    }
                                }
                                for(String s:pkgs){
                                    if(!ls.contains(s)){
                                        ls.add(s);
                                    }
                                }
                                myInfo.pkgList=ls.toArray(new String[ls.size()]);
                            }
                        }
                    }
                }
            }
        }
    }

    private static ActivityInfo resolveActivity(Intent intent) throws RemoteException{
        return PluginManager.getInstance().resolveActivityInfo(intent,0);
    }

    private static int findFirstIntentIndexInArgs(Object[] args) {
        if (args != null && args.length > 0) {
            int i = 0;
            for (Object arg : args) {
                if (arg != null && arg instanceof Intent) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    private static int findServiceConnectionIndex(Method method){
        Class<?>[] parameterTypes=method.getParameterTypes();
        if(parameterTypes!=null&&parameterTypes.length>0){
            for(int index=0;index<parameterTypes.length;index++){
                if(parameterTypes[index]!=null&&TextUtils.equals(parameterTypes[index].getSimpleName(),"IServiceConnection")){
                    return index;
                }
            }
        }
        return -1;
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

    private static class startService extends HookedMethodHandler{

        private ServiceInfo info=null;

        public startService(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            info=replaceFirstServiceIntentOfArgs(args);
            return super.beforeInvoke(receiver, method, args);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(invokeResult instanceof ComponentName){
                if(info!=null){
                    setFakedResult(new ComponentName(info.packageName,info.name));
                }
            }
            info=null;
            super.afterInvoke(receiver, method, args, invokeResult);
        }
    }

    private static ServiceInfo replaceFirstServiceIntentOfArgs(Object[] args) throws RemoteException{
        int intentOfArgIndex=findFirstIntentIndexInArgs(args);
        if(args!=null&&args.length>1&&intentOfArgIndex>=0){
            Intent intent= (Intent) args[intentOfArgIndex];
            ServiceInfo serviceInfo=resolveService(intent);
            if(serviceInfo!=null&&isPackagePlugin(serviceInfo.packageName)){
                ServiceInfo proxyService=selectProxyService(intent);
                if(proxyService!=null){
                    Intent newIntent=new Intent();
                    newIntent.setAction(proxyService.name+new Random().nextInt());
                    newIntent.setClassName(proxyService.packageName,proxyService.name);
                    newIntent.putExtra(Env.EXTRA_TARGET_INTENT,intent);
                    newIntent.setFlags(intent.getFlags());
                    args[intentOfArgIndex]=newIntent;
                    return serviceInfo;
                }
            }
        }
        return null;
    }

    private static ServiceInfo resolveService(Intent intent) throws RemoteException{
        return PluginManager.getInstance().resolveServiceInfo(intent,0);
    }

    private static ServiceInfo selectProxyService(Intent intent) {
        try {
            if (intent != null) {
                ServiceInfo proxyInfo = PluginManager.getInstance().selectStubServiceInfo(intent);
                if (proxyInfo != null) {
                    return proxyInfo;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class stopService extends HookedMethodHandler{

        public stopService(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            int index=1;
            if(args!=null&&args.length>index&&args[index] instanceof Intent){
                Intent intent= (Intent) args[index];
                ServiceInfo info= resolveService(intent);
                if(info!=null&&isPackagePlugin(info.packageName)){
                    int re= ServicesManager.getDefault().stopService(mHostContext,intent);
                    setFakedResult(re);
                    return true;
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private abstract static class MyIServiceConnection extends IServiceConnection.Stub{
        protected final ServiceInfo mInfo;

        private MyIServiceConnection(ServiceInfo info){
            mInfo=info;
        }
    }

    private static class bindService extends HookedMethodHandler{

        public bindService(Context hostContext) {
            super(hostContext);
        }

        private ServiceInfo info=null;

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            info=replaceFirstServiceIntentOfArgs(args);
            int index=findServiceConnectionIndex(method);
            if(info!=null&&index>=0){
                final Object oldIServiceConnection=args[index];
                args[index]=new MyIServiceConnection(info) {
                    @Override
                    public void connected(ComponentName name, IBinder service) throws RemoteException {
                         try{
                             MethodUtils.invokeMethod(oldIServiceConnection,"connected",new ComponentName(mInfo.packageName,mInfo.name),service);
                         }catch (Exception e){
                         }
                    }
                };
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private static class registerReceiver extends HookedMethodHandler{

        public registerReceiver(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                if(args!=null&&args.length>0){
                    for(int index=0;index<args.length;index++){
                        if(args[index] instanceof String){
                            String callerPackage= (String) args[index];
                            if(isPackagePlugin(callerPackage)){
                                args[index]=mHostContext.getPackageName();
                            }
                        }
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private static class getContentProvider extends ReplaceCallingPackageHookedMethodHandler{

        public getContentProvider(Context hostContext) {
            super(hostContext);
        }

        private ProviderInfo mStubProvider=null;
        private ProviderInfo mTargetProvider=null;

        //替换authority为stub的这样在AMS里可以通过(AMS会去PMS里查)
        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index=1;
                if(args.length>index&&args[index] instanceof String){
                    String name= (String) args[index];
                    Log.d("earthgee1","name="+name);
                    mStubProvider=null;
                    mTargetProvider=null;

                    //狗屁逻辑
                    ProviderInfo info=mHostContext.getPackageManager().resolveContentProvider(name,0);
                    mTargetProvider=PluginManager.getInstance().resolveContentProvider(name,0);
                    if(mTargetProvider!=null&&info!=null&&TextUtils.equals(mTargetProvider.packageName,info.packageName)){
                        mStubProvider=PluginManager.getInstance().selectStubProviderInfo(name);
                        if(mStubProvider!=null){
                            args[index]=mStubProvider.authority;
                        }
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            //从service获取到ContentProviderHolder,此时stub contentprovider onCreate已执行完成
            if(invokeResult!=null){
//                if(mStubProvider!=null&&mTargetProvider!=null){
//                    Object fromObj=invokeResult;
//                    Object toObj=ContentProviderHolderCompat.newInstance(mTargetProvider);
//                    copyField(fromObj,toObj,"provider");
//
//                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
//                        copyConnection(fromObj,toObj);
//                    }
//
//                    copyField(fromObj,toObj,"noReleaseNeeded");
//
//                    Object provider=FieldUtils.readField(invokeResult,"provider");
//                    if(provider!=null){
//                        boolean localProvider=FieldUtils.readField(toObj,"provider")==null;
//                        IContentProviderHook invocationHandler=new IContentProviderHook(mHostContext,provider,mStubProvider,mTargetProvider,localProvider);
//                        invocationHandler.setEnable(true);
//                        Class<?> clazz=provider.getClass();
//                        List<Class<?>> interfaces=Utils.getAllInterfaces(clazz);
//                        Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
//                        Object proxyprovider=MyProxy.newProxyInstance(clazz.getClassLoader(),ifs,invocationHandler);
//                        FieldUtils.writeField(invokeResult,"provider",proxyprovider);
//                        FieldUtils.writeField(toObj,"provider",proxyprovider);
//                    }
//                    setFakedResult(toObj);
//                }else if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR2){
//                    Object provider=FieldUtils.readField(invokeResult,"provider");
//
//                }

//                Object provider=FieldUtils.readField(invokeResult,"provider");
//                if(provider!=null){
//                    boolean localProvider=FieldUtils.readField(invokeResult,"provider")==null;
//                    IContentProviderHook invocationHandler = new IContentProviderHook(mHostContext, provider, mStubProvider, mTargetProvider, localProvider);
//                    invocationHandler.setEnable(true);
//                    Class<?> clazz = provider.getClass();
//                    List<Class<?>> interfaces = Utils.getAllInterfaces(clazz);
//                    Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
//                    Object proxyprovider = MyProxy.newProxyInstance(clazz.getClassLoader(), ifs, invocationHandler);
//                    FieldUtils.writeField(invokeResult, "provider", proxyprovider);
//                }

                ProviderInfo stubProvider2= (ProviderInfo) FieldUtils.readField(invokeResult,"info");
                if(mStubProvider!=null&&mTargetProvider!=null&&TextUtils.equals(stubProvider2.authority,mStubProvider.authority)){
                    Object fromObj=invokeResult;
                    Object toObj=ContentProviderHolderCompat.newInstance(mTargetProvider);
                    copyField(fromObj,toObj,"provider");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        copyConnection(fromObj, toObj);
                    }

                    //test
                    ProviderInfo info= (ProviderInfo) FieldUtils.readField(toObj,"info");
                    android.util.Log.d("earthgee1","after invoke="+info.applicationInfo.packageName);

                    //toObj.noReleaseNeeded = fromObj.noReleaseNeeded;
                    copyField(fromObj, toObj, "noReleaseNeeded");
                    Object provider = FieldUtils.readField(invokeResult, "provider");
                    if(provider!=null){
                        //todo
                    }
                    setFakedResult(toObj);
                }
            }
        }

        private void copyField(Object fromObj, Object toObj, String fieldName) throws IllegalAccessException {
            FieldUtils.writeField(toObj, fieldName, FieldUtils.readField(fromObj, fieldName));
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        private void copyConnection(Object fromObj, Object toObj) throws IllegalAccessException {
            copyField(fromObj, toObj, "connection");
        }
    }

}













































