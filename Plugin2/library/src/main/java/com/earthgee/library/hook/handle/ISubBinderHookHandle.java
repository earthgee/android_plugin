package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.util.ISubCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 替换包名
 */
public class ISubBinderHookHandle extends BaseHookHandle{


    public ISubBinderHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("getAllSubInfoList", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getAllSubInfoCount", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActiveSubscriptionInfo", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActiveSubscriptionInfoForIccId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActiveSubscriptionInfoForSimSlotIndex", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActiveSubscriptionInfoList", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActiveSubInfoCountMax", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("addSubInfoRecord", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setIconTint", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setDisplayName", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setDisplayNameUsingSrc", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setDisplayNumber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setDataRoaming", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getSlotId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDefaultSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("clearSubInfo", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getPhoneId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDefaultDataSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("etDefaultDataSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDefaultVoiceSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setDefaultVoiceSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDefaultSmsSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setDefaultSmsSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("clearDefaultsForInactiveSubIds", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActiveSubIdList", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setSubscriptionProperty", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getSubscriptionProperty", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getSimStateForSlotIdx", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isActiveSubId", new MyBaseHandler(mHostContext));
        addAllMethodFromHookedClass();
    }

    @Override
    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return ISubCompat.Class();
    }

    @Override
    protected HookedMethodHandler newBaseHandler() throws ClassNotFoundException {
        return new MyBaseHandler(mHostContext);
    }

    private static class MyBaseHandler extends ReplaceCallingPackageHookedMethodHandler {
        public MyBaseHandler(Context context) {
            super(context);
        }
    }
}
