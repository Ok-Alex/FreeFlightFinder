package me.akulakovsky.ffsearch.app.entities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import io.realm.RealmObject;
import me.akulakovsky.ffsearch.app.providers.AppProvider;
import me.akulakovsky.ffsearch.app.utils.DBHelper;

/**
 * Created by Ok-Alex on 7/5/17.
 */

public class Bearing implements Parcelable {

    private long id = -1;
    private long searchId;
    private int color;
    private String description;
    private double bearing;

    public Bearing(int color, String description, double bearing) {
        this.color = color;
        this.description = description;
        this.bearing = bearing;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public long getSearchId() {
        return searchId;
    }

    public void setSearchId(long searchId) {
        this.searchId = searchId;
    }

    public static List<Bearing> load(long searchId) {
        return null;
    }

    public long save(Context context) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.KEY_MY_SEARCH_ID, searchId);
        values.put(DBHelper.KEY_COLOR, color);
        values.put(DBHelper.KEY_NOTES, description);
        values.put(DBHelper.KEY_BEARING, bearing);

        if (id == -1) {
            Uri uri = context.getContentResolver().insert(AppProvider.BEARINGS_URI, values);
            id = ContentUris.parseId(uri);
        } else {
            context.getContentResolver().update(AppProvider.BEARINGS_URI, values, DBHelper.KEY_ID + "=" + id, null);
        }
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeLong(searchId);
        parcel.writeInt(color);
        parcel.writeString(description);
        parcel.writeDouble(bearing);
    }

    private void readParcel(Parcel in) {
        id = in.readLong();
        searchId = in.readLong();
        color = in.readInt();
        description = in.readString();
        bearing = in.readDouble();
    }

    private Bearing(Parcel in) {
        readParcel(in);
    }

    public static final Parcelable.Creator<Bearing> CREATOR = new Parcelable.Creator<Bearing>() {

        public Bearing createFromParcel(Parcel in) {
            return new Bearing(in);
        }

        @Override
        public Bearing[] newArray(int size) {
            return new Bearing[size];
        }
    };
}
