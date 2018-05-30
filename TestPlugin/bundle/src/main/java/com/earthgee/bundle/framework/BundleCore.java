package com.earthgee.bundle.framework;

import android.app.Application;

import com.earthgee.bundle.hack.AndroidHack;
import com.earthgee.bundle.hack.SysHacks;
import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;
import com.earthgee.bundle.runtime.BundleInstalledListener;
import com.earthgee.bundle.runtime.DelegateResources;
import com.earthgee.bundle.runtime.InstrumentationHook;
import com.earthgee.bundle.runtime.RuntimeArgs;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhaoruixuan on 2018/4/24.
 */
public class BundleCore {

    public static final String LIB_PATH = "assets/";

    protected static BundleCore instance;
    static final Logger log;
    private List<BundleInstalledListener> bundleDelayListeners;
    private List<BundleInstalledListener> bundleSyncListeners;

    private BundleCore(){
        bundleDelayListeners=new ArrayList<>();
        bundleSyncListeners=new ArrayList<>();
    }

    static {
        log= LoggerFactory.getLogcatLogger(BundleCore.class);
    }

    public static synchronized BundleCore getInstance(){
        if(instance==null){
            instance=new BundleCore();
        }
        return instance;
    }

    public void configLogger(boolean isOpenLog,int level){
        LoggerFactory.isNeedLog=isOpenLog;
        LoggerFactory.minLevel=Logger.LogLevel.getValue(level);
    }

    public void init(Application application) throws Exception{
        SysHacks.defineAndVerify();
        RuntimeArgs.androidApplication=application;
        RuntimeArgs.delegateResources=application.getResources();
        AndroidHack.injectInstrumentationHook(new InstrumentationHook(AndroidHack.getInstrumentation(),application.getBaseContext()));
    }

    public void startup(Properties properties) {
        try {
            Framework.startup(properties);
        } catch (Exception e) {
            log.log("Bundle Dex installation failure", Logger.LogLevel.ERROR, e);
            throw new RuntimeException("Bundle dex installation failed (" + e.getMessage() + ").");
        }
    }

    public List<Bundle> getBundles(){
        return Framework.getBundles();
    }

    public Bundle getBundle(String bundleName){
        return Framework.getBundle(bundleName);
    }

    public Bundle installBundle(String location, InputStream inputStream) throws BundleException{
        return Framework.installNewBundle(location,inputStream);
    }

    public void run(){
        try{
            log.log("run",Logger.LogLevel.ERROR);
            for(Bundle bundle:BundleCore.getInstance().getBundles()){
                BundleImpl bundleImpl= (BundleImpl) bundle;
                try{
                    bundleImpl.optDexFile();
                }catch (Exception e){
                    e.printStackTrace();
                    log.log("Error while dexopt >>>",Logger.LogLevel.ERROR,e);
                }
            }
            notifySyncBundleListeners();
            DelegateResources.newDelegateResources(RuntimeArgs.androidApplication,RuntimeArgs.delegateResources);
        }catch (Exception e){
            e.printStackTrace();
            log.log("Error while dexopt >>>",Logger.LogLevel.ERROR,e);
        }
        System.setProperty("BUNDLES_INSTALLED","true");
    }

    private void notifySyncBundleListeners(){
        if(!bundleSyncListeners.isEmpty()){
            for(BundleInstalledListener bundleInstalledListener:bundleSyncListeners){
                bundleInstalledListener.onBundleInstalled();
            }
        }
    }

    private void notifyDelayBundleListeners(){
        if(!bundleDelayListeners.isEmpty()){
            for(BundleInstalledListener bundleInstalledListener:bundleDelayListeners){
                bundleInstalledListener.onBundleInstalled();
            }
        }
    }

}









