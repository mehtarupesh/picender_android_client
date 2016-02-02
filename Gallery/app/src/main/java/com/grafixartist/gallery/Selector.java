package com.grafixartist.gallery;

import java.util.ArrayList;

/**
 * Created by rupesh on 1/31/16.
 */

/*
* Reqs:
* 1. long press should toggle a pic as selected/deselected
* 2. should return an arraylist of selected/deselected list
*/

public class Selector {
    ArrayList<Boolean> dataState = new ArrayList<Boolean>();

    private Selector(int size, Boolean val) {
        for(int i=0; i < size; i++) {
            dataState.add(val);
        }
    }

    private static Selector instance = null;
    public static Selector getInstance(int size, Boolean val) {

        if(instance == null) {
            instance = new Selector(size, val);
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void setState(int index, Boolean val) {
        dataState.set(index, val);
    }

    public Boolean getState(int index) {
        return dataState.get(index);
    }

    public void toggle(int index) {
        Boolean val = dataState.get(index);
        val = !val;
        dataState.set(index, val);
    }

    public ArrayList<Integer> getList(Boolean val) {

        ArrayList<Integer> indexList = new ArrayList<Integer>();

        for(int i = 0; i < dataState.size(); i++) {

            if(dataState.get(i) == val)
                indexList.add(i);
        }

        return indexList;
    }
}
