package com.example.rupesh.mastread;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * Created by rupesh on 5/7/17.
 */
public class MRAudioPlayer2 {
    private MediaPlayer mediaPlayer;
    private String TAG = "MRAudioPlayer2";
    private Boolean isLoaded = false;
    private float currentPlayBackRate;
    private static final float playBackdelta   = 0.25f;
    private static final float minPlayBackRate = 0.5f;
    private static final float maxPlayBackRate = 2.0f;

    private void initMediaPlayer() {

        assert (mediaPlayer != null);

        isLoaded = true;
        Log.d(TAG, "MRAudioPlayer2 create DONE!\n");

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onError called with error type = " + what);
                Log.d(TAG, "onError called with extra info = " + extra);
                isLoaded = false;
                return false;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "Releasing mediaplayer resources");
                //isLoaded = false;
                //mp.release();
                //mp.reset();
            }
        });

        currentPlayBackRate = 1.0f;
    }

    public MRAudioPlayer2(Context context,
                          File file) {

        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file));

        if (mediaPlayer == null) {
            Log.d(TAG, "MRAudioPlayer2 create FAILED!\n");
            return;
        }
        initMediaPlayer();
    }

    public MRAudioPlayer2(Context context,
                         int resId) {

        mediaPlayer = MediaPlayer.create(context, resId);

        if (mediaPlayer == null) {
            Log.d(TAG, "MRAudioPlayer2 create FAILED!\n");
            return;
        }
        initMediaPlayer();
    }

    private void initMediaPlayer(MediaPlayer.OnCompletionListener listener) {

        assert (mediaPlayer != null);

        isLoaded = true;
        Log.d(TAG, "MRAudioPlayer2 create DONE!\n");

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onError called with error type = " + what);
                Log.d(TAG, "onError called with extra info = " + extra);
                isLoaded = false;
                return false;
            }
        });

        mediaPlayer.setOnCompletionListener(listener);

        currentPlayBackRate = 1.0f;
    }

    public MRAudioPlayer2(Context context,
                          File file, MediaPlayer.OnCompletionListener listener) {

        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file));

        if (mediaPlayer == null) {
            Log.d(TAG, "MRAudioPlayer2 create FAILED!\n");
            return;
        }
        initMediaPlayer(listener);
    }

    public MRAudioPlayer2(Context context,
                          int resId, MediaPlayer.OnCompletionListener listener) {

        mediaPlayer = MediaPlayer.create(context, resId);

        if (mediaPlayer == null) {
            Log.d(TAG, "MRAudioPlayer2 create FAILED!\n");
            return;
        }
        initMediaPlayer(listener);
    }

    public void mrPlay() {

        if (!isLoaded) {
            return;
        }
        this.mrSetRate(currentPlayBackRate);
        mediaPlayer.start();
    }

    public void mrPause() {

        if (!isLoaded) {
            return;
        }

        mediaPlayer.pause();
        Log.d(TAG, "at " + mediaPlayer.getCurrentPosition());
        Log.d(TAG, "out of  " + mediaPlayer.getDuration());
    }

    public void mrResume() {

        if (!isLoaded) {
            return;
        }

        mediaPlayer.start();
    }

    public void mrIncrementPlayBackRate() {
        if (currentPlayBackRate + playBackdelta < maxPlayBackRate) {
            currentPlayBackRate += playBackdelta;
            this.mrSetRate(currentPlayBackRate);
        }
        else {
            Log.d(TAG, "cannot increment, currentRate : " + currentPlayBackRate);
        }
    }

    public void  mrDecrementPlayBackRate() {
        if (currentPlayBackRate - playBackdelta >= minPlayBackRate) {
            currentPlayBackRate -= playBackdelta;
            this.mrSetRate(currentPlayBackRate);
        }
        else {
            Log.d(TAG, "cannot decrement, currentRate : " + currentPlayBackRate);
        }
    }

    //rate 	float: playback rate (1.0 = normal playback, range 0.5 to 2.0)
    public void mrSetRate(float rate) {

        if (!isLoaded) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            Log.d(TAG, "Setting rate to " + rate);
            PlaybackParams params = mediaPlayer.getPlaybackParams();
            params.setSpeed(rate);
            mediaPlayer.setPlaybackParams(params);
        }

    }

    // returns total duration in ms
    public int getDuration() {
        if (!isLoaded) {
            return -1;
        }

        return mediaPlayer.getDuration();
    }

    // returns current position in milliseconds
    public int getCurrentPosition() {
        if (!isLoaded) {
            return -1;
        }

        return mediaPlayer.getCurrentPosition();
    }

    // returns current position in milliseconds
    public void setPosition(int seekTime) {
        if (!isLoaded) {
            return;
        }

        if (seekTime >= 0 && seekTime < mediaPlayer.getDuration()) {
            mediaPlayer.seekTo(seekTime);
        } else {
            Log.d(TAG, "Seek time out of bounds");
            Log.d(TAG, "seeking " + seekTime);
            Log.d(TAG, "duration  " + mediaPlayer.getDuration());
        }
    }

}
