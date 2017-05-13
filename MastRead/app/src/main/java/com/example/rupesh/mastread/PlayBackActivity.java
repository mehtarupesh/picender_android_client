package com.example.rupesh.mastread;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PlayBackActivity extends AppCompatActivity {

    private String TAG = "PlayBackActivity";
    private MRAudioPlayer2 mrAudioPlayer;
    private MRSyncWordEngine mrSyncWordEngine;
    private TextViewDisplayEngine mrTextViewDisplayEngine;
    private int focus;

    private enum playState {
        STOPPED,
        PLAYING,
        PAUSED
    };
    private playState mrState = playState.STOPPED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* starting word is index 0 */
        focus = 0;
        mrAudioPlayer = new MRAudioPlayer2(getApplicationContext(), R.raw.chambers_schultz);
        mrSyncWordEngine = new MRSyncWordEngine(getApplicationContext(), R.raw.chambers_schultz_json);
        mrTextViewDisplayEngine = new TextViewDisplayEngine((TextView) findViewById(R.id.textView), mrSyncWordEngine);
        Log.d(TAG, "In PBA \n");
    }

    private String getPlayPauseString(playState state) {
        String ret = "ERROR!!";
        switch (mrState) {
            case STOPPED:
            case PLAYING:
                ret = "PAUSE";
                break;
            case PAUSED:
                ret = "PLAY";
                break;
            default:
                Log.d(TAG, "unknown state?\n");
        }
        return ret;
    }

    public void playPauseButton (View view) {

            switch (mrState) {
                case PLAYING:
                    processOnPause();
                    break;
                case STOPPED:
                case PAUSED:
                    processOnPlay();
                    break;
                default:
                    Log.d(TAG, "unknown state?\n");
            }
            Button playPause = (Button) view;
            //show the next available option
            playPause.setText(getPlayPauseString(mrState));

    }

    public void forwardButton (View view) {
        if (mrState != playState.PAUSED)
            return;

        int handle;

        if (focus == -1) {
            int currentTime = mrAudioPlayer.getCurrentPosition();
            handle = mrSyncWordEngine.getHandleFromTimeStamp(currentTime);
        } else
            handle = focus;

        if (mrTextViewDisplayEngine.display(handle + 1)) {
            focus = handle + 1;
        }

    }


    public void backwardButton (View view) {
        if (mrState != playState.PAUSED)
            return;

        int handle;
        if (focus == -1) {
            int currentTime = mrAudioPlayer.getCurrentPosition();
            handle = mrSyncWordEngine.getHandleFromTimeStamp(currentTime);
        } else {
            handle = focus;
        }

        if (mrTextViewDisplayEngine.display(handle - 1)) {
            focus = handle - 1;
        }

    }

    private void processOnPlay() {
        if (focus != -1) {
            int seekTime = mrSyncWordEngine.getStartTimeFromHandle(focus);
            mrAudioPlayer.setPosition(seekTime);
        }
        mrAudioPlayer.mrPlay((float) 1.0);
        mrState = playState.PLAYING;
    }

    /* Handle inter-engine communication
    * get from one, set in another */
    private void processOnPause() {

        mrAudioPlayer.mrPause();
        mrState = playState.PAUSED;

        int currentTime = mrAudioPlayer.getCurrentPosition();
        int handle = mrSyncWordEngine.getHandleFromTimeStamp(currentTime);
        mrTextViewDisplayEngine.printFrame(handle);
        mrTextViewDisplayEngine.display(handle);
        focus = -1;
    }

}
