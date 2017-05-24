package com.earthgee.library.util;

import com.earthgee.library.core.PluginDirHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class Utils {

    private static final String VALID_JAVA_IDENTIFIER="(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern ANDROID_DATA_PATTERN=Pattern.compile(VALID_JAVA_IDENTIFIER);

    public static boolean valideJavaIdentifier(String identifier){
        return ANDROID_DATA_PATTERN.matcher(identifier).matches();
    }

    public static void deleteDir(String file){
        deleteFile(new File(file));
    }

    public static void copyFile(String src,String dst) throws IOException{
        BufferedInputStream in=null;
        BufferedOutputStream out=null;
        try{
            in=new BufferedInputStream(new FileInputStream(src));
            out=new BufferedOutputStream(new FileOutputStream(dst));
            byte[] buffer=new byte[8192];
            int read=0;
            while((read=in.read(buffer))!=-1){
                out.write(buffer,0,read);
            }
        }finally {
            if(in!=null){
                try{
                    in.close();
                }catch (Exception e){
                }
            }
            if(out!=null){
                try{
                    out.close();
                }catch (Exception e){
                }
            }
        }
    }

    private static void deleteFile(File file){
        if(file.isDirectory()){
            File[] files=file.listFiles();
            for(int i=0;i<files.length;i++){
                deleteFile(files[i]);
            }
        }
        file.delete();
    }

    public static void writeToFile(File file,byte[] data) throws IOException{
        FileOutputStream fou=null;
        try{
            fou=new FileOutputStream(file);
            fou.write(data);
        }finally {
            if(fou!=null){
                try{
                    fou.close();
                }catch (IOException e){
                }
            }
        }
    }

    public static byte[] readFromFile(File file) throws IOException {
        FileInputStream fin=null;
        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            fin=new FileInputStream(file);
            byte[] buffer=new byte[8192];
            int read=0;
            while ((read=fin.read(buffer))!=-1){
                out.write(buffer,0,read);
            }
            byte[] data=out.toByteArray();
            out.close();
            return data;
        }finally {
            if(fin!=null){
                try{
                    fin.close();
                }catch (IOException e){
                }
            }
        }
    }

    public static String md5(byte[] data){
        try{
            MessageDigest md=MessageDigest.getInstance("MD5");
            byte[] digest=md.digest(data);
            return toHex(digest);
        }catch (NoSuchAlgorithmException e){
        }
        return null;
    }

    private static final char[] HEX={'0','1','2','3','4','5','6','7',
            '8','9','A','B','C','D','E','F'};

    private static String toHex(byte[] b){
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<b.length;i++){
            int v=b[i];
            builder.append(HEX[(0xF0&v)>>4]);
            builder.append(HEX[0x0F&v]);
        }
        return builder.toString();
    }

}
