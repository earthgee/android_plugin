package com.earthgee.library;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.earthgee.library.utils.Constants;

/**
 * Created by zhaoruixuan on 2017/3/21.
 */
public class BasePluginFragmentActivity extends FragmentActivity implements PluginInterface {

    protected FragmentActivity mProxyActivity;

    protected FragmentActivity that;
    protected int mFrom = Constants.FROM_INTERNAL;
    protected PluginManager mPluginManager;
    protected PluginPackage mPluginPackage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFrom = savedInstanceState.getInt(Constants.FROM, Constants.FROM_INTERNAL);
        }

        if(mFrom==Constants.FROM_INTERNAL){
            super.onCreate(savedInstanceState);
            mProxyActivity=this;
            that=mProxyActivity;
        }

        mPluginManager=PluginManager.getInstance(that);
    }

    @Override
    public void setContentView(View view) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.setContentView(view);
        }else{
            mProxyActivity.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.setContentView(view, params);
        }else{
            mProxyActivity.setContentView(view, params);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.setContentView(layoutResID);
        }else{
            mProxyActivity.setContentView(layoutResID);
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestory() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void attach(Activity proxyActivity, PluginPackage pluginPackage) {
        mProxyActivity = (FragmentActivity) proxyActivity;
        that = mProxyActivity;
        mPluginPackage = pluginPackage;
    }
}
