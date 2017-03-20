package com.earthgee.plugin1;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.earthgee.library.PluginIntent;
import com.earthgee.library.PluginManager;
import com.earthgee.library.utils.PluginUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mPluginList;
    private List<PluginItem> mPluginItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPluginList= (ListView) findViewById(R.id.plugin_list);
        initData();
    }

    private void initData(){
        String pluginPath= Environment.getExternalStorageDirectory()+"/earthgee_plugin1";
        File file=new File(pluginPath);
        if(!file.exists()){
            file.mkdir();
        }
        File[] plugins=file.listFiles();
        if(plugins==null||plugins.length<=0) return;

        for(File plugin:plugins){
            PluginItem item=new PluginItem();
            item.pluginPath=plugin.getAbsolutePath();
            item.packageInfo= PluginUtils.getPackageInfo(this,item.pluginPath);
            if(item.packageInfo.activities!=null&&item.packageInfo.activities.length>0){
                item.launcherActivityName=item.packageInfo.activities[0].name;
            }
            if(item.packageInfo.services!=null&&item.packageInfo.services.length>0){
                item.launcherServiceName=item.packageInfo.services[0].name;
            }
            mPluginItems.add(item);
            PluginManager.getInstance(this).loadApk(item.pluginPath);
        }

        PackageManager pm=getPackageManager();
        String[] strs=new String[mPluginItems.size()];
        for(int i=0;i<strs.length;i++){
            PluginItem item=mPluginItems.get(i);
            PackageInfo packageInfo=item.packageInfo;

            ApplicationInfo applicationInfo=packageInfo.applicationInfo;
            applicationInfo.sourceDir=item.pluginPath;
            applicationInfo.publicSourceDir=item.pluginPath;
            strs[i]=pm.getApplicationLabel(applicationInfo).toString();
        }

        mPluginList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strs));
    }

    public static class PluginItem{
        public PackageInfo packageInfo;
        public String pluginPath;
        public String launcherActivityName;
        public String launcherServiceName;

        public PluginItem(){}

    }

}
