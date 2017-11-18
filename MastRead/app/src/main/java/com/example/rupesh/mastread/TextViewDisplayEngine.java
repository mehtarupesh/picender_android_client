package com.example.rupesh.mastread;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by rupesh on 5/8/17.
 */
public class TextViewDisplayEngine {

    private MRSyncWordEngine mrSyncWordEngine;
    private TextView textView;
    private String TAG = "TextViewDisplayEngine";
    private Range currentFrame;

    public void reset(int handle) {

        if (inCurrentFrame(handle)) {

            Log.d(TAG, "reset: In current frame");
            return;

        } else if(inPreviousFrame(handle)) {

            Log.d(TAG, "reset : In prev frame");
            currentFrame = getDummyFramePrevious(currentFrame);

        } else if (inNextFrame(handle)) {

            Log.d(TAG, "reset : In next frame");
            currentFrame = getDummyFrameNext(currentFrame);

        } else {

            Log.d(TAG, "reset : setting start = -1, end = -1");
            currentFrame.setStart(-1);
            currentFrame.setEnd(-1);

        }
     }

    public TextViewDisplayEngine(TextView view, MRSyncWordEngine engine) {
        this.mrSyncWordEngine = engine;
        this.textView = view;
        currentFrame = new Range(-1, -1);
    }

    private Boolean isPunctuation(String text) {

        if (text.equals(".") || text.equals(";") || text.equals(",")) {
            return true;
        }

        return false;
    }

    private Boolean inCurrentFrame(int handle) {
        return currentFrame.inRange(handle);
    }

    private Boolean inPreviousFrame(int handle) {

        Range range;

        range = getDummyFramePrevious(currentFrame);

        return range.inRange(handle);
    }

    private Boolean inNextFrame(int handle) {

        Range range;

        range = getDummyFrameNext(currentFrame);

        return range.inRange(handle);
    }
    public Boolean display(int handle) {

        Log.d(TAG, "display handle : " + handle);
        Range r = getDummyFrame(handle);
        Log.d(TAG, "display : dummy frame = " + r.printRange());

        if (r.getStart() == -1 || r.getEnd() == -1) {
            return false;
        }

        /* Adjust framing for rase of viewing */
        reset(handle);

        if (inCurrentFrame(handle)) {
            r = currentFrame;
        } else {
            currentFrame = r;
        }

        Log.d(TAG, "currentframe before loop = " + r.printRange());
        /* Display interval */
        String buffer = "";
        for (int i = r.getStart(); i <= r.getEnd(); i++) {

            String word = mrSyncWordEngine.getWordFromHandle(i);
            if (i == handle)
                buffer += "<b>" +  word +"</b> ";
            else
                buffer +=  word;

            if (r.inRange(i+1) && !isPunctuation(mrSyncWordEngine.getWordFromHandle(i + 1))) {
                buffer += " ";
            }
        }

        Log.d(TAG, "BUFFER = " + buffer);

        //String formattedText = "This <i>is</i> a <b>test</b> of <a href='http://foo.com'>html</a>";

        if (!buffer.equals("")) {
            textView.setText(Html.fromHtml(buffer));
            textView.setGravity(Gravity.CENTER);
            return true;
        }

        return false;

    }

    public void onPlay() {

        textView.setGravity(Gravity.CENTER);
        textView.setText("READ TEXTBOOK !");
        textView.setBackgroundColor(Color.YELLOW);
        textView.setTextColor(Color.RED);

    }

    public void onPause() {

        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.BLACK);
    }

    private int range = 5;
    public void printFrame(int handle) {

        int start = handle - range;
        int end = handle + range;

        if (!mrSyncWordEngine.isValidHandle(handle)) {
            Log.d(TAG, "INVALID HANDLE !!! - " + handle);
            return;
        }

        if (start < 0)
            start = 0;

        if (end >= mrSyncWordEngine.getLength())
            end = mrSyncWordEngine.getLength() - 1;

        for (int i = start; i <= end; i++) {
            if (i == handle) {
                Log.d(TAG, "index = " + i + ", HIGHLIGHT!!! - " + mrSyncWordEngine.getWordFromHandle(i));
            } else
                Log.d(TAG, "index = " + i + ", word -" + mrSyncWordEngine.getWordFromHandle(i));
        }

        Log.d(TAG, "-------------------------------------\n");

    }

    public int getDummyFrameStart(int handle) {

        if (!mrSyncWordEngine.isValidHandle(handle)) {
            Log.d(TAG, "INVALID HANDLE !!! - " + handle);
            return -1;
        }

        int start = handle - range;

        if (start < 0)
            start = 0;

        return start;

    }


    public int getDummyFrameEnd(int handle) {

        if (!mrSyncWordEngine.isValidHandle(handle)) {
            Log.d(TAG, "INVALID HANDLE !!! - " + handle);
            return -1;
        }

        int end = handle + range;

        if (end >= mrSyncWordEngine.getLength())
            end = mrSyncWordEngine.getLength() - 1;

        return end;

    }

    public Range getDummyFrame(int handle) {
                /* eww : get an interval */
        int start = getDummyFrameStart(handle);
        int end = getDummyFrameEnd(handle);

        Range r = new Range(start, end);
        return r;
    }

    public Range getDummyFramePrevious(Range r) {
        int start = r.getStart();
        int end = r.getEnd();

        if (!mrSyncWordEngine.isValidHandle(start) || !mrSyncWordEngine.isValidHandle(end))
            return new Range(-1, -1);

        if (start == 0 || end == mrSyncWordEngine.getLength() - 1)
            return r;

        end = start - 1;
        start = end - (2 * range);

        if (start < 0)
            start = 0;

        return new Range(start, end);
    }

    public Range getDummyFrameNext(Range r) {
        int start = r.getStart();
        int end = r.getEnd();

        if (!mrSyncWordEngine.isValidHandle(start) || !mrSyncWordEngine.isValidHandle(end))
            return new Range(-1, -1);

        if (start == 0 || end == mrSyncWordEngine.getLength() - 1)
            return r;

        start = end + 1;
        end = start + (2*range);

        if (end >= mrSyncWordEngine.getLength())
            end = mrSyncWordEngine.getLength() - 1;

        return new Range(start, end);
    }
}
