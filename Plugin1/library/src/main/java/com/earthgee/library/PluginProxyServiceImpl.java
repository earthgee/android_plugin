package com.earthgee.library;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import com.earthgee.library.utils.Config;
import com.earthgee.library.utils.Constants;

/**
 * Created by zhaoruixuan on 2017/3/22.
 */
public class PluginProxyServiceImpl {

    private Service mProxyService;
    private PluginServiceInterface mRemoteService;

    public PluginProxyServiceImpl(Service service){
        mProxyService=service;
    }

    public void init(Intent intent){
        intent.setExtrasClassLoader(Config.sPluginClassLoader);

        String packageName=intent.getStringExtra(Constants.EXTRA_PACKAGE);
        String clazz=intent.getStringExtra(Constants.EXTRA_CLASS);

        PluginManager pluginManager=PluginManager.getInstance(mProxyService);
        PluginPackage pluginPackage=pluginManager.getPackage(packageName);

        try {
            Class<?> localClass=pluginPackage.classLoader.loadClass(clazz);
            Object instance=localClass.newInstance();
            mRemoteService= (PluginServiceInterface) instance;
            ((PluginServiceAttachable)mProxyService).attach(mRemoteService);
            mRemoteService.attach(mProxyService,pluginPackage);

            mRemoteService.onCreate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
