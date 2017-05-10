package com.earthgee.plugin2;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.earthgee.library.pm.PluginManager;

import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/10.
 */
public class InstalledFragment extends ListFragment implements ServiceConnection{

    private ArrayAdapter<ApkItem> adapter;

    final Handler handler=new Handler();

    public InstalledFragment(){
    }

    private void startLoad(){
        new Thread("ApkScanner"){
            @Override
            public void run() {
                try{
                    final List<PackageInfo> infos=PluginManager.getInstance().getInstalledPackages(0);
                    final PackageManager pm=getActivity().getPackageManager();
                    for(final PackageInfo info:infos){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.add(new ApkItem(pm,info,info.applicationInfo.publicSourceDir));
                            }
                        });
                    }
                }catch (RemoteException e){

                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setListShown(true);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ApkItem item=adapter.getItem(position);
        if(v.getId()==R.id.button2){
            PackageManager pm=getActivity().getPackageManager();
            Intent intent=pm.getLaunchIntentForPackage(item.packageInfo.packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if(v.getId()==R.id.button3){
            doUninstall(item);
        }
    }

    private void doUninstall(final ApkItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("警告，你确定要删除么？");
        builder.setMessage("警告，你确定要删除" + item.title + "么？");
        builder.setNegativeButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!PluginManager.getInstance().isConnected()) {
                    Toast.makeText(getActivity(), "服务未连接", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        PluginManager.getInstance().deletePackage(item.packageInfo.packageName, 0);
                        Toast.makeText(getActivity(), "删除完成", Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        builder.setNeutralButton("取消", null);
        builder.show();
    }


    private MyBroadcastReceiver mMyBroadcastReceiver=new MyBroadcastReceiver();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMyBroadcastReceiver.registerReceiver(getActivity());
        adapter=new ArrayAdapter<ApkItem>(getActivity(),0){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView==null){
                    convertView= LayoutInflater.from(getActivity()).inflate(R.layout.apk_item,null);
                }

                ApkItem item=getItem(position);

                ImageView icon= (ImageView) convertView.findViewById(R.id.imageView);
                icon.setImageDrawable(item.icon);

                TextView title= (TextView) convertView.findViewById(R.id.textView1);
                title.setText(item.title);

                final TextView version= (TextView) convertView.findViewById(R.id.textView2);
                version.setText(String.format("%s(%s)",item.versionName,item.versionCode));

                TextView btn= (TextView) convertView.findViewById(R.id.button2);
                btn.setText("打开");
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onListItemClick(getListView(),v,position,getItemId(position));
                    }
                });

                btn= (TextView) convertView.findViewById(R.id.button3);
                btn.setText("卸载");
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onListItemClick(getListView(),v,position,getItemId(position));
                    }
                });

                return convertView;
            }
        };
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("没有安装插件");
        setListAdapter(adapter);
        setListShown(false);
        getListView().setOnItemClickListener(null);

        if(PluginManager.getInstance().isConnected()){
            startLoad();
        }else{
            PluginManager.getInstance().addServiceConnection(this);
        }
    }

    @Override
    public void onDestroy() {
        PluginManager.getInstance().removeServiceConnection(this);
        mMyBroadcastReceiver.unregisterReceiver(getActivity());
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        startLoad();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private class MyBroadcastReceiver extends BroadcastReceiver{

        void registerReceiver(Context con){
            IntentFilter f=new IntentFilter();
            f.addAction(PluginManager.ACTION_PACKAGE_ADDED);
            f.addAction(PluginManager.ACTION_PACKAGE_REMOVED);
            f.addDataScheme("package");
            con.registerReceiver(this,f);
        }

        void unregisterReceiver(Context con){
            con.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

}





















