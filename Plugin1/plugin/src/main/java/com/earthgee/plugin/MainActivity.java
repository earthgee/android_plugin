package com.earthgee.plugin;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.earthgee.library.BasePluginActivity;
import com.earthgee.library.PluginIntent;

public class MainActivity extends BasePluginActivity {

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;

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


    }



}
