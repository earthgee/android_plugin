package com.earthgee.library.hook;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public abstract class BaseHookHandle {

    protected Context mHostContext;

    protected Map<String,HookedMethodHandler> sHookedMethodHandlers=
            new HashMap<>(5);



}
