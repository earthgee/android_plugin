package com.earthgee.corelibrary.internal;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.net.Uri;
import android.util.Log;

import com.earthgee.corelibrary.PluginManager;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/8/8.
 */
public class PluginContentResolver extends ContentResolver{

    private ContentResolver mBase;
    private PluginManager mPluginManager;

    private static Method sAcquireProvider;
    private static Method sAcquireExistingProvider;
    private static Method sAcquireUnstableProvider;

    static {
        try{
            sAcquireProvider=ContentResolver.class.
                    getDeclaredMethod("acquireProvider",new Class[]{Context.class,String.class});
            sAcquireProvider.setAccessible(true);
            sAcquireExistingProvider=ContentResolver.class.
                    getDeclaredMethod("acquireExistingProvider",new Class[]{Context.class,String.class});
            sAcquireExistingProvider.setAccessible(true);
            sAcquireUnstableProvider=ContentResolver.class.
                    getDeclaredMethod("acquireUnstableProvider",new Class[]{Context.class,String.class});
            sAcquireUnstableProvider.setAccessible(true);
        }catch (Exception e){
        }
    }

    public PluginContentResolver(Context context) {
        super(context);
        mBase=context.getContentResolver();
        mPluginManager=PluginManager.getInstance(context);
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

    protected IContentProvider acquireProvider(Context context,String auth){
        try{
            if(mPluginManager.resolveContentProvider(auth,0)!=null){
                return mPluginManager.getIContentProvider();
            }

            return (IContentProvider) sAcquireProvider.invoke(mBase,context,auth);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    protected IContentProvider acquireExistingProvider(Context context,String auth){
        try{
            if(mPluginManager.resolveContentProvider(auth,0)!=null){
                return mPluginManager.getIContentProvider();
            }

            return (IContentProvider) sAcquireExistingProvider.invoke(mBase,context,auth);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    protected IContentProvider acquireUnstableProvider(Context context,String auth){
        try{
            if(mPluginManager.resolveContentProvider(auth,0)!=null){
                return mPluginManager.getIContentProvider();
            }

            return (IContentProvider) sAcquireUnstableProvider.invoke(mBase,context,auth);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public boolean releaseProvider(IContentProvider provider){
        return true;
    }

    public boolean releaseUnstableProvider(IContentProvider icp){
        return true;
    }

    public void unstableProviderDied(IContentProvider icp){

    }

    public static String getUri(Context context){
        return "content://"+context.getPackageName()+".VirtualSTUDY.Provider";
    }

    public static String getAuthority(Context context) {
        return context.getPackageName() + ".VirtualSTUDY.Provider";
    }

}













