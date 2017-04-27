package com.earthgee.library.hook.handle;

import android.content.Context;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IMediaRouterServiceHookHandle extends BaseHookHandle{
    public IMediaRouterServiceHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("registerClientAsUser",new registerClientAsUser(mHostContext));
    }

    private class registerClientAsUser extends HookedMethodHandler{

        public registerClientAsUser(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            int index=1;
            if(args!=null&&args.length>index&&args[index] instanceof String){
                String pkg= (String) args[index];
                if(!TextUtils.equals(pkg,mHostContext.getPackageName())){
                    args[index]=mHostContext.getPackageName();
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }
}
