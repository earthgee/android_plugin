package com.earthgee.library.pm.parser;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/5/2.
 */
public class IntentMatcher {

    private static final Comparator<ResolveInfo> mResolvePrioritySorter = new Comparator<ResolveInfo>() {
        @Override
        public int compare(ResolveInfo r1, ResolveInfo r2) {
            int v1 = r1.priority;
            int v2 = r2.priority;
            if (v1 != v2) {
                return (v1 > v2) ? -1 : 1;
            }
            v1 = r1.preferredOrder;
            v2 = r2.preferredOrder;
            if (v1 != v2) {
                return (v1 > v2) ? -1 : 1;
            }
            if (r1.isDefault != r2.isDefault) {
                return r1.isDefault ? -1 : 1;
            }
            v1 = r1.match;
            v2 = r2.match;
            //System.out.println("Comparing: m1=" + m1 + " m2=" + m2);
            if (v1 != v2) {
                return (v1 > v2) ? -1 : 1;
            }
//                    if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
//                        if (r1.system != r2.system) {
//                            return r1.system ? -1 : 1;
//                        }
//                    }
            return 0;
        }
    };

    public static final List<ResolveInfo> resolveReceiverIntent() {

    }

    public static final List<ResolveInfo> resolveIntent(Context context, Map<String, PluginPackageParser> pluginPackages
            , Intent intent, String resolvedType, int flags) throws Exception {
        if (intent == null || context == null) {
            return null;
        }

        List<ResolveInfo> list = new ArrayList<>();
        ComponentName comp = intent.getComponent();
        if (comp == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (intent.getSelector() != null) {
                    intent = intent.getSelector();
                    comp = intent.getComponent();
                }
            }
        }

        if (comp != null && comp.getPackageName() != null) {
            PluginPackageParser parser = pluginPackages.get(comp.getPackageName());
            if (parser != null) {
                queryIntentActivityForPackage(context, parser, intent, flags, list);
                if (list.size() > 0) {
                    Collections.sort(list, mResolvePrioritySorter);
                    return list;
                }
                queryIntentServiceForPackage(context, parser, intent, flags, list);
                if (list.size() > 0) {
                    Collections.sort(list, mResolvePrioritySorter);
                    return list;
                }
                queryIntentProviderForPackage(context, parser, intent, flags, list);
                if (list.size() > 0) {
                    Collections.sort(list, mResolvePrioritySorter);
                    return list;
                }
                queryIntentReveiverForPackage(context, parser, intent, flags, list);
                if (list.size() > 0) {
                    Collections.sort(list, mResolvePrioritySorter);
                    return list;
                }
            }
            Collections.sort(list, mResolvePrioritySorter);
            return list;
        }

