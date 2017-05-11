package com.earthgee.library;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Environment;

import com.earthgee.library.util.SystemPropertiesCompat;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/6.
 * 单例 每个进程主线程的crash handler
 */
public class MyCrashHandler implements Thread.UncaughtExceptionHandler{

    private static final MyCrashHandler sMyCrashHandler=new MyCrashHandler();

    private Thread.UncaughtExceptionHandler mOldHandler;

    private Context mContext;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT1 = new SimpleDateFormat("yyyyMMddHHmmss");

    public static MyCrashHandler getInstance(){
        return sMyCrashHandler;
    }

    public void register(Context context){
        if(context!=null){
            mOldHandler= Thread.getDefaultUncaughtExceptionHandler();
            if(mOldHandler!=this){
                Thread.setDefaultUncaughtExceptionHandler(this);
            }
            mContext=context;
        }
    }

    //日志写本地
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        PrintWriter writer = null;
        try {
            Date date = new Date();
            String dateStr = SIMPLE_DATE_FORMAT1.format(date);

            File file = new File(Environment.getExternalStorageDirectory(), String.format("PluginLog/CrashLog/CrashLog_%s_%s.log", dateStr, android.os.Process.myPid()));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (file.exists()) {
                file.delete();
            }

            writer = new PrintWriter(file);

            writer.println("Date:" + SIMPLE_DATE_FORMAT.format(date));
            writer.println("----------------------------------------System Infomation-----------------------------------");

            String packageName = mContext.getPackageName();
            writer.println("AppPkgName:" + packageName);
            try {
                PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
                writer.println("VersionCode:" + packageInfo.versionCode);
                writer.println("VersionName:" + packageInfo.versionName);
                writer.println("Debug:" + (0 != (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE)));
            } catch (Exception e) {
                writer.println("VersionCode:-1");
                writer.println("VersionName:null");
                writer.println("Debug:Unkown");
            }

            writer.println("PName:" + getProcessName());

            try {
                writer.println("imei:" + getIMEI(mContext));
            } catch (Exception e) {
            }

            writer.println("Board:" + SystemPropertiesCompat.get("ro.product.board", "unknown"));
            writer.println("ro.bootloader:" + SystemPropertiesCompat.get("ro.bootloader", "unknown"));
            writer.println("ro.product.brand:" + SystemPropertiesCompat.get("ro.product.brand", "unknown"));
            writer.println("ro.product.cpu.abi:" + SystemPropertiesCompat.get("ro.product.cpu.abi", "unknown"));
            writer.println("ro.product.cpu.abi2:" + SystemPropertiesCompat.get("ro.product.cpu.abi2", "unknown"));
            writer.println("ro.product.device:" + SystemPropertiesCompat.get("ro.product.device", "unknown"));
            writer.println("ro.build.display.id:" + SystemPropertiesCompat.get("ro.build.display.id", "unknown"));
            writer.println("ro.build.fingerprint:" + SystemPropertiesCompat.get("ro.build.fingerprint", "unknown"));
            writer.println("ro.hardware:" + SystemPropertiesCompat.get("ro.hardware", "unknown"));
            writer.println("ro.build.host:" + SystemPropertiesCompat.get("ro.build.host", "unknown"));
            writer.println("ro.build.id:" + SystemPropertiesCompat.get("ro.build.id", "unknown"));
            writer.println("ro.product.manufacturer:" + SystemPropertiesCompat.get("ro.product.manufacturer", "unknown"));
            writer.println("ro.product.model:" + SystemPropertiesCompat.get("ro.product.model", "unknown"));
            writer.println("ro.product.name:" + SystemPropertiesCompat.get("ro.product.name", "unknown"));
            writer.println("gsm.version.baseband:" + SystemPropertiesCompat.get("gsm.version.baseband", "unknown"));
            writer.println("ro.build.tags:" + SystemPropertiesCompat.get("ro.build.tags", "unknown"));
            writer.println("ro.build.type:" + SystemPropertiesCompat.get("ro.build.type", "unknown"));
            writer.println("ro.build.user:" + SystemPropertiesCompat.get("ro.build.user", "unknown"));
            writer.println("ro.build.version.codename:" + SystemPropertiesCompat.get("ro.build.version.codename", "unknown"));
            writer.println("ro.build.version.incremental:" + SystemPropertiesCompat.get("ro.build.version.incremental", "unknown"));
            writer.println("ro.build.version.release:" + SystemPropertiesCompat.get("ro.build.version.release", "unknown"));
            writer.println("ro.build.version.sdk:" + SystemPropertiesCompat.get("ro.build.version.sdk", "unknown"));
            writer.println("\n\n\n----------------------------------Exception---------------------------------------\n\n");
            writer.println("----------------------------Exception message:" + ex.getLocalizedMessage() + "\n");
            writer.println("----------------------------Exception StackTrace:");
            ex.printStackTrace(writer);
        } catch (Throwable e) {
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (Exception e) {
            }

            if (mOldHandler != null) {
                mOldHandler.uncaughtException(thread, ex);
            }
        }
    }

    private String getIMEI(Context mContext) {
        return "test";
    }

    public String getProcessName(){
        ActivityManager am= (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos=am.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo info:infos){
            if(info.pid==android.os.Process.myPid()){
                return info.processName;
            }
        }
        return null;
    }


}














