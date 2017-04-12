package com.earthgee.library.hook.handle;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/12.
 */
public class ISearchManagerHookHandle extends BaseHookHandle{

    public ISearchManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("getSearchableInfo",new getSearchInfo(mHostContext));
    }

    private class getSearchInfo extends HookedMethodHandler{

        public getSearchInfo(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null&&args.length>0&&args[args.length-1] instanceof ComponentName){
                ComponentName cpn= (ComponentName) args[args.length-1];
                //todo
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

}
