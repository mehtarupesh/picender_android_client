package com.example.rupesh.mastread;

import java.io.Serializable;

/**
 * Created by rupesh on 6/19/17.
 */
public class Page implements Serializable {
    private String bookId;
    private int number;
    private String textPath;
    private String audioPath;
    private String jsonPath;

    Page(int id) {
        number= id;
    }

    Page(String bookId, int pageNumber) {
        this.bookId = bookId;
        this.number = pageNumber;
    }

    private boolean isTextFile(String path) {

        return path.contains(".txt");
    }


    private boolean isAudiotFile(String path) {

        return path.contains(".mp3");
    }


    private boolean isJsonFile(String path) {

        return path.contains(".json");
    }


    public boolean addData(String data) {

        if (data == null) {
            return false;
        }

        boolean ret = true;
        if (isTextFile(data) && textPath == null) {
            textPath = data;
        } else if (isAudiotFile(data) && audioPath == null) {
            audioPath = data;
        } else if (isJsonFile(data) && jsonPath == null) {
            jsonPath = data;
        } else {
            ret = false;
        }

        return ret;
    }

    public int getNumber() {
        return number;
    }
    @Override
    public String toString() {
        String ret =
                "[" + "\n" +
                "number    =" + number    + "\n" +
                "audioPath =" + audioPath + "\n" +
                "textPath  =" + textPath  + "\n" +
                "jsonPath  =" + jsonPath  + "\n" +
                "]" + "\n";
        return ret;
    }

    public String getBookId() {return bookId;}

    public String getTextPath() {
        return textPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public String getJsonPath() {
        return jsonPath;
    }
}
