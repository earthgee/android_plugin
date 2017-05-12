package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.util.ITelephonyRegistryCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 替换包名
 */
public class ITelephonyRegistryHookHandle extends BaseHookHandle {
    public ITelephonyRegistryHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("addOnSubscriptionsChangedListener", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("removeOnSubscriptionsChangedListener", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("listen", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("listenForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCallState", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCallStateForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyServiceStateForPhoneId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifySignalStrength", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifySignalStrengthForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyMessageWaitingChangedForPhoneId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCallForwardingChanged", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCallForwardingChangedForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDataActivity", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDataActivityForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDataConnection", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDataConnectionForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDataConnectionFailed", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDataConnectionFailedForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCellLocation", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCellLocationForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyOtaspChanged", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCellInfo", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyPreciseCallState", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDisconnectCause", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyPreciseDataConnectionFailed", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCellInfoForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyDataConnectionRealTimeInfo", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyVoLteServiceStateChanged", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyOemHookRawEventForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifySubscriptionInfoChanged", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("notifyCarrierNetworkChange", new MyBaseHandler(mHostContext));
        addAllMethodFromHookedClass();
    }

    @Override
    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return ITelephonyRegistryCompat.Class();
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
