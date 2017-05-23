// IApplicationCallback.aidl
package com.earthgee.library;

// Declare any non-default types here with import statements

interface IApplicationCallback {

    //回调方法
    Bundle onCallback(in Bundle extra);

}
