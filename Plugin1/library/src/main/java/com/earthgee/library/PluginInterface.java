package com.earthgee.library;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Created by zhaoruixuan on 2017/3/15.
 */
public interface PluginInterface {

    void onCreate(Bundle savedInstanceState);
    void onStart();
    void onRestart();
    void onResume();
    void onPause();
    void onStop();
    void onDestory();
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onSaveInstanceState(Bundle outState);
    void onRestoreInstanceState(Bundle savedInstanceState);
    void onNewIntent(Intent intent);
    boolean onTouchEvent(MotionEvent event);
    boolean onKeyUp(int keyCode, KeyEvent event);
    void onWindowAttributesChanged(WindowManager.LayoutParams params);
    void onWindowFocusChanged(boolean hasFocus);
    void onBackPressed();
    void attach(Activity proxyActivity,PluginPackage pluginPackage);

}
