package com.earthgee.mutlidex;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/6/21.
 */
public class MultiDexExtractor {

    private static final String PREFS_FILE="multidex.version";
    private static final String KEY_TIME_STAMP="timestamp";
    private static final String KEY_CRC="crc";
    private static final long NO_VALUE=-1L;

    static List<File> load(Context context, ApplicationInfo applicationInfo,File dexDir,boolean forceReload) throws IOException{
        File sourceApk=new File(applicationInfo.sourceDir);
        long currentCrc=getZipCrc(sourceApk);
        List files;
        if(!forceReload&&!isModified(context,sourceApk,currentCrc)){
            try{
                files=loadExistingExtraction(context,sourceApk,dexDir);
            }catch (IOException e){
                files=performExtractions(sourceApk,dexDir);
                putStoreApkInfo(context,getTimeStamp(sourceApk),currentCrc,files.size()+1);
            }
        }else{
            files=performExtractions(sourceApk,dexDir);
            putStoreApkInfo(context,getTimeStamp(sourceApk),currentCrc,files.size()+1);
        }
    }

    private static long getZipCrc(File archive) throws IOException{
        long computedValue=ZipUtil.getZipCrc(archive);
        if(computedValue==-1L){
            --computedValue;
        }
        return computedValue;
    }

    private static boolean isModified(Context context,File archive,long currentCrc){
        SharedPreferences prefs=getMultiDexPreferences(context);
        return (prefs.getLong(KEY_TIME_STAMP,NO_VALUE)!=getTimeStamp(archive))||(prefs.getLong(KEY_CRC,NO_VALUE)!=currentCrc);
    }

    private static long getTimeStamp(File archive){
        long timeStamp=archive.lastModified();
        if(timeStamp==NO_VALUE){
            timeStamp--;
        }
        return timeStamp;
    }

    private static SharedPreferences getMultiDexPreferences(Context context){
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT<11?Context.MODE_PRIVATE:Context.MODE_PRIVATE|0x0004);
    }



}


















