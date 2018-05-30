package com.earthgee.bundle.framework;

import android.os.Build;

import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;
import com.earthgee.bundle.runtime.RuntimeArgs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhaoruixuan on 2018/4/24.
 * bundle管理类
 */
public class Framework {

    public static final String SYMBOL_SEMICOLON=";";

    private static String BASEDIR = null;
    static String STORAGE_LOCATION;

    static final Logger log;
    static Properties properties;
    static Map<String, Bundle> bundles;

    private static long nextBundleID;

    static {
        log = LoggerFactory.getLogcatLogger("Framework");
        bundles = new ConcurrentHashMap<>();
        nextBundleID = 1;
    }

    static void startup(Properties pros) throws BundleException {
        if (properties == null) {
            properties = new Properties();
        }
        properties = pros;
        startup();
    }

    public static List<Bundle> getBundles(){
        List<Bundle> arrayList=new ArrayList<>(bundles.size());
        synchronized (bundles) {
            arrayList.addAll(bundles.values());
        }
        return arrayList;
    }

    static void startup() throws BundleException {
        log.log("*------------------------------------*", Logger.LogLevel.DBUG);
        log.log(" Ctrip Bundle on " + Build.MODEL + "|" + Build.CPU_ABI + "starting...", Logger.LogLevel.DBUG);
        log.log("*------------------------------------*", Logger.LogLevel.DBUG);

        long currentTimeMillis = System.currentTimeMillis();
        initialize();
        launch();
        boolean isInit = getProperty("earthgee.bundle.init", false);
        if (isInit) {
            File file = new File(STORAGE_LOCATION);
            if (file.exists()) {
                log.log("Purging Storage ...", Logger.LogLevel.DBUG);
                deleteDirectory(file);
            }
            file.mkdirs();
            storeProfile();
        } else {
            restoreProfile();
        }

        long endTimeMillis = System.currentTimeMillis() - currentTimeMillis;

        log.log("*------------------------------------*", Logger.LogLevel.DBUG);
        log.log(" Framework " + (isInit ? "restarted" : "start") + " in " + endTimeMillis + " ms", Logger.LogLevel.DBUG);
        log.log("*------------------------------------*", Logger.LogLevel.DBUG);
    }

    private static void initialize() {
        File filesDir = RuntimeArgs.androidApplication.getFilesDir();
        BASEDIR = properties.getProperty("earthgee.android.bundle.basedir", filesDir.getAbsolutePath());
    }

    private static void launch() {
        STORAGE_LOCATION = properties.getProperty("earthgee.android.bundle.storage", properties.getProperty("earthgee.android.bundle.framework.dir", BASEDIR + File.separatorChar + "storage")) + File.separatorChar;
    }

    public static boolean getProperty(String str, boolean defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        String str2 = (String) properties.get(str);
        return str2 != null ? Boolean.valueOf(str2).booleanValue() : defaultValue;
    }

    public static void deleteDirectory(File file) {
        if (file != null) {
            File[] listFiles = file.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isDirectory()) {
                    deleteDirectory(listFiles[i]);
                } else {
                    listFiles[i].delete();
                }
            }
            file.delete();
        }
    }

    private static void storeProfile(){
        BundleImpl[] bundleImplArr=getBundles().toArray(new BundleImpl[bundles.size()]);
        for(BundleImpl bundleImpl:bundleImplArr){
            bundleImpl.updateMetadata();
        }
        storeMetadata();
    }

    private static void storeMetadata() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(new File(STORAGE_LOCATION, "meta")));
            dataOutputStream.writeLong(nextBundleID);
            dataOutputStream.flush();
            dataOutputStream.close();
        } catch (Throwable e) {
            log.log("Could not save meta data.", Logger.LogLevel.ERROR, e);
        }
    }

    private static int restoreProfile(){
        try{
            log.log("Restoring profile", Logger.LogLevel.DBUG);
            File file=new File(STORAGE_LOCATION,"meta");
            if(file.exists()){
                DataInputStream dataInputStream=new DataInputStream(new FileInputStream(file));
                nextBundleID=dataInputStream.readLong();
                dataInputStream.close();
                File file2=new File(STORAGE_LOCATION);
                File[] listFiles=file2.listFiles();
                int i=0;
                while (i<listFiles.length){
                    if(listFiles[i].isDirectory()&&new File(listFiles[i],"meta").exists()){
                        try{
                            String location = new BundleImpl(listFiles[i]).location;
                            log.log("RESTORED BUNDLE " + location, Logger.LogLevel.DBUG);
                        }catch (Exception e){
                            log.log(e.getMessage(), Logger.LogLevel.ERROR, e.getCause());
                        }
                    }
                    i++;
                }
                return 1;
            }
            log.log("Profile not found, performing clean start ...", Logger.LogLevel.DBUG);
            return -1;
        }catch (Exception e2){
            e2.printStackTrace();
            return 0;
        }
    }

    public static Bundle getBundle(String str){
        return bundles.get(str);
    }

    static BundleImpl installNewBundle(String location, InputStream inputStream) throws BundleException{
        BundleImpl bundleImpl= (BundleImpl) getBundle(location);
        if(bundleImpl!=null){
            return bundleImpl;
        }
        long j=nextBundleID;
        nextBundleID=1+j;
        bundleImpl=new BundleImpl(new File(STORAGE_LOCATION,String.valueOf(j)),location,j,inputStream);
        storeMetadata();
        return bundleImpl;
    }



}










