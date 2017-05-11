package com.example.rupesh.mastread;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rupesh on 5/7/17.
 */
public class MRSyncWordFormat1 {

    private static String WORD = "text";
    private static String START_MS = "start" ;
    private static String END_MS = "end" ;

    public MRSyncWordFormat1() {

    }
    public Boolean isValidEntry(JSONObject obj) {

        if (getStartTime(obj) != -1) {
            return true;
        }

        return false;

    }

    public String getWord(JSONObject obj) {
        String ret = null;

        if (obj.has(WORD)) {
            try {
                ret = obj.getString(WORD);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public int getStartTime(JSONObject obj) {
        int ret = -1;

        if (obj.has(START_MS)) {
            try {
                ret = obj.getInt(START_MS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public int getEndTime(JSONObject obj) {
        int ret = -1;

        if (obj.has(END_MS)) {
            try {
                ret = obj.getInt(END_MS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

}
