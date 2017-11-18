package com.example.rupesh.mastread;

import android.app.AlertDialog;
import android.content.Context;
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

import java.util.ArrayList;

public class PageBrowser extends AppCompatActivity {

    private ListView mrListView;
    private static final String TAG = "PageBrowser";
    public static final String TOKEN = "PAGE_BROWSER";
    private Context pageBrowserContext;
    private TextBook tBook;
    private ContentManagementEngine cme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pageBrowserContext = this;

        mrListView = (ListView) findViewById(R.id.pageListView);

        Bundle b = getIntent().getExtras();
        String resId = b.getString(PageBrowser.TOKEN);

        Log.d(TAG, "resId =" + resId);

        cme = ContentManagementEngine.getContentManagementEngine(this);
        tBook = cme.getTextBook(resId);
        ArrayList<Integer> pageNumbers = new ArrayList<>();

        for (int i = 0; i < tBook.getPages().size(); i++) {
            pageNumbers.add(tBook.getPages().get(i).getNumber());
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, pageNumbers);
        mrListView.setAdapter(adapter);
        mrListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Integer itemValue = (Integer) mrListView.getItemAtPosition(position);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case DialogInterface.BUTTON_NEGATIVE:
                                Log.d(TAG, "Getting Page # ...  " + itemValue);

                                Page p = cme.processPageRequest(tBook, itemValue);

                                if (p.getAudioIsOnDevice()) {
                                    Log.d(TAG, "cme says audio file exists, going to playback!\n");
                                    Intent intent = new Intent(pageBrowserContext, PlayBackActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("PAGE_INFO", p);
                                    startActivity(intent);
                                }
                                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                //intent.putExtra(MainActivity.RESOURCE_ID, curFile.getAbsolutePath());
                                //getApplicationContext().startActivity(intent);

                                //Yes button clicked
                                break;

                            case DialogInterface.BUTTON_POSITIVE:
                                //finish();
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(pageBrowserContext);
                builder.setMessage("Get Page " + itemValue + " ?").setPositiveButton("Cancel", dialogClickListener)
                        .setNegativeButton("OK", dialogClickListener).show();

            }
        });


    }

}
