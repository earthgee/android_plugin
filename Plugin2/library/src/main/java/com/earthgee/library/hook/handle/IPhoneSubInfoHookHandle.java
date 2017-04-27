package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.util.IPhoneSubInfoCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IPhoneSubInfoHookHandle extends BaseHookHandle{
    public IPhoneSubInfoHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("getDeviceId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getNaiForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDeviceIdForPhone", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getImeiForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDeviceSvn", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDeviceSvnUsingSubId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getSubscriberId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getSubscriberIdForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getGroupIdLevel1", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getGroupIdLevel1ForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIccSerialNumber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIccSerialNumberForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLine1Number", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLine1NumberForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLine1AlphaTag", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLine1AlphaTagForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getMsisdn", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getMsisdnForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getVoiceMailNumber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getVoiceMailNumberForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCompleteVoiceMailNumber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCompleteVoiceMailNumberForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getVoiceMailAlphaTag", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getVoiceMailAlphaTagForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIsimImpi", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIsimDomain", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIsimImpu", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIsimIst", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIsimPcscf", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIsimChallengeResponse", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getIccSimChallengeResponse", new MyBaseHandler(mHostContext));

        addAllMethodFromHookedClass();
    }

    @Override
    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return IPhoneSubInfoCompat.Class();
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
