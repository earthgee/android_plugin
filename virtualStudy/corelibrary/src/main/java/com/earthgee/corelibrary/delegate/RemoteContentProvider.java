package com.earthgee.corelibrary.delegate;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.internal.LoadedPlugin;
import com.earthgee.corelibrary.utils.RunUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/8/7.
 */
public class RemoteContentProvider extends ContentProvider{

    public static final String KEY_PKG="pkg";
    public static final String KEY_PLUGIN="plugin";
    public static final String KEY_URI="uri";

    public static final String KEY_WRAPPER_URI="wrapper_uri";

    private static Map<String,ContentProvider> sCachedProviders=new HashMap<>();

    @Override
    public boolean onCreate() {
        return true;
    }

    private ContentProvider getContentProvider(final Uri uri){
        final PluginManager pluginManager=PluginManager.getInstance(getContext());
        Uri pluginUri=Uri.parse(uri.getQueryParameter(KEY_URI));
        final String auth=pluginUri.getAuthority();
        ContentProvider cachedProvider=sCachedProviders.get(auth);
        if(cachedProvider!=null){
            return cachedProvider;
        }

        synchronized (sCachedProviders){
            LoadedPlugin plugin=pluginManager.getLoadedPlugin(uri.getQueryParameter(KEY_PKG));
            if(plugin==null){
                try{
                    pluginManager.loadPlugin(new File(uri.getQueryParameter(KEY_PLUGIN)));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            final ProviderInfo providerInfo=pluginManager.resolveContentProvider(auth,0);
            if(providerInfo!=null){
                RunUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            LoadedPlugin loadedPlugin=pluginManager.getLoadedPlugin(uri.getQueryParameter(KEY_PKG));
                            ContentProvider contentProvider= (ContentProvider) loadedPlugin.getClassLoader().loadClass(providerInfo.name).newInstance();
                            contentProvider.attachInfo(loadedPlugin.getPluginContext(),providerInfo);
                            sCachedProviders.put(auth,contentProvider);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },true);
                return sCachedProviders.get(auth);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ContentProvider provider=getContentProvider(uri);
        Uri pluginUri=Uri.parse(uri.getQueryParameter(KEY_URI));
        if(provider!=null){
            return provider.query(pluginUri,projection,selection,selectionArgs,sortOrder);
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        ContentProvider provider=getContentProvider(uri);
        Uri pluginUri=Uri.parse(uri.getQueryParameter(KEY_URI));
        if(provider!=null){
            return provider.getType(pluginUri);
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        ContentProvider provider=getContentProvider(uri);
        Uri pluginUri=Uri.parse(uri.getQueryParameter(KEY_URI));
        if(provider!=null){
            return provider.insert(pluginUri,values);
        }

        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        ContentProvider provider=getContentProvider(uri);
        Uri pluginUri=Uri.parse(uri.getQueryParameter(KEY_URI));
        if(provider!=null){
            return provider.delete(pluginUri,selection,selectionArgs);
        }

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ContentProvider provider=getContentProvider(uri);
        Uri pluginUri=Uri.parse(uri.getQueryParameter(KEY_URI));
        if(provider!=null){
            return provider.update(pluginUri,values,selection,selectionArgs);
        }

        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        ContentProvider provider=getContentProvider(uri);
        Uri pluginUri=Uri.parse(uri.getQueryParameter(KEY_URI));
        if(provider!=null){
            return provider.bulkInsert(pluginUri,values);
        }

        return 0;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        try{
            Field uriField=ContentProviderOperation.class.getDeclaredField("mUri");
            uriField.setAccessible(true);
            for(ContentProviderOperation operation:operations){
                Uri pluginUri=Uri.parse(operation.getUri().getQueryParameter(KEY_URI));
                uriField.set(operation,pluginUri);
            }
        }catch (Exception e){
            return new ContentProviderResult[0];
        }

        if(operations.size()>0){
            ContentProvider provider=getContentProvider(operations.get(0).getUri());
            if(provider!=null){
                return provider.applyBatch(operations);
            }
        }

        return new ContentProviderResult[0];
    }

    @Nullable
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if(extras==null||extras.getString(KEY_WRAPPER_URI)==null){
            return null;
        }

        Uri uri=Uri.parse(extras.getString(KEY_WRAPPER_URI));
        ContentProvider provider=getContentProvider(uri);
        if(provider!=null){
            return provider.call(method,arg,extras);
        }

        return null;
    }

}

















