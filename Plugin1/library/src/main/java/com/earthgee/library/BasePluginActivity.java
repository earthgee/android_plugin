package com.earthgee.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.earthgee.library.utils.Constants;

/**
 * Created by zhaoruixuan on 2017/3/18.
 */
public class BasePluginActivity extends Activity implements PluginInterface{

    protected Activity mProxyActivity;

    protected Activity that;
    protected PluginManager pluginManager;
    protected PluginPackage mPluginPackage;

    protected int mFrom= Constants.FROM_INTERNAL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            mFrom=savedInstanceState.getInt(Constants.FROM,Constants.FROM_INTERNAL);
        }

        if(mFrom==Constants.FROM_INTERNAL){
            super.onCreate(savedInstanceState);
            mProxyActivity=this;
            that=mProxyActivity;
        }

        pluginManager=PluginManager.getInstance(that);
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
            super.setContentView(view);
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
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.addContentView(view, params);
        }else{
            mProxyActivity.addContentView(view, params);
        }
    }

    @Override
    public View findViewById(int id) {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.findViewById(id);
        }else{
            return mProxyActivity.findViewById(id);
        }
    }

    @Override
    public Intent getIntent() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getIntent();
        }else{
            return mProxyActivity.getIntent();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getClassLoader();
        }else{
            return mProxyActivity.getClassLoader();
        }
    }

    @Override
    public Resources getResources() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getResources();
        }else{
            return mProxyActivity.getResources();
        }
    }

    @Override
    public String getPackageName() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getPackageName();
        }else{
            return mPluginPackage.packageName;
        }
    }

    @NonNull
    @Override
    public LayoutInflater getLayoutInflater() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getLayoutInflater();
        }else{
            return mProxyActivity.getLayoutInflater();
        }
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getSharedPreferences(name, mode);
        }else{
            return mProxyActivity.getSharedPreferences(name, mode);
        }
    }

    @Override
    public Context getApplicationContext() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getApplicationContext();
        }else{
            return mProxyActivity.getApplicationContext();
        }
    }

    @Override
    public WindowManager getWindowManager() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getWindowManager();
        }else{
            return mProxyActivity.getWindowManager();
        }
    }

    @Override
    public Window getWindow() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getWindow();
        }else{
            return mProxyActivity.getWindow();
        }
    }

    @Override
    public Object getSystemService(String name) {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getSystemService(name);
        }else{
            return mProxyActivity.getSystemService(name);
        }
    }

    @Override
    public void finish() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.finish();
        }else{
            mProxyActivity.finish();
        }
    }

    @Override
    public void onStart() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onStart();
        }
    }

    @Override
    public void onRestart() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onRestart();
        }
    }

    @Override
    public void onResume() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onResume();
        }
    }

    @Override
    public void onPause() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onPause();
        }
    }

    @Override
    public void onStop() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onStop();
        }
    }

    @Override
    public void onDestory() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onDestroy();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onNewIntent(intent);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.onKeyUp(keyCode,keyEvent);
        }
        return false;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onWindowAttributesChanged(params);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    public void onBackPressed() {
        if(mFrom==Constants.FROM_INTERNAL){
            super.onBackPressed();
        }
    }

    @Override
    public void attach(Activity proxyActivity, PluginPackage pluginPackage) {
        mProxyActivity=proxyActivity;
        that=mProxyActivity;
        mPluginPackage=pluginPackage;
    }

    public int startPluginActivity(PluginIntent pluginIntent){
        return startPluginActivityForResult(pluginIntent,-1);
    }

    public int startPluginActivityForResult(PluginIntent pluginIntent,int requestCode){
        if(mFrom==Constants.FROM_EXTERNAL){
            if(pluginIntent.getPluginPackage()==null){
                pluginIntent.setPluginPackage(mPluginPackage.packageName);
            }
        }
        return pluginManager.startPluginActivityForResult(that,pluginIntent,requestCode);
    }

    public int startPluginService(PluginIntent pluginIntent){
        if(mFrom==Constants.FROM_EXTERNAL){
            if(pluginIntent.getPluginPackage()==null){
                pluginIntent.setPluginPackage(mPluginPackage.packageName);
            }
        }
        return pluginManager.startPluginService(that,pluginIntent);
    }

    public int stopPluginService(PluginIntent pluginIntent){
        if(mFrom==Constants.FROM_EXTERNAL){
            if(pluginIntent.getPluginPackage()==null){
                pluginIntent.setPluginPackage(mPluginPackage.packageName);
            }
        }
        return pluginManager.stopPluginService(that,pluginIntent);
    }


}














