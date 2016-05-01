package com.grafixartist.gallery;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by rupesh on 1/31/16.
 */

/*
* Reqs:
* 1. long press should toggle a pic as selected/deselected
* 2. should return an arraylist of selected/deselected list
* 3. can mark pics as deleted.
* 4. Store onCLoud state. The whole damn motivation of the app.
*/

public class Selector {

    private String TAG = "Selector";
    private ArrayList<FileState> dataState = new ArrayList<FileState>();
    private int size;

    private Selector(int size) {

        this.size = size;
        for(int i=0; i < size; i++) {
            dataState.add(new FileState(false, false, false));
        }
    }

    private static Selector instance = null;

    private Boolean validIndex(int index) {

        if (index >= size) {
            Log.d(TAG, "ERROR : index >= size !");
            return false;
        }

        return true;
    }

    public static Selector getInstance(int size) {

        if(instance == null) {
            instance = new Selector(size);
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }


    /* operations on selected state */
    public void setSelectedState(int index, Boolean val) {

        if(!validIndex(index))
            return;

        FileState s = dataState.get(index);
        s.setSelected(val);
        dataState.set(index, s);
    }

    public Boolean getSelectedState(int index) {

        if(!validIndex(index))
            return false;

        FileState s = dataState.get(index);
        return s.getSelected();
    }

    public void toggleSelectedState(int index) {

        if(!validIndex(index))
            return;

        FileState s = dataState.get(index);

        Boolean val = s.getSelected();
        val = !val;
        s.setSelected(val);

        dataState.set(index, s);
    }

    public ArrayList<Integer> getSelectedStateList(Boolean val) {

        ArrayList<Integer> indexList = new ArrayList<Integer>();

        for(int i = 0; i < dataState.size(); i++) {

            FileState s = dataState.get(i);
            if(s.getSelected() == val)
                indexList.add(i);
        }

        return indexList;
    }
}
