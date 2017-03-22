package com.earthgee.plugin;

import android.os.Bundle;

import com.earthgee.library.BasePluginActivity;
import com.earthgee.library.BasePluginFragmentActivity;

/**
 * Created by zhaoruixuan on 2017/3/21.
 */
public class PluginActivity extends BasePluginFragmentActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_main);
    }

}
