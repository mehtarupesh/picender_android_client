package com.example.rupesh.mastread;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by rupesh on 5/7/17.
 */
public class MRSyncWordEngine {

    private JSONArray wordSyncArray;
    private String TAG = "MRSyncWordEngine";
    private MRSyncWordFormat1 jsonFormat;

    public MRSyncWordEngine(Context context,
                            int resId) {
        /* get the format */
        jsonFormat = new MRSyncWordFormat1();

        /* build json string */
        InputStream in = (InputStream) context.getResources().openRawResource(resId);
        int size = 0;
        byte[] buffer = {0};
        try {
            size = in.available();
            buffer = new byte[size];
            in.read(buffer);
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonString = null;
        try {
            jsonString = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /* build json array */
        JSONArray tempWordSyncArray;
        wordSyncArray = new JSONArray();

        try {
            tempWordSyncArray = new JSONArray(jsonString);

            for (int i = 0; i < tempWordSyncArray.length(); i++) {
                    //if (jsonFormat.isValidEntry(tempWordSyncArray.getJSONObject(i))) {
                        wordSyncArray.put(tempWordSyncArray.getJSONObject(i));
                    //}
            }

            Log.d(TAG, "Number of timed words =" + wordSyncArray.length());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Boolean isValidHandle(int handle) {
        if (handle >= 0 && handle < wordSyncArray.length())
            return true;

        return false;
    }

    public int getLength() {
        return wordSyncArray.length();
    }

    private void printJsonObject(JSONObject obj) {

        if (obj.has("text"))
            Log.d(TAG, "word -- > " + jsonFormat.getWord(obj));

        if (obj.has("start"))
            Log.d(TAG, "start (ms) -- > " + jsonFormat.getStartTime(obj));

        if (obj.has("end"))
            Log.d(TAG, "end (ms) -- > " + jsonFormat.getEndTime(obj));


    }

    /* round it up i.e to the next whole word */
    public int getHandleFromTimeStamp(int ts) {
        int ret = -1;

        for (int i = 0; i < wordSyncArray.length(); i++) {
            try {

                if (!jsonFormat.isValidEntry(wordSyncArray.getJSONObject(i))) {
                    continue;
                }
                if (ts >= jsonFormat.getStartTime((wordSyncArray.getJSONObject(i))) && ts <= jsonFormat.getEndTime((wordSyncArray.getJSONObject(i)))) {
                    return i;
                } else if(ts < jsonFormat.getStartTime((wordSyncArray.getJSONObject(i)))) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return ret;
    }


    public String getWordFromHandle(int handle) {

        String ret = null;

        if (isValidHandle(handle)) {
            try {
                ret = jsonFormat.getWord(wordSyncArray.getJSONObject(handle));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public int getStartTimeFromHandle(int handle) {

        int ret = -1;

        if (isValidHandle(handle)) {
            try {
                ret = jsonFormat.getStartTime(wordSyncArray.getJSONObject(handle));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public int getEndTimeFromHandle(int handle) {

        int ret = -1;

        if (isValidHandle(handle)) {
            try {
                ret = jsonFormat.getEndTime(wordSyncArray.getJSONObject(handle));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

}
