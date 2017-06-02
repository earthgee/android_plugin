package com.earthgee.libaray.core;

import dalvik.system.DexClassLoader;

/**
 * Created by zhaoruixuan on 2017/6/2.
 */
public class PluginClassLoader extends DexClassLoader{

    public PluginClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }



}
