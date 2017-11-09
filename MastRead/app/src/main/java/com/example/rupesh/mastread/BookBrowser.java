package com.example.rupesh.mastread;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class BookBrowser extends AppCompatActivity {

    private ListView mrListView;
    public static final String TOKEN= "token";
    private static final String TAG = "BookBrowser";
    private File curFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mrListView = (ListView) findViewById(R.id.listView);

        Bundle b = getIntent().getExtras();
        String bookDir = b.getString(BookBrowser.TOKEN);

        Log.d(TAG, "bookDir =" + bookDir);

        curFile = new File(bookDir);

        if (curFile.isDirectory()) {


            ArrayList<String> values = new ArrayList<>();

            File[] files = curFile.listFiles();
            Boolean recurseToNextDirectory = true;

            for (int i = 0; i < files.length; i++) {

                Log.d(TAG, files[i].getAbsolutePath());
                if (!files[i].isDirectory())
                {
                    recurseToNextDirectory = false;
                    break;
                }
                values.add(files[i].getName());
            }

            if (recurseToNextDirectory && files.length > 0) {

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, values);
                mrListView.setAdapter(adapter);
                mrListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String itemValue = (String) mrListView.getItemAtPosition(position);
                        Intent intent = new Intent(getApplicationContext(), BookBrowser.class);
                        intent.putExtra(BookBrowser.TOKEN, curFile.getAbsolutePath() + "/" + itemValue);
                        getApplicationContext().startActivity(intent);
                    }
                });
            } else {

                Log.d(TAG, "Selected --> " + curFile.getAbsolutePath());

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which){
                            case DialogInterface.BUTTON_NEGATIVE:
                                Log.d(TAG, "Downloading ...  " + curFile.getName());

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra(MainActivity.RESOURCE_ID, curFile.getAbsolutePath());
                                getApplicationContext().startActivity(intent);

                                //Yes button clicked
                                break;

                            case DialogInterface.BUTTON_POSITIVE:
                                finish();
                                //No button clicked
                                break;
                        }
                    }
                };

                String[] components = curFile.getAbsolutePath().split("/");
                int len = components.length;

                String displayText = curFile.getAbsolutePath();

                if (len > 0)
                    displayText = components[len - 1];

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Select " + displayText + " ?").setPositiveButton("Cancel", dialogClickListener)
                        .setNegativeButton("OK", dialogClickListener).show();

            }
        }

        else {

            Log.d(TAG, "INVALID param!!!");
        }
    }

}
