package com.earthgee.corelibrary.delegate;

import android.content.Context;
import android.content.IContentProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/8/9.
 */
public class IContentProviderProxy implements InvocationHandler{

    private IContentProvider mBase;
    private Context mContext;

    private IContentProviderProxy(Context context, IContentProvider iContentProvider){
        mBase=iContentProvider;
        mContext=context;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }


}
