package com.example.rupesh.mastread;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by rupesh on 6/21/17.
 */
public class MRDb {
    MRDbHelper mrDbHelper;
    private final String TAG = "MRDb";

    MRDb(Context context) {
        mrDbHelper = new MRDbHelper(context);
    }

    public void addTextBookEntry(TextBook b) {
        SQLiteDatabase db = mrDbHelper.getWritableDatabase();

        for (int i = 0; i < b.getPages().size(); i++) {

            Page p = b.getPages().get(i);

            // Assuming jsonPath to be unique per page
            if (!jsonPathEntryExists(p.getJsonPath())) {
                ContentValues values = new ContentValues();
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID, b.getName());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER, p.getNumber());

                /* DB does not know where actual file is, so sending relative filePath */
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT, MRResource.readFileToString(p.getTextPath(), false));
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH, p.getAudioPath());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH, p.getJsonPath());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH, p.getTextPath());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_BOARD, b.getBoard());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_MEDIUM, b.getMedium());
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_GRADE, b.getGrade());

                long rowId = db.insertWithOnConflict(MRDbContract.MRDbEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                Log.d(TAG, "inserted rowId + " + rowId);
            }
        }
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

                /* DB knows where actual file resides */
                values.put(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT, MRResource.readFileToString(p.getTextPath(), true));
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

    public TextBook searchText(String query) {
        SQLiteDatabase db = mrDbHelper.getReadableDatabase();
        //Page ret = null;
        TextBook tBook = null;

        String [] projection = {
                //MRDbContract.MRDbEntry._ID,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID,
                MRDbContract.MRDbEntry.COLUMN_NAME_BOARD,
                MRDbContract.MRDbEntry.COLUMN_NAME_MEDIUM,
                MRDbContract.MRDbEntry.COLUMN_NAME_GRADE,
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
            String board = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_BOARD));
            String medium = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_MEDIUM));
            String grade = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_GRADE));

            assert(audioFilePath != null);
            /*if (audioFilePath == null) {
                audioFilePath = "dummy.mp3";
            }*/

            /* using network fetched data */

            /* TODO: BUG FIX : allocate correct number of pages instead of arbitrary 100 here ...*/
            tBook = new TextBook(board, medium, grade, bookId, 100);
            Log.d(TAG, "adding audio ? =" + tBook.addEntry((audioFilePath)));
            Log.d(TAG, "Adding json ? =" + tBook.addEntry((jsonFilePath)));

            /*
            ret = new Page(bookId, pageNumber);

            if (ret.addData(MRResource.getAbsoluteFilePath(audioFilePath)) && ret.addData(MRResource.getAbsoluteFilePath(jsonFilePath))) {
                File check = new File(ret.getAudioPath());
                assert(check.exists() && check.length() > 0);

                check = new File(ret.getJsonPath());
                assert(check.exists() && check.length() > 0);



            } else {
                Log.d(TAG, "Error adding audio and json path in found page");
                Log.d(TAG, "bookId = " + bookId);
                Log.d(TAG, "pageNum = " + pageNumber);
                //Log.d(TAG, "text size in chars = " + pageText.length());
                Log.d(TAG, "audioFilePath = " + audioFilePath);
                Log.d(TAG, "jsonFilePath = " + jsonFilePath);
                Log.d(TAG, "-----------------------");
                ret = null;
            }*/
        }

        cursor.close();
        return tBook;

    }

    public void printBookInfo(String bookid) {

        SQLiteDatabase db = mrDbHelper.getReadableDatabase();

        String [] projection = {
                //MRDbContract.MRDbEntry._ID,
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

            //int uid = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry._ID));
            String pageText = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT));
            int pageNumber = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER));
            String audioFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH));
            String jsonFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH));

            //Log.d(TAG, "uid = " + uid);
            Log.d(TAG, "pageNum = " + pageNumber);
            Log.d(TAG, "text size in chars = " + pageText.length());
            Log.d(TAG, "audioFilePath = " + audioFilePath);
            Log.d(TAG, "jsonFilePath = " + jsonFilePath);

        }

        cursor.close();

    }

    public void printTextBookInfo(String board, String medium, String grade, String bookName) {

        SQLiteDatabase db = mrDbHelper.getReadableDatabase();

        String [] projection = {
                /*MRDbContract.MRDbEntry._ID,*/
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH

        };

        /* filter */
        String selection = MRDbContract.MRDbEntry.COLUMN_NAME_BOARD + " = ?" + " AND " +
                           MRDbContract.MRDbEntry.COLUMN_NAME_MEDIUM + " = ?" + " AND " +
                           MRDbContract.MRDbEntry.COLUMN_NAME_GRADE + " = ?" + " AND " +
                           MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID + " = ?";

        String[] selectionArgs = {board, medium, grade, bookName};

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

        Log.d(TAG, "TEXT-BOOK = " + board + "/" + medium + "/" + grade + "/" + bookName);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            //int uid = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry._ID));
            String pageText = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT));
            int pageNumber = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER));
            String audioFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH));
            String jsonFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH));
            String pageTextPath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH));


            //Log.d(TAG, "uid = " + uid);
            Log.d(TAG, "pageNum = " + pageNumber);

            Log.d(TAG, "text  = " + pageText);

            Log.d(TAG, "audioFilePath = " + audioFilePath);
            Log.d(TAG, "jsonFilePath = " + jsonFilePath);
            Log.d(TAG, "textpath = " + pageTextPath);

        }

        cursor.close();

    }

    public void addPageTextInfo(String board, String medium, String grade, String bookName) {
        //ArrayList<String> textBookFiles = getTextBookFiles(board, medium, grade, bookName);
        //assert(textBookFiles != null);
        SQLiteDatabase db = mrDbHelper.getWritableDatabase();

        String [] projection = {
                /*MRDbContract.MRDbEntry._ID,*/
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH

        };

        /* filter */
        String selection = MRDbContract.MRDbEntry.COLUMN_NAME_BOARD + " = ?" + " AND " +
                MRDbContract.MRDbEntry.COLUMN_NAME_MEDIUM + " = ?" + " AND " +
                MRDbContract.MRDbEntry.COLUMN_NAME_GRADE + " = ?" + " AND " +
                MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID + " = ?";

        String[] selectionArgs = {board, medium, grade, bookName};

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

        Log.d(TAG, "Downloading text for = " + board + "/" + medium + "/" + grade + "/" + bookName);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            //int uid = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry._ID));
            int pageNumber = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER));
            String jsonFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH));
            String pageTextPath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH));


            //Log.d(TAG, "uid = " + uid);
            Log.d(TAG, "pageNum = " + pageNumber);
            Log.d(TAG, "jsonFilePath = " + jsonFilePath);
            Log.d(TAG, "textpath = " + pageTextPath);

            ContentValues value = new ContentValues();

            /* DB does know where actual file resides */
            value.put(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT, MRResource.readFileToString(pageTextPath, false));
            db.update(MRDbContract.MRDbEntry.TABLE_NAME, value, MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH+"=\""+jsonFilePath+"\"",null);
        }

        cursor.close();

        Log.d(TAG, "Checking DB -----------------------------------------> ");
        printTextBookInfo(board, medium, grade, bookName);

    }

    public ArrayList<String> getTextBookFiles(String board, String medium, String grade, String bookName) {

        ArrayList<String> retFileList = new ArrayList<>();

        SQLiteDatabase db = mrDbHelper.getReadableDatabase();

        String [] projection = {
                /*MRDbContract.MRDbEntry._ID,*/
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT,
                MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER,
                MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH,
                MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH

        };

        /* filter */
        String selection = MRDbContract.MRDbEntry.COLUMN_NAME_BOARD + " = ?" + " AND " +
                MRDbContract.MRDbEntry.COLUMN_NAME_MEDIUM + " = ?" + " AND " +
                MRDbContract.MRDbEntry.COLUMN_NAME_GRADE + " = ?" + " AND " +
                MRDbContract.MRDbEntry.COLUMN_NAME_BOOK_ID + " = ?";

        String[] selectionArgs = {board, medium, grade, bookName};

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

        Log.d(TAG, "TEXT-BOOK = " + board + "/" + medium + "/" + grade + "/" + bookName);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            //int uid = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry._ID));
            String pageText = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_TEXT));
            int pageNumber = cursor.getInt(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_PAGE_NUMBER));
            String audioFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_AUDIO_PATH));
            String jsonFilePath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_JSON_PATH));
            String pageTextPath = cursor.getString(cursor.getColumnIndex(MRDbContract.MRDbEntry.COLUMN_NAME_TEXT_PATH));


            //Log.d(TAG, "uid = " + uid);
            Log.d(TAG, "pageNum = " + pageNumber);

            //Log.d(TAG, "text  = " + pageText);

            //Log.d(TAG, "audioFilePath = " + audioFilePath);
            //Log.d(TAG, "jsonFilePath = " + jsonFilePath);
            //Log.d(TAG, "textpath = " + pageTextPath);

            retFileList.add(audioFilePath);
            retFileList.add(jsonFilePath);
            retFileList.add(pageTextPath);
        }

        cursor.close();
        return retFileList;
    }
}
