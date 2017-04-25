package com.earthgee.library.pm.parser;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;

import com.earthgee.library.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class PackageParserApi15 extends PackageParserApi20{

    PackageParserApi15(Context context) throws Exception {
        super(context);
    }

    @Override
    public ActivityInfo generateActivityInfo(Object activity, int flags) throws Exception {
        Method method= MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generateActivityInfo",sActivityClass,int.class);
        return (ActivityInfo) method.invoke(null,activity,flags);
    }

    @Override
    public ServiceInfo generateServiceInfo(Object service, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generateServiceInfo",sServiceClass,int.class);
        return (ServiceInfo) method.invoke(null,service,flags);
    }

    @Override
    public ProviderInfo generateProviderInfo(Object provider, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generateProviderInfo",sProviderClass,int.class);
        return (ProviderInfo) method.invoke(null,provider,flags);
    }

    @Override
    public ApplicationInfo generateApplicationInfo(int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generateApplicationInfo",mPackage.getClass(),int.class);
        return (ApplicationInfo) method.invoke(null,mPackage,flags);
    }

    @Override
    public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermission) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,
                "generatePackageInfo",mPackage.getClass(),int[].class,
                int.class,long.class,long.class);
        return (PackageInfo) method.invoke(null,mPackage,gids,flags,firstInstallTime,
                lastUpdateTime);
    }
}
