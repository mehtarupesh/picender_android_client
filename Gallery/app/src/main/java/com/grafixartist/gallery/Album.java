package com.grafixartist.gallery;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by rupesh on 1/29/16.
 */

public class Album implements Parcelable {

    String name;
    ArrayList<String> uriData;
    public static final String PAR_KEY="Album";

    public Album() {

        uriData = new ArrayList<String>();

    }

    public Album(String name, ArrayList<String> list) {

        this.name = name;
        this.uriData = list;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getUriData() {
        return uriData;
    }

    public void setUriData(ArrayList<String> uriData) {
        this.uriData = uriData;
    }

    public int getAlbumSize() {
        return uriData.size();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeStringList(uriData);
    }

    public static final Parcelable.Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            Album mAlbum = new Album();
            mAlbum.name = source.readString();
            source.readStringList(mAlbum.uriData);
            return mAlbum;

        }
        public Album[] newArray(int size) {
            return new Album[size];
        }

    };
}
