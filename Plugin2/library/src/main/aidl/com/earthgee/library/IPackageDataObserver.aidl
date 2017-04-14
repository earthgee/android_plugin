// IPackageDataObserver.aidl
package com.earthgee.library;

// Declare any non-default types here with import statements

interface IPackageDataObserver {

    void onRemoveCompleted(String packageName,boolean succeeded);

}
