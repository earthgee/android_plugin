package com.earthgee.corelibrary.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import com.earthgee.corelibrary.PluginManager;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class ComponentsHandler {

    private PluginManager mPluginManager;
    private Context mContext;

    private StubActivityInfo mStubActivityInfo=new StubActivityInfo();

    public ComponentsHandler(PluginManager pluginManager){
        mPluginManager=pluginManager;
        mContext=pluginManager.getHostContext();
    }

    public Intent transformIntentToExplicitAsNeeded(Intent intent){
        ComponentName component=intent.getComponent();
        if(component==null){
            ResolveInfo info=mPluginManager.resolveActivity(intent);
            if(info!=null&&info.activityInfo!=null){
                component=new ComponentName(info.activityInfo.packageName,info.activityInfo.name);
                intent.setComponent(component);
            }
        }

        return intent;
    }

    public void markIntentIfNeeded(Intent intent){
        if(intent.getComponent()==null){
            return;
        }

        String targetPackageName=intent.getComponent().getPackageName();
        String targetClassName=intent.getComponent().getClassName();
        if(!targetPackageName.equals(mContext.getPackageName())
                &&mPluginManager.getLoadedPlugin(targetPackageName)!=null){
            intent.putExtra(Constants.KEY_IS_PLUGIN,true);
            intent.putExtra(Constants.KEY_TARGET_PACKAGE,targetPackageName);
            intent.putExtra(Constants.KEY_TARGET_ACTIVITY,targetClassName);
            dispatchStubActivity(intent);
        }
    }

    private void dispatchStubActivity(Intent intent){
        ComponentName component=intent.getComponent();
        String targetClassName=intent.getComponent().getClassName();
        LoadedPlugin loadedPlugin=mPluginManager.getLoadedPlugin(intent);
        ActivityInfo info=loadedPlugin.getActivityInfo(component);
        if(info==null){
            throw new RuntimeException("can not find "+component);
        }
        int launchMode=info.launchMode;
        Resources.Theme themeObj=loadedPlugin.getResources().newTheme();
        themeObj.applyStyle(info.theme,true);
        String stubActivity=mStubActivityInfo.getStubActivity(targetClassName,launchMode,themeObj);
        intent.setClassName(mContext,stubActivity);
    }

}
















