package com.earthgee.library.stub;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.earthgee.library.core.Env;
import com.earthgee.library.pm.PluginManager;

import java.net.URISyntaxException;

/**
 * Created by zhaoruixuan on 2017/5/17.
 */
public class ShortCutProxyActivity extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            Intent intent=getIntent();

            if(intent!=null){
                Intent forwordIntent=getForwarIntent();
                if(forwordIntent!=null){
                    forwordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    forwordIntent.putExtras(intent);
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                        forwordIntent.setSelector(null);
                    }
                    if(PluginManager.getInstance().isConnected()){
                        if(isPlugin(forwordIntent)){
                            execStartForwordIntent(forwordIntent);
                        }
                        finish();
                    }else{
                        waitAndStart(forwordIntent);
                    }
                }else{
                    finish();
                }
            }else{
                finish();
            }
        }catch (Exception e){
            finish();
        }
    }

    protected void execStartForwordIntent(Intent forwordIntent){
        startActivity(forwordIntent);
    }

    private boolean isPlugin(Intent intent){
        try{
            String pkg=null;
            if(intent.getComponent()!=null&&intent.getComponent().getPackageName()!=null){
                pkg=intent.getComponent().getPackageName();
            }else{
                ResolveInfo info=PluginManager.getInstance().resolveIntent(intent,null,0);
                pkg=info.resolvePackageName;
            }
            return pkg!=null&&PluginManager.getInstance().isPluginPackage(pkg);
        }catch (Exception e){
            return false;
        }
    }

    private void waitAndStart(final Intent forwordIntent){
        new Thread(){
            @Override
            public void run() {
                try{
                    PluginManager.getInstance().waitForConnected();
                    if(isPlugin(forwordIntent)){
                        execStartForwordIntent(forwordIntent);
                    }
                }catch (Exception e){

                }finally {
                    finish();
                }
            }
        }.start();
    }

    private Intent getForwarIntent(){
        Intent intent=getIntent();
        try{
            if(intent!=null){
                Intent forwordIntent=intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
                String intentUri=intent.getStringExtra(Env.EXTRA_TARGET_INTENT_URI);
                if(intentUri!=null){
                    try{
                        Intent res=Intent.parseUri(intentUri,0);
                        return res;
                    }catch (URISyntaxException e){
                    }
                }else if(forwordIntent!=null){
                    return forwordIntent;
                }
            }
        }catch (Exception e){
        }
        return null;
    }
}



















