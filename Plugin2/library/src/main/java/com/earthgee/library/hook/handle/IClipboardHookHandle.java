package com.earthgee.library.hook.handle;

import android.content.Context;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class IClipboardHookHandle extends BaseHookHandle{

    public IClipboardHookHandle(Context hostContext) {
        super(hostContext);
    }

    //17
//    void setPrimaryClip(in ClipData clip);
//    ClipData getPrimaryClip(String pkg);
//    ClipDescription getPrimaryClipDescription();
//    boolean hasPrimaryClip();
//    void addPrimaryClipChangedListener(in IOnPrimaryClipChangedListener listener);
//    void removePrimaryClipChangedListener(in IOnPrimaryClipChangedListener listener);
//    boolean hasClipboardText();

    //API 21,19,18
//    void setPrimaryClip(ClipData clip, String callingPackage);
//    ClipData getPrimaryClip(String pkg);
//    ClipDescription getPrimaryClipDescription(String callingPackage);
//    boolean hasPrimaryClip(String callingPackage);
//    void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage);
//    void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener);
//    boolean hasClipboardText(String callingPackage);

    @Override
    protected void init() {
        sHookedMethodHandlers.put("setPrimaryClip",new setPrimaryClip(mHostContext));
        sHookedMethodHandlers.put("getPrimaryClip",new getPrimaryClip(mHostContext));
        sHookedMethodHandlers.put("getPrimaryClipDescription",new getPrimaryClipDescription(mHostContext));
        sHookedMethodHandlers.put("hasPrimaryClip",new hasPrimaryClip(mHostContext));
        sHookedMethodHandlers.put("addPrimaryClipChangedListener",new addPrimaryClipChangedListener(mHostContext));
        sHookedMethodHandlers.put("removePrimaryClipChangedListener",new removePrimaryClipChangedListener(mHostContext));
        sHookedMethodHandlers.put("hasClipboardText",new hasClipboardText(mHostContext));
    }

    private class MyBaseHookedMethodHandler extends HookedMethodHandler{

        public MyBaseHookedMethodHandler(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(args!=null&&args.length>0&&args[args.length-1] instanceof String){
                String pkg= (String) args[args.length-1];
                if(!TextUtils.equals(pkg,mHostContext.getPackageName())){
                    args[args.length-1]=mHostContext.getPackageName();
                }
            }
            return super.beforeInvoke(receiver,method,args);
        }
    }

    private class setPrimaryClip extends MyBaseHookedMethodHandler{

        public setPrimaryClip(Context hostContext) {
            super(hostContext);
        }

    }

    private class getPrimaryClip extends MyBaseHookedMethodHandler{

        public getPrimaryClip(Context hostContext) {
            super(hostContext);
        }
    }

    private class getPrimaryClipDescription extends MyBaseHookedMethodHandler{

        public getPrimaryClipDescription(Context hostContext) {
            super(hostContext);
        }
    }

    private class hasPrimaryClip extends MyBaseHookedMethodHandler{

        public hasPrimaryClip(Context hostContext) {
            super(hostContext);
        }
    }

    private class addPrimaryClipChangedListener extends MyBaseHookedMethodHandler{

        public addPrimaryClipChangedListener(Context hostContext) {
            super(hostContext);
        }
    }

    private class removePrimaryClipChangedListener extends MyBaseHookedMethodHandler{

        public removePrimaryClipChangedListener(Context hostContext) {
            super(hostContext);
        }
    }

    private class hasClipboardText extends MyBaseHookedMethodHandler{

        public hasClipboardText(Context hostContext) {
            super(hostContext);
        }
    }


}


































