package com.earthgee.library.core;

/**
 * Created by zhaoruixuan on 2017/4/14.
 */

import android.content.Context;
import android.os.Environment;
import android.view.animation.CycleInterpolator;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

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

    public static String makePluginBaseDir(Context context,String pluginInfoPackageName){
        init(context);
        return enforceDirExists(new File(sBaseDir,pluginInfoPackageName));
    }

    public static String getPluginDataDir(Context context,String pluginInfoPackageName){
        return enforceDirExists(new File(makePluginBaseDir(context,pluginInfoPackageName)
                ,"data/"+pluginInfoPackageName));
    }

    public static String getPluginSignatureDir(Context context,String pluginInfoPackageName){
        return enforceDirExists(new File(makePluginBaseDir(context,pluginInfoPackageName),"Signature/"));
    }

    public static String getPluginSignatureFile(Context context,String pluginInfoPackageName,int index){
        return new File(getPluginSignatureDir(context,pluginInfoPackageName),String.format("Signature_%s.key",index)).getPath();
    }

    public static List<String> getPluginSignatureFiles(Context context,String pluginInfoPackageName){
        ArrayList<String> files=new ArrayList<>();
        String dir=getPluginSignatureDir(context,pluginInfoPackageName);
        File d=new File(dir);
        File[] fs=d.listFiles();
        if(fs!=null&&fs.length>0){
            for(File f:fs){
                files.add(f.getPath());
            }
        }
        return files;
    }

    public static String getContextDataDir(Context context){
        String dataDir=new File(Environment.getDataDirectory(),"data/").getPath();
        return new File(dataDir,context.getPackageName()).getPath();
    }

    //存放插件dex包的目录
    public static String getPluginDalvikCacheDir(Context context,String pluginInfoPackageName){
        return enforceDirExists(new File(makePluginBaseDir(context,pluginInfoPackageName),"dalvik-cache"));
    }

    //存放插件so包的目录
    public static String getPluginNativeLibraryDir(Context context,String pluginInfoPackageName){
        return enforceDirExists(new File(makePluginBaseDir(context, pluginInfoPackageName),"lib"));
    }

    //查找apk文件所在目录
    public static String getPluginApkDir(Context context,String pluginInfoPackageName){
        return enforceDirExists(new File(makePluginBaseDir(context,pluginInfoPackageName),"apk"));
    }

    //查找插件apk文件所在
    public static String getPluginApkFile(Context context,String pluginInfoPackageName){
        return new File(getPluginApkDir(context,pluginInfoPackageName),"base-1.apk").getPath();
    }

    //清理dex包所在目录
    public static void cleanOptimizedDirectory(String optimizedDirectory){
        try{
            File dir=new File(optimizedDirectory);
            if(dir.exists()&&dir.isDirectory()){
                File[] files=dir.listFiles();
                if(files!=null&&files.length>0){
                    for(File f:files){
                        f.delete();
                    }
                }
            }

            if(dir.exists()&&dir.isFile()){
                dir.delete();
                dir.mkdirs();
            }
        }catch (Exception e){
        }
    }



}















































