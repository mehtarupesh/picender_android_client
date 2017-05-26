package com.example.rupesh.mastread;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by rupesh on 5/14/17.
 */
public class MRStatistics {
    private HashMap<Integer, Integer> hmap;
    private String name;
    private Range range;
    private int numElements;
    final String TAG = "MRStatistics";
    private int min = -1;
    private int max = -1;

    MRStatistics(String name, int min, int max) {

        this.name = name;
        range = new Range(min, max);
        hmap = new HashMap<Integer, Integer>();
    }

    public void add(int key) {

        if (range.inRange(key)) {
            int curVal =  (hmap.containsKey(key)) ? hmap.get(key) : 0;
            curVal++;
            hmap.put(key, curVal);
            numElements++;
        }
    }

    public int min() {

        if (min == -1) {
            for (int i = range.getStart(); i <= range.getEnd(); i++) {

                if (hmap.containsKey(i)) {
                    min = i;
                    break;
                }
            }

            Log.d(TAG, "NO MIN FOUND");
        }
        return min;
    }

    public int max() {

        if (max == -1) {
            for (int i = range.getEnd(); i >= range.getStart(); i--) {

                if (hmap.containsKey(i)) {
                    max = i;
                    break;
                }
            }

            Log.d(TAG, "NO MAX FOUND");
        }
        return max;
    }

    /* returns number of pixels */
    public void calculateCDF() {

        int cdf_val = 0;
        for (int i = range.getStart(); i < range.getEnd(); i++) {

            if (hmap.containsKey(i)) {
                cdf_val += hmap.get(i);
                hmap.put(i, cdf_val);
            }
        }

        assert(cdf_val == numElements);

    }

    public int equalize(int key) {
        int ret = 0;


        int min = min();
        //int max = max();

        ret = Math.round(((float)(hmap.get(key) - min) / (float)(numElements - 1)) * range.getEnd());

        return ret;
    }

}
