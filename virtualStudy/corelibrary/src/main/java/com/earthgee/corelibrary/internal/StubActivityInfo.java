package com.earthgee.corelibrary.internal;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.util.HashMap;

/**
 * Created by zhaoruixuan on 2017/7/27.
 */
public class StubActivityInfo {

    public static final int MAX_COUNT_STANDARD=1;
    public static final int MAX_COUNT_SINGLETOP=8;
    public static final int MAX_COUNT_SINGLETASK=8;
    public static final int MAX_COUNT_SINGLEINSTANCE=8;

    public static final String corePackage="com.earthgee.corelibrary";
    public static final String STUB_ACTIVITY_STANDARD="%s.A$%d";
    public static final String STUB_ACTIVITY_SINGLETOP="%s.B$%d";
    public static final String STUB_ACTIVITY_SINGLETASK="%s.C$%d";
    public static final String STUB_ACTIVITY_SINGLEINSTANCE="%s.D$%d";

    public final int usedStandardStubActivity=1;
    public int usedSingleTopStubActivity=0;
    public int usedSingleTaskStubActivity=0;
    public int usedSingleInstanceStubActivity=0;

    private HashMap<String,String> mCachedStubActivity=new HashMap<>();

    public String getStubActivity(String className, int launchMode, Resources.Theme theme){
        String stubActivity=mCachedStubActivity.get(className);
        if(stubActivity!=null){
            return stubActivity;
        }

        TypedArray array=theme.obtainStyledAttributes(new int[]{
                android.R.attr.windowIsTranslucent,
                android.R.attr.windowBackground
        });
        boolean windowIsTranslucent=array.getBoolean(0,false);
        array.recycle();
        stubActivity=String.format(STUB_ACTIVITY_STANDARD,corePackage,usedStandardStubActivity);
        switch (launchMode){
            case ActivityInfo.LAUNCH_MULTIPLE:{
                stubActivity=String.format(STUB_ACTIVITY_STANDARD,corePackage,usedStandardStubActivity);
                if(windowIsTranslucent){
                    stubActivity=String.format(STUB_ACTIVITY_STANDARD,corePackage,2);
                }
                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_TOP:{
                usedSingleTopStubActivity=usedSingleTopStubActivity%MAX_COUNT_SINGLETOP+1;
                stubActivity=String.format(STUB_ACTIVITY_SINGLETOP,corePackage,usedSingleTopStubActivity);
                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_TASK:{
                usedSingleTaskStubActivity=usedSingleTaskStubActivity%MAX_COUNT_SINGLETASK+1;
                stubActivity=String.format(STUB_ACTIVITY_SINGLETASK,corePackage,usedSingleTaskStubActivity);
                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE:{
                usedSingleInstanceStubActivity=usedSingleInstanceStubActivity%MAX_COUNT_SINGLEINSTANCE+1;
                stubActivity=String.format(STUB_ACTIVITY_SINGLEINSTANCE,corePackage,usedSingleInstanceStubActivity);
                break;
            }
            default:break;
        }

        mCachedStubActivity.put(className,stubActivity);
        return stubActivity;
    }

}










































