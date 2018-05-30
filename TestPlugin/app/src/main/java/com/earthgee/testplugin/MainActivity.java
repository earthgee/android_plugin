package com.earthgee.testplugin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by zhaoruixuan on 2018/4/24.
 */
public class MainActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app);
        Button btn= (Button) findViewById(R.id.plugin);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(MainActivity.this,
                            Class.forName("com.earthgee.demo1.Demo1Activity")));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
