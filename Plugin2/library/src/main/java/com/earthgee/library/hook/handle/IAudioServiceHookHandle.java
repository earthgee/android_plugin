package com.earthgee.library.hook.handle;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/25.
 */
public class IAudioServiceHookHandle extends BaseHookHandle{

    public IAudioServiceHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("adjustVolume",new adjustVolume(mHostContext));
        sHookedMethodHandlers.put("adjustLocalOrRemoteStreamVolume",new adjustLocalOrRemoteStreamVolume(mHostContext));
        sHookedMethodHandlers.put("adjustSuggestedStreamVolume",new adjustSuggestedStreamVolume(mHostContext));
        sHookedMethodHandlers.put("adjustStreamVolume",new adjustStreamVolume(mHostContext));
        sHookedMethodHandlers.put("adjustMasterVolume",new adjustMasterVolume(mHostContext));
        sHookedMethodHandlers.put("setStreamVolume",new setStreamVolume(mHostContext));
        sHookedMethodHandlers.put("setMasterVolume",new setMasterVolume(mHostContext));
        sHookedMethodHandlers.put("requestAudioFocus",new requestAudioFocus(mHostContext));
        sHookedMethodHandlers.put("registerRemoteControlClient",new registerRemoteControlClient(mHostContext));
    }

    private static class MyBaseHandler extends HookedMethodHandler{
        public MyBaseHandler(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
                if(args!=null&&args.length>0){
                    for(int index=0;index<args.length;index++){
                        if(args[index] instanceof String){
                            String callingPkg= (String) args[index];
                            if(TextUtils.equals(callingPkg,mHostContext.getPackageName())&&
                                    PluginManager.getInstance().isPluginPackage(callingPkg)){
                                args[index]=mHostContext.getPackageName();
                            }
                        }
                    }
                }
            }

            return super.beforeInvoke(receiver, method, args);
        }
    }

    private static class adjustVolume extends MyBaseHandler{

        public adjustVolume(Context hostContext) {
            super(hostContext);
        }

    }

    private static class adjustLocalOrRemoteStreamVolume extends MyBaseHandler{

        public adjustLocalOrRemoteStreamVolume(Context hostContext) {
            super(hostContext);
        }
    }

    private static class adjustSuggestedStreamVolume extends MyBaseHandler{

        public adjustSuggestedStreamVolume(Context hostContext) {
            super(hostContext);
        }
    }

    private static class adjustStreamVolume extends MyBaseHandler{

        public adjustStreamVolume(Context hostContext) {
            super(hostContext);
        }
    }

    private static class adjustMasterVolume extends MyBaseHandler{

        public adjustMasterVolume(Context hostContext) {
            super(hostContext);
        }
    }

    private static class setStreamVolume extends MyBaseHandler{

        public setStreamVolume(Context hostContext) {
            super(hostContext);
        }
    }

    private static class setMasterVolume extends MyBaseHandler{

        public setMasterVolume(Context hostContext) {
            super(hostContext);
        }
    }

    private static class requestAudioFocus extends MyBaseHandler{

        public requestAudioFocus(Context hostContext) {
            super(hostContext);
        }
    }

    private static class registerRemoteControlClient extends MyBaseHandler{

        public registerRemoteControlClient(Context hostContext) {
            super(hostContext);
        }
    }

}






















