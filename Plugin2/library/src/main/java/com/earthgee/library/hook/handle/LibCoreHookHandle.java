package com.earthgee.library.hook.handle;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.earthgee.library.core.PluginDirHelper;
import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/5/9.
 * hook libcore(插件内部文件的管理) i/o重定向
 */
public class LibCoreHookHandle extends BaseHookHandle{
    public LibCoreHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("access",new access(mHostContext));
        sHookedMethodHandlers.put("chmod",new chmod(mHostContext));
        sHookedMethodHandlers.put("chown",new chown(mHostContext));
        sHookedMethodHandlers.put("execv",new execv(mHostContext));
        sHookedMethodHandlers.put("execve",new execve(mHostContext));
        sHookedMethodHandlers.put("mkdir",new mkdir(mHostContext));
        sHookedMethodHandlers.put("open",new open(mHostContext));
        sHookedMethodHandlers.put("remove",new remove(mHostContext));
        sHookedMethodHandlers.put("rename",new rename(mHostContext));
        sHookedMethodHandlers.put("stat",new stat(mHostContext));
        sHookedMethodHandlers.put("statvfs",new statvfs(mHostContext));
        sHookedMethodHandlers.put("symlink",new symlink(mHostContext));
    }

    private abstract static class BaseLibCore extends HookedMethodHandler{

        private final String mDataDir;
        private final String mHostDataDir;
        private final String mHostPkg;

        public BaseLibCore(Context hostContext) {
            super(hostContext);
            // /data/data目录
            mDataDir=new File(Environment.getDataDirectory(),"data/").getPath();
            // 宿主程序/data/data目录
            mHostDataDir= PluginDirHelper.getContextDataDir(hostContext);
            //宿主程序包名
            mHostPkg=hostContext.getPackageName();
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            int index=0;
            replace(args,index);
            return super.beforeInvoke(receiver, method, args);
        }

        protected void replace(Object[] args,int index){
            if(args!=null&&args.length>index&&args[index] instanceof String){
                String path= (String) args[index];
                String newPath=tryReplacePath(path);
                if(newPath!=null){
                    args[index]=newPath;
                }
            }
        }

        //当插件访问“/data/data/插件包名/xxx”时，需要把路径替换成“/data/data/插件宿主包名/Plugin/插件包名/data/插件包名/xxx”
        private String tryReplacePath(String tarDir){
            if(tarDir!=null&&tarDir.length()>mDataDir.length()&&
                    !TextUtils.equals(tarDir, mDataDir) && tarDir.startsWith(mDataDir)){
                if (!tarDir.startsWith(mHostDataDir) && !TextUtils.equals(tarDir, mHostDataDir)){
                    String pkg=tarDir.substring(mDataDir.length()+1);
                    int index=pkg.indexOf("/");
                    if(index>0){
                        pkg=pkg.substring(0,index);
                    }
                    if(!TextUtils.equals(pkg,mHostPkg)){
                        tarDir=tarDir.replace(pkg,String.format("%s/Plugin/%s/data/%s",mHostPkg,pkg,pkg));
                        return tarDir;
                    }
                }
            }
            return null;
        }
    }

    private class access extends BaseLibCore {
        public access(Context context) {
            super(context);
        }
    }

    private class chmod extends BaseLibCore {
        public chmod(Context context) {
            super(context);
        }
    }

    private class chown extends BaseLibCore {
        public chown(Context context) {
            super(context);
        }
    }

    private class execv extends BaseLibCore {
        public execv(Context context) {
            super(context);
        }
    }

    private class execve extends BaseLibCore {
        public execve(Context context) {
            super(context);
        }
    }

    private class mkdir extends BaseLibCore {
        public mkdir(Context context) {
            super(context);
        }
    }

    private class open extends BaseLibCore {
        public open(Context context) {
            super(context);
        }
    }

    private class remove extends BaseLibCore {
        public remove(Context context) {
            super(context);
        }
    }

    private class rename extends BaseLibCore {
        public rename(Context context) {
            super(context);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            int index = 1;
            replace(args, index);
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class stat extends BaseLibCore {
        public stat(Context context) {
            super(context);
        }
    }

    private class statvfs extends BaseLibCore {
        public statvfs(Context context) {
            super(context);
        }
    }

    private class symlink extends BaseLibCore {
        public symlink(Context context) {
            super(context);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            int index = 1;
            replace(args, index);
            return super.beforeInvoke(receiver, method, args);
        }
    }

}












