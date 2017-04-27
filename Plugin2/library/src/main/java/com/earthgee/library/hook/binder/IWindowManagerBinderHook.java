package com.earthgee.library.hook.binder;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IWindowManagerHookHandle;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.util.IWindowManagerCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IWindowManagerBinderHook extends BinderHook{

    private static final String SERVICE_NAME="window";

    public IWindowManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IWindowManagerCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IWindowManagerHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        super.onInstall(classLoader);
        try {
            Class clazz=Class.forName("com.android.internal.policy.PhoneWindow$WindowManagerHolder");
            FieldUtils.writeStaticField(clazz,"sWindowManager",MyServiceManager.getProxiedObj(getServiceName()));
        }catch (Exception e){
        }
    }

    public static void fixWindowManagerHook(Activity activity){
        try {
            Object mWindow=FieldUtils.readField(activity,"mWindow");
            Class clazz=mWindow.getClass();
            Class WindowManagerHolder=Class.forName(clazz.getName()+"$WindowManagerHolder");
            Object obj=FieldUtils.readStaticField(WindowManagerHolder,"sWindowManager");
            Object proxiedObj=MyServiceManager.getProxiedObj(SERVICE_NAME);
            if(obj==proxiedObj){
                return;
            }
            FieldUtils.writeStaticField(WindowManagerHolder,"sWindowManager",proxiedObj);
        }catch (Exception e){
        }
    }

}











