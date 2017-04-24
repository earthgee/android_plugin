package com.earthgee.library.pm.parser;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.earthgee.library.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class PackageParserApi22Preview1 extends PackageParserApi21{

    PackageParserApi22Preview1(Context context) throws Exception {
        super(context);
    }

    @Override
    public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermission) throws Exception {
        try{
            return super.generatePackageInfo(gids, flags, firstInstallTime, lastUpdateTime, grantedPermission);
        }catch (Exception e){
        }

        Method method= MethodUtils.getAccessibleMethod(sPackageParserClass,"generatePackageInfo",
                mPackage.getClass(),int[].class,int.class,long.class,long.class,
                sArraySetClass,sPackageUserStateClass,int.class);

        Object grantedPermissionsArray=null;
        try{
            Constructor constructor=sArraySetClass.getConstructor(Collection.class);
            grantedPermissionsArray=constructor.newInstance(constructor,grantedPermission);
        }catch (Exception e){
        }
        if(grantedPermissionsArray==null){
            grantedPermissionsArray=grantedPermission;
        }
        return (PackageInfo) method.invoke(null,mPackage,gids,flags,firstInstallTime,lastUpdateTime,grantedPermissionsArray,
                mDefaultPackageUserState,mUserId);
    }

}
