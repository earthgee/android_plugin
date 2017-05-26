package com.earthgee.libaray.pm.parser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class IntentMatcher {

    private static final Comparator<ResolveInfo> mResolvePrioritySorter =
            new Comparator<ResolveInfo>() {
                public int compare(ResolveInfo r1, ResolveInfo r2) {
                    int v1 = r1.priority;
                    int v2 = r2.priority;
                    //System.out.println("Comparing: q1=" + q1 + " q2=" + q2);
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
                    if (v1 != v2) {
                        return (v1 > v2) ? -1 : 1;
                    }
                    return 0;
                }
            };

    public static final List<ResolveInfo> resolveActivityIntent(Context context, Map<String,PluginPackageParser> pluginPackages
            , Intent intent,String resolvedType,int flags) throws Exception{
        if(intent==null||context==null){
            return null;
        }
        List<ResolveInfo> list=new ArrayList<>();
        ComponentName comp=intent.getComponent();
        if(comp==null){
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                if(intent.getSelector()!=null){
                    intent=intent.getSelector();
                    comp=intent.getComponent();
                }
            }
        }

        if(comp!=null&&comp.getPackageName()!=null){
            PluginPackageParser parser=pluginPackages.get(comp.getPackageName());
            if(parser!=null){
                queryIntentActivityForPackage(context,parser,intent,flags,list);
                if(list.size()>0){
                    Collections.sort(list,mResolvePrioritySorter);
                    return list;
                }
            }else{
            }
            Collections.sort(list,mResolvePrioritySorter);
            return list;
        }

        final String pkgName=intent.getPackage();
        if(pkgName!=null){
            PluginPackageParser parser=pluginPackages.get(pkgName);
            if(parser!=null){
                queryIntentActivityForPackage(context,parser,intent,flags,list);
            }
        }else{
            for(PluginPackageParser parser:pluginPackages.values()){
                queryIntentActivityForPackage(context,parser,intent,flags,list);
            }
        }

        Collections.sort(list,mResolvePrioritySorter);
        return list;
    }

    private static void queryIntentActivityForPackage(Context context,PluginPackageParser packageParser
            ,Intent intent,int flags,List<ResolveInfo> outList) throws Exception{
        List<ActivityInfo> activityInfos=packageParser.getActivities();
        if(activityInfos!=null&&activityInfos.size()>=0){
            for(ActivityInfo activityInfo:activityInfos){
                ComponentName className=new ComponentName(activityInfo.packageName,activityInfo.name);
                List<IntentFilter> intentFilters=packageParser.getActivityIntentFilter(className);
                if(intentFilters!=null&&intentFilters.size()>0){
                    for(IntentFilter intentFilter:intentFilters){
                        int match=intentFilter.match(context.getContentResolver(),intent,true,"");
                        if(match>0){
                            ActivityInfo flagInfo=packageParser.getActivityInfo(new ComponentName(activityInfo.packageName,
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

}

















