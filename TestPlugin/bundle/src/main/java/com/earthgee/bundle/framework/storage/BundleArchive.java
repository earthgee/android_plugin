package com.earthgee.bundle.framework.storage;

import com.earthgee.bundle.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

/**
 * Created by zhaoruixuan on 2018/4/28.
 * 打包在apk中的组件转换到内部磁盘存储上
 */
public class BundleArchive implements Archive{

    private static final String REVISION_DICTORY="version";
    private static final Long BEGIN_VERSION=1L;
    private final TreeMap<Long,BundleArchiveRevision> revisionTreeMap;

    private File bundleDir;
    private final BundleArchiveRevision currentRevision;

    public BundleArchive(File file) throws IOException{
        this.revisionTreeMap=new TreeMap<>();
        String[] lists=file.list();
        if(lists!=null){
            for(String str:lists){
                if(str.startsWith(REVISION_DICTORY)){
                    Long parseLong=Long.parseLong(StringUtil.subStringAfter(str,"_"));
                    if (parseLong > 0) {
                        this.revisionTreeMap.put(parseLong, null);
                    }
                }
            }
        }
        if (revisionTreeMap.isEmpty()) {
            throw new IOException("No Valid revisions in bundle archive directory");
        }
        this.bundleDir = file;
        long longValue = this.revisionTreeMap.lastKey();
        BundleArchiveRevision bundleArchiveRevision=new BundleArchiveRevision(longValue,
                new File(file,REVISION_DICTORY+"_"+String.valueOf(longValue)));
        this.revisionTreeMap.put(longValue, bundleArchiveRevision);
        this.currentRevision = bundleArchiveRevision;
    }

    public BundleArchive(File file, InputStream inputStream) throws IOException {
        this.revisionTreeMap = new TreeMap();
        this.bundleDir = file;
        BundleArchiveRevision bundleArchiveRevision = new BundleArchiveRevision(
                BEGIN_VERSION, new File(file, REVISION_DICTORY + "_" +
                String.valueOf(BEGIN_VERSION)), inputStream);
        this.revisionTreeMap.put(BEGIN_VERSION, bundleArchiveRevision);
        this.currentRevision = bundleArchiveRevision;
    }

    @Override
    public void close() {

    }

    @Override
    public File getArchiveFile() {
        return this.currentRevision.getRevisionFile();
    }

    @Override
    public BundleArchiveRevision getCurrentRevision() {
        return null;
    }

    @Override
    public boolean isBundleInstalled() {
        return false;
    }

    @Override
    public boolean isDexOpted() {
        return false;
    }

    @Override
    public void optDexFile() throws Exception {
        this.currentRevision.optDexFile();
    }

    @Override
    public void purge() throws Exception {

    }

    @Override
    public BundleArchiveRevision newRevision(File storageFile, InputStream inputStream) throws IOException {
        return null;
    }

    @Override
    public InputStream openAssetInputStream(String fileName) throws IOException {
        return null;
    }

    @Override
    public InputStream openNonAssetInputStream(String fileName) throws IOException {
        return null;
    }
}
