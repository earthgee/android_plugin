package com.earthgee.library.util;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.SparseArray;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by zhaoruixuan on 2017/5/3.
 * TypedArray 缓存
 */
public class AttributeCache {

    private static AttributeCache sInstance=null;

    private final Context mContext;
    private final WeakHashMap<String,Package> mPackages=new WeakHashMap<>();
    private final Configuration mConfiguration=new Configuration();

    public final static class Package{
        private final Context context;
        private final SparseArray<HashMap<int[],Entry>> mMap=new SparseArray<>();

        public Package(Context c){
            context=c;
        }
    }

    public AttributeCache(Context context){
        mContext=context;
    }

    public static void init(Context context){
        if(sInstance==null){
            sInstance=new AttributeCache(context);
        }
    }

    public static AttributeCache instance(){
        return sInstance;
    }

    public final static class Entry{
        public final Context context;
        public final TypedArray array;

        public Entry(Context c,TypedArray ta){
            context=c;
            array=ta;
        }
    }

    public void removePackage(String packageName){
        synchronized (this){
            mPackages.remove(packageName);
        }
    }

    public void updateConfiguration(Configuration config){
        synchronized (this){
            int changes=mConfiguration.updateFrom(config);
            if((changes&~(ActivityInfo.CONFIG_FONT_SCALE|
                    ActivityInfo.CONFIG_KEYBOARD_HIDDEN|
                    ActivityInfo.CONFIG_ORIENTATION))!=0){
                mPackages.clear();
            }
        }
    }

    public Entry get(String packageName,int resId,int[] styleable){
        synchronized (this){
            Package pkg=mPackages.get(packageName);
            HashMap<int[],Entry> map=null;
            Entry ent=null;
            if(pkg!=null){
                map=pkg.mMap.get(resId);
                if(map!=null){
                    ent=map.get(styleable);
                    if(ent!=null){
                        return ent;
                    }
                }
            }else{
                Context context;
                try{
                    context=mContext.createPackageContext(packageName,0);
                    if(context==null){
                        return null;
                    }
                }catch (PackageManager.NameNotFoundException e){
                    return null;
                }
                pkg=new Package(context);
                mPackages.put(packageName,pkg);
            }

            if(map==null){
                map=new HashMap<>();
                pkg.mMap.put(resId,map);
            }

            try{
                ent=new Entry(pkg.context,pkg.context.obtainStyledAttributes(resId,styleable));
                map.put(styleable,ent);
            }catch (Resources.NotFoundException e){
                return null;
            }

            return ent;
        }
    }

}


























