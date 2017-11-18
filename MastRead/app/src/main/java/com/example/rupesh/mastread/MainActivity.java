package com.example.rupesh.mastread;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    ContentManagementEngine mrCme;
    public static String RESOURCE_ID = "RES_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle b = getIntent().getExtras();

        mrCme = ContentManagementEngine.getContentManagementEngine(getApplicationContext());
        Button camCapture = (Button)findViewById(R.id.camCaptureButton);
        Button pageSelect = (Button)findViewById(R.id.selectPageButton);


        if (b != null) {
            String resource_id = b.getString(MainActivity.RESOURCE_ID);

            if (resource_id != null) {
                Log.d(TAG, "Received signal to download res_id = " + resource_id);
                mrCme.setCurrentResourceId(resource_id);
                mrCme.downloadTextBookWithoutAudio(getApplicationContext(), resource_id);
            }
        }

        if (mrCme.getCurrentResourceId() != null) {
            String[] components = mrCme.getCurrentResourceId().split("/");
            int len = components.length;

            String displayText = "<Please Select Book>";

            if (len > 0) {

                displayText = "";
                Boolean parseText = false;
                for (int i = 0; i < len; i++) {

                    if (components[i].compareTo("BOOKS") == 0) {
                        parseText = true;
                    }
                    else if (parseText == true) {
                        displayText = displayText + components[i] + "\n";
                    }
                }
            }
            Log.d(TAG, "Display Text = " + displayText);
            ((TextView) findViewById(R.id.textView3)).setText(displayText);

            camCapture.setVisibility(View.VISIBLE);
            pageSelect.setVisibility(View.VISIBLE);
        } else {
            camCapture.setVisibility(View.GONE);
            pageSelect.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void cameraCapture(View view) {
        Log.d(TAG, "capturing image");
        if (mrCme.getCurrentResourceId() != null) {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(view.getContext(), "Please Select TextBook to MastRead", Toast.LENGTH_SHORT).show();
        }
    }

    public void bookBrowser(View view) {
        Log.d(TAG, "Running Book Select test");
/*
        String board = "Board2";
        String medium = "MediumX";
        String grade = "Grade1";
        String bookName = "mast_read_book_3";

        String board = "Board1";
        String medium = "MediumY";
        String grade = "Grade3";
        String bookName = "mast_read_book_2";
        mrCme.downloadTextBookWithoutAudio(getApplicationContext(), board, medium, grade, bookName);

*/

        String bookDir = MRResource.getAbsoluteFilePath("./BOOKS");
        Intent intent = new Intent(getApplicationContext(), BookBrowser.class);
        intent.putExtra(BookBrowser.TOKEN, bookDir);
        getApplicationContext().startActivity(intent);

    }

    public void pageBrowser(View view) {

        if (mrCme.getCurrentResourceId() != null) {
            Log.d(TAG, "Running Page Select test");
            String resId = mrCme.getCurrentResourceId();
            Intent intent = new Intent(getApplicationContext(), PageBrowser.class);
            intent.putExtra(PageBrowser.TOKEN, resId);
            getApplicationContext().startActivity(intent);
        } else {
            Toast.makeText(view.getContext(), "Please Select TextBook to MastRead", Toast.LENGTH_SHORT).show();
        }



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
