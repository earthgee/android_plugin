package com.earthgee.libaray.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhaoruixuan on 2017/5/25.
 */
public class Utils {

    public static void deleteDir(String file) {
        deleteFile(new File(file));
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        }
        file.delete();
    }

    public static void copyFile(String src, String dst) throws IOException {
        BufferedInputStream in = null;
        BufferedOutputStream ou = null;
        try {
            in = new BufferedInputStream(new FileInputStream(src));
            ou = new BufferedOutputStream(new FileOutputStream(dst));
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                ou.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }

            if (ou != null) {
                try {
                    ou.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void writeToFile(File file, byte[] data) throws IOException {
        FileOutputStream fou = null;
        try {
            fou = new FileOutputStream(file);
            fou.write(data);
        } finally {
            if (fou != null) {
                try {
                    fou.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static byte[] readFromFile(File file) throws IOException {
        FileInputStream fin = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            fin = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = fin.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            byte[] data = out.toByteArray();
            out.close();
            return data;
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
