package com.wiadufachen.bookofmistakes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2014-03-27.
 */
public class CategoryDao {

    private DBHelper helper;

    CategoryDao(Context context) {
        helper = new DBHelper(context);
    }

    public void delete(Integer... ids) {
        String[] c = new String[ids.length];
        StringBuffer sb = new StringBuffer();
        if (ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                sb.append('?').append(',');
                c[i] = ids[i].toString();
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("categories", "id in (" + sb.toString() + ")", c);
        db.close();
    }

    public void save(Category question) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into categories (name) values(?)", new Object[] {
                question.getName()});
        db.close();
    }

    public Category find(Integer id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from categories where id=?",
                new String[] { String.valueOf(id) });
        if (cursor.moveToNext()) {
            return new Category(cursor.getInt(0), cursor.getString(1));
        }
        return null;
    }

    public List<Category> getAll() {
        List<Category> persons = new ArrayList<Category>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from categories", null);
        while (cursor.moveToNext()) {
            persons.add(new Category(cursor.getInt(0), cursor.getString(1)));
        }
        return persons;
    }
}
