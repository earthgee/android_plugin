package com.earthgee.corelibrary.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

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

}






















































