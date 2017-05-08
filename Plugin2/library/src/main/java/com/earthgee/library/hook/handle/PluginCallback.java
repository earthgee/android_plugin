package com.earthgee.library.hook.handle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.earthgee.library.core.Env;
import com.earthgee.library.core.PluginProcessManager;
import com.earthgee.library.hook.proxy.IPackageManagerHook;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.reflect.FieldUtils;

/**
 * Created by zhaoruixuan on 2017/5/5.
 */
public class PluginCallback implements Handler.Callback{

    public static final int LAUNCH_ACTIVITY = 100;
    public static final int PAUSE_ACTIVITY = 101;
    public static final int PAUSE_ACTIVITY_FINISHING = 102;
    public static final int STOP_ACTIVITY_SHOW = 103;
    public static final int STOP_ACTIVITY_HIDE = 104;
    public static final int SHOW_WINDOW = 105;
    public static final int HIDE_WINDOW = 106;
    public static final int RESUME_ACTIVITY = 107;
    public static final int SEND_RESULT = 108;
    public static final int DESTROY_ACTIVITY = 109;
    public static final int BIND_APPLICATION = 110;
    public static final int EXIT_APPLICATION = 111;
    public static final int NEW_INTENT = 112;
    public static final int RECEIVER = 113;
    public static final int CREATE_SERVICE = 114;
    public static final int SERVICE_ARGS = 115;
    public static final int STOP_SERVICE = 116;
    public static final int REQUEST_THUMBNAIL = 117;
    public static final int CONFIGURATION_CHANGED = 118;
    public static final int CLEAN_UP_CONTEXT = 119;
    public static final int GC_WHEN_IDLE = 120;
    public static final int BIND_SERVICE = 121;
    public static final int UNBIND_SERVICE = 122;
    public static final int DUMP_SERVICE = 123;
    public static final int LOW_MEMORY = 124;
    public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
    public static final int RELAUNCH_ACTIVITY = 126;
    public static final int PROFILER_CONTROL = 127;
    public static final int CREATE_BACKUP_AGENT = 128;
    public static final int DESTROY_BACKUP_AGENT = 129;
    public static final int SUICIDE = 130;
    public static final int REMOVE_PROVIDER = 131;
    public static final int ENABLE_JIT = 132;
    public static final int DISPATCH_PACKAGE_BROADCAST = 133;
    public static final int SCHEDULE_CRASH = 134;
    public static final int DUMP_HEAP = 135;
    public static final int DUMP_ACTIVITY = 136;
    public static final int SLEEPING = 137;
    public static final int SET_CORE_SETTINGS = 138;
    public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
    public static final int TRIM_MEMORY = 140;
    public static final int DUMP_PROVIDER = 141;
    public static final int UNSTABLE_PROVIDER_DIED = 142;
    public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
    public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
    public static final int INSTALL_PROVIDER = 145;
    public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
    public static final int CANCEL_VISIBLE_BEHIND = 147;
    public static final int BACKGROUND_VISIBLE_BEHIND_CHANGED = 148;
    public static final int ENTER_ANIMATION_COMPLETE = 149;

