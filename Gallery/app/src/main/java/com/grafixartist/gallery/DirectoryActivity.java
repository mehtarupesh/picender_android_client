package com.grafixartist.gallery;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class DirectoryActivity extends AppCompatActivity {

    GalleryAdapter mAdapter;
    RecyclerView mRecyclerView;

    String dirName;
    ArrayList<ImageModel> data = new ArrayList<>();
    String TAG = "DirectoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        Album album = (Album) getIntent().getParcelableExtra(Album.PAR_KEY);
        ArrayList<String> ids = album.getUriData();
        dirName = album.getName();
        int n_images = ids.size();

        /* Reverse the list while adding data
        * Ideally, the list passed from the parent intent should
        * contain information based on which we can sort the images (in this case
        * for example: DATE_ADDED/DATE_TAKEN
        * I am too lazy to do that for *now*, so assuming the list is in
        * ascending order of time and simply reversing it should provide the intended result of
        * this exercise:
        *
        * 'SHOW LATEST IMAGES FIRST'
        *
        *  */
        for (int i = n_images - 1; i >= 0; i--) {

            ImageModel imageModel = new ImageModel();
            imageModel.setName("Image " + Integer.toString(n_images - i));
            imageModel.setUrl(ids.get(i));
            data.add(imageModel);
        }

        Selector s = Selector.getInstance(data.size());
        for (int i = 0; i < data.size(); i++) {
            if(Metadata.loadCacheInfo(data.get(i).getUrl()) != null) {
                s.setOnCloudState(i, true);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new GalleryAdapter(DirectoryActivity.this, data);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {

                        Selector s = Selector.getInstance(data.size());

                        if (s.getMarkForDeletion(position) == true)
                            return;

                        /* if selected, then deselect else show image in full size */
                        if(s.getSelectedState(position) == true) {
                            s.toggleSelectedState(position);
                            GalleryAdapter.refreshImage(position);
                        } else {
                            Intent intent = new Intent(DirectoryActivity.this, DetailActivity.class);
                            intent.putParcelableArrayListExtra("data", data);
                            intent.putExtra("pos", position);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        Selector s = Selector.getInstance(data.size());

                        if (s.getMarkForDeletion(position) == true)
                            return;

                        ImageView mImg = (ImageView) view.findViewById(R.id.item_img);
                        s.toggleSelectedState(position);
                        GalleryAdapter.refreshImage(position);
                    }
                }));
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }


    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.d("-->", " >= 14");
            MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri) {
                    Log.d("ExternalStorage", "Scanned " + path + ":");
                    Log.d("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.d("-->", " < 14");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        Selector s = Selector.getInstance(data.size());

        ArrayList<Integer> l = s.getMarkForDeletionList(true);
        for(int i=0; i < l.size(); i++) {
            int index = l.get(i);

            if (Metadata.loadCacheInfo(data.get(index).getUrl()) != null) {

                    Metadata.delete(data.get(index).getUrl());
                    callBroadCast();

            } else {
                Log.d(TAG, "onDestroy INVALID STATE !!");
            }
        }
        Selector.destroyInstance();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Selector s = Selector.getInstance(data.size());

        if (id == R.id.action_selectall) {

            for(int i = 0; i <data.size(); i++) {

                if (s.getMarkForDeletion(i) == true)
                    continue;

                s.setSelectedState(i, true);
                GalleryAdapter.refreshImage(i);
            }

            return true;

        }else if (id == R.id.action_upload) {

            ArrayList<Integer> l = s.getSelectedStateList(true);

            for(int i=0; i < l.size(); i++) {
                int index = l.get(i);

                s.toggleSelectedState(index);
                Sender S = new Sender(dirName, data.get(index).getUrl(), index);
                S.execute();
            }

            return true;

        } else if (id == R.id.action_delete) {

            ArrayList<Integer> l = s.getSelectedStateList(true);

            for(int i=0; i < l.size(); i++) {
                int index = l.get(i);

                s.toggleSelectedState(index);
                s.setMarkForDeletion(index, true);

                if (s.getOnCloudState(index) == false) {
                    Sender S = new Sender(dirName, data.get(index).getUrl(), index);
                    S.execute();
                } else {
                    GalleryAdapter.refreshImage(index);
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
