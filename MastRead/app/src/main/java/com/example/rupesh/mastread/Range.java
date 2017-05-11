package com.example.rupesh.mastread;

/**
 * Created by rupesh on 5/9/17.
 */
public class Range {
    private int start;
    private int end;

    public Range(int s, int e) {
        start = s;
        end = e;
    }
    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(int val) {
        start = val;
    }

    public void setEnd(int val) {
        end = val;
    }

    public Boolean inRange(int val) {
        if (start <= val && val <= end) {
            return true;
        }
        return false;
    }
}
