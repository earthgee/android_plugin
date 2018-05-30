package com.earthgee.bundle.framework;

import com.earthgee.bundle.framework.storage.Archive;
import com.earthgee.bundle.framework.storage.BundleArchive;
import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhaoruixuan on 2018/4/27.
 */
public class BundleImpl implements Bundle{
    static final Logger log;
    //组件内容
    Archive archive;
    //组件id
    final long bundleID;
    //组件放置地址
    final File bundleDir;
    //包名
    final String location;
    //解包
    volatile boolean isOpt;

    static {
        log = LoggerFactory.getLogcatLogger("BundleImpl");
    }

    BundleImpl(File bundleDir) throws Exception{
        this.isOpt=false;
        DataInputStream dataInputStream=new DataInputStream(new FileInputStream(new File(bundleDir,"meta")));
        this.bundleID=dataInputStream.readLong();
        this.location=dataInputStream.readUTF();

        dataInputStream.close();

        this.bundleDir=bundleDir;
        try{
            this.archive=new BundleArchive(bundleDir);
            Framework.bundles.put(this.location,this);
        }catch (Exception e){

        }
    }

    BundleImpl(File bundleDir,String location,long bundleId,InputStream inputStream) throws BundleException{
        this.isOpt=false;
        this.bundleID=bundleId;
        this.location=location;
        this.bundleDir=bundleDir;
        this.bundleDir.mkdir();
        if(inputStream==null){
            throw new BundleException("Arg InputStream is null.Bundle:"+location);
        }else{
            try {
                this.archive = new BundleArchive(bundleDir, inputStream);
            } catch (Exception e) {
                Framework.deleteDirectory(bundleDir);
                throw new BundleException("Can not install bundle " + location, e);
            }
        }
        this.updateMetadata();
        Framework.bundles.put(location, this);
    }

    @Override
    public Long getBundleId() {
        return null;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void update(InputStream inputStream) throws BundleException {

    }

    void updateMetadata(){
        File file=new File(this.bundleDir,"meta");
        DataOutputStream dataOutputStream;
        try{
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            dataOutputStream=new DataOutputStream(fileOutputStream);
            dataOutputStream.writeLong(this.bundleID);
            dataOutputStream.writeUTF(this.location);

            dataOutputStream.flush();
            fileOutputStream.getFD().sync();
            if (dataOutputStream != null)
                try {
                    dataOutputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        }catch (Throwable e) {
            log.log("Could not save meta data " + file.getAbsolutePath(), Logger.LogLevel.ERROR, e);
        }
    }

    public Archive getArchive(){
        return this.archive;
    }

    public synchronized void optDexFile() throws Exception{
        if(!isOpt){
            long startTime=System.currentTimeMillis();
            getArchive().optDexFile();
            isOpt=true;
            log.log("执行："+getLocation()+",时间----"+String.valueOf(System.currentTimeMillis()-startTime),
                    Logger.LogLevel.ERROR);
        }
    }

}