    String codeToString(int code) {
        switch (code) {
            case LAUNCH_ACTIVITY:
                return "LAUNCH_ACTIVITY";
            case PAUSE_ACTIVITY:
                return "PAUSE_ACTIVITY";
            case PAUSE_ACTIVITY_FINISHING:
                return "PAUSE_ACTIVITY_FINISHING";
            case STOP_ACTIVITY_SHOW:
                return "STOP_ACTIVITY_SHOW";
            case STOP_ACTIVITY_HIDE:
                return "STOP_ACTIVITY_HIDE";
            case SHOW_WINDOW:
                return "SHOW_WINDOW";
            case HIDE_WINDOW:
                return "HIDE_WINDOW";
            case RESUME_ACTIVITY:
                return "RESUME_ACTIVITY";
            case SEND_RESULT:
                return "SEND_RESULT";
            case DESTROY_ACTIVITY:
                return "DESTROY_ACTIVITY";
            case BIND_APPLICATION:
                return "BIND_APPLICATION";
            case EXIT_APPLICATION:
                return "EXIT_APPLICATION";
            case NEW_INTENT:
                return "NEW_INTENT";
            case RECEIVER:
                return "RECEIVER";
            case CREATE_SERVICE:
                return "CREATE_SERVICE";
            case SERVICE_ARGS:
                return "SERVICE_ARGS";
            case STOP_SERVICE:
                return "STOP_SERVICE";
            case CONFIGURATION_CHANGED:
                return "CONFIGURATION_CHANGED";
            case CLEAN_UP_CONTEXT:
                return "CLEAN_UP_CONTEXT";
            case GC_WHEN_IDLE:
                return "GC_WHEN_IDLE";
            case BIND_SERVICE:
                return "BIND_SERVICE";
            case UNBIND_SERVICE:
                return "UNBIND_SERVICE";
            case DUMP_SERVICE:
                return "DUMP_SERVICE";
            case LOW_MEMORY:
                return "LOW_MEMORY";
            case ACTIVITY_CONFIGURATION_CHANGED:
                return "ACTIVITY_CONFIGURATION_CHANGED";
            case RELAUNCH_ACTIVITY:
                return "RELAUNCH_ACTIVITY";
            case PROFILER_CONTROL:
                return "PROFILER_CONTROL";
            case CREATE_BACKUP_AGENT:
                return "CREATE_BACKUP_AGENT";
            case DESTROY_BACKUP_AGENT:
                return "DESTROY_BACKUP_AGENT";
            case SUICIDE:
                return "SUICIDE";
            case REMOVE_PROVIDER:
                return "REMOVE_PROVIDER";
            case ENABLE_JIT:
                return "ENABLE_JIT";
            case DISPATCH_PACKAGE_BROADCAST:
                return "DISPATCH_PACKAGE_BROADCAST";
            case SCHEDULE_CRASH:
                return "SCHEDULE_CRASH";
            case DUMP_HEAP:
                return "DUMP_HEAP";
            case DUMP_ACTIVITY:
                return "DUMP_ACTIVITY";
            case SLEEPING:
                return "SLEEPING";
            case SET_CORE_SETTINGS:
                return "SET_CORE_SETTINGS";
            case UPDATE_PACKAGE_COMPATIBILITY_INFO:
                return "UPDATE_PACKAGE_COMPATIBILITY_INFO";
            case TRIM_MEMORY:
                return "TRIM_MEMORY";
            case DUMP_PROVIDER:
                return "DUMP_PROVIDER";
            case UNSTABLE_PROVIDER_DIED:
                return "UNSTABLE_PROVIDER_DIED";
            case REQUEST_ASSIST_CONTEXT_EXTRAS:
                return "REQUEST_ASSIST_CONTEXT_EXTRAS";
            case TRANSLUCENT_CONVERSION_COMPLETE:
                return "TRANSLUCENT_CONVERSION_COMPLETE";
            case INSTALL_PROVIDER:
                return "INSTALL_PROVIDER";
            case ON_NEW_ACTIVITY_OPTIONS:
                return "ON_NEW_ACTIVITY_OPTIONS";
            case CANCEL_VISIBLE_BEHIND:
                return "CANCEL_VISIBLE_BEHIND";
            case BACKGROUND_VISIBLE_BEHIND_CHANGED:
                return "BACKGROUND_VISIBLE_BEHIND_CHANGED";
            case ENTER_ANIMATION_COMPLETE:
                return "ENTER_ANIMATION_COMPLETE";
        }
        return Integer.toString(code);
    }

    private Handler mOldHandle=null;
    private Handler.Callback mCallback=null;
    private Context mHostContext;

    private boolean mEnable=false;

    public PluginCallback(Context hostContext,Handler oldHandle,Handler.Callback callback){
        mOldHandle=oldHandle;
        mCallback=callback;
        mHostContext=hostContext;
    }

    public void setEnable(boolean enable){
        this.mEnable=enable;
    }

    public boolean isEnable(){
        return mEnable;
    }

    @Override
    public boolean handleMessage(Message msg) {
        long b=System.currentTimeMillis();
        try{
            if(!mEnable){
                return false;
            }

            if(PluginProcessManager.isPluginProcess(mHostContext)){
                if(!PluginManager.getInstance().isConnected()){
                    mOldHandle.sendMessageDelayed(Message.obtain(msg),5);
                    return true;
                }
            }

            if(msg.what==LAUNCH_ACTIVITY){
                return handleLaunchActivity(msg);
            }

            if(mCallback!=null){
                return mCallback.handleMessage(msg);
            }else{
                return false;
            }
        }finally {

        }
    }

