package me.akulakovsky.ffsearch.app.entities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akulakovsky.ffsearch.app.providers.AppProvider;
import me.akulakovsky.ffsearch.app.utils.DBHelper;
import me.akulakovsky.ffsearch.app.utils.DateUtils;

public class MySearch implements Parcelable {

    private int id = -1;
    private LatLng startPoint;
    private LatLng endPoint;
    private List<Bearing> bearings;
    private String encodedRoute;
    private Date date;

    public MySearch() {}

    public MySearch(int id, LatLng startPoint, LatLng endPoint, List<Bearing> bearings, Date date) {
        this.id = id;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.bearings = bearings;
        this.date = date;
    }

    public MySearch(Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID));
        this.startPoint = new LatLng(
                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_START_LAT)),
                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_START_LNG))
        );
        this.endPoint = new LatLng(
                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_END_LAT)),
                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_END_LNG))
        );
        this.bearings = Bearing.load(id);
        this.date = DateUtils.toDate(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DATE)));
    }

    @Override
    public boolean equals(Object o) {
        return this.id == ((MySearch) o).getId();
    }

    public static MySearch newSearch(Context context, LatLng startPoint, Date date) {
        MySearch mySearch = new MySearch();
        mySearch.setStartPoint(startPoint);
        mySearch.setDate(date);
        mySearch.setId(mySearch.save(context));

        return mySearch;
    }

    public int save(Context context) {
        ContentValues values = new ContentValues();
        if (startPoint != null) {
            values.put(DBHelper.KEY_START_LAT, startPoint.latitude);
            values.put(DBHelper.KEY_START_LNG, startPoint.longitude);
        }

        if (endPoint != null) {
            values.put(DBHelper.KEY_END_LAT, endPoint.latitude);
            values.put(DBHelper.KEY_END_LNG, endPoint.longitude);
        }

        //values.put(DBHelper.KEY_BEARINGS, bearings.toString());
        values.put(DBHelper.KEY_DATE, DateUtils.toString(date));

        if (id == -1) {
            Uri uri = context.getContentResolver().insert(AppProvider.SEARCHES_URI, values);
            id = (int) ContentUris.parseId(uri);
        } else {
            context.getContentResolver().update(AppProvider.SEARCHES_URI, values, DBHelper.KEY_ID + "=" + id, null);
        }
        return id;
    }

    public void delete(Context context) {
        context.getContentResolver().delete(AppProvider.SEARCHES_URI, DBHelper.KEY_ID + "=" + this.id, null);
        context.getContentResolver().delete(AppProvider.SEARCH_SETS_URI, DBHelper.KEY_MY_SEARCH_ID + "=" + this.id, null);
        context.getContentResolver().delete(AppProvider.BEARINGS_URI, DBHelper.KEY_MY_SEARCH_ID + "=" + this.id, null);
    }

    public static MySearch getSearchById(Context context, int searchId) {
        MySearch mySearch = null;
        Cursor cursor = context.getContentResolver().query(
                AppProvider.SEARCHES_URI,
                null,
                DBHelper.KEY_ID + "=" + searchId,
                null,
                null);

        if (cursor.moveToNext()) {
            mySearch = new MySearch(cursor);
        }
        cursor.close();
        return mySearch;
    }

    public static void saveRoute(Context context, int searchId, List<LatLng> route) {
        context.getContentResolver().delete(AppProvider.SEARCH_SETS_URI, DBHelper.KEY_MY_SEARCH_ID + "=" + searchId, null);
        for (LatLng latLng: route) {
            ContentValues values = new ContentValues();
            values.put(DBHelper.KEY_MY_SEARCH_ID, searchId);
            values.put(DBHelper.KEY_LAT, latLng.latitude);
            values.put(DBHelper.KEY_LNG, latLng.longitude);

            context.getContentResolver().insert(AppProvider.SEARCH_SETS_URI, values);
        }
    }

    public static List<LatLng> getRoute(Context context, int searchId) {
        List<LatLng> latLngList = new ArrayList<LatLng>();
        Cursor cursor = context.getContentResolver().query(
                AppProvider.SEARCH_SETS_URI,
                null,
                DBHelper.KEY_MY_SEARCH_ID + "=" + searchId,
                null,
                null);

        while (cursor.moveToNext()) {
            latLngList.add(new LatLng(
                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_LAT)),
                cursor.getDouble(cursor.getColumnIndex(DBHelper.KEY_LNG))
            ));
        }
        cursor.close();
        Log.d(TAG, "LOADED POINTS = " + latLngList.size());
        return latLngList;
    }

    public int getId() {
        return id;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public Date getDate() {
        return date;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }

    public List<Bearing> getBearings() {
        return bearings;
    }

    public void setBearings(List<Bearing> bearings) {
        this.bearings = bearings;
    }

    public String getEncodedRoute() {
        return encodedRoute;
    }

    public void setEncodedRoute(String encodedRoute) {
        this.encodedRoute = encodedRoute;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeParcelable(startPoint, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeParcelable(endPoint, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeTypedList(bearings);
        parcel.writeSerializable(date);
    }

    private void readParcel(Parcel in) {
        id = in.readInt();
        startPoint = in.readParcelable(LatLng.class.getClassLoader());
        endPoint = in.readParcelable(LatLng.class.getClassLoader());
        bearings = new ArrayList<>();
        in.readTypedList(bearings, Bearing.CREATOR);
        date = (Date) in.readSerializable();
    }

    private MySearch(Parcel in) {
        readParcel(in);
    }

    public static final Parcelable.Creator<MySearch> CREATOR = new Parcelable.Creator<MySearch>() {

        public MySearch createFromParcel(Parcel in) {
            return new MySearch(in);
        }

        @Override
        public MySearch[] newArray(int size) {
            return new MySearch[size];
        }
    };

    public static final String TAG = MySearch.class.getName();
}
