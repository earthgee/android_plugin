/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\android_wp\\android_plugin\\Plugin2\\library\\src\\main\\aidl\\com\\earthgee\\library\\IPackageDataObserver.aidl
 */
package com.earthgee.library;
// Declare any non-default types here with import statements

public interface IPackageDataObserver extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.earthgee.library.IPackageDataObserver
{
private static final java.lang.String DESCRIPTOR = "com.earthgee.library.IPackageDataObserver";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.earthgee.library.IPackageDataObserver interface,
 * generating a proxy if needed.
 */
public static com.earthgee.library.IPackageDataObserver asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.earthgee.library.IPackageDataObserver))) {
return ((com.earthgee.library.IPackageDataObserver)iin);
}
return new com.earthgee.library.IPackageDataObserver.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onRemoveCompleted:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
this.onRemoveCompleted(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.earthgee.library.IPackageDataObserver
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onRemoveCompleted(java.lang.String packageName, boolean succeeded) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeInt(((succeeded)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_onRemoveCompleted, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onRemoveCompleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onRemoveCompleted(java.lang.String packageName, boolean succeeded) throws android.os.RemoteException;
}
