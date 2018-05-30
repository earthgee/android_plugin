package com.earthgee.bundle.hotpatch;

import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;
import com.earthgee.bundle.runtime.RuntimeArgs;

import java.io.File;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Created by zhaoruixuan on 2018/4/27.
 */
public class HotPatchManager {

    private static volatile HotPatchManager instance;
    private static final Logger log;
    private File patchDir;
    private static TreeMap<Integer, HotPatchItem> sortedMap;

    static {
        log= LoggerFactory.getLogcatLogger("HotPatchManager");
    }

    private HotPatchManager(){
        File baseFile= RuntimeArgs.androidApplication.getFilesDir();
        patchDir=new File(baseFile,"hotpatch");
        sortedMap = new TreeMap(new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs.compareTo(lhs);
            }
        });
    }

    public static HotPatchManager getInstance(){
        if(instance==null){
            synchronized (HotPatchManager.class){
                if(instance==null){
                    instance=new HotPatchManager();
                }
            }
        }
        return instance;
    }

    public void purge() {
        if (patchDir.exists())
            deleteDirectory(patchDir);
    }

    private void deleteDirectory(File file) {
        try {
            File[] listFiles = file.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isDirectory()) {
                    deleteDirectory(listFiles[i]);
                } else {
                    listFiles[i].delete();
                }
            }
            file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}










