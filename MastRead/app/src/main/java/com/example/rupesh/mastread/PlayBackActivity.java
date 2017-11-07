package com.example.rupesh.mastread;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

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


    private File getFileFromPath(String path) {
        File ret = null;

        if (path == null)
            return ret;

        ret = new File(path);

        if (!ret.exists()) {

            Log.d(TAG, "not found : " + path );
            ret = null;
        } else {
            Log.d(TAG, "found : " + path);
        }
        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String displayText = "404 : Page not found!";
        Page page_info = (Page) getIntent().getSerializableExtra("PAGE_INFO");
        File audioFilePath = null;
        File jsonFilePath = null;
        if (page_info != null) {
            Log.d(TAG, "Playing page : \n" + page_info.toString());
            displayText = page_info.getBookId() + "\n" + "Page number : " + page_info.getNumber();

            Log.d(TAG, "mp3 played = " + page_info.getAudioPath());
            Log.d(TAG,"json used = " + page_info.getJsonPath());

            audioFilePath = getFileFromPath(MRResource.getAbsoluteFilePath(page_info.getAudioPath()));
            jsonFilePath = getFileFromPath(MRResource.getAbsoluteFilePath(page_info.getJsonPath()));
        }

        /* starting word is index 0 */
        focus = 0;

        if (audioFilePath != null && jsonFilePath != null) {

            Log.d(TAG, "audio file size = " + audioFilePath.length());
            Log.d(TAG, "json file size = " + jsonFilePath.length());

            mrAudioPlayer = new MRAudioPlayer2(getApplicationContext(), audioFilePath);
            mrSyncWordEngine = new MRSyncWordEngine(getApplicationContext(), jsonFilePath);
        } else {
            displayText += "\nSource Files not found:(";
            //audioFilePath = new File("/storage/emulated/0/Android/data/com.example.rupesh.mastread/files/./Board1/MediumY/Grade3/mast_read_book_2/page_12.mp3");
            //jsonFilePath = new File("/storage/emulated/0/Android/data/com.example.rupesh.mastread/files/./Board1/MediumY/Grade3/mast_read_book_2/page_12.json");

            //Log.d(TAG, "audio file size = " + audioFilePath.length());
            //Log.d(TAG, "json file size = " + jsonFilePath.length());

            //mrAudioPlayer = new MRAudioPlayer2(getApplicationContext(), audioFilePath);
            //mrSyncWordEngine = new MRSyncWordEngine(getApplicationContext(), jsonFilePath);
        }

        mrTextViewDisplayEngine = new TextViewDisplayEngine((TextView) findViewById(R.id.textView), mrSyncWordEngine);
        ((TextView) findViewById(R.id.textView)).setText(displayText);
        Log.d(TAG, "In PBA \n");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mrState == playState.PLAYING) {
            processOnPause();
            Button playPause = (Button) findViewById(R.id.button2);
            //show the next available option
            playPause.setText(getPlayPauseString(mrState));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        /* if paused, use as word selector */
        if (mrState == playState.PAUSED) {
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
        /* if playing, use as playback speed control */
        else if (mrState == playState.PLAYING) {
            mrAudioPlayer.mrIncrementPlayBackRate();

        }

    }


    public void backwardButton (View view) {

        if (mrState == playState.PAUSED) {
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
        } else if (mrState == playState.PLAYING) {
            mrAudioPlayer.mrDecrementPlayBackRate();
        }

    }

    private void processOnPlay() {
        if (focus != -1) {
            int seekTime = mrSyncWordEngine.getStartTimeFromHandle(focus);
            mrAudioPlayer.setPosition(seekTime);
        }
        mrAudioPlayer.mrPlay();
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
