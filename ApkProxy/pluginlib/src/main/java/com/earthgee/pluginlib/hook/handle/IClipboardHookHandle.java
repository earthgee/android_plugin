package com.earthgee.pluginlib.hook.handle;

import android.content.Context;
import android.text.TextUtils;

import com.earthgee.pluginlib.hook.BaseHookHandle;
import com.earthgee.pluginlib.hook.HookedMethodHandler;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2019/12/9.
 */
public class IClipboardHookHandle extends BaseHookHandle {

    public IClipboardHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookMethodHandlers.put("setPrimaryClip",new setPrimaryClip(mHostContext));
    }

    private class MyBaseHookedMethodHandler extends HookedMethodHandler{

        public MyBaseHookedMethodHandler(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Exception {
            if(args!=null&&args.length>0&&args[args.length-1] instanceof String){
                String pkg= (String) args[args.length-1];
                if(!TextUtils.equals(pkg,mHostContext.getPackageName())){
                    args[args.length-1]=mHostContext.getPackageName();
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class setPrimaryClip extends MyBaseHookedMethodHandler{

        public setPrimaryClip(Context hostContext) {
            super(hostContext);
        }
    }

}
