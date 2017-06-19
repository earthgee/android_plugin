package com.earthgee.libaray.stub;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.earthgee.libaray.core.Env;
import com.earthgee.libaray.core.PluginProcessManager;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.FieldUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class AbstractContentProviderStub extends ContentProvider{

    private ContentResolver mContentResolver;
    private Map<String,ContentProviderClient> sContentProviderClients=new HashMap<>();

    private Uri buildNewUri(Uri uri,String targetAuthority){
        Uri.Builder b=new Uri.Builder();
        b.scheme(uri.getScheme());
        b.authority(targetAuthority);
        b.path(uri.getPath());

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB){
            Set<String> names=uri.getQueryParameterNames();
            if(names!=null&&names.size()>0){
                for(String name:names){
                    if(!TextUtils.equals(name,Env.EXTRA_TARGET_AUTHORITY)){
                        b.appendQueryParameter(name,uri.getQueryParameter(name));
                    }
                }
            }
        }else{
            b.query(uri.getQuery());
        }

        b.fragment(uri.getFragment());
        return b.build();
    }

    private synchronized ContentProviderClient getContentProviderClient(final String targetAuthority){
        ContentProviderClient client=sContentProviderClients.get(targetAuthority);
        if(client!=null){
            return client;
        }

        if(Looper.getMainLooper()!=Looper.myLooper()){
            PluginManager.getInstance().waitForConnected();
        }

        ProviderInfo stubInfo=null;
        ProviderInfo targetInfo=null;
        try{
            String authority=getMyAuthority();
            stubInfo=getContext().getPackageManager().resolveContentProvider(authority,0);
            targetInfo=PluginManager.getInstance().resolveContentProvider(targetAuthority,0);
        }catch (Exception e){
        }

        if(stubInfo!=null&&targetInfo!=null){
            try{
                PluginManager.getInstance().reportMyProcessName(stubInfo.processName,targetInfo.processName,targetInfo.packageName);
            }catch (RemoteException e){
            }
        }

        try{
            if(targetInfo!=null){
                PluginProcessManager.preLoadApk(getContext(),targetInfo);
            }
        }catch (Exception e){
        }

        ContentProviderClient newClient=mContentResolver.acquireContentProviderClient(targetAuthority);
        sContentProviderClients.put(targetAuthority,newClient);

        try{
            if(stubInfo!=null&&targetInfo!=null){
                PluginManager.getInstance().onProviderCreated(stubInfo,targetInfo);
            }
        }catch (Exception e){
        }

        return sContentProviderClients.get(targetAuthority);
    }

    private String getMyAuthority() throws Exception{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            return (String) FieldUtils.readField(this,"mAuthority");
        }else{
            Context context=getContext();
            PackageInfo pkgInfo=context.getPackageManager().getPackageInfo
                    (context.getPackageName(), PackageManager.GET_PROVIDERS);
            if(pkgInfo!=null&&pkgInfo.providers!=null&&pkgInfo.providers.length>0){
                for(ProviderInfo info:pkgInfo.providers){
                    if(TextUtils.equals(info.name,getClass().getName())){
                        return info.authority;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        Log.d("earthgee1","stub content provider onCreate");
        mContentResolver=getContext().getContentResolver();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String targetAuthority=uri.getQueryParameter(Env.EXTRA_TARGET_AUTHORITY);
        if(!TextUtils.isEmpty(targetAuthority)&&!TextUtils.equals(targetAuthority,uri.getAuthority())){
            ContentProviderClient client=getContentProviderClient(targetAuthority);
            try{
                return client.query(buildNewUri(uri,targetAuthority),projection,selection,selectionArgs,sortOrder);
            }catch (RemoteException e){
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        String targetAuthority=uri.getQueryParameter(Env.EXTRA_TARGET_AUTHORITY);
        if(!TextUtils.isEmpty(targetAuthority)&&!TextUtils.equals(targetAuthority,uri.getAuthority())){
            ContentProviderClient client=getContentProviderClient(targetAuthority);
            try{
                return client.getType(buildNewUri(uri,targetAuthority));
            }catch (RemoteException e){
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String targetAuthority = uri.getQueryParameter(Env.EXTRA_TARGET_AUTHORITY);
        if (!TextUtils.isEmpty(targetAuthority) && !TextUtils.equals(targetAuthority, uri.getAuthority())) {
            ContentProviderClient client = getContentProviderClient(targetAuthority);
            try {
                return client.insert(buildNewUri(uri, targetAuthority), contentValues);
            } catch (RemoteException e) {
            }
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String targetAuthority = uri.getQueryParameter(Env.EXTRA_TARGET_AUTHORITY);
        if (!TextUtils.isEmpty(targetAuthority) && !TextUtils.equals(targetAuthority, uri.getAuthority())) {
            ContentProviderClient client = getContentProviderClient(targetAuthority);
            try {
                return client.delete(buildNewUri(uri, targetAuthority), selection, selectionArgs);
            } catch (RemoteException e) {
            }
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        String targetAuthority = uri.getQueryParameter(Env.EXTRA_TARGET_AUTHORITY);
        if (!TextUtils.isEmpty(targetAuthority) && !TextUtils.equals(targetAuthority, uri.getAuthority())) {
            ContentProviderClient client = getContentProviderClient(targetAuthority);
            try {
                return client.update(buildNewUri(uri, targetAuthority), contentValues, selection, selectionArgs);
            } catch (RemoteException e) {
            }
        }
        return 0;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        String targetAuthority = extras != null ? extras.getString(Env.EXTRA_TARGET_AUTHORITY) : null;
        String targetMethod = extras != null ? extras.getString(Env.EXTRA_TARGET_AUTHORITY) : null;
        if (!TextUtils.isEmpty(targetMethod) && !TextUtils.equals(targetMethod, method)) {
            ContentProviderClient client = getContentProviderClient(targetAuthority);
            try {
                return client.call(targetMethod, arg, extras);
            } catch (RemoteException e) {
            }
        }
        return super.call(method, arg, extras);
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        String targetAuthority = uri.getQueryParameter(Env.EXTRA_TARGET_AUTHORITY);
        if (!TextUtils.isEmpty(targetAuthority) && !TextUtils.equals(targetAuthority, uri.getAuthority())) {
            ContentProviderClient client = getContentProviderClient(targetAuthority);
            try {
                return client.bulkInsert(buildNewUri(uri, targetAuthority), values);
            } catch (RemoteException e) {
            }
        }
        return super.bulkInsert(uri, values);
    }

}


















