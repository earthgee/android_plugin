package com.earthgee.library.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zhaoruixuan on 2017/3/17.
 */
public class SoLibManager {

    private ExecutorService mSoExecutor= Executors.newCachedThreadPool();

    private static SoLibManager sInstance=new SoLibManager();

    private static String sNativeLibDir="";

    private SoLibManager(){

    }

    public static SoLibManager getSoLoader(){
        return sInstance;
    }

    private String getCpuName(){
        try {
            FileReader fr=new FileReader("/proc/cpuinfo");
            BufferedReader br=new BufferedReader(fr);
            String text=br.readLine();
            br.close();
            String[] array=text.split(":\\s+",2);
            if(array.length>=2){
                return array[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getCpuArch(String cpuName){
        String cpuArchitect=Constants.CPU_ARMEABI;
        if(cpuName.toLowerCase().contains("arm")){
            cpuArchitect=Constants.CPU_ARMEABI;
        }else if(cpuName.toLowerCase().contains("x86")){
            cpuArchitect=Constants.CPU_X86;
        }else if(cpuName.toLowerCase().contains("mips")){
            cpuArchitect=Constants.CPU_MIPS;
        }

        return cpuArchitect;
    }

    public void copyPluginSoLib(Context context,String dexPath,
                                String nativeLibDir){
        String cpuName=getCpuName();
        String cpuArchitect=getCpuArch(cpuName);

        sNativeLibDir=nativeLibDir;
        long start=System.currentTimeMillis();

        try {
            ZipFile zipFile=new ZipFile(dexPath);
            Enumeration<? extends ZipEntry> entries=zipFile.entries();
            while (entries.hasMoreElements()){
                ZipEntry zipEntry=entries.nextElement();
                while(zipEntry.isDirectory()){
                    continue;
                }
                String zipEntryName=zipEntry.getName();
                if(zipEntryName.endsWith(".so")&&zipEntryName.contains(cpuArchitect)){
                    final long lastModify=zipEntry.getTime();
                    if(lastModify<=Config.getSoLastModifiedTime(context,zipEntryName)){
                        continue;
                    }
                    mSoExecutor.execute(new CopySoTask(context,zipFile,zipEntry,lastModify));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Long end=System.currentTimeMillis();
        //log记录耗时
    }

    private class CopySoTask implements Runnable{

        private String mSoFileName;
        private ZipFile mZipFile;
        private ZipEntry mZipEntry;
        private Context mContext;
        private long mLastModifyTime;

        public CopySoTask(Context context,ZipFile zipFile,ZipEntry zipEntry,long lastModify){
            mZipFile=zipFile;
            mContext=context;
            mZipEntry=zipEntry;
            mSoFileName=parseSoFileName(zipEntry.getName());
            mLastModifyTime=lastModify;
        }

        private String parseSoFileName(String zipEntryName){
            return zipEntryName.substring(zipEntryName.lastIndexOf("/")+1);
        }

        @Override
        public void run() {
            try {
                writeSoFile2LibDir();
                Config.setSoLastModifiedTime(mContext,mZipEntry.getName(),mLastModifyTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeSoFile2LibDir() throws IOException{
            InputStream is=null;
            FileOutputStream fos=null;
            is=mZipFile.getInputStream(mZipEntry);
            fos=new FileOutputStream(new File(sNativeLibDir,mSoFileName));
            copy(is,fos);
            mZipFile.close();
        }

        public void copy(InputStream is, OutputStream os) throws IOException{
            if(is==null||os==null){
                return;
            }
            BufferedInputStream bis=new BufferedInputStream(is);
            BufferedOutputStream bos=new BufferedOutputStream(os);
            int size=getAvailableSize(bis);
            byte[] buf=new byte[size];
            int i=0;
            while((i=bis.read(buf,0,size))!=-1){
                bos.write(buf,0,i);
            }
            bos.flush();
            bos.close();
            bis.close();
        }

        private int getAvailableSize(InputStream is) throws IOException{
            if(is==null){
                return 0;
            }
            int available=is.available();
            return available<=0?1024:available;
        }

    }

}















