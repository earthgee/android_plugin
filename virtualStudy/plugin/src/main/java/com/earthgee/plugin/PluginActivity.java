package com.earthgee.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by zhaoruixuan on 2017/7/26.
 */
public class PluginActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        Button button=new Button(this);
        button.setText("start service");
        Button button2=new Button(this);
        button2.setText("stop service");
        Button button3=new Button(this);
        button3.setText("bind service");
        Button button4=new Button(this);
        button4.setText("unbind service");
        Button button5=new Button(this);
        button5.setText("send broadcast");
        ll.addView(button);
        ll.addView(button2);
        ll.addView(button3);
        ll.addView(button4);
        ll.addView(button5);
        setContentView(ll);

        final TestServiceConnection connection=new TestServiceConnection();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PluginActivity.this,PluginService.class);
                startService(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PluginActivity.this,PluginService.class);
                stopService(intent);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PluginActivity.this,PluginService.class);
                bindService(intent, connection,BIND_AUTO_CREATE);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PluginActivity.this,PluginService.class);
                unbindService(connection);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent("com.earthgee.pluginreceiver");
                sendBroadcast(intent);
            }
        });
    }

    private class TestServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ITest testBinder= ITest.Stub.asInterface(service);
            try {
                testBinder.test();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
