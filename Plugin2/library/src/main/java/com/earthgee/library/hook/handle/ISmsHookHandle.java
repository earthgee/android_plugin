package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.util.ISmsCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ISmsHookHandle extends BaseHookHandle{
    public ISmsHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("getAllMessagesFromIccEfForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("updateMessageOnIccEfForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("copyMessageToIccEfForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendDataForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendDataForSubscriberWithSelfPermissions",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendTextForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendTextForSubscriberWithSelfPermissions",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("injectSmsPduForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendMultipartTextForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("enableCellBroadcastForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("disableCellBroadcastForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("enableCellBroadcastRangeForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("disableCellBroadcastRangeForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getPremiumSmsPermission",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getPremiumSmsPermissionForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setPremiumSmsPermission",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setPremiumSmsPermissionForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isImsSmsSupportedForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isSmsSimPickActivityNeeded",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getPreferredSmsSubscription",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getImsSmsFormatForSubscriber",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isSMSPromptEnabled",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendStoredText",new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendStoredMultipartText",new MyBaseHandler(mHostContext));

        addAllMethodFromHookedClass();
    }

    @Override
    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return ISmsCompat.Class();
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
