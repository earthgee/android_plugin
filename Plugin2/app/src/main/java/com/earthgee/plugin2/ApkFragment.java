package com.earthgee.plugin2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

import java.io.File;
import java.io.Flushable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/11.
 */
public class ApkFragment extends ListFragment implements ServiceConnection{

    private ArrayAdapter<ApkItem> adapter;
    final Handler handler=new Handler();

    public ApkFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

                TextView version= (TextView) convertView.findViewById(R.id.textView2);
                version.setText(String.format("%s(%s)",item.versionName,item.versionCode));

                TextView btn3= (TextView) convertView.findViewById(R.id.button3);
                btn3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onListItemClick(getListView(),v,position,getItemId(position));
                    }
                });
                TextView btn= (TextView) convertView.findViewById(R.id.button2);
                try{
                    if(item.installing){
                        btn.setText("安装中ing");
                    }else{
                        if(PluginManager.getInstance().isConnected()){
                            btn.setText(PluginManager.getInstance().
                                    getPackageInfo(item.packageInfo.packageName,0)!=null?"已经安装":"安装");
                        }else{
                            btn.setText("等待初始化服务");
                        }
                    }
                }catch (Exception e){
                }

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

    boolean isViewCreated=false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated=true;
        setEmptyText("没有在sdcard找到apk");
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
    public void onDestroyView() {
        isViewCreated=false;
        super.onDestroyView();
    }

    public void setListShown(boolean shown){
        if(isViewCreated){
            super.setListShown(shown);
        }
    }

    private void startLoad(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startLoadInner();
        }else{
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0x1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0x1){
            if(permissions!=null&&permissions.length>0){
                for(int i=0;i<permissions.length;i++){
                    String permission=permissions[i];
                    int grantResult=grantResults[i];
                    if(Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)){
                        if(grantResult==PackageManager.PERMISSION_GRANTED){
                            startLoadInner();
                        }else{
                            Toast.makeText(getActivity(),"没有授权，无法使用",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    private void startLoadInner(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                setListShown(true);
            }
        });
        if(!isViewCreated){
            return;
        }
        new Thread("ApkScanner"){
            @Override
            public void run() {
                File file= Environment.getExternalStorageDirectory();

                List<File> apks=new ArrayList<File>(10);
                File[] files=file.listFiles();
                if(files!=null){
                    for(File apk:files){
                        if(apk.exists()&&apk.getPath().toLowerCase().endsWith(".apk")){
                            apks.add(apk);
                        }
                    }
                }

                file=new File(Environment.getExternalStorageDirectory(),"earthgee_plugin");
                if(file.exists()&&file.isDirectory()){
                    File[] files1=file.listFiles();
                    if(files!=null){
                        for(File apk:files1){
                            if(apk.exists()&&apk.getPath().toLowerCase().endsWith(".apk")){
                                apks.add(apk);
                            }
                        }
                    }
                }

                PackageManager pm=getActivity().getPackageManager();
                for(final File apk:apks){
                    try{
                        if(apk.exists()&&apk.getPath().toLowerCase().endsWith("apk")){
                            final PackageInfo info=pm.getPackageArchiveInfo(apk.getPath(),0);
                            if(info!=null&&isViewCreated){
                                try{
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.add(new ApkItem(getActivity(),info,apk.getPath()));
                                        }
                                    });
                                }catch (Exception e){
                                }
                            }
                        }
                    }catch (Exception e){
                    }
                }
            }
        }.start();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final ApkItem item=adapter.getItem(position);
        if(v.getId()==R.id.button2){
            if(item.installing){
                return;
            }
            if(!PluginManager.getInstance().isConnected()){
                Toast.makeText(getActivity(),"插件服务正在初始化...",Toast.LENGTH_SHORT).show();
                return;
            }
            try{
                if(PluginManager.getInstance().getPackageInfo(item.packageInfo.packageName,0)!=null){
                    Toast.makeText(getActivity(), "已经安装了，不能再安装", Toast.LENGTH_SHORT).show();
                }else{
                    new Thread(){
                        @Override
                        public void run() {
                            doInstall(item);
                        }
                    }.start();
                }
            }catch (Exception e){
            }
        }else if(v.getId()==R.id.button3){
            doUninstall(item);
        }
    }

    private void doInstall(ApkItem item){
        item.installing=true;

        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        try{
            final int re=PluginManager.getInstance().installPackage(item.apkfile,0);
            item.installing=false;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    switch (re){
                        case PluginManager.INSTALL_FAILED_NO_REQUESTEDPERMISSION:
                            Toast.makeText(getActivity(),"安装失败,文件请求的权限太多",Toast.LENGTH_SHORT).show();
                            break;
                        case INSTALL_FAILED_NOT_SUPPORT_ABI:
                            Toast.makeText(getActivity(),"宿主不支持插件的abi环境，可能宿主运行时为64位，但插件只支持32位",Toast.LENGTH_SHORT).show();
                            break;
                        case INSTALL_SUCCEED:
                            Toast.makeText(getActivity(),"安装完成",Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                            break;
                    }
                }
            });
        }catch (RemoteException e){
        }
    }

    private void doUninstall(final ApkItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("警告，你确定要删除么？");
        builder.setMessage("警告，你确定要删除" + item.title + "么？");
        builder.setNegativeButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new File(item.apkfile).delete();
                adapter.remove(item);
                Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNeutralButton("取消", null);
        builder.show();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        startLoad();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    public void onDestroy() {
        PluginManager.getInstance().removeServiceConnection(this);
        super.onDestroy();
    }
}

















