package com.example.rupesh.mastread;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PlayBackActivity extends AppCompatActivity {

    String TAG = "PlayBackActivity";
    MRAudioPlayer mrAudioPlayer;

    private enum playState {
        STOPPED,
        PLAYING,
        PAUSED
    };

    playState mrState = playState.STOPPED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mrAudioPlayer = new MRAudioPlayer(getApplicationContext(), R.raw.chambers_schultz);
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
                case STOPPED:
                    mrAudioPlayer.mrPlay((float) 1.0);
                    mrState = playState.PLAYING;
                    break;
                case PLAYING:
                    mrAudioPlayer.mrPause();
                    mrState = playState.PAUSED;
                    break;
                case PAUSED:
                    mrAudioPlayer.mrPause();
                    mrState = playState.PLAYING;
                    break;
                default:
                    Log.d(TAG, "unknown state?\n");
            }
            Button playPause = (Button) view;
            //show the next available option
            playPause.setText(getPlayPauseString(mrState));

    }

}
