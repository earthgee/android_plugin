package com.earthgee.bundle.framework;

import java.io.InputStream;

/**
 * Created by zhaoruixuan on 2018/4/27.
 */
public interface Bundle {
    Long getBundleId();
    String getLocation();
    int getState();
    void update(InputStream inputStream) throws BundleException;
}
