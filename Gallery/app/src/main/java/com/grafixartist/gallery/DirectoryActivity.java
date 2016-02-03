package com.grafixartist.gallery;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
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

        for (int i = 0; i < ids.size(); i++) {

            ImageModel imageModel = new ImageModel();
            imageModel.setName("Image " + i);
            imageModel.setUrl(ids.get(i));
            data.add(imageModel);

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

                        Selector s = Selector.getInstance(data.size(), false);
                        ImageView mImg = (ImageView) view.findViewById(R.id.item_img);
                        /* if selected, deselect */
                        if(s.getState(position) == true) {
                            mImg.clearColorFilter();
                            s.toggle(position);
                        } else {
                            Intent intent = new Intent(DirectoryActivity.this, DetailActivity.class);
                            intent.putParcelableArrayListExtra("data", data);
                            intent.putExtra("pos", position);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        Selector s = Selector.getInstance(data.size(), false);
                        ImageView mImg = (ImageView) view.findViewById(R.id.item_img);

                        /* toggle state */
                        if(s.getState(position) == false) {
                            mImg.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN);
                        }else
                            mImg.clearColorFilter();

                        s.toggle(position);

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
        /* reset selector */
        Selector s = Selector.getInstance(data.size(), false);
        ArrayList<Integer> l = s.getList(true);

        GridLayoutManager layoutManager = ((GridLayoutManager)mRecyclerView.getLayoutManager());
        for(int i = 0; i <l.size(); i++) {
            View childView = layoutManager.getChildAt(l.get(i));
            ImageView mImg = null;
            if (childView != null) mImg = (ImageView) childView.findViewById(R.id.item_img);
            if (mImg != null) mImg.clearColorFilter();
        }
        Selector.destroyInstance();

        super.onStop();
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

        if (id == R.id.action_selectall) {

            Selector s = Selector.getInstance(data.size(), false);

            GridLayoutManager layoutManager = ((GridLayoutManager)mRecyclerView.getLayoutManager());
            for(int i = 0; i <data.size(); i++) {
                View childView = layoutManager.getChildAt(i);
                ImageView mImg = null;
                if (childView != null) mImg = (ImageView) childView.findViewById(R.id.item_img);
                if (mImg != null) mImg.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN);

                s.setState(i, true);
            }
            return true;

        }else if (id == R.id.action_upload) {
            Selector s = Selector.getInstance(data.size(), false);
            ArrayList<Integer> l = s.getList(true);

            Log.d(TAG, "Sending...");
            for(int i=0; i < l.size(); i++) {

                int index = l.get(i);

                Log.d(TAG, data.get(index).getUrl());
                Log.d(TAG, "------------------------");
            }

            return true;
        } else if (id == R.id.action_delete) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
