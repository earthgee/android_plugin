package com.earthgee.library.hook.handle;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;

import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 许多binder hook其实只修复这个问题，就是把调用参数中的包名修改成host的
 */
public class ReplaceCallingPackageHookedMethodHandler extends HookedMethodHandler{

    public ReplaceCallingPackageHookedMethodHandler(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            if(args!=null&&args.length>0){
                for(int index=0;index<args.length;index++){
                    if(args[index]!=null&&(args[index] instanceof String)){
                        String str= (String) args[index];
                        if(isPackagePlugin(str)){
                            args[index]=mHostContext.getPackageName();
                        }
                    }
                }
            }
        }
        return super.beforeInvoke(receiver, method, args);
    }

    private static boolean isPackagePlugin(String packageName) throws RemoteException{
        return PluginManager.getInstance().isPluginPackage(packageName);
    }
}
