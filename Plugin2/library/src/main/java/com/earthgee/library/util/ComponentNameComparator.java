package com.earthgee.library.util;

import android.content.ComponentName;
import android.text.TextUtils;

import java.util.Comparator;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class ComponentNameComparator implements Comparator<ComponentName>{

    @Override
    public int compare(ComponentName lhs, ComponentName rhs) {
        if(lhs==null&&rhs==null){
            return 0;
        }else if(lhs!=null&&rhs==null){
            return 1;
        }else if(lhs==null&&rhs!=null){
            return -1;
        }else{
            if(TextUtils.equals(lhs.getPackageName(),rhs.getPackageName())){
                return 0;
            }else{
                return lhs.compareTo(rhs);
            }
        }
    }

}
