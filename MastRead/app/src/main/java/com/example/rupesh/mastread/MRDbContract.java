package com.example.rupesh.mastread;

import android.provider.BaseColumns;

/**
 * Created by rupesh on 6/21/17.
 */
public final class MRDbContract {
    private MRDbContract() {}

    public static class MRDbEntry implements BaseColumns {
        public static final String TABLE_NAME = "mastread";
        public static final String COLUMN_NAME_BOOK_ID = "bookid";
        public static final String COLUMN_NAME_PAGE_NUMBER ="pagenumber";
        public static final String COLUMN_NAME_PAGE_TEXT = "pagetext";
        public static final String COLUMN_NAME_JSON_PATH = "jsonpath";
        public static final String COLUMN_NAME_AUDIO_PATH = "audiopath";
        public static final String COLUMN_NAME_TEXT_PATH = "textpath";
        public static final String COLUMN_NAME_BOARD = "board";
        public static final String COLUMN_NAME_MEDIUM = "medium";
        public static final String COLUMN_NAME_GRADE = "grade";

        //TODO: Linked tables if perf hit.
        //public static final String TABLE_NAME_FTS = "mastread_fts";
        //public static final String COLUMN_NAME_BOOK_ID_FTS = "bookid";
        //public static final String COLUMN_NAME_PAGE_TEXT_FTS = "pagetext";


    }
}
