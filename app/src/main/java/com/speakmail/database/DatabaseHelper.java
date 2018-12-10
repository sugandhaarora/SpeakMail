package com.speakmail.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.speakmail.database.model.Draft;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "drafts_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Draft.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Draft.TABLE_NAME);
        onCreate(db);
    }

    public long insertDraft(String from, String to, String subject, String message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Draft.COLUMN_FROM, from);
        values.put(Draft.COLUMN_TO, to);
        values.put(Draft.COLUMN_SUBJECT, subject);
        values.put(Draft.COLUMN_MESSAGE, message);

        long id = db.insert(Draft.TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public Draft getDraft(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Draft.TABLE_NAME,
                new String[]{Draft.COLUMN_ID, Draft.COLUMN_FROM, Draft.COLUMN_TO, Draft.COLUMN_SUBJECT, Draft.COLUMN_MESSAGE},
                Draft.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Draft draft = new Draft(
                cursor.getInt(cursor.getColumnIndex(Draft.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Draft.COLUMN_FROM)),
                cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TO)),
                cursor.getString(cursor.getColumnIndex(Draft.COLUMN_SUBJECT)),
                cursor.getString(cursor.getColumnIndex(Draft.COLUMN_MESSAGE)));

        cursor.close();

        return draft;
    }


    public Draft searchDraft(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + Draft.TABLE_NAME + " WHERE "
                + Draft.COLUMN_SUBJECT + " LIKE  '%" + keyword + "%'", null);
        Draft draft = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                draft = new Draft(
                        cursor.getInt(cursor.getColumnIndex(Draft.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Draft.COLUMN_FROM)),
                        cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TO)),
                        cursor.getString(cursor.getColumnIndex(Draft.COLUMN_SUBJECT)),
                        cursor.getString(cursor.getColumnIndex(Draft.COLUMN_MESSAGE)));
            }
            cursor.close();
        } else {
            Log.d("DATABASE HELPER", "searchDraft: CURSOR NULL");
        }
        return draft;
    }

    public List<Draft> getAllDrafts(String currentUser) {
        List<Draft> drafts = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Draft.TABLE_NAME +
                " WHERE " + Draft.COLUMN_FROM + " LIKE '" + currentUser + "' ORDER BY " +
                Draft.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Draft draft = new Draft();
                draft.setId(cursor.getInt(cursor.getColumnIndex(Draft.COLUMN_ID)));
                draft.setTo(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_FROM)));
                draft.setTo(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TO)));
                draft.setSubject(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_SUBJECT)));
                draft.setMessage(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_MESSAGE)));
                drafts.add(draft);
            } while (cursor.moveToNext());
        }

        db.close();

        return drafts;
    }

    public int getDraftsCount(String currentUser) {
        String countQuery = "SELECT  * FROM " + Draft.TABLE_NAME + " WHERE " + Draft.COLUMN_FROM + " LIKE '" + currentUser + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public int updateDraft(Draft draft) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Draft.COLUMN_FROM, draft.getFrom());
        values.put(Draft.COLUMN_TO, draft.getTo());
        values.put(Draft.COLUMN_SUBJECT, draft.getSubject());
        values.put(Draft.COLUMN_MESSAGE, draft.getMessage());

        // updating row
        return db.update(Draft.TABLE_NAME, values, Draft.COLUMN_ID + " = ?",
                new String[]{String.valueOf(draft.getId())});
    }

    public void deleteDraft(Draft draft) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Draft.TABLE_NAME, Draft.COLUMN_ID + " = ?",
                new String[]{String.valueOf(draft.getId())});
        db.close();
    }

    public void deleteAllDrafts(String currentUser) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(Draft.TABLE_NAME, Draft.COLUMN_FROM + " LIKE '" + currentUser + "'", null);
        db.close();
    }
}
