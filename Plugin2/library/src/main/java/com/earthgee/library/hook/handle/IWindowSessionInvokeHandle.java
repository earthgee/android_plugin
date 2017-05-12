package com.earthgee.library.hook.handle;

import android.content.Context;
import android.text.TextUtils;
import android.view.WindowManager;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/5/12.
 */
public class IWindowSessionInvokeHandle extends BaseHookHandle{
    public IWindowSessionInvokeHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("add",new add(mHostContext));
        sHookedMethodHandlers.put("addToDisplay",new addToDisplay(mHostContext));
        sHookedMethodHandlers.put("addWithoutInputChannel",new addWithoutInputChannel(mHostContext));
        sHookedMethodHandlers.put("addToDisplayWithoutInputChannel",new addToDisplayWithoutInputChannel(mHostContext));
        sHookedMethodHandlers.put("relayout",new relayout(mHostContext));
    }

    private class IWindowSessionHookedMethodHandler extends HookedMethodHandler{

        public IWindowSessionHookedMethodHandler(Context hostContext) {
            super(hostContext);
        }

        int findWindowManagerLayoutParamsIndex(Object[] args){
            if(args!=null&&args.length>0){
                for(int i=0;i<args.length;i++){
                    if(args[i] instanceof WindowManager.LayoutParams){
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null&&args.length>0){
                int index=findWindowManagerLayoutParamsIndex(args);
                if(index>=0){
                    WindowManager.LayoutParams attr= (WindowManager.LayoutParams) args[index];
                    if(!TextUtils.equals(attr.packageName,mHostContext.getPackageName())){
                        attr.packageName=mHostContext.getPackageName();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class add extends IWindowSessionHookedMethodHandler{

        public add(Context hostContext) {
            super(hostContext);
        }
    }

    private class addToDisplay extends IWindowSessionHookedMethodHandler{

        public addToDisplay(Context hostContext) {
            super(hostContext);
        }
    }

    private class addWithoutInputChannel extends IWindowSessionHookedMethodHandler{

        public addWithoutInputChannel(Context hostContext) {
            super(hostContext);
        }
    }

    private class addToDisplayWithoutInputChannel extends IWindowSessionHookedMethodHandler{

        public addToDisplayWithoutInputChannel(Context hostContext) {
            super(hostContext);
        }
    }

    private class relayout extends IWindowSessionHookedMethodHandler{

        public relayout(Context hostContext) {
            super(hostContext);
        }
    }

}




















