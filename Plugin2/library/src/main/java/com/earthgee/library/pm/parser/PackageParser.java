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
import android.support.v4.content.ContextCompat;

import com.earthgee.library.util.SystemPropertiesCompat;

import java.io.File;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public abstract class PackageParser {

    protected Context mContext;

    protected Object mPackageParser;

    PackageParser(Context context){
        mContext=context;
    }

    public abstract void parsePackage(File file,int flags) throws Exception;

    public abstract void collectCertificates(int flags) throws Exception;

    public abstract ActivityInfo generateActivityInfo(Object activity,int flags) throws Exception;

    public abstract ServiceInfo generateServiceInfo(Object service,int flags) throws Exception;

    public abstract ProviderInfo generateProviderInfo(Object provider,int flags) throws Exception;

    public ActivityInfo generateReceiverInfo(Object receiver,int flags) throws Exception{
        return generateActivityInfo(receiver, flags);
    }

    public abstract InstrumentationInfo generateInstrumentationInfo(Object instrumentation,int flags) throws Exception;

    public abstract ApplicationInfo generateApplicationInfo(int flags) throws Exception;

    public abstract PermissionGroupInfo generatePermissionGroupInfo(Object permission,int flags);

    public abstract PermissionInfo generatePermissionInfo(Object permission,int flags) throws Exception;

    public abstract PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime
            , long lastUpdateTime, HashSet<String> grantedPermission) throws Exception;

    public abstract List getActivities() throws Exception;

    public abstract List getServices() throws Exception;

    public abstract List getProviders() throws Exception;

    public abstract List getPermissions() throws Exception;

    public abstract List getPermissionGroups() throws Exception;

    public abstract List getReceivers() throws Exception;

    public abstract List getInstrumentations() throws Exception;

    public abstract String getPackageName() throws Exception;

    public abstract String readNameFromComponent(Object data) throws Exception;

    public abstract List<IntentFilter> readIntentFilterFromComponent(Object data) throws Exception;

    public abstract void writeSignature(Signature[] signatures) throws Exception;

    public static PackageParser newPluginParser(Context context) throws Exception{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP_MR1){
            if("1".equals(SystemPropertiesCompat.
                    get("ro.build.version.perview_sdk",""))){
                return new PackageParserApiPreview1(context);
            }else{
                return new PackageParserApi22(context);
            }
        }else if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            return new PackageParserApi21(context);
        }else if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR1
                && Build.VERSION.SDK_INT<= Build.VERSION_CODES.KITKAT_WATCH){
            return new PackageParserApi20(context);
        }else if(Build.VERSION.SDK_INT== Build.VERSION_CODES.JELLY_BEAN){
            return new PackageParserApi16(context);
        }else {
            return new PackageParserApi15(context);
        }
    }

}






