        final String pkgName = intent.getPackage();
        if (pkgName != null) {
            PluginPackageParser parser = pluginPackages.get(pkgName);
            if (parser != null) {
                queryIntentActivityForPackage(context, parser, intent, flags, list);
                queryIntentServiceForPackage(context, parser, intent, flags, list);
                queryIntentProviderForPackage(context, parser, intent, flags, list);
                queryIntentReveiverForPackage(context, parser, intent, flags, list);
            }
        } else {
            for (PluginPackageParser parser : pluginPackages.values()) {
                queryIntentActivityForPackage(context, parser, intent, flags, list);
                queryIntentServiceForPackage(context, parser, intent, flags, list);
                queryIntentProviderForPackage(context, parser, intent, flags, list);
                queryIntentReveiverForPackage(context, parser, intent, flags, list);
            }
        }
        Collections.sort(list, mResolvePrioritySorter);
        return list;
    }

    private static void queryIntentActivityForPackage(Context context,PluginPackageParser packageparser
            ,Intent intent,int flags,List<ResolveInfo> outList) throws Exception{
        List<ActivityInfo> activityInfos=packageparser.getActivities();
        if(activityInfos!=null&&activityInfos.size()>0){
            for(ActivityInfo activityInfo:activityInfos){
                ComponentName className=new ComponentName(activityInfo.packageName,activityInfo.name);
                List<IntentFilter> intentFilters=packageparser.getActivityIntentFilter(className);
                if(intentFilters!=null&&intentFilters.size()>0){
                    for(IntentFilter intentFilter:intentFilters){
                        int match=intentFilter.match(context.getContentResolver(),intent,true,"");
                        if(match>=0){
                            ActivityInfo flagInfo=packageparser.getActivityInfo(new ComponentName(activityInfo.packageName,
                                    activityInfo.name),flags);
                            if((flags& PackageManager.MATCH_DEFAULT_ONLY)!=0){
                                if(intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)){
                                    ResolveInfo resolveInfo=newResolveInfo(flagInfo,intentFilter);
                                    resolveInfo.match=match;
                                    resolveInfo.isDefault=true;
                                    outList.add(resolveInfo);
                                }
                            }else{
                                ResolveInfo resolveInfo=newResolveInfo(flagInfo,intentFilter);
                                resolveInfo.match=match;
                                resolveInfo.isDefault=false;
                                outList.add(resolveInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    private static ResolveInfo newResolveInfo(ActivityInfo activityInfo,IntentFilter intentFilter){
        ResolveInfo resolveInfo=new ResolveInfo();
        resolveInfo.activityInfo=activityInfo;
        resolveInfo.filter=intentFilter;
        resolveInfo.resolvePackageName=activityInfo.packageName;
        resolveInfo.labelRes=activityInfo.labelRes;
        resolveInfo.icon=activityInfo.icon;
        resolveInfo.specificIndex=1;
        resolveInfo.priority=intentFilter.getPriority();
        resolveInfo.preferredOrder=0;
        return resolveInfo;
    }

    private static void queryIntentServiceForPackage(Context context,PluginPackageParser packageParser,Intent intent
            ,int flags,List<ResolveInfo> outList) throws Exception{
        List<ServiceInfo> serviceInfos=packageParser.getServices();
        if(serviceInfos!=null&&serviceInfos.size()>=0){
            for(ServiceInfo serviceInfo:serviceInfos){
                ComponentName className=new ComponentName(serviceInfo.packageName,serviceInfo.name);
                List<IntentFilter> intentFilters=packageParser.getServiceIntentFilter(className);
                if(intentFilters!=null&&intentFilters.size()>0){
                    for(IntentFilter intentFilter:intentFilters){
                        int match=intentFilter.match(context.getContentResolver(),intent,true,"");
                        if(match>=0){
                            ServiceInfo flagServiceInfo=packageParser.getServiceInfo(
                                    new ComponentName(serviceInfo.packageName,serviceInfo.name),flags);
                            if((flags&PackageManager.MATCH_DEFAULT_ONLY)!=0){
                                if(intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)){
                                    ResolveInfo resolveInfo=newResolveInfo(flagServiceInfo,intentFilter);
                                    resolveInfo.match=match;
                                    resolveInfo.isDefault=true;
                                    outList.add(resolveInfo);
                                }
                            }else{
                                ResolveInfo resolveInfo=newResolveInfo(flagServiceInfo,intentFilter);
                                resolveInfo.match=match;
                                resolveInfo.isDefault=false;
                                outList.add(resolveInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    private static ResolveInfo newResolveInfo(ServiceInfo serviceInfo,IntentFilter intentFilter){
        ResolveInfo resolveInfo=new ResolveInfo();
        resolveInfo.serviceInfo=serviceInfo;
        resolveInfo.filter=intentFilter;
        resolveInfo.resolvePackageName=serviceInfo.packageName;
        resolveInfo.labelRes=serviceInfo.labelRes;
        resolveInfo.icon=serviceInfo.icon;
        resolveInfo.specificIndex=1;
        resolveInfo.priority=intentFilter.getPriority();
        resolveInfo.preferredOrder=0;
        return resolveInfo;
    }

    private static void queryIntentProviderForPackage(Context context, PluginPackageParser packageParser, Intent intent, int flags, List<ResolveInfo> outList) throws Exception {
        List<ProviderInfo> providerInfos = packageParser.getProviders();
        if (providerInfos != null && providerInfos.size() >= 0) {
            for (ProviderInfo providerInfo : providerInfos) {
                ComponentName className = new ComponentName(providerInfo.packageName, providerInfo.name);
                List<IntentFilter> intentFilters = packageParser.getProviderIntentFilter(className);
                if (intentFilters != null && intentFilters.size() > 0) {
                    for (IntentFilter intentFilter : intentFilters) {
                        int match = intentFilter.match(context.getContentResolver(), intent, true, "");
                        if (match >= 0) {
                            ProviderInfo flagInfo = packageParser.getProviderInfo(new ComponentName(providerInfo.packageName, providerInfo.name), flags);
                            if ((flags & PackageManager.MATCH_DEFAULT_ONLY) != 0) {
                                if (intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)) {
                                    ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
                                    resolveInfo.match = match;
                                    resolveInfo.isDefault = true;
                                    outList.add(resolveInfo);
                                } else {
                                    //只是匹配默认。这里也算匹配不上。
                                }
                            } else {
                                ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
                                resolveInfo.match = match;
                                resolveInfo.isDefault = false;
                                outList.add(resolveInfo);
                            }
                        }
                    }
                    if (outList.size() <= 0) {
                        //没有在插件包中找到IntentFilter匹配的Service
                    }
                } else {
                    //该插件包中没有具有IntentFilter的Service
                }
            }
        } else {
            //该插件apk包中没有Service
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static ResolveInfo newResolveInfo(ProviderInfo providerInfo, IntentFilter intentFilter) {
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.providerInfo = providerInfo;
        resolveInfo.filter = intentFilter;
        resolveInfo.resolvePackageName = providerInfo.packageName;
        resolveInfo.labelRes = providerInfo.labelRes;
        resolveInfo.icon = providerInfo.icon;
        resolveInfo.specificIndex = 1;
//      默认就是false，不用再设置了。
//        resolveInfo.system = false;
        resolveInfo.priority = intentFilter.getPriority();
        resolveInfo.preferredOrder = 0;
        return resolveInfo;
    }

    private static void queryIntentReceiverForPackage(Context context, PluginPackageParser packageParser, Intent intent, int flags, List<ResolveInfo> outList) throws Exception {
        List<ActivityInfo> receivers = packageParser.getReceivers();
        if (receivers != null && receivers.size() >= 0) {
            for (ActivityInfo receiver : receivers) {
                List<IntentFilter> intentFilters = packageParser.getReceiverIntentFilter(receiver);
                if (intentFilters != null && intentFilters.size() > 0) {
                    for (IntentFilter intentFilter : intentFilters) {
                        int match = intentFilter.match(context.getContentResolver(), intent, true, "");
                        if (match >= 0) {
                            ActivityInfo flagInfo = packageParser.getReceiverInfo(new ComponentName(receiver.packageName, receiver.name), flags);
                            if ((flags & PackageManager.MATCH_DEFAULT_ONLY) != 0) {
                                if (intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)) {
                                    ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
                                    resolveInfo.match = match;
                                    resolveInfo.isDefault = true;
                                    outList.add(resolveInfo);
                                } else {
                                    //只是匹配默认。这里也算匹配不上。
                                }
                            } else {
                                ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
                                resolveInfo.match = match;
                                resolveInfo.isDefault = false;
                                outList.add(resolveInfo);
                            }
                        }
                    }
                    if (outList.size() <= 0) {
                        //没有在插件包中找到IntentFilter匹配的ACTIVITY
                    }
                } else {
                    //该插件包中没有具有IntentFilter的ACTIVITY
                }
            }
        } else {
            //该插件apk包中没有ACTIVITY
        }
    }


}






















