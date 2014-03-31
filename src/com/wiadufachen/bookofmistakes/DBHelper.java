package com.wiadufachen.bookofmistakes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by win7 on 2014-03-26.
 */
public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "bom.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS questions" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR, answer VARCHAR, solution VARCHAR, category INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS categories" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS questions");
        db.execSQL("DROP TABLE IF EXISTS categories");
        onCreate(db);
    }
}
