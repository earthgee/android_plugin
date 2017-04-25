package com.earthgee.library.hook.handle;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.HookedMethodHandler;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.util.Utils;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/25.
 */
public class IMountServiceHookHandle extends BaseHookHandle{
    public IMountServiceHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("mkdirs",new mkdirs(mHostContext));
    }

    private class mkdirs extends HookedMethodHandler{

        public mkdirs(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
                final int index=0;
                if(args!=null&&args.length>index&&args[index] instanceof String){
                    String callingPkg= (String) args[index];
                    if(!TextUtils.equals(callingPkg,mHostContext.getPackageName())){
                        args[index]=mHostContext.getPackageName();
                    }
                }

                final int index1=1;
                if(args!=null&&args.length>index1&&args[index1] instanceof String){
                    String path= (String) args[index1];
                    if(path!=null&&path.indexOf(mHostContext.getPackageName())<0){
                        String[] dirs=path.split("/");
                        if(dirs!=null&&dirs.length>0){
                            String pluginPackageName=null;
                            for(int i=0;i<dirs.length;i++){
                                String str=dirs[i];
                                if(TextUtils.isEmpty(str)){
                                    continue;
                                }
                                if(!Utils.valideJavaIdentifier(str)){
                                    continue;
                                }
                                if(PluginManager.getInstance().isPluginPackage(str)){
                                    pluginPackageName=str;
                                    break;
                                }
                            }
                            if(pluginPackageName!=null){
                                path=path.replaceFirst(pluginPackageName,
                                        mHostContext.getPackageName()+"/Plugin/"+pluginPackageName);
                                args[index1]=path;
                            }
                        }
                    }
                }
            }else{
                final int index1=0;
                if(args!=null&&args.length>index1&&
                        args[index1] instanceof String){
                    String path= (String) args[index1];
                    if(path!=null&&path.indexOf(mHostContext.getPackageName())<0){
                        String[] dirs=path.split("/");
                        if(dirs!=null&&dirs.length>0){
                            String pluginPackageName=null;
                            for(int i=0;i<dirs.length;i++){
                                String str=dirs[i];
                                if(TextUtils.isEmpty(str)){
                                    continue;
                                }
                                if(!Utils.valideJavaIdentifier(str)){
                                    continue;
                                }
                                if(PluginManager.getInstance().isPluginPackage(str)){
                                    pluginPackageName=str;
                                    break;
                                }
                            }
                            if(pluginPackageName!=null){
                                path=path.replaceFirst(pluginPackageName,mHostContext.getPackageName()+"/Plugin"+pluginPackageName);
                                args[index1]=path;
                            }
                        }
                    }
                }
            }

            return super.beforeInvoke(receiver, method, args);
        }
    }

}
