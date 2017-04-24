package com.earthgee.library.pm.parser;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;

import com.earthgee.library.reflect.MethodUtils;
import com.earthgee.library.util.UserHandleCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class PackageParserApi21 extends PackageParser{

    protected Class<?> sPackageUserStateClass;
    protected Class<?> sPackageParserClass;
    protected Class<?> sActivityClass;
    protected Class<?> sServiceClass;
    protected Class<?> sProviderClass;
    protected Class<?> sInstrumentationClass;
    protected Class<?> sPermissionClass;
    protected Class<?> sPermissionGroupClass;
    protected Class<?> sArraySetClass;

    protected Object mPackage;
    protected Object mDefaultPackageUserState;

    protected int mUserId;

    PackageParserApi21(Context context) throws Exception{
        super(context);
        initClasses();
    }

    private void initClasses() throws Exception{
        sPackageParserClass=Class.forName("android.content.pm.PackageParser");
        sActivityClass=Class.forName("android.content.pm.PackageParser$Activity");
        sServiceClass=Class.forName("android.content.pm.PackageParser$Service");
        sProviderClass=Class.forName("android.content.pm.PackageParser$Provider");
        sInstrumentationClass=Class.forName("anroid.content.pm.PackageParser$Instrumentation");
        sPermissionClass=Class.forName("android.content.pm.PackageParser$Permission");
        sPermissionGroupClass=Class.forName("android.content.pm.PackageParser$PermissionGroup");
        try{
            sArraySetClass=Class.forName("android.util.ArraySet");
        }catch (ClassNotFoundException e){
        }

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR1){
            sPackageUserStateClass=Class.forName("android.content.pm.PackageUserState");
            mDefaultPackageUserState=sPackageUserStateClass.newInstance();
            mUserId= UserHandleCompat.getCallingUserId();
        }
    }

    @Override
    public void parsePackage(File file, int flags) throws Exception {
        mPackageParser=sPackageParserClass.newInstance();
        mPackage= MethodUtils.invokeMethod(mPackageParser,"parsePackage",file,flags);
    }

    @Override
    public void collectCertificates(int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"collectCertificates",mPackage.getClass(),int.class);
        method.invoke(mPackageParser,mPackage,flags);
    }

    @Override
    public ActivityInfo generateActivityInfo(Object activity, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generateActivityInfo",sActivityClass,int.class);
        return (ActivityInfo) method.invoke(null,activity,false,mDefaultPackageUserState,mUserId);
    }

    @Override
    public ServiceInfo generateServiceInfo(Object service, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generateServiceInfo",sServiceClass,int.class);
        return (ServiceInfo) method.invoke(null,service,flags,mDefaultPackageUserState,mUserId);
    }

    @Override
    public ProviderInfo generateProviderInfo(Object provider, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generateProviderInfo",sProviderClass,int.class);
        return (ProviderInfo) method.invoke(null,provider,flags,mDefaultPackageUserState,mUserId);
    }

    @Override
    public InstrumentationInfo generateInstrumentationInfo(Object instrumentation, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generateInstrumentationInfo",sInstrumentationClass,int.class);
        return (InstrumentationInfo) method.invoke(null,instrumentation,flags);
    }

    @Override
    public ApplicationInfo generateApplicationInfo(int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generateApplicationInfo",
                mPackage.getClass(),int.class,sPackageUserStateClass,int.class);
        return (ApplicationInfo) method.invoke(null,mPackage,flags,mDefaultPackageUserState,mUserId);
    }

    @Override
    public PermissionGroupInfo generatePermissionGroupInfo(Object permission, int flags) throws Exception{
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generatePermissionGroupInfo",sPermissionGroupClass,int.class);
        return (PermissionGroupInfo) method.invoke(null,permission,flags);
    }

    @Override
    public PermissionInfo generatePermissionInfo(Object permission, int flags) throws Exception {
        Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generatePermissionInfo",sPermissionClass,int.class);
        return (PermissionInfo) method.invoke(null,permission,flags);
    }

    @Override
    public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermission) throws Exception {
        try{
            Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generatePackageInfo",mPackage.getClass(),
                    int[].class,int.class,long.class,long.class, Set.class,sPackageUserStateClass,int.class);
            return (PackageInfo) method.invoke(null,mPackage,gids,flags,firstInstallTime,lastUpdateTime,grantedPermission,
                    mDefaultPackageUserState,mUserId);
        }catch (NoSuchMethodException e){
        }

        try{
            Method method=MethodUtils.getAccessibleMethod(sPackageParserClass,"generatePackageInfo",mPackage.getClass(),
                    int[].class,int.class,long.class,long.class,sArraySetClass,sPackageUserStateClass,int.class);
            return method.invoke(null,mPackage,gids,flags,firstInstallTime,lastUpdateTime,
                    )
        }catch (NoSuchMethodException e){
        }
        return null;
    }

    @Override
    public List getActivities() throws Exception {
        return null;
    }

    @Override
    public List getServices() throws Exception {
        return null;
    }

    @Override
    public List getProviders() throws Exception {
        return null;
    }

    @Override
    public List getPermissions() throws Exception {
        return null;
    }

    @Override
    public List getPermissionGroups() throws Exception {
        return null;
    }

    @Override
    public List getRequestedPermissions() throws Exception {
        return null;
    }

    @Override
    public List getReceivers() throws Exception {
        return null;
    }

    @Override
    public List getInstrumentations() throws Exception {
        return null;
    }

    @Override
    public String getPackageName() throws Exception {
        return null;
    }

    @Override
    public String readNameFromComponent(Object data) throws Exception {
        return null;
    }

    @Override
    public List<IntentFilter> readIntentFilterFromComponent(Object data) throws Exception {
        return null;
    }

    @Override
    public void writeSignature(Signature[] signatures) throws Exception {

    }
}
