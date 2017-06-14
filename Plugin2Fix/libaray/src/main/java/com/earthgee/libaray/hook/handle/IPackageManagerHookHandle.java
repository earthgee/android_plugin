package com.earthgee.libaray.hook.handle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.earthgee.libaray.helper.ParceledListSliceCompat;
import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.HookedMethodHandler;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class IPackageManagerHookHandle extends BaseHookHandle{

    public IPackageManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("getPackageInfo",new getPackageInfo(mHostContext));
        sHookedMethodHandlers.put("queryIntentActivities",new queryIntentActivities(mHostContext));
        sHookedMethodHandlers.put("resolveIntent",new resolveIntent(mHostContext));
        sHookedMethodHandlers.put("getActivityInfo",new getActivityInfo(mHostContext));
        sHookedMethodHandlers.put("resolveContentProvider",new resolveContentProvider(mHostContext));
        sHookedMethodHandlers.put("getApplicationInfo",new getApplicationInfo(mHostContext));
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
                        packageInfo=PluginManager.getInstance().getPackageInfo(packageName,flags);
                    }catch (Exception e){
                    }

                    if(packageInfo!=null){
                        setFakedResult(packageInfo);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    //在系统查询后加上插件的信息
    private class queryIntentActivities extends HookedMethodHandler{

        public queryIntentActivities(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(args!=null&&(invokeResult instanceof List||
                    ParceledListSliceCompat.isParceledListSlice(invokeResult))){
                final int index0=0,index1=1,index2=2;
                Intent intent=null;
                if(args.length>index0){
                    if(args[index0] instanceof Intent){
                        intent= (Intent) args[index0];
                    }
                }

                String resolvedType=null;
                if(args.length>index1){
                    if(args[index1] instanceof String){
                        resolvedType= (String) args[index1];
                    }
                }

                Integer flags=0;
                if(args.length>index2){
                    if(args[index2] instanceof Integer){
                        flags= (Integer) args[index2];
                    }
                }

                if(intent!=null){
                    List<ResolveInfo> infos= PluginManager.getInstance().queryIntentActivities(intent,resolvedType,flags);
                    if(infos!=null&&infos.size()>0){
                        if(invokeResult instanceof List){
                            List old= (List) invokeResult;
                            old.addAll(infos);
                        }else if(ParceledListSliceCompat.isParceledListSlice(invokeResult)){
                            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR2){
                                Method getListMethod= MethodUtils.getAccessibleMethod(invokeResult.getClass(),"getList");
                                List data= (List) getListMethod.invoke(invokeResult);
                                data.addAll(infos);
                            }
                        }
                    }
                }
            }
            super.afterInvoke(receiver, method, args, invokeResult);
        }
    }

    private class resolveIntent extends HookedMethodHandler{

        public resolveIntent(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if (args != null) {
                final int index0 = 0, index1 = 1, index2 = 2;
                Intent intent = null;
                if (args.length > index0) {
                    if (args[index0] instanceof Intent) {
                        intent = (Intent) args[index0];
                    }
                }

                String resolvedType = null;
                if (args.length > index1) {
                    if (args[index1] instanceof String) {
                        resolvedType = (String) args[index1];
                    }
                }

                Integer flags = 0;
                if (args.length > index2) {
                    if (args[index2] instanceof Integer) {
                        flags = (Integer) args[index2];
                    }
                }

                if (intent != null) {
                    ResolveInfo info = PluginManager.getInstance().resolveIntent(intent, resolvedType, flags);
                    if (info != null) {
                        setFakedResult(info);
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
                if(args.length>=2
                        &&args[index0] instanceof ComponentName
                        &&args[index1] instanceof Integer){
                    ComponentName className= (ComponentName) args[index0];
                    int flags= (int) args[index1];
                    ActivityInfo info=PluginManager.getInstance().getActivityInfo(className,flags);
                    if(info!=null){
                        setFakedResult(info);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class resolveContentProvider extends HookedMethodHandler{

        public resolveContentProvider(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(args!=null){
                if(invokeResult==null){
                    final int index0=0,index1=1;
                    if(args.length>=2&&args[index0] instanceof String&&args[index1] instanceof Integer){
                        String name= (String) args[index0];
                        Integer flags= (Integer) args[index1];
                        ProviderInfo info=PluginManager.getInstance().resolveContentProvider(name,flags);
                        if(info!=null){
                            setFakedResult(info);
                        }
                    }
                }
            }
            super.afterInvoke(receiver, method, args, invokeResult);
        }
    }

    private class getApplicationInfo extends HookedMethodHandler {
        public getApplicationInfo(Context context) {
            super(context);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            //API 2.3, 4.01, 4.0.3_r1
        /* public ApplicationInfo getApplicationInfo(String packageName, int flags) throws RemoteException;*/
            //API 4.1.1_r1, 4.2_r1, 4.3_r1, 4.4_r1, 5.0.2_r1
        /* public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException;*/
            if (args != null) {
                final int index0 = 0, index1 = 1;
                if (args.length >= 2 && args[index0] instanceof String && args[index1] instanceof Integer) {
                    String packageName = (String) args[index0];
                    int flags = (Integer) args[index1];
                    ApplicationInfo info = PluginManager.getInstance().getApplicationInfo(packageName, flags);
                    if (info != null) {
                        setFakedResult(info);
                        return true;
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

}













































