package com.earthgee.library.hook.binder;

import android.content.Context;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.reflect.Utils;
import com.earthgee.library.util.MyProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/11.
 * binder hook流程:
 * 1.通过ServiceManager的hook拦截了queryLocalInterface方法
 * 2.getOldObj方法获取正常情况下的binder代理对象
 * 3.对这个对象进行动态代理 并在被hook的queryLocalInterface方法执行时返回给系统
 * 这样就完成了binder代理对象的hook
 */
abstract class BinderHook extends Hook implements InvocationHandler{

    private Object mOldObj;

    protected BinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        new ServiceManagerCacheBinderHook(mHostContext,getServiceName()).onInstall(classLoader);
        mOldObj=getOldObj();
        Class<?> clazz=mOldObj.getClass();
        List<Class<?>> interfaces= Utils.getAllInterfaces(clazz);
        Class[] ifs=interfaces!=null&&interfaces.size()>0?
                interfaces.toArray(new Class[interfaces.size()]):new Class[0];
        Object proxiedObj= MyProxy.newProxyInstance(clazz.getClassLoader(),ifs,this);
        MyServiceManager.addProxiedObj(getServiceName(),proxiedObj);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try{
            if(!isEnable()){
                return method.invoke(mOldObj,args);
            }
            HookedMethodHandler hookedMethodHandler=mHookHandles.getHookedMethodHandler(method);
            if(hookedMethodHandler!=null){
                return hookedMethodHandler.doHookInner(mOldObj,method,args);
            }else{
                return method.invoke(mOldObj,args);
            }
        }catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause != null && MyProxy.isMethodDeclaredThrowable(method, cause)) {
                throw cause;
            } else if (cause != null) {
                RuntimeException runtimeException = !TextUtils.isEmpty(cause.getMessage()) ? new RuntimeException(cause.getMessage()) : new RuntimeException();
                runtimeException.initCause(cause);
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

    //要hook的service的名字，Context类的一个String常量
    public abstract String getServiceName();

    abstract Object getOldObj() throws Exception;

    void setOldObj(Object mOldObj){
        this.mOldObj=mOldObj;
    }

}






















