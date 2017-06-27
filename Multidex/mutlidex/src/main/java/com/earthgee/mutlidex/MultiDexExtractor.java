package com.earthgee.mutlidex;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by zhaoruixuan on 2017/6/21.
 */
public class MultiDexExtractor {

    private static final String PREFS_FILE="multidex.version";
    private static final String KEY_TIME_STAMP="timestamp";
    private static final String KEY_CRC="crc";
    private static final long NO_VALUE=-1L;
    private static final String KEY_DEX_NUMBER="dex.number";

    private static final String DEX_PREFIX="classes";
    private static final String DEX_SUFFIX=".dex";

    private static final String EXTRACTED_NAME_EXT=".classes";
    private static final String EXTRACTED_SUFFIX=".zip";
    private static final int MAX_EXTRACT_ATTEMPTS=3;

    private static final int BUFFER_SIZE=0x4000;

    static List<File> load(Context context, ApplicationInfo applicationInfo,File dexDir,boolean forceReload) throws IOException{
        File sourceApk=new File(applicationInfo.sourceDir);
        long currentCrc=getZipCrc(sourceApk);
        List files;
        if(!forceReload&&!isModified(context,sourceApk,currentCrc)){
            try{
                files=loadExistingExtraction(context,sourceApk,dexDir);
            }catch (IOException e){
                files=performExtractions(sourceApk,dexDir);
                putStoreApkInfo(context,getTimeStamp(sourceApk),currentCrc,files.size()+1);
            }
        }else{
            files=performExtractions(sourceApk,dexDir);
            putStoreApkInfo(context,getTimeStamp(sourceApk),currentCrc,files.size()+1);
        }
        return files;
    }

    private static void putStoreApkInfo(Context context,long timeStamp,long crc,int totalDexNumber){
        SharedPreferences prefs=getMultiDexPreferences(context);
        SharedPreferences.Editor edit=prefs.edit();
        edit.putLong(KEY_TIME_STAMP,timeStamp);
        edit.putLong(KEY_CRC,crc);
        edit.putInt(KEY_DEX_NUMBER,totalDexNumber);
        apply(edit);
    }

    private static long getZipCrc(File archive) throws IOException{
        long computedValue=ZipUtil.getZipCrc(archive);
        if(computedValue==-1L){
            --computedValue;
        }
        return computedValue;
    }

    private static boolean isModified(Context context,File archive,long currentCrc){
        SharedPreferences prefs=getMultiDexPreferences(context);
        return (prefs.getLong(KEY_TIME_STAMP,NO_VALUE)!=getTimeStamp(archive))||(prefs.getLong(KEY_CRC,NO_VALUE)!=currentCrc);
    }

    private static long getTimeStamp(File archive){
        long timeStamp=archive.lastModified();
        if(timeStamp==NO_VALUE){
            timeStamp--;
        }
        return timeStamp;
    }

