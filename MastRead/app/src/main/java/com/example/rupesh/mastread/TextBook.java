package com.example.rupesh.mastread;

import java.io.Serializable;

/**
 * Created by rupesh on 10/25/17.
 */
public class TextBook extends Book implements Serializable {

    private String mBoard;
    private String mMedium;
    private String mGrade;
    private static final String TAG = "TextBook";
    TextBook(String board, String medium, String grade, String name, int length) {
        super(name, length);
        mBoard = board;
        mMedium = medium;
        mGrade = grade;
    }

    TextBook(String board, String medium, String grade, String name) {
        super(name);
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
