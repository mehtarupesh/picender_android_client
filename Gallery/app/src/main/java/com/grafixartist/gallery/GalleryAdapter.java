package com.grafixartist.gallery;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman19 on 10/22/15.
 */
public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<ImageModel> data = new ArrayList<>();
    static MyItemHolder imgList[];
    static String TAG = "GalleryAdapter";

    public GalleryAdapter(Context context, List<ImageModel> data) {
        this.context = context;
        this.data = data;
        imgList = new MyItemHolder[data.size()];

        assert(imgList.length == data.size());
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View v;
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item, parent, false);
            viewHolder = new MyItemHolder(v);

        Log.d(TAG, "OnCreateViewholder : viewtype = " + Integer.toString(viewType));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Glide.with(context).load(data.get(position).getUrl())
                    .thumbnail(0.5f)
                    .override(200, 200)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) /* cache only transformed image */
                    .dontAnimate() /* to avoid getting pics washed out */
                    .into(((MyItemHolder) holder).mImg);

            /* refresh image */
            Selector s = Selector.getInstance(data.size());
            if(s.getSelectedState(position) == true) {
                ((MyItemHolder) holder).mImg.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN);
                //((MyItemHolder) holder).mImg_state.setVisibility(View.VISIBLE);
                //((MyItemHolder) holder).mImg_state.setImageResource(R.drawable.tick);

            } else {
                ((MyItemHolder) holder).mImg.clearColorFilter(); /* to avoid mass pic corruption */
                //((MyItemHolder) holder).mImg_state.setVisibility(View.INVISIBLE);
            }

        if (imgList[position] == null) {
            Log.d(TAG, "OnBindViewholder : url = " + data.get(position).getUrl());
            Log.d(TAG, "OnBindViewholder : position = " + Integer.toString(position));
            imgList[position] = (MyItemHolder)holder;
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyItemHolder extends RecyclerView.ViewHolder {
        ImageView mImg;
        ImageView mImg_state;

        public MyItemHolder(View itemView) {
            super(itemView);

            mImg = (ImageView) itemView.findViewById(R.id.item_img);
            mImg_state = (ImageView) itemView.findViewById(R.id.item_img_state);
        }

    }

    public static void markImage(int position) {

        MyItemHolder i = imgList[position];

        if (i != null) {
            i.mImg_state.setVisibility(View.VISIBLE);
            i.mImg_state.setImageResource(R.drawable.tick);
        }else
            Log.d(TAG, "NULL imgList pos = " + Integer.toString(position));
    }

    public static void refreshImage(int position) {

        if(position >= imgList.length) {
            Log.d(TAG, "refreshImage invalid positon");
            return;
        }

        Selector s = Selector.getInstance(imgList.length);
        MyItemHolder i = imgList[position];

        if (s.getSelectedState(position) == true) {
            i.mImg.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN);
        } else
            i.mImg.clearColorFilter(); /* to avoid mass pic corruption */

    }
}
