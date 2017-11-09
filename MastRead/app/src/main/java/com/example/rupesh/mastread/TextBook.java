package com.example.rupesh.mastread;

/**
 * Created by rupesh on 10/25/17.
 */
public class TextBook extends Book {

    //TODO: remove TextBook, aint needed
    private String mBoard;
    private String mMedium;
    private String mGrade;
    private static final String TAG = "TextBook";


    TextBook(String board, String medium, String grade, String name, String resourceId, int length) {
        super(name, resourceId, length);
        mBoard = board;
        mMedium = medium;
        mGrade = grade;
    }

    TextBook(String board, String medium, String grade, String name, String resourceId) {
        super(name, resourceId);
        mBoard = board;
        mMedium = medium;
        mGrade = grade;
    }

    public String getBoard() {
        return mBoard;
    }

    public String getMedium() {
        return mMedium;
    }

    public String getGrade() {
        return mGrade;
    }

}
