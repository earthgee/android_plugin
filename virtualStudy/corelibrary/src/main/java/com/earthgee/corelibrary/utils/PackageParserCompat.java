package com.earthgee.corelibrary.utils;

import android.content.Context;
import android.content.pm.PackageParser;
import android.os.Build;

import java.io.File;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class PackageParserCompat {

    public static final PackageParser.Package parsePackage(final Context context
            , final File apk,final int flags) throws PackageParser.PackageParserException{
        if(Build.VERSION.SDK_INT>=24){
            return PackageParserV24.parsePackage(context,apk,flags);
        }else if(Build.VERSION.SDK_INT>=21){
            return PackageParserLollipop.parsePackage(context,apk,flags);
        }else{
            return PackageParserLegacy.parsePackage(context,apk,flags);
        }
    }

    private static final class PackageParserV24{

        static final PackageParser.Package parsePackage(Context context,File apk,int flags) throws PackageParser.PackageParserException{
            PackageParser parser=new PackageParser();
            PackageParser.Package pkg=parser.parsePackage(apk,flags);
            ReflectUtil.invokeNoException(PackageParser.class,null,"collectCertificates",
                    new Class[]{PackageParser.Package.class,int.class},pkg,flags);
            return pkg;
        }

    }

    private static final class PackageParserLollipop{

        static final PackageParser.Package parsePackage(final Context context,final File apk,final int flags) throws PackageParser.PackageParserException{
            PackageParser parser=new PackageParser();
            PackageParser.Package pkg=parser.parsePackage(apk,flags);
            try{
                parser.collectCertificates(pkg,flags);
            }catch (Throwable e){
            }
            return pkg;
        }

    }

    private static final class PackageParserLegacy{

        static final PackageParser.Package parsePackage(final Context context,final File apk,int flags){
            PackageParser parser=new PackageParser(apk.getAbsolutePath());
            PackageParser.Package pkg=parser.parsePackage(apk,apk.getAbsolutePath(),
                    context.getResources().getDisplayMetrics(),flags);
            ReflectUtil.invokeNoException(PackageParser.class,parser,"collectCertificates",
                    new Class[]{PackageParser.Package.class,int.class},pkg,flags);
            return pkg;
        }

    }

}



















