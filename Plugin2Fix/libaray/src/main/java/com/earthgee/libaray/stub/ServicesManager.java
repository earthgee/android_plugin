package com.earthgee.libaray.stub;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import com.earthgee.libaray.core.Env;
import com.earthgee.libaray.core.PluginProcessManager;
import com.earthgee.libaray.helper.ActivityThreadCompat;
import com.earthgee.libaray.helper.CompatibilityInfoCompat;
import com.earthgee.libaray.helper.QueuedWorkCompat;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.FieldUtils;
import com.earthgee.libaray.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/6/6.
 */
public class ServicesManager {

    private Map<Object,Service> mTokenServices=new HashMap<>();
    private Map<Object,Service> mNameService=new HashMap<>();
    private Map<Object,Integer> mServiceTaskIds=new HashMap<>();

    private ServicesManager(){
    }

    private static ServicesManager sServcesManager;

    public static ServicesManager getDefault() {
        synchronized (ServicesManager.class) {
            if (sServcesManager == null) {
                sServcesManager = new ServicesManager();
            }
        }
        return sServcesManager;
    }

    public boolean hasServiceRunning(){
        return mTokenServices.size()>0&&mNameService.size()>0;
    }

    private Object findTokenByService(Service service){
        for(Object s:mTokenServices.keySet()){
            if(mTokenServices.get(s)==service){
                return s;
            }
        }
        return null;
    }

    public int onStart(Context context, Intent intent,int flags,int startId) throws Exception{
        Intent targetIntent=intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if(targetIntent!=null){
            ServiceInfo targetInfo= PluginManager.getInstance().resolveServiceInfo(targetIntent,0);
            if(targetInfo!=null){
                Service service=mNameService.get(targetInfo.name);
                if(service==null){
                    handleCreateServiceOne(context,intent,targetInfo);
                }
                handleOnStartOne(targetIntent,flags,startId);
            }
        }
        return -1;
    }

