package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 修复包名
 */
public class ILocationManagerHookHandle extends BaseHookHandle{

    public ILocationManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("requestLocationUpdates", new requestLocationUpdates(mHostContext));
        sHookedMethodHandlers.put("removeUpdates", new removeUpdates(mHostContext));
        sHookedMethodHandlers.put("requestGeofence", new requestGeofence(mHostContext));
        sHookedMethodHandlers.put("removeGeofence", new removeGeofence(mHostContext));
        sHookedMethodHandlers.put("getLastLocation", new getLastLocation(mHostContext));
        sHookedMethodHandlers.put("addGpsStatusListener", new addGpsStatusListener(mHostContext));
        sHookedMethodHandlers.put("removeGpsStatusListener", new removeGpsStatusListener(mHostContext));
        sHookedMethodHandlers.put("geocoderIsPresent", new geocoderIsPresent(mHostContext));
    }

    private static class BaseILocationManagerHookedMethodHandler extends ReplaceCallingPackageHookedMethodHandler {
        public BaseILocationManagerHookedMethodHandler(Context hostContext) {
            super(hostContext);
        }
    }

    private class requestLocationUpdates extends BaseILocationManagerHookedMethodHandler {
        public requestLocationUpdates(Context hostContext) {
            super(hostContext);
        }
    }

    private class removeUpdates extends BaseILocationManagerHookedMethodHandler {
        public removeUpdates(Context hostContext) {
            super(hostContext);
        }
    }

    private class requestGeofence extends BaseILocationManagerHookedMethodHandler {
        public requestGeofence(Context hostContext) {
            super(hostContext);
        }
    }

    private class removeGeofence extends BaseILocationManagerHookedMethodHandler {
        public removeGeofence(Context hostContext) {
            super(hostContext);
        }
    }

    private class getLastLocation extends BaseILocationManagerHookedMethodHandler {
        public getLastLocation(Context hostContext) {
            super(hostContext);
        }
    }

    private class addGpsStatusListener extends BaseILocationManagerHookedMethodHandler {
        public addGpsStatusListener(Context hostContext) {
            super(hostContext);
        }
    }

    private class removeGpsStatusListener extends BaseILocationManagerHookedMethodHandler {
        public removeGpsStatusListener(Context hostContext) {
            super(hostContext);
        }
    }

    private class geocoderIsPresent extends BaseILocationManagerHookedMethodHandler {
        public geocoderIsPresent(Context hostContext) {
            super(hostContext);
        }
    }
}
