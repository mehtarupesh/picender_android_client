package com.example.rupesh.mastread;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by rupesh on 6/19/17.
 */
public class Book {
    private ArrayList<Page> pages;
    private String name;
    private Range range;
    private final String TAG = "Book";

    public String getName() {
        return name;
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    Book(String name, int length) {
        this.name = name;
        pages = new ArrayList<>(length);
        /* page numbering starts from 1 */
        range = new Range(1, length);

    }

    Book(String name) {
        this(name, 50);
    }

    /* path format = *page_<number>.<file_format> */
    public static int getPageNumber(String path) {

        int indexOfUnderScore = path.lastIndexOf("_");
        int indexOfDot = path.lastIndexOf(".");

        Range pathRange = new Range(0, path.length() - 1);
        if (pathRange.inRange(indexOfDot) && pathRange.inRange(indexOfUnderScore)) {
            String pageNumber = path.substring(indexOfUnderScore + 1, indexOfDot);
            return Integer.parseInt(pageNumber);
        } else
            return -1;
    }

    public Page getPage(int pageNumber) {
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getNumber() == pageNumber)
                return pages.get(i);
        }
        return null;
    }
    public boolean addEntry(String pathToResource) {

        int pageNumber = getPageNumber(pathToResource);
        Log.d(TAG, "path = " + pathToResource);
        Log.d(TAG,"page number = " + pageNumber);
        boolean ret = false;

        if (range.inRange(pageNumber)) {

            Page p = getPage(pageNumber);
            boolean newPage = false;

            if (p == null) {
                p = new Page(name, pageNumber);
                newPage = true;
            }

            Log.d(TAG, "adding page resource = " + pathToResource);
            p.addData(pathToResource);

            if (newPage)
                pages.add(p);

            ret = true;
        }

        return ret;
    }

    public boolean addMultipleEntries(String[] data) {
        boolean ret = true;

        for (int i = 0; i < data.length; i++) {

            /* add as much as possible */
            if (!this.addEntry(data[i])) {
                ret = false;
            }

        }

        return ret;
    }

    @Override
    public String toString() {
        String ret =
                "[" + "\n" +
                "name    =" + name + "\n";

        for (int i = 0; i < pages.size(); i++) {
            ret = ret + pages.get(i).toString();
        }

        ret = ret + "\n" + "]" + "\n";
        return ret;
    }

}
