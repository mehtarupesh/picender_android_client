package com.grafixartist.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
  private OnItemClickListener mListener;
  private  RecyclerView mRecyclerView;

  public interface OnItemClickListener {
    public void onItemClick(View view, int position);
    public void onItemLongClick(View view, int position);
  }

  GestureDetector mGestureDetector;

  public RecyclerItemClickListener(Context context, RecyclerView view, OnItemClickListener listener) {
    mListener = listener;
    mRecyclerView = view;
    mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
      @Override public boolean onSingleTapUp(MotionEvent e) {
        return true;
      }
      @Override public void onLongPress(MotionEvent e) {
          View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());

          if(childView != null && mListener != null) {
              mListener.onItemLongClick(childView, mRecyclerView.getChildPosition(childView));
          }
      }

    });
  }

  @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
    View childView = view.findChildViewUnder(e.getX(), e.getY());
    if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
      mListener.onItemClick(childView, view.getChildPosition(childView));
      return true;
    }
    return false;
  }

  @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

  @Override
  public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

  }
}