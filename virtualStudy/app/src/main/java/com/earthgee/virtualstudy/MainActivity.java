package com.earthgee.virtualstudy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.earthgee.corelibrary.PluginManager;

import java.io.File;

/**
 * Created by zhaoruixuan on 2017/7/26.
 */
public class MainActivity extends Activity{

    private Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        test= (Button) findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPlugin(MainActivity.this);
            }
        });
    }

    private void loadPlugin(Context context){
        PluginManager pluginManager=PluginManager.getInstance(context);
        File apk = new File(Environment.getExternalStorageDirectory(), "Test.apk");
        if (apk.exists()) {
            try {
                pluginManager.loadPlugin(apk);

                Intent intent=new Intent();
                String packageName="com.earthgee.plugin";
                String className="com.earthgee.plugin.PluginActivity";
                intent.setClassName(packageName,className);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
