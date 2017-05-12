package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.util.ITelephonyCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 替换包名
 */
public class ITelephonyHookHandle extends BaseHookHandle{
    public ITelephonyHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("dial", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("call", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("endCall", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("endCallForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("answerRingingCall", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("answerRingingCallForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("silenceRinger", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isOffhook", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isOffhookForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isRingingForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isRinging", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isIdle", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isIdleForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isRadioOn", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isRadioOnForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isSimPinEnabled", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPin", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPinForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPuk", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPukForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPinReportResult", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPinReportResultForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPukReportResult", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("supplyPukReportResultForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("handlePinMmi", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("handlePinMmiForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("toggleRadioOnOff", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("toggleRadioOnOffForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setRadio", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setRadioForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setRadioPower", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("updateServiceLocation", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("updateServiceLocationForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("enableLocationUpdates", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("enableLocationUpdatesForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("disableLocationUpdates", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("disableLocationUpdatesForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("enableDataConnectivity", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("disableDataConnectivity", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isDataConnectivityPossible", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCellLocation", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getNeighboringCellInfo", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCallState", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCallStateForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDataActivity", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDataState", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActivePhoneType", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getActivePhoneTypeForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaEriIconIndex", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaEriIconIndexForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaEriIconMode", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaEriIconModeForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaEriText", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaEriTextForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("needsOtaServiceProvisioning", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setVoiceMailNumber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getVoiceMessageCount", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getVoiceMessageCountForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getNetworkType", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getNetworkTypeForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDataNetworkType", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDataNetworkTypeForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getVoiceNetworkTypeForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("hasIccCard", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("hasIccCardUsingSlotId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLteOnCdmaMode", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLteOnCdmaModeForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getAllCellInfo", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setCellInfoListRate", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDefaultSim", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("IccOpenLogicalChannelResponse", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("iccOpenLogicalChannel", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("iccCloseLogicalChannel", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("iccTransmitApduLogicalChannel", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("iccTransmitApduBasicChannel", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("iccExchangeSimIO", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendEnvelopeWithStatus", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("nvReadItem", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("nvWriteItem", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("nvWriteCdmaPrl", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("nvResetConfig", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCalculatedPreferredNetworkType", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getPreferredNetworkType", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getTetherApnRequired", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setNetworkSelectionModeAutomatic", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCellNetworkScanResults", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setNetworkSelectionModeManual", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setPreferredNetworkType", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setDataEnabled", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDataEnabled", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getPcscfAddress", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setImsRegistrationState", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaMdn", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCdmaMin", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCarrierPrivilegeStatus", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("checkCarrierPrivilegesForPackage", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("checkCarrierPrivilegesForPackageAnyPhone", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCarrierPackageNamesForIntentAndPhone", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setLine1NumberForDisplayForSubscriber", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLine1NumberForDisplay", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLine1AlphaTagForDisplay", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getMergedSubscriberIds", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setOperatorBrandOverride", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setRoamingOverride", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("invokeOemRilRequestRaw", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("needMobileRadioShutdown", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("shutdownMobileRadios", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setRadioCapability", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getRadioAccessFamily", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("enableVideoCalling", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isVideoCallingEnabled", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("canChangeDtmfToneLength", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isWorldPhone", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isTtyModeSupported", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isHearingAidCompatibilitySupported", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isImsRegistered", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isWifiCallingAvailable", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isVolteAvailable", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("isVideoTelephonyAvailable", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getDeviceId", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getSubIdForPhoneAccount", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("factoryReset", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getLocaleFromDefaultSim", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getModemActivityInfo", new MyBaseHandler(mHostContext));

        addAllMethodFromHookedClass();
    }

    @Override
    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return ITelephonyCompat.Class();
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
