package com.earthgee.library.pm.parser;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;

import com.earthgee.library.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class PackageParserApi16 extends PackageParserApi20{

    private boolean mStopped;
    private int mEnabledState;

    public PackageParserApi16(Context context) throws Exception {
        super(context);
        mStopped=false;
        mEnabledState=0;
    }

    @Override
    public ActivityInfo generateActivityInfo(Object activity, int flags) throws Exception {
        Method method= MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generateActivityInfo",sActivityClass,int.class,boolean.class,
                int.class,int.class);
        return (ActivityInfo) method.invoke(null,activity,flags,mStopped,mEnabledState,mUserId);
    }

    @Override
    public ServiceInfo generateServiceInfo(Object service, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generateServiceInfo",sServiceClass,int.class,boolean.class,int.class,int.class);
        return (ServiceInfo) method.invoke(null,service,flags,mStopped,mEnabledState,mUserId);
    }

    @Override
    public ProviderInfo generateProviderInfo(Object provider, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generateProviderInfo",sProviderClass,int.class,boolean.class,int.class,int.class);
        return (ProviderInfo) method.invoke(null,provider,flags,mStopped,mEnabledState,mUserId);
    }

    @Override
    public InstrumentationInfo generateInstrumentationInfo(Object instrumentation, int flags) throws Exception {
        return super.generateInstrumentationInfo(instrumentation, flags);
    }

    @Override
    public ApplicationInfo generateApplicationInfo(int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass
                ,"generateApplicationInfo",mPackage.getClass(),int.class,boolean.class,int.class,int.class);
        return (ApplicationInfo) method.invoke(null,mPackage,flags,mStopped,mEnabledState,mUserId);
    }

    @Override
    public PermissionGroupInfo generatePermissionGroupInfo(Object permission, int flags) throws Exception {
        return super.generatePermissionGroupInfo(permission, flags);
    }

    @Override
    public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermission) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generatePackageInfo",mPackage.getClass(),int[].class,int.class,
                long.class,long.class,HashSet.class,boolean.class,int.class,int.class);
        return (PackageInfo) method.invoke(null,mPackage,gids,flags,firstInstallTime,lastUpdateTime,
                grantedPermission,mStopped,mEnabledState,mUserId);
    }
}

















