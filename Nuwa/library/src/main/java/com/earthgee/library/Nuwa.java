package com.earthgee.library;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhaoruixuan on 2017/6/29.
 */
public class Nuwa {

    private static final String DEX_DIR="nuwa";
    private static final String HACK_DEX="hack.apk";

    private static final String DEX_OPT_DIR="nuwaopt";

    public static void init(Context context){
        File dexDir=new File(context.getFilesDir(),DEX_DIR);
        dexDir.mkdir();

        String dexPath=null;
        try{
            dexPath=AssetUtils.copyAsset(context,HACK_DEX,dexDir);
        }catch (IOException e){
        }

        loadPatch(context,dexPath);
    }

    public static void loadPatch(Context context,String dexPath){
        if(context==null){
            return;
        }

        if(!new File(dexPath).exists()){
            return;
        }

        File dexOptDir=new File(context.getFilesDir(),DEX_OPT_DIR);
        dexOptDir.mkdir();
        try{
            DexUtils.injectDexAtFirst(dexPath,dexOptDir.getAbsolutePath());
        }catch (Exception e){
        }
    }

}















