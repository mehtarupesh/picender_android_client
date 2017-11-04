package com.example.rupesh.mastread;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by rupesh on 6/21/17.
 */
public class MRDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "MastRead.db";
    private final String TAG = "MRDbHelper";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE VIRTUAL TABLE " + MRDbContract.MRDbEntry.TABLE_NAME + " USING fts4(" +
                    MRDbContract.MRDbEntry._ID + " INTEGER PRIMARY KEY," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID + " TEXT," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER + " INTEGER," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT + " TEXT," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH + " TEXT UNIQUE," + /* Use json path as unique identifer which is ignored in fts; useful in normal */
                    MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH + " TEXT," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH + " TEXT," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_BOARD + " TEXT," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_MEDIUM + " TEXT," +
                    MRDbContract.MRDbEntry.COLUMN_NAME_GRADE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MRDbContract.MRDbEntry.TABLE_NAME;

    public static final String SQL_SEARCH_STRING =
            "SELECT * FROM " +  MRDbContract.MRDbEntry.TABLE_NAME + " WHERE " + MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT + " MATCH ?";

    public MRDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        Log.d(TAG, "oldVersion" + oldVersion);
        Log.d(TAG, "newVersion" + newVersion);
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onDowngrade");
        Log.d(TAG, "oldVersion" + oldVersion);
        Log.d(TAG, "newVersion" + newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }

}
