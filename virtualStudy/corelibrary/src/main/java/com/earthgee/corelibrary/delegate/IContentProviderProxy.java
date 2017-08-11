package com.earthgee.corelibrary.delegate;

import android.content.Context;
import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.internal.LoadedPlugin;
import com.earthgee.corelibrary.internal.PluginContentResolver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by zhaoruixuan on 2017/8/9.
 */
public class IContentProviderProxy implements InvocationHandler{

    private IContentProvider mBase;
    private Context mContext;

    private IContentProviderProxy(Context context, IContentProvider iContentProvider){
        mBase=iContentProvider;
        mContext=context;
    }

    public static IContentProvider newInstance(Context context,IContentProvider iContentProvider){
        return (IContentProvider) Proxy.newProxyInstance(iContentProvider.getClass().getClassLoader(),
                new Class[]{IContentProvider.class},new IContentProviderProxy(context, iContentProvider));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        wrapperUri(method,args);

        try{
            return method.invoke(mBase,args);
        }catch (InvocationTargetException e){
            throw e.getTargetException();
        }
    }

    private void wrapperUri(Method method,Object[] args){
        Uri uri=null;
        int index=0;
        if(args!=null){
            for(int i=0;i<args.length;i++){
                if(args[i] instanceof Uri){
                    uri= (Uri) args[i];
                    index=i;
                    break;
                }
            }
        }

        Bundle bundleInCallMethod=null;
        if(method.getName().equals("call")){
            bundleInCallMethod=getBundleParameter(args);
            if(bundleInCallMethod!=null){
                String uriString=bundleInCallMethod.getString(RemoteContentProvider.KEY_WRAPPER_URI);
                if(uriString!=null){
                    uri=Uri.parse(uriString);
                }
            }
        }

        if(uri==null){
            return;
        }

        PluginManager pluginManager=PluginManager.getInstance(mContext);
        ProviderInfo info=pluginManager.resolveContentProvider(uri.getAuthority(),0);
        if(info!=null){
            String pkg=info.packageName;
            LoadedPlugin plugin=pluginManager.getLoadedPlugin(pkg);
            String pluginUri=Uri.encode(uri.toString());
            StringBuilder builder=new StringBuilder(PluginContentResolver.getUri(mContext));
            builder.append("/?plugin="+plugin.getLocation());
            builder.append("&pkg="+pkg);
            builder.append("&uri="+pluginUri);
            Uri wrapperUri=Uri.parse(builder.toString());
            if(method.getName().equals("call")){
                bundleInCallMethod.putString(RemoteContentProvider.KEY_WRAPPER_URI,wrapperUri.toString());
            }else{
                args[index]=wrapperUri;
            }
        }
    }

    private Bundle getBundleParameter(Object[] args){
        Bundle bundle=null;
        if(args!=null){
            for(int i=0;i<args.length;i++){
                if(args[i] instanceof Bundle){
                    bundle= (Bundle) args[i];
                    break;
                }
            }
        }

        return bundle;
    }

}















