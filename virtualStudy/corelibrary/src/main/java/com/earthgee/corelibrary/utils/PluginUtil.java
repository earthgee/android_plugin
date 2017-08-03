package com.earthgee.corelibrary.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.internal.Constants;
import com.earthgee.corelibrary.internal.LoadedPlugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zhaoruixuan on 2017/7/25.
 */
public class PluginUtil {

    public static String getTargetActivity(Intent intent){
        return intent.getStringExtra(Constants.KEY_TARGET_ACTIVITY);
    }

    public static boolean isIntentFromPlugin(Intent intent){
        return intent.getBooleanExtra(Constants.KEY_IS_PLUGIN,false);
    }

    public static int getTheme(Context context,Intent intent){
        return PluginUtil.getTheme(context,PluginUtil.getComponent(intent));
    }

    public static int getTheme(Context context,ComponentName component){
        LoadedPlugin loadedPlugin= PluginManager.getInstance(context).getLoadedPlugin(component);

        if(null==loadedPlugin){
            return 0;
        }

        ActivityInfo info=loadedPlugin.getActivityInfo(component);

        if(null==info){
            return 0;
        }

        if(0!=info.theme){
            return info.theme;
        }

        ApplicationInfo appInfo=info.applicationInfo;
        if(null!=appInfo&&appInfo.theme!=0){
            return appInfo.theme;
        }

        return PluginUtil.selectDefaultTheme(0, Build.VERSION.SDK_INT);
    }


    public static int selectDefaultTheme(final int curTheme,final int targetSdkVersion){
        return selectSystemTheme(curTheme,targetSdkVersion,android.R.style.Theme,
                android.R.style.Theme_Holo,android.R.style.Theme_DeviceDefault,android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
    }

    private static int selectSystemTheme(final int curTheme,final int targetSdkVersion,final int orig,final int holo,
                                         final int dark,final int deviceDefault){
        if(curTheme!=0){
            return curTheme;
        }

        if(targetSdkVersion<11){
            return orig;
        }

        if(targetSdkVersion<14){
            return holo;
        }

        if(targetSdkVersion<24){
            return dark;
        }

        return deviceDefault;
    }

    public static final boolean isLocalService(final ServiceInfo serviceInfo){
        return TextUtils.isEmpty(serviceInfo.processName)||serviceInfo.applicationInfo.packageName.equals(serviceInfo.processName);
    }

    public static void putBinder(Bundle bundle, String key, IBinder value){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bundle.putBinder(key,value);
        }else{
            try{
                ReflectUtil.invoke(Bundle.class,bundle,"putIBinder",new Class[]{String.class,IBinder.class},key,value);
            }catch (Exception e){
            }
        }
    }

    public static IBinder getBinder(Bundle bundle,String key){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return bundle.getBinder(key);
        }else{
            try{
                return (IBinder) ReflectUtil.invoke(Bundle.class,bundle,"getIBinder",key);
            }catch (Exception e){
            }

            return null;
        }
    }

    public static void copyNativeLib(File apk, Context context,
                                     PackageInfo packageInfo,File nativeLibDir){
        try{
            String cpuArch;
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                cpuArch=Build.SUPPORTED_ABIS[0];
            }else{
                cpuArch=Build.CPU_ABI;
            }
            boolean findSo=false;

            ZipFile zipFile=new ZipFile(apk.getAbsolutePath());
            ZipEntry entry;
            Enumeration e=zipFile.entries();
            while (e.hasMoreElements()){
                entry= (ZipEntry) e.nextElement();
                if(entry.isDirectory()){
                    continue;
                }
                if(entry.getName().endsWith("so")&&entry.getName().contains("lib/"+cpuArch)){
                    findSo=true;
                    break;
                }
            }
            e=zipFile.entries();
            while (e.hasMoreElements()){
                entry= (ZipEntry) e.nextElement();
                if(entry.isDirectory()||!entry.getName().endsWith(".so")) continue;
                if((findSo&&entry.getName().contains("lib/"+cpuArch))||
                        (!findSo&&entry.getName().contains("lib/armeabi/"))){
                    String[] temp=entry.getName().split("/");
                    String libName=temp[temp.length-1];
                    File libFile=new File(nativeLibDir.getAbsolutePath()+File.separator+libName);
                    String key=packageInfo.packageName+"_"+libName;
                    if(libFile.exists()){
                        int VersionCode=Settings.getSoVersion(context,key);
                        if(VersionCode==packageInfo.versionCode){
                            continue;
                        }
                    }
                    FileOutputStream fos=new FileOutputStream(libFile);
                    copySo(zipFile.getInputStream(entry),fos);
                    Settings.setSoVersion(context,key,packageInfo.versionCode);
                }
            }

            zipFile.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void copySo(InputStream input, OutputStream output) throws IOException{
        BufferedInputStream bufferedInput=new BufferedInputStream(input);
        BufferedOutputStream bufferedOutput=new BufferedOutputStream(output);
        int count;
        byte[] data=new byte[8192];
        while ((count=bufferedInput.read(data,0,8192))!=-1){
            bufferedOutput.write(data,0,count);
        }
        bufferedOutput.flush();
        bufferedOutput.close();
        output.close();
        bufferedInput.close();
        input.close();
    }

    public static ComponentName getComponent(Intent intent){
        return new ComponentName(intent.getStringExtra(Constants.KEY_TARGET_PACKAGE),
                intent.getStringExtra(Constants.KEY_TARGET_ACTIVITY));
    }

}






















































