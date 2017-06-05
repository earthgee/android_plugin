package com.earthgee.libaray.stub;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class AbstractServiceStub extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startKillService(Context context,Intent service){
        service.putExtra("ActionKillSelf",true);
        context.startService(service);
    }

}
