package com.earthgee.library.hook.handle;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;

import com.earthgee.library.core.Env;
import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/26.
 */
public class IContentServiceHandle extends BaseHookHandle{

    public IContentServiceHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("registerContentObserver",
                new registerContentObserver(mHostContext));
        sHookedMethodHandlers.put("notifyChange",new notifyChange(mHostContext));
    }

    private static class IContentServiceHookedMethodHandler extends HookedMethodHandler{

        public IContentServiceHookedMethodHandler(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null){
                final int index=1;
                if(args.length>index&&args[index] instanceof Uri){
                    Uri uri= (Uri) args[index];
                    String authority=uri.getAuthority();
                    ProviderInfo provider= PluginManager.getInstance().resolveContentProvider(authority,0);
                    if(provider!=null){
                        ProviderInfo info=PluginManager.getInstance().selectStubProviderInfo(authority);
                        Uri.Builder newUri=new Uri.Builder();
                        newUri.scheme("content");
                        newUri.authority(uri.getAuthority());
                        newUri.path(uri.getPath());
                        newUri.query(uri.getQuery());
                        newUri.appendQueryParameter(Env.EXTRA_TARGET_AUTHORITY,authority);
                        args[index]=newUri.build();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private static class registerContentObserver extends IContentServiceHookedMethodHandler{

        public registerContentObserver(Context hostContext) {
            super(hostContext);
        }
    }

    private static class notifyChange extends IContentServiceHookedMethodHandler{

        public notifyChange(Context hostContext) {
            super(hostContext);
        }
    }

}





















