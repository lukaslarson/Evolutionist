package com.luklar9.assignment4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "highScoreList";
    private static final String TABLE_HIGHSCORES = "highScores";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SCORE = "score";

    public DatabaseHandler(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HIGHSCORE_TABLE = "CREATE TABLE " + TABLE_HIGHSCORES + "(" + KEY_ID
                + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_SCORE + " INTEGER" + ")";
        db.execSQL(CREATE_HIGHSCORE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIGHSCORES);
        onCreate(db);
    }

    public void addHighScore(HighScore h) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, h.getName());
        values.put(KEY_SCORE, h.getScore());
        db.insert(TABLE_HIGHSCORES, null, values);
        db.close();
    }

    /* not used
    HighScore getHighScore(int i) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(
                TABLE_HIGHSCORES,
                new String[]{KEY_ID, KEY_NAME, KEY_SCORE},
                KEY_ID + "=?", new String[]{String.valueOf(i)},
                null, null, null, null);

        if (c != null) {
            c.moveToFirst();
        }
        return new HighScore(Integer.parseInt(c.getString(0)), c.getString(1), c.getInt(2));
    } */

    /* not used
    public List<HighScore> getAllHighScores() {
        List<HighScore> highScoreList = new ArrayList<HighScore>();

        String selectQuery = "SELECT * FROM " + TABLE_HIGHSCORES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                HighScore h = new HighScore();
                h.setId(Integer.parseInt(c.getString(0)));
                h.setName(c.getString(1));
                h.setScore(c.getInt(2));

                highScoreList.add(h);

            } while (c.moveToNext());
        }
        return highScoreList;

    }*/

    public List<HighScore> getSortedHighScores() {
        List<HighScore> highScoreList = new ArrayList<HighScore>();

        String selectQuery = "SELECT * FROM " + TABLE_HIGHSCORES + " ORDER BY " + KEY_SCORE + " ASC LIMIT 0, 5";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                HighScore h = new HighScore();
                h.setId(Integer.parseInt(c.getString(0)));
                h.setName(c.getString(1));
                h.setScore(Integer.parseInt(c.getString(2)));

                highScoreList.add(h);

            } while (c.moveToNext());
        }
        return highScoreList;

    }

    /* not used
    public int updateHighScore(HighScore h) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, h.getName());
        values.put(KEY_SCORE, h.getScore());

        return db.update(TABLE_HIGHSCORES, values,
                KEY_ID + " = ?",
                new String[]{String.valueOf(h.getId())});

    } */

    /* not used
    public void deleteHighScore(HighScore h) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HIGHSCORES, KEY_ID + " = ?",
                new String[]{String.valueOf(h.getId())});
        db.close();
    } */

    /* not used
    public int getHighScoreCount() {
        String countQuery = "SELECT * FROM " + TABLE_HIGHSCORES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(countQuery, null);
        return c.getCount();
    } */

}
