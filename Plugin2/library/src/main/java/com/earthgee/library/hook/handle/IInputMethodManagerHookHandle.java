package com.earthgee.library.hook.handle;

import android.content.Context;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IInputMethodManagerHookHandle extends BaseHookHandle{
    public IInputMethodManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("startInput",new startInput(mHostContext));
        sHookedMethodHandlers.put("windowGainedFocus",new windowGainedFocus(mHostContext));
    }

    private class IInputMethodManagerHookedMethodHandler extends HookedMethodHandler {
        public IInputMethodManagerHookedMethodHandler(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    if (arg instanceof EditorInfo) {
                        EditorInfo info = ((EditorInfo) arg);
                        if (!TextUtils.equals(mHostContext.getPackageName(), info.packageName)) {
                            info.packageName = mHostContext.getPackageName();
                        }
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class startInput extends IInputMethodManagerHookedMethodHandler {
        public startInput(Context hostContext) {
            super(hostContext);
        }
    }

    private class windowGainedFocus extends IInputMethodManagerHookedMethodHandler {
        public windowGainedFocus(Context hostContext) {
            super(hostContext);
        }
    }

}
