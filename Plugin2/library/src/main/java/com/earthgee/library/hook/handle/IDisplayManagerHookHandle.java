package com.earthgee.library.hook.handle;

import android.content.Context;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/5/9.
 */
public class IDisplayManagerHookHandle extends BaseHookHandle{
    public IDisplayManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("createVirtualDisplay",new createVirtualDisplay(mHostContext));
    }

    private static class createVirtualDisplay extends HookedMethodHandler{

        public createVirtualDisplay(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            final int pkgIndex=2;
            if(args!=null&&args.length>0&&args[pkgIndex] instanceof String){
                String pkg= (String) args[pkgIndex];
                if(!TextUtils.equals(pkg,mHostContext.getPackageName())){
                    args[pkgIndex]=mHostContext.getPackageName();
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }
}



















