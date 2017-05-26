package com.earthgee.libaray.hook.handle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.RemoteException;

import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.HookedMethodHandler;
import com.earthgee.libaray.pm.PluginManager;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class IActivityManagerHookHandle extends BaseHookHandle{
    public IActivityManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("startActivity",new startActivity(mHostContext));
    }

    //todo replace pacakge name
    private static class startActivity extends HookedMethodHandler{

        public startActivity(Context hostContext) {
            super(hostContext);
        }

        protected boolean doReplaceIntentForStartActivityAPILow(Object[] args) throws RemoteException{
            int intentOfArgIndex = findFirstIntentIndexInArgs(args);
            if(args!=null&&args.length>1&&intentOfArgIndex>=0){
                Intent intent= (Intent) args[intentOfArgIndex];
                ActivityInfo activityInfo=resolveActivity(intent);
            }
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            //todo
            boolean bRet=true;
            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.JELLY_BEAN_MR2){
                bRet=doReplaceIntentForStartActivityAPILow(args);
            }else{
                bRet=doReplaceIntentForStartActivityAPIHigh(args);
            }
            if(!bRet){
                setFakedResult(Activity.RESULT_CANCELED);
                return true;
            }
            return super.beforeInvoke(receiver, method, args);
        }

    }

    private static ActivityInfo resolveActivity(Intent intent) throws RemoteException{
        return PluginManager.getInstance().resolveActivityInfo(intent,0);
    }

    private static int findFirstIntentIndexInArgs(Object[] args) {
        if (args != null && args.length > 0) {
            int i = 0;
            for (Object arg : args) {
                if (arg != null && arg instanceof Intent) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

}


















