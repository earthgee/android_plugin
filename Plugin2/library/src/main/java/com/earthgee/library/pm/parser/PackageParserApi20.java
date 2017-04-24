package com.earthgee.library.pm.parser;

import android.content.Context;
import android.util.DisplayMetrics;

import com.earthgee.library.reflect.MethodUtils;

import java.io.File;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class PackageParserApi20 extends PackageParserApi21{

    PackageParserApi20(Context context) throws Exception {
        super(context);
    }

    @Override
    public void parsePackage(File file, int flags) throws Exception {
        DisplayMetrics metrics=new DisplayMetrics();
        metrics.setToDefaults();
        String destCodePath=file.getPath();
        mPackageParser= MethodUtils.invokeConstructor(sPackageParserClass,destCodePath);
        mPackage=MethodUtils.invokeMethod(mPackageParser,"parsePackage",file,
                destCodePath,metrics,flags);
    }
}
