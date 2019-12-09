package com.earthgee.pluginlib.helper;

import android.app.ActivityManager;
import android.content.Context;

import java.lang.reflect.Method;

public class ProcessUtils {

    private static IProcessChecker sIProcessChecker;

    static {
        sIProcessChecker = new IProcessChecker() {
            @Override
            public boolean isPluginProcess(String processName) {
                return processName != null && processName.contains(":Plugin");
            }

            @Override
            public boolean isManagerProcess(String processName) {
                return processName != null && processName.contains(":CoreManager");
            }
        };
    }

    public static void setProcessChecker(IProcessChecker sIProcessChecker) {
        ProcessUtils.sIProcessChecker = sIProcessChecker;
    }

    public static IProcessChecker getProcessChecker() {
        return sIProcessChecker;
    }

    public static boolean isMainProcess(Context context){
        String processName=getProcessName(context);
        return !getProcessChecker().isManagerProcess(processName)&&
                !getProcessChecker().isPluginProcess(processName);
    }

    public interface IProcessChecker {
        boolean isPluginProcess(String processName);

        boolean isManagerProcess(String processName);
    }

    private static String getProcessName(Context context){
        String processName=null;
        try{
            Class<?> activityThread=Class.forName("android.app.ActivityThread");
            Method currentActivityThread=activityThread.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object am=currentActivityThread.invoke(null);
            Method getProcessName=activityThread.getDeclaredMethod("getProcessName");
            getProcessName.setAccessible(true);
            processName= (String) getProcessName.invoke(am);
        }catch (Exception e){
            int pid=android.os.Process.myPid();
            ActivityManager manager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for(ActivityManager.RunningAppProcessInfo process:manager.getRunningAppProcesses()){
                if(process.pid==pid){
                    processName=process.processName;
                    break;
                }
            }
        }
        return processName;
    }

}












