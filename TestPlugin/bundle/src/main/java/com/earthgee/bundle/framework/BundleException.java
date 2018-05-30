package com.earthgee.bundle.framework;

/**
 * Created by zhaoruixuan on 2018/4/26.
 */
public class BundleException extends Exception{

    private transient Throwable throwable;

    public BundleException(String str, Throwable th) {
        super(str);
        this.throwable = th;
    }

    public BundleException(String str) {
        super(str);
        this.throwable = null;
    }

    public Throwable getNestedException() {
        return this.throwable;
    }

}
