package com.example.rupesh.mastread;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    ContentManagementEngine mrCme;
    MRNetworkEngine mrNe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mrCme = ContentManagementEngine.getContentManagementEngine(getApplicationContext());
        //mrNe = new MRNetworkEngine(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void playBackAudio(View view) {
        Log.d(TAG, "going to playback!\n");
        Intent intent = new Intent(MainActivity.this, PlayBackActivity.class);
        startActivity(intent);
    }

    public void cameraCapture(View view) {
        Log.d(TAG, "capturing image");
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }

    public void serverDLTest(View view) {
        Log.d(TAG, "Running server DL Test");
        //mrNe.testSampleDownload(getApplicationContext());

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
