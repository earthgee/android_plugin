package com.earthgee.library.hook.handle;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.earthgee.library.core.PluginProcessManager;
import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by zhaoruixuan on 2017/4/25.
 */
public class INotificationManagerHookHandle extends BaseHookHandle {
    public INotificationManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        init1();
        sHookedMethodHandlers.put("enqueueNotification", new enqueueNotification(mHostContext));
        sHookedMethodHandlers.put("cancelNotification", new cancelNotification(mHostContext));
        sHookedMethodHandlers.put("cancelAllNotification", new cancelAllNotifications(mHostContext));
        sHookedMethodHandlers.put("enqueueToast", new enqueueToast(mHostContext));
        sHookedMethodHandlers.put("cancelToast", new cancelToast(mHostContext));
        sHookedMethodHandlers.put("enqueueNotificationWithTag", new enqueueNotificationWithTag(mHostContext));
        sHookedMethodHandlers.put("enqueueNotificationWithTagPriority", new enqueueNotificationWithTagPriority(mHostContext));
        sHookedMethodHandlers.put("cancelNotificationWithTag", new cancelNotificationWithTag(mHostContext));
        sHookedMethodHandlers.put("setNotificationsEnabledForPackage", new setNotificationsEnabledForPackage(mHostContext));
        sHookedMethodHandlers.put("areNotificationsEnabledForPackage", new areNotificationsEnabledForPackage(mHostContext));
    }

    private static Map<Integer, String> sSystemLayoutResIds =
            new HashMap<>(0);

    private static void init1() {
        try {
            Class clazz = Class.forName("com.android.internal.R$layout");
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isPublic(field.getModifiers())
                        && Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())) {
                    try {
                        int id = field.getInt(null);
                        sSystemLayoutResIds.put(id, field.getName());
                    } catch (IllegalAccessException e) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static int getResIdByName(String name) {
        for (Integer integer : sSystemLayoutResIds.keySet()) {
            if (TextUtils.equals(name, sSystemLayoutResIds.get(integer))) {
                return integer;
            }
        }
        return -1;
    }

    private class MyNotification extends HookedMethodHandler {

        public MyNotification(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            int index = 0;
            if (args != null && args.length > index) {
                if (args[index] instanceof String) {
                    String pkg = (String) args[index];
                    if (!TextUtils.equals(pkg, mHostContext.getPackageName())) {
                        args[index] = mHostContext.getPackageName();
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private int findFirstNotificationIndex(Object[] args) {
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Notification) {
                    return i;
                }
            }
        }
        return -1;
    }

    private class enqueueNotification extends MyNotification {

        public enqueueNotification(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            final int index = 0;
            if (args != null && args.length > index && args[index] instanceof String) {
                String pkg = (String) args[index];
                if (!TextUtils.equals(pkg, mHostContext.getPackageName())) {
                    args[index] = mHostContext.getPackageName();
                }
            }
            final int index2 = findFirstNotificationIndex(args);
            if (index2 >= 0) {
                Notification notification = (Notification) args[index2];
                if (isPluginNotification(notification)) {
                    if (shouldBlock(notification)) {
                        return true;
                    } else {
                        hackNotification(notification);
                        return false;
                    }
                }
            }
            return false;
        }
    }

    private boolean isPluginNotification(Notification notification) {
        if (notification == null) {
            return false;
        }

        if (notification.contentView != null && !TextUtils.equals(mHostContext.getPackageName(),
                notification.contentView.getPackage())) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (notification.tickerView != null && !TextUtils.equals(mHostContext.getPackageName(),
                    notification.contentView.getPackage())) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (notification.bigContentView != null && !TextUtils.equals(mHostContext.getPackageName(),
                    notification.contentView.getPackage())) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notification.headsUpContentView != null && !TextUtils.equals(mHostContext.getPackageName(),
                    notification.headsUpContentView.getPackage())) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getSmallIcon();
            if (icon != null) {
                try {
                    Object mString1Obj = FieldUtils.readField(icon, "mString1", true);
                    if (mString1Obj instanceof String) {
                        String mString1 = (String) mString1Obj;
                        if (PluginManager.getInstance().isPluginPackage(mString1)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getLargeIcon();
            if (icon != null) {
                try {
                    Object mString1Obj = FieldUtils.readField(icon, "mString1", true);
                    if (mString1Obj instanceof String) {
                        String mString1 = (String) mString1Obj;
                        if (PluginManager.getInstance().isPluginPackage(mString1)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        try {
            Bundle mExtras = (Bundle) FieldUtils.readField(notification, "extras", true);
            for (String s : mExtras.keySet()) {
                if (mExtras.get(s) != null && mExtras.get(s) instanceof ApplicationInfo) {
                    ApplicationInfo applicationInfo = (ApplicationInfo) mExtras.get(s);
                    return !TextUtils.equals(mHostContext.getPackageName(), applicationInfo.packageName);
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    private boolean shouldBlockByRemoteViews(RemoteViews remoteViews) {
        if (remoteViews == null) {
            return false;
        } else if (remoteViews != null && sSystemLayoutResIds.containsKey(remoteViews.getLayoutId())) {
            return false;
        } else {
            return true;
        }
    }

    private boolean shouldBlock(Notification notification) {
        if (shouldBlockByRemoteViews(notification.contentView)) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (shouldBlockByRemoteViews(notification.tickerView)) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (shouldBlockByRemoteViews(notification.bigContentView)) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (shouldBlockByRemoteViews(notification.headsUpContentView)) {
                return true;
            }
        }
        return false;
    }

    private void hackNotification(Notification notification) throws Exception {
        if (notification != null) {
            notification.icon = mHostContext.getApplicationInfo().icon;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                hackRemoteViews(notification.tickerView);
            }
            hackRemoteViews(notification.contentView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                hackRemoteViews(notification.bigContentView);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hackRemoteViews(notification.headsUpContentView);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.graphics.drawable.Icon icon = notification.getSmallIcon();
                if (icon != null) {
                    Bitmap bitmap = drawableToBitMap(icon.loadDrawable(mHostContext));
                    if (bitmap != null) {
                        android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                        FieldUtils.writeField(notification, "mSmallIcon", newIcon, true);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.graphics.drawable.Icon icon = notification.getLargeIcon();
                if (icon != null) {
                    Bitmap bitmap = drawableToBitMap(icon.loadDrawable(mHostContext));
                    if (bitmap != null) {
                        android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                        FieldUtils.writeField(notification, "mLargeIcon", newIcon, true);
                    }
                }
            }
        }
    }

    private void hackRemoteViews(RemoteViews remoteViews) throws Exception{
        if(remoteViews!=null&&!TextUtils.equals(remoteViews.getPackage(),mHostContext.getPackageName())){
            if(sSystemLayoutResIds.containsKey(remoteViews.getLayoutId())){
                Object mActionsObj=FieldUtils.readField(remoteViews,"mActions");
                if(mActionsObj instanceof Collection){
                    Collection mActions= (Collection) mActionsObj;
                    String aPackage=remoteViews.getPackage();
                    Application pluginContent= PluginProcessManager.getPluginContext(aPackage);
                    if(pluginContent!=null){
                        Iterator iterator=mActions.iterator();
                        Class TextViewDrawableActionClass=null;
                        try {
                              if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                                  TextViewDrawableActionClass=Class.forName(RemoteViews.class.getName()+"$TextViewDrawableAction");
                              }
                        }catch (ClassNotFoundException e){
                        }
                        Class ReflectionActionClass=Class.forName(RemoteViews.class.getName()+"$ReflectionAction");
                        while (iterator.hasNext()){
                            Object action=iterator.next();
                            if(ReflectionActionClass.isInstance(action)){
                                String methodName= (String) FieldUtils.readField(action,"methodName");
                                if("setImageResource".equals(methodName)){
                                    Object BITMAP=FieldUtils.readStaticField(action.getClass(),"BITMAP");
                                    int resId= (int) FieldUtils.readField(action,"value");
                                    Bitmap bitmap= BitmapFactory.decodeResource(pluginContent.getResources(),resId);
                                    FieldUtils.writeField(action,"type",BITMAP);
                                    FieldUtils.writeField(action,"value",bitmap);
                                    FieldUtils.writeField(action,"methodName","setImageBitmap");
                                }else if("setImageURI".equals(methodName)){
                                    iterator.remove();
                                }else if("setLabelFor".equals(methodName)){
                                    iterator.remove();
                                }
                            }else if(TextViewDrawableActionClass!=null&&TextViewDrawableActionClass.isInstance(action)){
                                iterator.remove();
                            }
                        }
                    }
                }
            }
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                FieldUtils.writeField(remoteViews,"mApplication",mHostContext.getApplicationInfo());
            }else{
                FieldUtils.writeField(remoteViews,"mPackage",mHostContext.getPackageName());
            }
        }
    }

    private Bitmap drawableToBitMap(Drawable drawable){
        if(drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable= (BitmapDrawable) drawable;
            return bitmapDrawable.getBitmap();
        }else{
            Bitmap bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),
                    drawable.getOpacity()!= PixelFormat.OPAQUE?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565);
            Canvas canvas=new Canvas(bitmap);
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    private class cancelNotification extends MyNotification{

        public cancelNotification(Context hostContext) {
            super(hostContext);
        }
    }

    private class cancelAllNotification extends MyNotification{

        public cancelAllNotification(Context hostContext) {
            super(hostContext);
        }
    }

    private class enqueueToast extends MyNotification{

        public enqueueToast(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                int index=1;
                if(args!=null&&args.length>index){
                    Object obj=args[index];
                    View view= (View) FieldUtils.readField(obj,"mView");
                    View nextView= (View) FieldUtils.readField(obj,"mNextView");
                    if(nextView!=null){
                        FieldUtils.writeField(nextView,"mContext",mHostContext);
                    }
                    if(view!=null){
                        FieldUtils.writeField(view,"mContext",mHostContext);
                    }
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    

}












