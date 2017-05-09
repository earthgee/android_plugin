package com.earthgee.library.core;

import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * Created by zhaoruixuan on 2017/5/9.
 */
public class PluginClassLoader extends DexClassLoader{

    public PluginClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    private static final List<String> sPreLoader=new ArrayList<>();

    //todo
}























