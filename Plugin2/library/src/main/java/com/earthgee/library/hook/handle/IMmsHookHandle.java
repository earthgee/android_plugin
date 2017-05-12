package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.util.IMmsCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 * 替换包名
 */
public class IMmsHookHandle extends BaseHookHandle{
    public IMmsHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("sendMessage", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("downloadMessage", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getCarrierConfigValues", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("importTextMessage", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("importMultimediaMessage", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("deleteStoredMessage", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("deleteStoredConversation", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("updateStoredMessageStatus", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("archiveStoredConversation", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("addTextMessageDraft", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("addMultimediaMessageDraft", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("sendStoredMessage", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("setAutoPersisting", new MyBaseHandler(mHostContext));
        sHookedMethodHandlers.put("getAutoPersisting", new MyBaseHandler(mHostContext));

        addAllMethodFromHookedClass();
    }

    @Override
    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return IMmsCompat.Class();
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
