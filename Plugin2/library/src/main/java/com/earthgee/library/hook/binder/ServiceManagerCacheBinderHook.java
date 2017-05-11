package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.Utils;
import com.earthgee.library.util.MyProxy;
import com.earthgee.library.util.ServiceManagerCompat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/11.
 * android.os.ServiceManager的sCache变量保存着所有的service引用对象
 * 先尝试从getService中获取IBinder对象，并将其添加到MyServiceManager的originService列表中（待后续使用）
 * 然后对获得的IBinder对象做动态代理，并hook queryLocalInterface方法
 * 这样就使得ContextImpl.getSystemService()获取的对象是hook过的
 */
public class ServiceManagerCacheBinderHook extends Hook implements InvocationHandler {

    private String mServiceName;

    protected ServiceManagerCacheBinderHook(Context hostContext, String servicename) {
        super(hostContext);
        mServiceName = servicename;
        setEnable(true);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ServiceManagerHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        Object sCacheObj = FieldUtils.readStaticField(ServiceManagerCompat.Class(), "sCache");
        if (sCacheObj instanceof Map) {
            Map sCache = (Map) sCacheObj;
            Object obj = sCache.get(mServiceName);
            sCache.remove(mServiceName);
            IBinder mServiceIBinder = ServiceManagerCompat.getService(mServiceName);
            if (mServiceIBinder == null) {
                if (obj != null && obj instanceof IBinder && !Proxy.isProxyClass(obj.getClass())) {
                    mServiceIBinder = (IBinder) obj;
                }
            }
            if (mServiceIBinder != null) {
                MyServiceManager.addOriginService(mServiceName, mServiceIBinder);
                Class clazz = mServiceIBinder.getClass();
                List<Class<?>> interfaces = Utils.getAllInterfaces(clazz);
                Class[] ifs = interfaces != null && interfaces.size() > 0 ?
                        interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
                IBinder mProxyServiceIBinder = (IBinder) MyProxy.newProxyInstance(clazz.getClassLoader(), ifs, this);
                sCache.put(mServiceName, mProxyServiceIBinder);
                MyServiceManager.addProxiedServiceCache(mServiceName, mProxyServiceIBinder);
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            IBinder originService = MyServiceManager.getOriginService(mServiceName);
            if (!isEnable()) {
                return method.invoke(originService, args);
            }
            HookedMethodHandler hookedMethodHandler = mHookHandles.getHookedMethodHandler(method);
            //如注册有对应的HookedMethodHandler,使用其的doHookInner方法
            if (hookedMethodHandler != null) {
                return hookedMethodHandler.doHookInner(originService, method, args);
            } else {
                return method.invoke(originService, args);
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause != null && MyProxy.isMethodDeclaredThrowable(method, cause)) {
                throw cause;
            } else if (cause != null) {
                RuntimeException runtimeException =
                        !TextUtils.isEmpty(cause.getMessage())
                                ? new RuntimeException(cause.getMessage()) : new RuntimeException();
                runtimeException.initCause(e);
                throw runtimeException;
            } else {
                RuntimeException runtimeException = !TextUtils.isEmpty(e.getMessage()) ? new RuntimeException(e.getMessage()) : new RuntimeException();
                runtimeException.initCause(e);
                throw runtimeException;
            }
        } catch (IllegalArgumentException e) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(" DROIDPLUGIN{");
                if (method != null) {
                    sb.append("method[").append(method.toString()).append("]");
                } else {
                    sb.append("method[").append("NULL").append("]");
                }
                if (args != null) {
                    sb.append("args[").append(Arrays.toString(args)).append("]");
                } else {
                    sb.append("args[").append("NULL").append("]");
                }
                sb.append("}");

                String message = e.getMessage() + sb.toString();
                throw new IllegalArgumentException(message, e);
            } catch (Throwable e1) {
                throw e;
            }
        } catch (Throwable e) {
            if (MyProxy.isMethodDeclaredThrowable(method, e)) {
                throw e;
            } else {
                RuntimeException runtimeException = !TextUtils.isEmpty(e.getMessage()) ? new RuntimeException(e.getMessage()) : new RuntimeException();
                runtimeException.initCause(e);
                throw runtimeException;
            }
        }
    }

    private class ServiceManagerHookHandle extends BaseHookHandle {
        public ServiceManagerHookHandle(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void init() {
            sHookedMethodHandlers.put("queryLocalInterface", new QueryLocalInterface(mHostContext));
        }

        class QueryLocalInterface extends HookedMethodHandler {

            public QueryLocalInterface(Context hostContext) {
                super(hostContext);
            }

            //从MyServiceManager中获取被hook过的binder代理对象并返回(跨进程binder处理，本地binder不进行hook)
            @Override
            protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
                Object localInterface = invokeResult;
                Object proxiedObj = MyServiceManager.getProxiedObj(mServiceName);
                if (localInterface == null && proxiedObj != null) {
                    setFakeResult(proxiedObj);
                }
            }
        }
    }

}























