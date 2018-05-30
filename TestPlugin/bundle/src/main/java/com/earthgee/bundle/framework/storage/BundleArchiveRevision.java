package com.earthgee.bundle.framework.storage;

import com.earthgee.bundle.loader.BundlePathLoader;
import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;
import com.earthgee.bundle.runtime.RuntimeArgs;
import com.earthgee.bundle.util.ApkUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2018/4/28.
 * 采用PathClassLoader 加载dex文件，并opt释放优化后的dex
 */
public class BundleArchiveRevision {

    private final long revisionNum;
    private File revisionDir;
    private File bundleFile;
    static final String BUNDLE_FILE_NAME="bundle.zip";
    static final String BUNDLE_DEX_FILE="bundle.dex";
    static final String FILE_PROTOCOL="file:";
    private String revisionLocation;

    static final Logger log;
    static {
        log = LoggerFactory.getLogcatLogger("BundleArchiveRevision");
    }

    BundleArchiveRevision(long revisionNumber,File file) throws IOException{
        File fileMeta=new File(file,"meta");
        if(fileMeta.exists()){
            DataInputStream dataInputStream=new DataInputStream(new FileInputStream(fileMeta));
            this.revisionLocation=dataInputStream.readUTF();
            dataInputStream.close();
            this.revisionNum=revisionNumber;
            this.revisionDir=file;
            if(!this.revisionDir.exists()){
                this.revisionDir.mkdirs();
            }
            this.bundleFile=new File(file,BUNDLE_FILE_NAME);
            return;
        }
        throw new IOException();
    }

    BundleArchiveRevision(long revisionNumber, File file, InputStream inputStream) throws IOException{
        this.revisionNum=revisionNumber;
        this.revisionDir=file;
        if(!this.revisionDir.exists()){
            this.revisionDir.mkdirs();
        }
        this.revisionLocation=FILE_PROTOCOL;
        this.bundleFile=new File(file,BUNDLE_FILE_NAME);
        ApkUtil.copyInputStreamToFile(inputStream, this.bundleFile);
        updateMetaData();
    }

    void updateMetaData() throws IOException {

        File file = new File(this.revisionDir, "meta");
        DataOutputStream dataOutputStream = null;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            dataOutputStream = new DataOutputStream(new FileOutputStream(file));
            dataOutputStream.writeUTF(this.revisionLocation);
            dataOutputStream.flush();

        } catch (IOException ex) {
            throw new IOException("Can not save meta data " + file.getAbsolutePath());
        } finally {
            if (dataOutputStream != null) dataOutputStream.close();
        }
    }

    public void optDexFile() throws Exception{
        List<File> files=new ArrayList<>();
        files.add(this.bundleFile);
        BundlePathLoader.installBundleDexs(RuntimeArgs.androidApplication.getClassLoader(),
                revisionDir,files,false);
    }

    public File getRevisionDir(){
        return this.revisionDir;
    }

    public File getRevisionFile(){
        return this.bundleFile;
    }

}










