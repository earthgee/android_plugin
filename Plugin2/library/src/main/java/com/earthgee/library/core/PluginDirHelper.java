package com.earthgee.library.core;

/**
 * Created by zhaoruixuan on 2017/4/14.
 */

import android.content.Context;

import java.io.File;

/**
 * 插件目录结构
 * 基本目录： /data/data/com.HOST.PACKAGE/Plugin
 * 单个插件的基本目录： /data/data/com.HOST.PACKAGE/Plugin
 * source_dir： /data/data/com.HOST.PACKAGE/Plugin/PLUGIN.PKG/apk/base-1.apk
 * 数据目录： /data/data/com.HOST.PACKAGE/Plugin/PLUGIN.PKG/data/PLUGIN.PKG
 * dex缓存目录： /data/data/com.HOST.PACKAGE/Plugin/PLUGIN.PKG/dalvik-cache/
 * <p>
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/2/5.
 */
public class PluginDirHelper {

    private static File sBaseDir=null;

    private static void init(Context context){
        if(sBaseDir==null){
            sBaseDir=new File(context.getCacheDir().getParentFile(),"Plugin");
            enforceDirExists(sBaseDir);
        }
    }

    private static String enforceDirExists(File file){
        if(!file.exists()){
            file.mkdirs();
        }
        return file.getPath();
    }

    public static String getBaseDir(Context context){
        init(context);
        return enforceDirExists(sBaseDir);
    }



}


















