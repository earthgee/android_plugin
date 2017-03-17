package com.earthgee.library;

import android.content.Intent;
import android.os.Parcelable;

import com.earthgee.library.utils.Config;

import java.io.Serializable;

/**
 * Created by zhaoruixuan on 2017/3/17.
 */
public class PluginIntent extends Intent{

    private String mPluginPackage;
    private String mPluginClass;

    public PluginIntent(){
        super();
    }

    public PluginIntent(String pluginPackage){
        super();
        this.mPluginPackage=pluginPackage;
    }

    public PluginIntent(String pluginPackage,String pluginClass){
        super();
        this.mPluginPackage=pluginPackage;
        this.mPluginClass=pluginClass;
    }

    public PluginIntent(String pluginPackage,Class<?> clazz){
        super();
        this.mPluginPackage=pluginPackage;
        this.mPluginClass=clazz.getName();
    }

    public String getPluginPackage(){
        return mPluginPackage;
    }

    public void setPluginPackage(){
        this.mPluginPackage=mPluginPackage;
    }

    public String getPluginClass(){
        return mPluginClass;
    }

    public void setPluginClass(String pluginClass){
        this.mPluginClass=pluginClass;
    }

    public void setPluginClass(Class<?> clazz){
        this.mPluginClass=clazz.getName();
    }

    @Override
    public Intent putExtra(String name, Parcelable value){
        setupExtraClassLoader(value);
        return super.putExtra(name,value);
    }

    @Override
    public Intent putExtra(String name, Serializable value){
        setupExtraClassLoader(value);
        return super.putExtra(name,value);
    }

    private void setupExtraClassLoader(Object value){
        ClassLoader pluginLoader=value.getClass().getClassLoader();
        Config.sPluginClassLoader=pluginLoader;
        setExtrasClassLoader(pluginLoader);
    }

}















