package me.akulakovsky.ffsearch.app.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ffsearch.db";
    private static final int DATABASE_VERSION = 10;

    public static final String KEY_ID = "_id";
    public static final String KEY_START_LAT = "start_lat";
    public static final String KEY_START_LNG = "start_lng";
    public static final String KEY_END_LAT = "end_lat";
    public static final String KEY_END_LNG = "end_lng";
    public static final String KEY_BEARINGS = "bearings";
    public static final String KEY_DATE = "date";

    public static final String KEY_MY_SEARCH_ID = "my_search_id";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";

    public static final String KEY_BEARING = "bearing";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_COLOR = "color";

    public static final String TABLE_SEARCHES = "searches";

    private static final String TABLE_SEARCHES_CREATE = "CREATE TABLE " + TABLE_SEARCHES + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_START_LAT + " REAL, "
            + KEY_START_LNG + " REAL, "
            + KEY_END_LAT + " REAL, "
            + KEY_END_LNG + " REAL, "
            + KEY_BEARINGS + " REAL, "
            + KEY_DATE + " TEXT);";

    private static final String TABLE_SEARCHES_DROP = "DROP TABLE IF EXISTS " + TABLE_SEARCHES;

    public static final String TABLE_SEARCH_SETS = "search_sets";

    private static final String TABLE_SEARCH_SETS_CREATE = "CREATE TABLE " + TABLE_SEARCH_SETS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_MY_SEARCH_ID + " INTEGER, "
            + KEY_LAT + " REAL, "
            + KEY_LNG + " REAL);";

    private static final String TABLE_SEARCH_SETS_DROP = "DROP TABLE IF EXISTS " + TABLE_SEARCH_SETS;

    public static final String TABLE_BEARINGS = "bearings";

    private static final String TABLE_BEARINGS_CREATE = "CREATE TABLE " + TABLE_BEARINGS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_MY_SEARCH_ID + " INTEGER, "
            + KEY_BEARING + " REAL, "
            + KEY_NOTES + " TEXT, "
            + KEY_COLOR + " INTEGER);";

    private static final String TABLE_BEARINGS_DROP = "DROP TABLE IF EXISTS " + TABLE_BEARINGS;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_SEARCHES_CREATE);
        db.execSQL(TABLE_SEARCH_SETS_CREATE);
        db.execSQL(TABLE_BEARINGS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL(TABLE_SEARCHES_DROP);
        db.execSQL(TABLE_SEARCH_SETS_DROP);
        db.execSQL(TABLE_BEARINGS_DROP);
        onCreate(db);
    }
}
