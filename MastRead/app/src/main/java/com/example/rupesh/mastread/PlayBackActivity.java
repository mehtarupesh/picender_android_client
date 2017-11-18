package com.example.rupesh.mastread;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class PlayBackActivity extends AppCompatActivity {

    private String TAG = "PlayBackActivity";
    private MRAudioPlayer2 mrAudioPlayer;
    private MRSyncWordEngine mrSyncWordEngine;
    private TextViewDisplayEngine mrTextViewDisplayEngine;
    private int focus;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient client;
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "PlayBack Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.rupesh.mastread/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "PlayBack Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.rupesh.mastread/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
//    }

    private enum playState {
        STOPPED,
        PLAYING,
        PAUSED
    }

    ;
    private playState mrState = playState.STOPPED;


    private File getFileFromPath(String path) {
        File ret = null;

        if (path == null)
            return ret;

        ret = new File(path);

        if (!ret.exists()) {

            Log.d(TAG, "not found : " + path);
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String displayText = "";
        Page page_info = (Page) getIntent().getSerializableExtra("PAGE_INFO");
        File audioFilePath = null;
        File jsonFilePath = null;
        if (page_info != null) {
            Log.d(TAG, "Playing page : \n" + page_info.toString());

            if (page_info.getBookId() != null)
                displayText = page_info.getBookId() + "\n";

            displayText += "Page number : " + page_info.getNumber();

            Log.d(TAG, "mp3 played = " + page_info.getAudioPath());
            Log.d(TAG, "json used = " + page_info.getJsonPath());

            audioFilePath = getFileFromPath(MRResource.getAbsoluteFilePath(page_info.getAudioPath()));
            jsonFilePath = getFileFromPath(MRResource.getAbsoluteFilePath(page_info.getJsonPath()));
        } else {
            displayText = "404 : Page not found!";
        }

        /* starting word is index 0 */
        focus = 0;

        if (audioFilePath != null && jsonFilePath != null) {

            Log.d(TAG, "audio file size = " + audioFilePath.length());
            Log.d(TAG, "json file size = " + jsonFilePath.length());

            /* on completionListener */
            MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "Releasing mediaplayer resources");
                    togglePlayPauseButton((Button)findViewById(R.id.playButton));
                    //isLoaded = false;
                    //mp.release();
                    //mp.reset();
                }
            };

            mrAudioPlayer = new MRAudioPlayer2(getApplicationContext(), audioFilePath, listener);
            mrSyncWordEngine = new MRSyncWordEngine(getApplicationContext(), jsonFilePath);
            mrTextViewDisplayEngine = new TextViewDisplayEngine((TextView) findViewById(R.id.playBackTextView), mrSyncWordEngine);

        } else {
            displayText += "\nSource Files not found :(";
            //audioFilePath = new File("/storage/emulated/0/Android/data/com.example.rupesh.mastread/files/./Board1/MediumY/Grade3/mast_read_book_2/page_12.mp3");
            //jsonFilePath = new File("/storage/emulated/0/Android/data/com.example.rupesh.mastread/files/./Board1/MediumY/Grade3/mast_read_book_2/page_12.json");

            //Log.d(TAG, "audio file size = " + audioFilePath.length());
            //Log.d(TAG, "json file size = " + jsonFilePath.length());

            //mrAudioPlayer = new MRAudioPlayer2(getApplicationContext(), audioFilePath);
            //mrSyncWordEngine = new MRSyncWordEngine(getApplicationContext(), jsonFilePath);
        }

        ((TextView) findViewById(R.id.playBackTextView)).setText(displayText);
        Log.d(TAG, "In PBA \n");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mrState == playState.PLAYING) {
            processOnPause();
            Button playPause = (Button) findViewById(R.id.playButton);
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

    private void togglePlayPauseButton(Button playPause) {
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
        //show the next available option
        playPause.setText(getPlayPauseString(mrState));
    }

    public void playPauseButton(View view) {

        togglePlayPauseButton((Button)view);
        /*switch (mrState) {
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
        playPause.setText(getPlayPauseString(mrState));*/

    }

    public void forwardButton(View view) {

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


    public void backwardButton(View view) {

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
        mrTextViewDisplayEngine.onPlay();
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

        mrTextViewDisplayEngine.onPause();
        mrTextViewDisplayEngine.display(handle);
        focus = -1;
    }

}
