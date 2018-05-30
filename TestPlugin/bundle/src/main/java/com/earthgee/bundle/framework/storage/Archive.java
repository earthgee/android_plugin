package com.earthgee.bundle.framework.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

/**
 * Created by zhaoruixuan on 2018/4/28.
 */
public interface Archive {

    void close();

    File getArchiveFile();

    BundleArchiveRevision getCurrentRevision();

    boolean isBundleInstalled();

    boolean isDexOpted();

    void optDexFile() throws Exception;

    void purge() throws Exception;

    BundleArchiveRevision newRevision(File storageFile, InputStream inputStream) throws IOException;

    InputStream openAssetInputStream(String fileName) throws IOException;

    InputStream openNonAssetInputStream(String fileName) throws IOException;

}









