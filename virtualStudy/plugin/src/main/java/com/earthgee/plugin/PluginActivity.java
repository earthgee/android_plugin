package com.earthgee.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

/**
 * Created by zhaoruixuan on 2017/7/26.
 */
public class PluginActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button=new Button(this);
        button.setText("test");
        setContentView(button);
    }
}