    private boolean handleLaunchActivity(Message msg){
        try{
            Object obj=msg.obj;
            Intent stubIntent= (Intent) FieldUtils.readField(obj,"intent");
            stubIntent.setExtrasClassLoader(mHostContext.getClassLoader());
            Intent targetIntent=stubIntent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);

            if(targetIntent!=null&&!isShortcutProxyActivity(stubIntent)){
                IPackageManagerHook.fixContextPackageManager(mHostContext);
                ComponentName targetComponentName=targetIntent.resolveActivity(mHostContext.getPackageManager());
                ActivityInfo targetActivityInfo=PluginManager.getInstance().getActivityInfo(targetComponentName,0);
                if(targetActivityInfo!=null){
                    if(targetComponentName!=null&&targetComponentName.getClassName().startsWith(".")){
                        targetIntent.setClassName(targetComponentName.getPackageName(),targetComponentName.getPackageName()+
                                targetComponentName.getClassName());
                    }

                    ResolveInfo resolveInfo=mHostContext.getPackageManager().resolveActivity(stubIntent,0);
                    ActivityInfo stubActivityInfo=resolveInfo!=null?resolveInfo.activityInfo:null;
                    if(stubActivityInfo!=null){
                        PluginManager.getInstance().reportMyProcessName(stubActivityInfo.processName,targetActivityInfo.processName,
                                targetActivityInfo.packageName);
                    }
                    PluginProcessManager.preLoadApk(mHostContext,targetActivityInfo);
                    ClassLoader pluginClassLoader=PluginProcessManager.getPluginClassLoader(targetComponentName.getPackageName());
                    setIntentClassLoader(targetIntent,pluginClassLoader);
                    setIntentClassLoader(stubIntent,pluginClassLoader);

                    boolean success=false;
                    try{
                        targetIntent.putExtra(Env.EXTRA_TARGET_INFO,targetActivityInfo);
                        if(stubActivityInfo!=null){
                            targetIntent.putExtra(Env.EXTRA_STUB_INFO,stubActivityInfo);
                        }
                        success=true;
                    }catch (Exception e){
                    }

                    if(!success&& Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT){
                        try{
                            ClassLoader oldParent=fixedClassLoader(pluginClassLoader);
                            targetIntent.putExtras(targetIntent.getExtras());

                            targetIntent.putExtra(Env.EXTRA_TARGET_INFO,targetActivityInfo);
                            if(stubActivityInfo!=null){
                                targetIntent.putExtra(Env.EXTRA_STUB_INFO,stubActivityInfo);
                            }
                            fixedClassLoader(oldParent);
                            success=true;
                        }catch (Exception e){
                        }
                    }

                    if(!success){
                        Intent newTargetIntent=new Intent();
                        newTargetIntent.setComponent(targetIntent.getComponent());
                        newTargetIntent.putExtra(Env.EXTRA_TARGET_INFO,targetActivityInfo);
                        if(stubActivityInfo!=null){
                            newTargetIntent.putExtra(Env.EXTRA_STUB_INFO,stubActivityInfo);
                        }
                        FieldUtils.writeDeclaredField(msg.obj,"intent",newTargetIntent);
                    }else{
                        FieldUtils.writeDeclaredField(msg.obj,"intent",targetIntent);
                    }
                    FieldUtils.writeDeclaredField(msg.obj,"activityInfo",targetActivityInfo);
                }
            }
        }catch (Exception e){
        }

        if(mCallback!=null){
            return mCallback.handleMessage(msg);
        }else{
            return false;
        }
    }

    private boolean isShortcutProxyActivity(Intent targetIntent){
        return false;
    }

    private void setIntentClassLoader(Intent intent,ClassLoader classLoader){
        try{
            Bundle mExtras= (Bundle) FieldUtils.readField(intent,"mExtras");
            if(mExtras!=null){
                mExtras.setClassLoader(classLoader);
            }else{
                Bundle value=new Bundle();
                value.setClassLoader(classLoader);
                FieldUtils.writeField(intent,"mExtras",value);
            }
        }catch (Exception e){
        }finally {
            intent.setExtrasClassLoader(classLoader);
        }
    }

    private ClassLoader fixedClassLoader(ClassLoader newParent){
        ClassLoader nowClassLoader=PluginCallback.class.getClassLoader();
        ClassLoader oldParent=nowClassLoader.getParent();
        try{
            if(newParent!=null&&newParent!=oldParent){
                FieldUtils.writeField(nowClassLoader,"parent",newParent);
            }
        }catch (Exception e){
        }
        return oldParent;
    }

}



















