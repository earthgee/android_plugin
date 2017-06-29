package com.earthgee.library;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhaoruixuan on 2017/6/29.
 */
public class Nuwa {

    private static final String DEX_DIR="nuwa";

    public static void init(Context context){
        File dexDir=new File(context.getFilesDir(),DEX_DIR);
        dexDir.mkdir();

        String dexPath=null;
        try{

        }catch (IOException e){
            
        }
    }

}
