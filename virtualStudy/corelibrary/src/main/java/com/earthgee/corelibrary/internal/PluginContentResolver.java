package com.earthgee.corelibrary.internal;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * Created by zhaoruixuan on 2017/8/8.
 */
public class PluginContentResolver extends ContentResolver{



    public PluginContentResolver(Context context) {
        super(context);
    }

    public static Uri wrapperUri(LoadedPlugin loadedPlugin,Uri pluginUri){
        String pkg=loadedPlugin.getPackageName();
        String pluginUriString=Uri.encode(pluginUri.toString());
        StringBuilder builder=new StringBuilder(PluginContentResolver.getUri(loadedPlugin.getHostContext()));
        builder.append("/?plugin="+loadedPlugin.getLocation());
        builder.append("&pkg="+pkg);
        builder.append("&uri="+pluginUriString);
        Uri wrappedUri=Uri.parse(builder.toString());
        Log.d("earthgee2","translateuri="+wrappedUri.toString());
        return wrappedUri;
    }

    public static String getUri(Context context){
        return "content://"+context.getPackageName()+".VirtualSTUDY.Provider";
    }

}
