package com.earthgee.library.stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zhaoruixuan on 2017/4/12.
 */
public class AbstractServiceStub extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
