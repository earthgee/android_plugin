package com.earthgee.plugin;

import android.content.Intent;
import android.widget.Toast;

import com.earthgee.library.BasePluginService;

/**
 * Created by zhaoruixuan on 2017/3/22.
 */
public class PluginService extends BasePluginService{

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this,"plugin service onCreate",Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this,"plugin service onDestroy",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
