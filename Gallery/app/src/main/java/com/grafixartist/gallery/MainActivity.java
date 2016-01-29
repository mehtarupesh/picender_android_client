package com.grafixartist.gallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    ArrayList<Album> albumList;
    ListView dirView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        albumList = getAlbumList();

        final ArrayList<String> dirNames = new ArrayList<String>();

        for(int i = 0; i < albumList.size(); i++)
            dirNames.add(albumList.get(i).getName());

        dirView = (ListView)findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, dirNames);

        dirView.setAdapter(adapter);

        dirView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*String val = (String)dirView.getItemAtPosition(position);
                  Toast.makeText(getApplicationContext(),
                        "Pos = " +position+" DIR = "+val,
                        Toast.LENGTH_SHORT).show();
                */
                Intent intent = new Intent(MainActivity.this, DirectoryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Album.PAR_KEY, albumList.get(position));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }

    public ArrayList<Album> getAlbumList(){

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        HashMap<String,Album> hmap = new HashMap<String, Album>();

        albumList = new ArrayList<Album>();

        int count = cursor.getCount();
        int i = 0;
        if (cursor != null) {
            int dirColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            while (cursor.moveToNext()) {

                String albumName = cursor.getString(dirColumnIndex);
                String uriData = cursor.getString(dataColumnIndex);

                if(hmap.containsKey(albumName)) {

                    Album a = hmap.get(albumName);
                    a.getUriData().add(uriData);

                }else {
                    Album a = new Album(albumName, new ArrayList<String>());
                    a.getUriData().add(uriData);
                    hmap.put(albumName, a);
                    albumList.add(a);
                }

                //Log.d(TAG, "DIR -------> " + cursor.getString(dirColumnIndex));
                //Log.d(TAG, "DATA -------> " + cursor.getString(dataColumnIndex));

                //Log.d(TAG, "---------------------------------------------");

                i++;
            }
            cursor.close();
            Log.d(TAG, "count = " + Integer.toString(count));

        }else
            Log.d(TAG, "EMPTY!!");

        return albumList;
    }

    public void printAlbums(ArrayList<Album> ids) {

        Log.d(TAG, "STATE ---> " + Environment.getExternalStorageState());
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d(TAG, "PATH ---> " + root);

        int albumSize = ids.size();
        Log.d(TAG, "unique directories = "+ Integer.toString(albumSize));

        for(int i =0; i < albumSize; i++) {

            Album iter = ids.get(i);

            Log.d(TAG, "ALBUM = "+ iter.getName());

            int j;
            for(j = 0; j <iter.getUriData().size(); j++)
                Log.d(TAG, iter.getUriData().get(j));
            Log.d(TAG, "Count = " + Integer.toString(iter.getUriData().size()));
            Log.d(TAG, "-------------------------------------------");
        }
    }
}
