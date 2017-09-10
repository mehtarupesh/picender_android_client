package com.example.rupesh.mastread;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by rupesh on 6/21/17.
 */
public class MRDb {
    MRDbHelper mrDbHelper;
    private final String TAG = "MRDb";

    MRDb(Context context) {
        mrDbHelper = new MRDbHelper(context);
    }

    private String readFileToString(String filePath) {

        StringBuilder sb = new StringBuilder();

        try {
            InputStream in = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line = null;
            line = br.readLine();

            /* first line */
            if (line != null) {
                sb.append(line);
                Log.d(TAG, line);
            }

            /* prepend every other line with a newline char */
            while ((line = br.readLine()) != null) {
                sb.append("\n").append(line);
                Log.d(TAG, line);
            }

            in.close();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public void addEntry(Book b) {
        SQLiteDatabase db = mrDbHelper.getWritableDatabase();

        for (int i = 0; i < b.getPages().size(); i++) {

            Page p = b.getPages().get(i);

            // Assuming jsonPath to be unique per page
            if (!jsonPathEntryExists(p.getJsonPath())) {
                ContentValues values = new ContentValues();
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID, b.getName());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER, p.getNumber());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT, readFileToString(p.getTextPath()));
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH, p.getAudioPath());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH, p.getJsonPath());

                long rowId = db.insertWithOnConflict(MRDbContract.MRDbEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                Log.d(TAG, "inserted rowId + " + rowId);
            }
        }
    }

    public long getNumberofEntries() {
        SQLiteDatabase db = mrDbHelper.getReadableDatabase();
        long ret = DatabaseUtils.queryNumEntries(db, MRDbContract.MRDbEntry.TABLE_NAME);

        return ret;
    }

    private boolean jsonPathEntryExists(String jsonFilePath) {
        SQLiteDatabase db = mrDbHelper.getReadableDatabase();
        boolean ret =false;

        String [] projection = {
                //MRDbContract.MRDbEntry._ID,
                //MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT,
                //MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                //MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID
        };

        /* filter */
        String selection = MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH + " = ?";
        String[] selectionArgs = {jsonFilePath};

        Cursor cursor = db.query(
                MRDbContract.MRDbEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.getCount() > 0) {
            ret = true;
        }

        cursor.close();
        return ret;

    }

    public int wordEntryMatches(String word) {
        SQLiteDatabase db = mrDbHelper.getReadableDatabase();
        int ret = 0;

        String [] projection = {
                //MRDbContract.MRDbEntry._ID,
                //MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT,
                //MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                //MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID
        };

        /* filter */
        //String selection = MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT + " = ?";
        //String[] selectionArgs = {word};

        /*Cursor cursor = db.query(
                MRDbContract.MRDbEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );*/

        String[] selectionArgs = {word};
        Cursor cursor = db.rawQuery(MRDbHelper.SQL_SEARCH_STRING, selectionArgs);

        if (cursor.getCount() > 0) {
            Log.d(TAG, "FOUND num of times = " + cursor.getCount());
            ret = cursor.getCount();
        }

        cursor.close();
        return ret;
    }

    public Page searchText(String query) {
        SQLiteDatabase db = mrDbHelper.getReadableDatabase();
        Page ret = null;
        String [] projection = {
                MRDbContract.MRDbEntry._ID,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID
        };

        /* filter */
        String[] selectionArgs = {query};
        Cursor cursor = db.rawQuery(MRDbHelper.SQL_SEARCH_STRING, selectionArgs);

        Log.d(TAG, "NUM OF ENTRIES FOUND = " + cursor.getCount());

        if (cursor.getCount() == 1) {

            cursor.moveToFirst();
            String bookId = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID));
            String pageText = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT));
            int pageNumber = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER));
            String audioFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH));
            String jsonFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH));

            ret = new Page(bookId, pageNumber);

            if (audioFilePath == null) {
                audioFilePath = "dummy.mp3";
            }

            if (ret.addData(audioFilePath) && ret.addData(jsonFilePath)) {

            } else {
                Log.d(TAG, "Error adding audio and json path in found page");
                Log.d(TAG, "bookId = " + bookId);
                Log.d(TAG, "pageNum = " + pageNumber);
                //Log.d(TAG, "text size in chars = " + pageText.length());
                Log.d(TAG, "audioFilePath = " + audioFilePath);
                Log.d(TAG, "jsonFilePath = " + jsonFilePath);
                Log.d(TAG, "-----------------------");
                ret = null;
            }
        }

        cursor.close();
        return ret;

    }

    public void printBookInfo(String bookid) {

        SQLiteDatabase db = mrDbHelper.getReadableDatabase();

        String [] projection = {
                MRDbContract.MRDbEntry._ID,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH
        };

        /* filter */
        String selection = MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID + " = ?";
        String[] selectionArgs = {bookid};

        String sorOrder = MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER + " ASC";

        Cursor cursor = db.query(
                MRDbContract.MRDbEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sorOrder
        );

        Log.d(TAG, "BOOK = " + bookid);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            int uid = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry._ID));
            String pageText = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT));
            int pageNumber = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER));
            String audioFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH));
            String jsonFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH));

            Log.d(TAG, "uid = " + uid);
            Log.d(TAG, "pageNum = " + pageNumber);
            Log.d(TAG, "text size in chars = " + pageText.length());
            Log.d(TAG, "audioFilePath = " + audioFilePath);
            Log.d(TAG, "jsonFilePath = " + jsonFilePath);

        }

        cursor.close();

    }
}
