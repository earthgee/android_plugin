package com.earthgee.testplugin;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.earthgee.bundle.framework.BundleCore;
import com.earthgee.bundle.framework.BundleException;
import com.earthgee.bundle.hotpatch.HotPatchManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zhaoruixuan on 2018/4/24.
 */
public class BundleBaseApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences;
        boolean isDexInstalled=true;
        final String bundleKey;
        try{
            BundleCore.getInstance().init(this);
            BundleCore.getInstance().configLogger(true,1);
            Properties properties = new Properties();
            sharedPreferences = getSharedPreferences("bundlecore_configs", 0);
            String lastBundleKey = sharedPreferences.getString("last_bundle_key", "");
            bundleKey = buildBundleKey();
            if (!TextUtils.equals(bundleKey, lastBundleKey)) {
                properties.put("earthgee.bundle.init", "true");
                isDexInstalled = false;
                HotPatchManager.getInstance().purge();
            }
            BundleCore.getInstance().startup(properties);
            if(isDexInstalled){
                //HotPatchManager.getInstance().run();
                BundleCore.getInstance().run();
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ZipFile zipFile=new ZipFile(getApplicationInfo().sourceDir);
                            List bundleFiles=getBundleEntryNames(zipFile,BundleCore.LIB_PATH,".so");
                            if(bundleFiles!=null&&bundleFiles.size()>0){
                                processLibsBundles(zipFile,bundleFiles);
                                SharedPreferences.Editor edit = getSharedPreferences("bundlecore_configs", 0).edit();
                                edit.putString("last_bundle_key", bundleKey);
                                edit.commit();
                            }else{
                                Log.e("Error Bundle", "not found bundle in apk");
                            }
                            if (zipFile != null) {
                                try {
                                    zipFile.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            BundleCore.getInstance().run();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String buildBundleKey() throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
        return String.valueOf(packageInfo.versionCode) + "_" + packageInfo.versionName;
    }

    private List<String> getBundleEntryNames(ZipFile zipFile,String str,String str2){
        List<String> arrayList=new ArrayList<>();
        try{
            Enumeration entries=zipFile.entries();
            while(entries.hasMoreElements()){
                String name=((ZipEntry)entries.nextElement()).getName();
                if(name.startsWith(str)&&name.endsWith(str2)){
                    arrayList.add(name);
                }
            }
        }catch (Throwable e){
            Log.e("getBundleEntryNames", "Exception while get bundles in assets or lib", e);
        }
        return arrayList;
    }

    private void processLibsBundles(ZipFile zipFile,List<String> list){
        for(String str:list){
            processLibsBundle(zipFile,str);
        }
    }

    private boolean processLibsBundle(ZipFile zipFile,String str){
        String packageNameFromEntryName=getPackageNameFromEntryName(str);
        if(BundleCore.getInstance().getBundle(packageNameFromEntryName)==null){
            try{
                BundleCore.getInstance().installBundle(packageNameFromEntryName,
                        zipFile.getInputStream(zipFile.getEntry(str)));
                Log.e("Succeed install", "Succeed to install bundle " + packageNameFromEntryName);
                return true;
            }catch (BundleException e1){
                Log.e("Fail install", "Could not install bundle.", e1);
            }catch (IOException e2){
                Log.e("Fail install", "Could not install bundle.", e2);
            }
        }
        return false;
    }

    private String getPackageNameFromEntryName(String entryName){
        return entryName.substring(entryName.indexOf(BundleCore.LIB_PATH) + BundleCore.LIB_PATH.length(), entryName.indexOf(".so")).replace("_", ".");
    }

}






