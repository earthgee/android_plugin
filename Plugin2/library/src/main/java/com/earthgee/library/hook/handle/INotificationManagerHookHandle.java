package com.earthgee.library.hook.handle;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/25.
 */
public class INotificationManagerHookHandle extends BaseHookHandle{
    public INotificationManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        init1();
        sHookedMethodHandlers.put("enqueueNotification",new enqueueNotification(mHostContext));
        sHookedMethodHandlers.put("cancelNotification",new cancelNotification(mHostContext));
        sHookedMethodHandlers.put("cancelAllNotification",new cancelAllNotifications(mHostContext));
        sHookedMethodHandlers.put("enqueueToast",new enqueueToast(mHostContext));
        sHookedMethodHandlers.put("cancelToast",new cancelToast(mHostContext));
        sHookedMethodHandlers.put("enqueueNotificationWithTag",new enqueueNotificationWithTag(mHostContext));
        sHookedMethodHandlers.put("enqueueNotificationWithTagPriority",new enqueueNotificationWithTagPriority(mHostContext));
        sHookedMethodHandlers.put("cancelNotificationWithTag",new cancelNotificationWithTag(mHostContext));
        sHookedMethodHandlers.put("setNotificationsEnabledForPackage",new setNotificationsEnabledForPackage(mHostContext));
        sHookedMethodHandlers.put("areNotificationsEnabledForPackage",new areNotificationsEnabledForPackage(mHostContext));
    }

    private static Map<Integer,String> sSystemLayoutResIds=
            new HashMap<>(0);

    private static void init1(){
        try{
            Class clazz=Class.forName("com.android.internal.R$layout");
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields){
                if(Modifier.isPublic(field.getModifiers())
                        &&Modifier.isStatic(field.getModifiers())
                        &&Modifier.isFinal(field.getModifiers())){
                    try{
                        int id=field.getInt(null);
                        sSystemLayoutResIds.put(id,field.getName());
                    }catch (IllegalAccessException e){
                    }
                }
            }
        }catch (Exception e){
        }
    }

}
