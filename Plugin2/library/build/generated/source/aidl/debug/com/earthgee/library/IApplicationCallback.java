/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\android_wp\\android_plugin\\Plugin2\\library\\src\\main\\aidl\\com\\earthgee\\library\\IApplicationCallback.aidl
 */
package com.earthgee.library;
// Declare any non-default types here with import statements

public interface IApplicationCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.earthgee.library.IApplicationCallback
{
private static final java.lang.String DESCRIPTOR = "com.earthgee.library.IApplicationCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.earthgee.library.IApplicationCallback interface,
 * generating a proxy if needed.
 */
public static com.earthgee.library.IApplicationCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.earthgee.library.IApplicationCallback))) {
return ((com.earthgee.library.IApplicationCallback)iin);
}
return new com.earthgee.library.IApplicationCallback.Stub.Proxy(obj);
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
case TRANSACTION_onCallback:
{
data.enforceInterface(DESCRIPTOR);
android.os.Bundle _arg0;
if ((0!=data.readInt())) {
_arg0 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
android.os.Bundle _result = this.onCallback(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.earthgee.library.IApplicationCallback
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
@Override public android.os.Bundle onCallback(android.os.Bundle extra) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((extra!=null)) {
_data.writeInt(1);
extra.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onCallback, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_onCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public android.os.Bundle onCallback(android.os.Bundle extra) throws android.os.RemoteException;
}
