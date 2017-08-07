package com.earthgee.plugin.contentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhaoruixuan on 2017/8/7.
 */
public class DBHelper extends SQLiteOpenHelper{

    private static final String DB_NAME="Articles.db";
    private static final String DB_TABLE="ArticlesTable";
    private static final int DB_VERSION=1;

    private static final String DB_CREATE="create table "+DB_NAME+" ("
            +Articles.ID+" integer primary key autoincrement, "+Articles.TITLE+
            " text not null, "+Articles.ABSTRACT+" text not null, "+
            Articles.URL+" text not null);";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE);
        onCreate(db);
    }
}