    private ClassLoader getClassLoader(ApplicationInfo pluginApplicationInfo) throws Exception{
        Object object=ActivityThreadCompat.currentActivityThread();
        if(object!=null){
            final Object obj;
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB){
                obj=MethodUtils.invokeMethod(object,"getPackageInfoNoCheck",
                        pluginApplicationInfo,CompatibilityInfoCompat.DEFAULT_COMPATIBILITY_INFO());
            }else{
                obj=MethodUtils.invokeMethod(object,"getPackageInfoNoCheck",pluginApplicationInfo);
            }

            return (ClassLoader) MethodUtils.invokeMethod(obj,"getClassLoader");
        }
        return null;
    }

    private void handleCreateServiceOne(Context hostContext,Intent stubIntent,ServiceInfo info) throws Exception{
        ResolveInfo resolveInfo=hostContext.getPackageManager().resolveService(stubIntent,0);
        ServiceInfo stubInfo=resolveInfo!=null?resolveInfo.serviceInfo:null;
        PluginManager.getInstance().reportMyProcessName(stubInfo.processName,info.processName,info.packageName);
        PluginProcessManager.preLoadApk(hostContext,info);
        Object activityThread= ActivityThreadCompat.currentActivityThread();
        IBinder fakeToken=new MyFakeIBinder();
        Class CreateServiceData=Class.forName(ActivityThreadCompat.activityThreadClass().getName()+"$CreateServiceData");
        Constructor init=CreateServiceData.getDeclaredConstructor();
        if(!init.isAccessible()){
            init.setAccessible(true);
        }
        Object data=init.newInstance();

        FieldUtils.writeField(data,"token",fakeToken);
        FieldUtils.writeField(data,"info",info);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB){
            FieldUtils.writeField(data,"compatInfo", CompatibilityInfoCompat.DEFAULT_COMPATIBILITY_INFO());
        }

        Method method=activityThread.getClass().getDeclaredMethod("handleCreateService",CreateServiceData);
        if(!method.isAccessible()){
            method.setAccessible(true);
        }

        method.invoke(activityThread,data);
        Object mService=FieldUtils.readField(activityThread,"mServices");
        Service service= (Service) MethodUtils.invokeMethod(mService,"get",fakeToken);
        MethodUtils.invokeMethod(mService,"remove",fakeToken);
        mTokenServices.put(fakeToken,service);
        mNameService.put(info.name,service);

        if(stubInfo!=null){
            PluginManager.getInstance().onServiceCreated(stubInfo,info);
        }
    }

    private void handleOnStartOne(Intent intent,int flags,int startIds) throws Exception{
        ServiceInfo info=PluginManager.getInstance().resolveServiceInfo(intent,0);
        if(info!=null){
            Service service=mNameService.get(info.name);
            if(service!=null){
                ClassLoader classLoader=getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                Object token=findTokenByService(service);
                Integer integer=mServiceTaskIds.get(token);
                if(integer==null){
                    integer=-1;
                }
                int startId=integer+1;
                mServiceTaskIds.put(token,startId);
                int res=service.onStartCommand(intent,flags,startId);
                QueuedWorkCompat.waitToFinish();
            }
        }
    }

    private void handleOnTaskRemovedOne(Intent intent) throws Exception{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            ServiceInfo info=PluginManager.getInstance().resolveServiceInfo(intent,0);
            if(info!=null){
                Service service=mNameService.get(info.name);
                if(service!=null){
                    if(service!=null){
                        ClassLoader classLoader=getClassLoader(info.applicationInfo);
                        intent.setExtrasClassLoader(classLoader);
                        service.onTaskRemoved(intent);
                        QueuedWorkCompat.waitToFinish();
                    }
                    QueuedWorkCompat.waitToFinish();
                }
            }
        }
    }

    public void onTaskRemoved(Context context,Intent intent) throws Exception{
        Intent targetIntent=intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if(targetIntent!=null){
            ServiceInfo info=PluginManager.getInstance().resolveServiceInfo(targetIntent,0);
            Service service=mNameService.get(info.name);
            if(service==null){
                handleCreateServiceOne(context,intent,info);
            }
            handleOnTaskRemovedOne(targetIntent);
        }
    }

    public IBinder onBind(Context context,Intent intent) throws Exception{
        Intent targetIntent=intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if(targetIntent!=null){
            ServiceInfo info=PluginManager.getInstance().resolveServiceInfo(targetIntent,0);
            Service service=mNameService.get(info.name);
            if(service==null){
                handleCreateServiceOne(context,intent,info);
            }
            return handleOnBindOne(targetIntent);
        }
        return null;
    }

    private IBinder handleOnBindOne(Intent intent) throws Exception{
        ServiceInfo info=PluginManager.getInstance().resolveServiceInfo(intent,0);
        if(info!=null){
            Service service=mNameService.get(info.name);
            if(service!=null){
                ClassLoader classLoader=getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                return service.onBind(intent);
            }
        }
        return null;
    }

    public void onRebind(Context context,Intent intent) throws Exception{
        Intent targetIntent=intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if(targetIntent!=null){
            ServiceInfo info=PluginManager.getInstance().resolveServiceInfo(targetIntent,0);
            Service service=mNameService.get(info.name);
            if(service==null){
                handleCreateServiceOne(context,intent,info);
            }
            handleOnRebindOne(targetIntent);
        }
    }

    private void handleOnRebindOne(Intent intent) throws Exception{
        ServiceInfo info=PluginManager.getInstance().resolveServiceInfo(intent,0);
        if(info!=null){
            Service service=mNameService.get(info.name);
            if(service!=null){
                ClassLoader classLoader=getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                service.onRebind(intent);
            }
        }
    }

    public boolean onUnbind(Intent intent) throws Exception {
        Intent targetIntent = intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if (targetIntent != null) {
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(targetIntent, 0);
            Service service = mNameService.get(info.name);
            if (service != null) {
                return handleOnUnbindOne(targetIntent);
            }
        }
        return false;
    }

    private boolean handleOnUnbindOne(Intent intent) throws Exception {
        ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
        if (info != null) {
            Service service = mNameService.get(info.name);
            if (service != null) {
                ClassLoader classLoader = getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                return service.onUnbind(intent);
            }
        }
        return false;
    }

    public void onDestroy() {
        for (Service service : mTokenServices.values()) {
            service.onDestroy();
        }
        mTokenServices.clear();
        mServiceTaskIds.clear();
        mNameService.clear();
        QueuedWorkCompat.waitToFinish();
    }

    public int stopService(Context context, Intent intent) throws Exception {
        ServiceInfo targetInfo = PluginManager.getInstance().resolveServiceInfo(intent, 0);
        if (targetInfo != null) {
            handleOnUnbindOne(intent);
            handleOnDestroyOne(targetInfo);
            return 1;
        }
        return 0;
    }

    public boolean stopServiceToken(ComponentName cn, IBinder token, int startId) throws Exception {
        Service service = mTokenServices.get(token);
        if (service != null) {
            Integer lastId = mServiceTaskIds.get(token);
            if (lastId == null) {
                return false;
            }
            if (startId != lastId) {
                return false;
            }
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
            if (info != null) {
                handleOnUnbindOne(intent);
                handleOnDestroyOne(info);
                return true;
            }
        }
        return false;
    }

    private void handleOnDestroyOne(ServiceInfo targetInfo) {
        Service service = mNameService.get(targetInfo.name);
        if (service != null) {
            service.onDestroy();
            mNameService.remove(targetInfo.name);
            Object token = findTokenByService(service);
            mTokenServices.remove(token);
            mServiceTaskIds.remove(token);
            service = null;
            QueuedWorkCompat.waitToFinish();
            PluginManager.getInstance().onServiceDestory(null, targetInfo);
        }
        QueuedWorkCompat.waitToFinish();
    }

}





























