package com.earthgee.bundle.runtime;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.earthgee.bundle.framework.Bundle;
import com.earthgee.bundle.framework.BundleImpl;
import com.earthgee.bundle.framework.Framework;
import com.earthgee.bundle.hack.AndroidHack;
import com.earthgee.bundle.hack.SysHacks;
import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by earthgee on 18/5/28.
 */

public class DelegateResources extends Resources{

    static final Logger log;

    static {
        log= LoggerFactory.getLogcatLogger("DelegateResources");
    }

    public DelegateResources(AssetManager assets,Resources resources){
        super(assets,resources.getDisplayMetrics(),resources.getConfiguration());
    }

    public static void newDelegateResources(Application application,Resources resources) throws Exception{
        List<Bundle> bundles= Framework.getBundles();
        if(bundles!=null&&!bundles.isEmpty()){
            Resources delegateResources;
            List<String> arrayList=new ArrayList<>();
            arrayList.add(application.getApplicationInfo().sourceDir);
            for(Bundle bundle:bundles){
                arrayList.add(((BundleImpl)bundle).getArchive().getArchiveFile().getAbsolutePath());
            }
            AssetManager assetManager=AssetManager.class.newInstance();
            for(String str:arrayList){
                SysHacks.AssetManager_addAssetPath.invoke(assetManager,str);
            }
            if(resources==null||!resources.getClass().getName().equals("android.content.res.MiuiResources")){
                delegateResources=new DelegateResources(assetManager,resources);
            }else{
                Constructor declaredConstructor=Class.forName("android.content.res.MiuiResources").
                        getDeclaredConstructor(new Class[]{AssetManager.class,DisplayMetrics.class,Configuration.class});
                declaredConstructor.setAccessible(true);
                delegateResources= (Resources) declaredConstructor.
                        newInstance(new Object[]{assetManager,resources.getDisplayMetrics(),resources.getConfiguration()});
            }
            RuntimeArgs.delegateResources=delegateResources;
            AndroidHack.injectResources(application,delegateResources);
            StringBuffer stringBuffer=new StringBuffer();
            stringBuffer.append("newDelegateResources [");
            for(int i=0;i<arrayList.size();i++){
                if(i>0){
                    stringBuffer.append(",");
                }
                stringBuffer.append(arrayList.get(i));
            }
            stringBuffer.append("]");
            log.log(stringBuffer.toString(),Logger.LogLevel.DBUG);
        }
    }

}
