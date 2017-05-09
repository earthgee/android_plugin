package com.earthgee.library.hook.proxy;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.LibCoreHookHandle;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.util.MyProxy;

import java.util.ArrayList;

/**
 * Created by zhaoruixuan on 2017/5/9.
 */
public class LibCoreHook extends ProxyHook{
    public LibCoreHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public BaseHookHandle createHookHandle() {
        return new LibCoreHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        if(!installHook1()){
            installHook2();
        }
    }

    private Class<?>[] getAllInterfaces(Class clz){
        ArrayList<Class> re=new ArrayList<>();
        Class<?>[] ifss=clz.getInterfaces();
        for (Class<?> ifs:ifss){
            if(!re.contains(ifs)){
                re.add(ifs);
            }
        }

        Class superClass=clz.getSuperclass();
        while (superClass!=null){
            Class<?>[] sifss=superClass.getInterfaces();
            for(Class<?> ifs:sifss){
                if(!re.contains(ifs)){
                    re.add(ifs);
                }
            }
            superClass=superClass.getSuperclass();
        }
        return re.toArray(new Class[re.size()]);
    }

    private boolean installHook1(){
        try{
            Class LibCore=Class.forName("libcore.io.Libcore");
            Object LibCoreOs= FieldUtils.readStaticField(LibCore,"os");
            Object Posix=FieldUtils.readField(LibCoreOs,"os",true);
            setOldObj(Posix);
            Class<?> aClass=mOldObj.getClass();
            Class<?>[] interfaces=getAllInterfaces(aClass);
            Object proxyObj= MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(),interfaces,this);
            FieldUtils.writeField(LibCoreOs,"os",proxyObj,true);
            return true;
        }catch (Throwable e){
        }
        return false;
    }

    private void installHook2() throws Exception{
        Class LibCore=Class.forName("libcore.io.Libcore");
        Object oldObj=FieldUtils.readStaticField(LibCore,"os");
        setOldObj(oldObj);
        Class<?> aClass=mOldObj.getClass();
        Class<?>[] interfaces=getAllInterfaces(aClass);
        Object proxyObj=MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(),interfaces,this);
        FieldUtils.writeStaticField(LibCore,"os",proxyObj);
    }

}




















