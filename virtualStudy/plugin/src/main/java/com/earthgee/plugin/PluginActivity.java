package com.earthgee.plugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

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
        ll.addView(button);
        ll.addView(button2);
        setContentView(ll);
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
    }
}
