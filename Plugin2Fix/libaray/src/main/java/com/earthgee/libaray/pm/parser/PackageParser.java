package com.earthgee.libaray.pm.parser;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;

import com.earthgee.libaray.helper.SystemPropertiesCompat;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/25.
 */
public abstract class PackageParser {

    protected Context mContext;
    protected Object mPackageParser;

    PackageParser(Context context){
        mContext=context;
    }

    public final static int PARSE_IS_SYSTEM = 1 << 0;
    public final static int PARSE_CHATTY = 1 << 1;
    public final static int PARSE_MUST_BE_APK = 1 << 2;
    public final static int PARSE_IGNORE_PROCESSES = 1 << 3;
    public final static int PARSE_FORWARD_LOCK = 1 << 4;
    public final static int PARSE_ON_SDCARD = 1 << 5;
    public final static int PARSE_IS_SYSTEM_DIR = 1 << 6;
    public final static int PARSE_IS_PRIVILEGED = 1 << 7;
    public final static int PARSE_COLLECT_CERTIFICATES = 1 << 8;
    public final static int PARSE_TRUSTED_OVERLAY = 1 << 9;

    public static PackageParser newPluginParser(Context context) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if ("1".equals(SystemPropertiesCompat.get("ro.build.version.preview_sdk", ""))) {
                return new PackageParserApi22Preview1(context);
            } else {
                return new PackageParserApi22(context);//API 20
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PackageParserApi21(context);//API 21
        }
        return new PackageParserApi21(context);//API 21
    }

    public abstract void parsePackage(File file,int flags) throws Exception;

    public abstract String getPackageName() throws Exception;

    public abstract void collectCertificates(int flags) throws Exception;

    public abstract PackageInfo generatePackageInfo(int gids[], int flags
            , long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermissions) throws Exception;

    public abstract List getActivities() throws Exception;

    public abstract String readNameFromComponent(Object data) throws Exception;

    public abstract ActivityInfo generateActivityInfo(Object activity,int flags) throws Exception;

    public abstract List<IntentFilter> readIntentFilterFromComponent(Object data) throws Exception;

    public abstract List getServices() throws Exception;

    public abstract ServiceInfo generateServiceInfo(Object service,int flags) throws Exception;

    public abstract List getProviders() throws Exception;

    public abstract ProviderInfo generateProviderInfo(Object provider,int flags) throws Exception;

    public abstract List getReceivers() throws Exception;

    public ActivityInfo generateReceiverInfo(Object receiver, int flags) throws Exception {
        return generateActivityInfo(receiver, flags);
    }

    public abstract List getInstrumentations() throws Exception;

    public abstract List getPermissions() throws Exception;

    public abstract PermissionInfo generatePermissionInfo(Object permission, int flags) throws Exception;

    public abstract List getPermissionGroups() throws Exception;

    public abstract List getRequestedPermissions() throws Exception;

    public abstract ApplicationInfo generateApplicationInfo(int flags) throws Exception;

    public abstract void writeSignature(Signature[] signatures) throws Exception;
}






















