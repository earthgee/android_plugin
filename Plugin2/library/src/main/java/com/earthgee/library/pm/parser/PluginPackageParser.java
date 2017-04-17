package com.earthgee.library.pm.parser;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class PluginPackageParser {

    private final File mPluginFile;
    private final PackageParser mParser;
    private final String mPackageName;
    private final Context mHostContext;
    private final PackageInfo mHostPackageInfo;

    private Map<ComponentName,Object> mActivityObjCache=new TreeMap<>()

    public PluginPackageParser(Context hostContext, File pluginFile) throws Exception{
        mHostContext=hostContext;
        mPluginFile=pluginFile;
        mParser=PackageParser.newPluginParser(hostContext);
        mParser.parsePackage(pluginFile,0);
        mPackageName=mParser.getPackageName();
        mHostPackageInfo=mHostContext.getPackageManager().
                getPackageInfo(mHostContext.getPackageName(), 0);

        List datas=mParser.getActivities();
        for(Object data:datas){

        }
    }

}




























