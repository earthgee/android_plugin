package com.earthgee.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.earthgee.library.BasePluginActivity;
import com.earthgee.library.PluginIntent;
import com.earthgee.library.PluginPackage;

public class MainActivity extends BasePluginActivity {

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;

    private ServiceConnection conn;

    private static Handler handler=new Handler(){

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1= (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(that,"test",Toast.LENGTH_SHORT).show();
                PluginIntent pluginIntent=new PluginIntent(getPackageName(),PluginActivity.class);
                pluginIntent.putExtra("test","trans above activity");
                startPluginActivityForResult(pluginIntent,0);
            }
        });

        btn2= (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PluginIntent pluginIntent=new PluginIntent(getPackageName(),PluginService.class);
                startPluginService(pluginIntent);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PluginIntent pluginIntent=new PluginIntent(getPackageName(),PluginService.class);
                        stopPluginService(pluginIntent);
                    }
                },2000);
            }
        });

        btn3= (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PluginIntent pluginIntent=new PluginIntent(getPackageName(),PluginService.class);
                if(conn==null){
                    conn=new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            PluginBridge pluginBridge= (PluginBridge) service;
                            Log.d("earthgee1","service onBind");
                            Log.d("earthgee1",pluginBridge.sum(125,125)+"");
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            Log.d("earthgee1","service unbind");
                        }
                    };
                }
                bindPluginService(pluginIntent, conn, Context.BIND_AUTO_CREATE);
            }
        });


        btn4= (Button) findViewById(R.id.btn4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PluginIntent pluginIntent=new PluginIntent(getPackageName(),PluginService.class);
                unBindPluginService(pluginIntent,conn);
            }
        });
    }



}
