package com.wiadufachen.bookofmistakes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2014-03-26.
 */
public class QuestionDao {
    private DBHelper helper;

    QuestionDao(Context context) {
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
        db.delete("questions", "id in (" + sb.toString() + ")", c);
        db.close();
    }

    public void deleteByCategory(Integer... ids) {
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
        db.delete("questions", "category in (" + sb.toString() + ")", c);
        db.close();
    }

    public int save(Question question) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", question.getTitle());
        values.put("answer", question.getAnswer());
        values.put("solution", question.getSolution());
        values.put("category", question.getCategory());
        long rowid = db.insert("questions", null, values);
        Cursor cursor = db.rawQuery("select * from questions where rowid=?",
                new String[] { String.valueOf(rowid) });
        if (cursor.moveToNext()) {
            return cursor.getInt(0);
        }
        return 0;
        /*
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into questions (title,answer,solution,category) values(?,?,?,?)", new Object[] {
                question.getTitle(), question.getAnswer(), question.getSolution(), question.getCategory()});
        db.close();*/
    }

    public void update(Question question) {
        SQLiteDatabase db =helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", question.getTitle());
        cv.put("answer", question.getAnswer());
        cv.put("solution", question.getSolution());
        cv.put("category", question.getCategory());
        String[] args = {String.valueOf(question.getId())};
        db.update("questions", cv, "id=?",args);
    }

    public Question find(Integer id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from questions where id=?",
                new String[] { String.valueOf(id) });
        if (cursor.moveToNext()) {
            return new Question(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2),cursor.getString(3), cursor.getInt(4));
        }
        return null;
    }

    public List<Question> getAll() {
        List<Question> persons = new ArrayList<Question>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from questions", null);
        while (cursor.moveToNext()) {
            persons.add(new Question(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2),cursor.getString(3), cursor.getInt(4)));
        }
        return persons;
    }

    public List<Question> findByCategoryIdAndFilter(Integer category,String filter) {
        List<Question> persons = new ArrayList<Question>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from questions where category=? and title like ?",  new String[] { String.valueOf(category) ,"%"
         + filter + "%"});
        while (cursor.moveToNext()) {
            persons.add(new Question(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2),cursor.getString(3), cursor.getInt(4)));
        }
        return persons;
    }
}
