package com.example.rupesh.mastread;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

/**
 * Created by rupesh on 5/6/17.
 */
public class MRAudioPlayer {

    private SoundPool spAudioPlayer;
    private int soundId;
    private int streamId;
    private Boolean loadComplete = false;

    String TAG = "MRAudioPlayer";

    public MRAudioPlayer(Context context,
                         int resId) {

        // @TargetAPI 21
        //SoundPool.Builder spBuilder = new SoundPool.Builder();
        spAudioPlayer = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = spAudioPlayer.load(context, resId, 1);
        loadComplete = false;
        spAudioPlayer.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loadComplete = true;
                Log.d(TAG, "load complete!!\n");

            }
        });
    }

    public void mrPlay(float rate) {

        if (loadComplete == false) {
            Log.d(TAG, "still loading!!\n");
            return;
        }
        streamId = spAudioPlayer.play(this.soundId,
                (float) 0.5,
                (float) 0.5,
                1,
                1,
                rate);
        Log.d(TAG, "Playing content with streamId :" + streamId);
    }

    public void mrPause() {
        spAudioPlayer.pause(this.streamId);
    }

    public void mrResume() {
        spAudioPlayer.resume(this.streamId);
    }

    //rate 	float: playback rate (1.0 = normal playback, range 0.5 to 2.0)
    public void mrSetRate(float rate) {
        spAudioPlayer.setRate(this.streamId, rate);
    }

}
