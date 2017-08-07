package com.earthgee.plugin.contentProvider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * Created by zhaoruixuan on 2017/8/7.
 */
public class ArticlesProvider extends ContentProvider{

    private static final String DB_NAME="Articles.db";
    private static final String DB_TABLE="ArticlesTable";
    private static final int DB_VERSION=1;
//
//    private static final String DB_CREATE="create table "+DB_NAME+" ("
//            +Articles.ID+" integer primary key autoincrement, "+Articles.TITLE+
//            " text not null, "+Articles.ABSTRACT+" text not null, "+
//            Articles.URL+" text not null);";

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Articles.AUTHORITY,"item",Articles.ITEM);
        uriMatcher.addURI(Articles.AUTHORITY,"item/#",Articles.ITEM_ID);
        uriMatcher.addURI(Articles.AUTHORITY,"pos/#",Articles.ITEM_POS);
    }

    private static final HashMap<String,String> articleProjectionMap;

    static {
        articleProjectionMap=new HashMap<>();
        articleProjectionMap.put(Articles.ID,Articles.ID);
        articleProjectionMap.put(Articles.TITLE,Articles.TITLE);
        articleProjectionMap.put(Articles.ABSTRACT,Articles.ABSTRACT);
        articleProjectionMap.put(Articles.URL,Articles.URL);
    }

    private DBHelper dbHelper;
    private ContentResolver resolver;

    @Override
    public boolean onCreate() {
        Context context=getContext();
        resolver=context.getContentResolver();
        dbHelper=new DBHelper(context,DB_NAME,null,DB_VERSION);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case Articles.ITEM:
                return Articles.CONTENT_TYPE;
            case Articles.ITEM_ID:
            case Articles.ITEM_POS:
                return Articles.CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(uriMatcher.match(uri)!=Articles.ITEM){
            throw new IllegalArgumentException("Error uri: "+uri);
        }

        SQLiteDatabase db=dbHelper.getWritableDatabase();

        long id=db.insert(DB_TABLE,Articles.ID,values);

        Uri newUri= ContentUris.withAppendedId(uri,id);
        resolver.notifyChange(newUri,null);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        int count=0;

        switch (uriMatcher.match(uri)){
            case Articles.ITEM:{
                count=db.delete(DB_TABLE,selection,selectionArgs);
                break;
            }
            case Articles.ITEM_ID:{
                String id=uri.getPathSegments().get(1);
                count=db.delete(DB_TABLE,Articles.ID+"="+id+(!TextUtils.isEmpty(selection)?" and ("+selection+")":""),selectionArgs);
                break;
            }
        }

        resolver.notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        int count=0;

        switch (uriMatcher.match(uri)){
            case Articles.ITEM:{
                count=db.update(DB_TABLE,values,selection,selectionArgs);
                break;
            }
            case Articles.ITEM_ID:{
                String id=uri.getPathSegments().get(1);
                count=db.update(DB_TABLE,values,Articles.ID+"="+id+(!TextUtils.isEmpty(selection)?" and ("+selection+')':""),selectionArgs);
                break;
            }
        }

        resolver.notifyChange(uri,null);
        return count;
    }
}

