    private static SharedPreferences getMultiDexPreferences(Context context){
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT<11?Context.MODE_PRIVATE:Context.MODE_PRIVATE|0x0004);
    }

    private static List<File> loadExistingExtraction(Context context,File sourceApk,File dexDir) throws IOException{
        final String extractedFilePrefix=sourceApk.getName()+EXTRACTED_NAME_EXT;
        int totalDexNumber=getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER,1);
        final List<File> files=new ArrayList<>(totalDexNumber);

        for(int secondaryNumber=2;secondaryNumber<=totalDexNumber;secondaryNumber++){
            String fileName=extractedFilePrefix+secondaryNumber+EXTRACTED_SUFFIX;
            File extractedFile=new File(dexDir,fileName);
            if(extractedFile.isFile()){
                files.add(extractedFile);
                if(!verifyZipFile(extractedFile)){
                    throw new IOException("Invalid ZIP file");
                }
            }else{
                throw new IOException("Missing extracted secondary dex file '"+extractedFile.getPath()+"'");
            }
        }

        return files;
    }

    static boolean verifyZipFile(File file){
        try{
            ZipFile zipFile=new ZipFile(file);
            try{
                zipFile.close();
                return true;
            }catch (IOException e){
            }
        }catch (ZipException e){
        }catch (IOException e){
        }

        return false;
    }

    private static List<File> performExtractions(File sourceApk,File dexDir) throws IOException{
        final String extractedFilePrefix=sourceApk.getName()+EXTRACTED_NAME_EXT;
        prepareDexDir(dexDir,extractedFilePrefix);

        List<File> files=new ArrayList<>();

        final ZipFile apk=new ZipFile(sourceApk);
        try{
            int secondaryNumber=2;

            ZipEntry dexFile=apk.getEntry(DEX_PREFIX+secondaryNumber+DEX_SUFFIX);
            while (dexFile!=null){
                String fileName=extractedFilePrefix+secondaryNumber+EXTRACTED_SUFFIX;
                File extractedFile=new File(dexDir,fileName);
                files.add(extractedFile);

                int numAttempts=0;
                boolean isExtractionSuccessful=false;
                while (numAttempts<MAX_EXTRACT_ATTEMPTS&&!isExtractionSuccessful){
                    numAttempts++;

                    extract(apk,dexFile,extractedFile,extractedFilePrefix);

                    isExtractionSuccessful=verifyZipFile(extractedFile);

                    if(!isExtractionSuccessful){
                        extractedFile.delete();
                        if(extractedFile.exists()){
                        }
                    }
                }
                if(!isExtractionSuccessful){
                    throw new IOException("Could not create Zip File "+extractedFile.getAbsolutePath()+" for secondary dex ("+
                        secondaryNumber+")");
                }
                secondaryNumber++;
                dexFile=apk.getEntry(DEX_PREFIX+secondaryNumber+DEX_SUFFIX);
            }
        }finally {
            try{
                apk.close();
            }catch (IOException e){
            }
        }

        return files;
    }

    private static void prepareDexDir(File dexDir,final String extractedFilePrefix){
        FileFilter filter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.getName().startsWith(extractedFilePrefix);
            }
        };
        File[] files=dexDir.listFiles(filter);
        if(files==null){
            return;
        }
        for(File oldFile:files){
            oldFile.delete();
        }
    }

    private static void extract(ZipFile apk,ZipEntry dexFile,File extractTo,String extractedFilePrefix)
            throws IOException,FileNotFoundException{
        InputStream in=apk.getInputStream(dexFile);
        ZipOutputStream out=null;
        File tmp=File.createTempFile(extractedFilePrefix,EXTRACTED_SUFFIX,extractTo.getParentFile());
        try{
            out=new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
            try{
                ZipEntry classesDex=new ZipEntry("classes.dex");
                classesDex.setTime(dexFile.getTime());
                out.putNextEntry(classesDex);

                byte[] buffer=new byte[BUFFER_SIZE];
                int length=in.read(buffer);
                while (length!=-1){
                    out.write(buffer,0,length);
                    length=in.read(buffer);
                }
                out.closeEntry();
            }finally {
                out.close();
            }
            tmp.renameTo(extractTo);
        }finally {
            closeQuitely(in);
            tmp.delete();
        }
    }

    private static void closeQuitely(Closeable closeable){
        try{
            closeable.close();
        }catch (IOException e){
        }
    }

    private static Method sApplyMethod;
    static {
        try{
            Class<?> cls=SharedPreferences.Editor.class;
            sApplyMethod=cls.getMethod("apply");
        }catch (NoSuchMethodException unused){
            sApplyMethod=null;
        }
    }

    private static void apply(SharedPreferences.Editor editor){
        if(sApplyMethod!=null){
            try{
                sApplyMethod.invoke(editor);
            }catch (InvocationTargetException unused){
            }catch (IllegalAccessException unused){
            }
        }
    }

}


















