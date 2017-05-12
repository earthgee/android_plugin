package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.hook.proxy.IWindowSessionHook;
import com.earthgee.library.reflect.Utils;
import com.earthgee.library.util.MyProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 对openSession返回的IWindowSession对象进行hook
 */
public class IWindowManagerHookHandle extends BaseHookHandle{
    public IWindowManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("openSession",new openSession(mHostContext));
        sHookedMethodHandlers.put("overridePendingAppTransition",
                new overridePendingAppTransition(mHostContext));
        sHookedMethodHandlers.put("setAppStartingWindow",new setAppStartingWindow(mHostContext));
    }

    private class openSession extends HookedMethodHandler{

        public openSession(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            super.afterInvoke(receiver, method, args, invokeResult);
            Class clazz=invokeResult.getClass();
            IWindowSessionHook invocationHandler=new IWindowSessionHook(mHostContext,invokeResult);
            invocationHandler.setEnable(true);
            List<Class<?>> interfaces= Utils.getAllInterfaces(clazz);
            Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
            Object newProxy= MyProxy.newProxyInstance(clazz.getClassLoader(),ifs,invocationHandler);
            setFakeResult(newProxy);
        }
    }

    private class overridePendingAppTransition extends HookedMethodHandler{

        public overridePendingAppTransition(Context hostContext) {
            super(hostContext);
        }
    }

    private class setAppStartingWindow extends HookedMethodHandler{

        public setAppStartingWindow(Context hostContext) {
            super(hostContext);
        }
    }

}



















