package me.akulakovsky.ffsearch.app.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import me.akulakovsky.ffsearch.app.utils.DBHelper;

public class AppProvider extends ContentProvider {

    private static final String AUTHORITY =
            "me.akulakovsky.ffsearch.app.providers.AppProvider";

    public static final Uri SEARCHES_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_SEARCHES);
    public static final Uri SEARCH_SETS_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_SEARCH_SETS);
    public static final Uri BEARINGS_URI = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.TABLE_BEARINGS);

    private static final String SEARCHES_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + DBHelper.TABLE_SEARCHES;

    private static final String SEARCHES_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + DBHelper.TABLE_SEARCHES;

    private static final String SEARCH_SETS_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + DBHelper.TABLE_SEARCH_SETS;

    private static final String SEARCH_SETS_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + DBHelper.TABLE_SEARCH_SETS;

    private static final String BEARINGS_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + DBHelper.TABLE_BEARINGS;

    private static final String BEARINGS_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + DBHelper.TABLE_BEARINGS;

    private static final int SEARCHES = 1;
    private static final int SEARCHES_ID = 2;
    private static final int SEARCH_SETS = 3;
    private static final int SEARCH_SETS_ID = 4;
    private static final int BEARINGS = 5;
    private static final int BEARINGS_ID = 6;

    private static final UriMatcher sURIMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, DBHelper.TABLE_SEARCHES, SEARCHES);
        sURIMatcher.addURI(AUTHORITY, DBHelper.TABLE_SEARCHES + "/#", SEARCHES_ID);
        sURIMatcher.addURI(AUTHORITY, DBHelper.TABLE_SEARCH_SETS, SEARCH_SETS);
        sURIMatcher.addURI(AUTHORITY, DBHelper.TABLE_SEARCH_SETS + "/#", SEARCH_SETS_ID);
        sURIMatcher.addURI(AUTHORITY, DBHelper.TABLE_BEARINGS, BEARINGS);
        sURIMatcher.addURI(AUTHORITY, DBHelper.TABLE_BEARINGS + "/#", BEARINGS_ID);
    }

    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case SEARCHES:
                queryBuilder.setTables(DBHelper.TABLE_SEARCHES);
                break;
            case SEARCHES_ID:
                queryBuilder.setTables(DBHelper.TABLE_SEARCHES);
                queryBuilder.appendWhere(DBHelper.KEY_ID + "="
                        + uri.getLastPathSegment());
                break;
            case SEARCH_SETS:
                queryBuilder.setTables(DBHelper.TABLE_SEARCH_SETS);
                break;
            case SEARCH_SETS_ID:
                queryBuilder.setTables(DBHelper.TABLE_SEARCH_SETS);
                queryBuilder.appendWhere(DBHelper.KEY_ID + "="
                        + uri.getLastPathSegment());
                break;
            case BEARINGS:
                queryBuilder.setTables(DBHelper.TABLE_BEARINGS);
                break;
            case BEARINGS_ID:
                queryBuilder.setTables(DBHelper.TABLE_BEARINGS);
                queryBuilder.appendWhere(DBHelper.KEY_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCHES:
                return SEARCHES_TYPE;
            case SEARCHES_ID:
                return SEARCHES_ITEM_TYPE;
            case SEARCH_SETS:
                return SEARCH_SETS_TYPE;
            case SEARCH_SETS_ID:
                return SEARCH_SETS_ITEM_TYPE;
            case BEARINGS:
                return SEARCH_SETS_TYPE;
            case BEARINGS_ID:
                return SEARCH_SETS_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();

        long id = 0;

        Uri mUri = null;

        switch (uriType) {
            case SEARCHES:
            case SEARCHES_ID:
                id = sqlDB.insert(DBHelper.TABLE_SEARCHES,
                        null, contentValues);
                mUri = Uri.parse(SEARCHES_URI + "/" + id);
                break;
            case SEARCH_SETS:
            case SEARCH_SETS_ID:
                id = sqlDB.insert(DBHelper.TABLE_SEARCH_SETS,
                        null, contentValues);
                mUri = Uri.parse(SEARCH_SETS_URI + "/" + id);
                break;
            case BEARINGS:
            case BEARINGS_ID:
                id = sqlDB.insert(DBHelper.TABLE_BEARINGS,
                        null, contentValues);
                mUri = Uri.parse(BEARINGS_URI + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return mUri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {

        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        String id = null;

        switch (sURIMatcher.match(uri)) {
            case SEARCHES:
                rowsDeleted = sqlDB.delete(DBHelper.TABLE_SEARCHES,
                        s,
                        strings);
                break;

            case SEARCHES_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(s)) {
                    rowsDeleted = sqlDB.delete(DBHelper.TABLE_SEARCHES,
                            DBHelper.KEY_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DBHelper.TABLE_SEARCHES,
                            DBHelper.KEY_ID + "=" + id
                                    + " and " + s,
                            strings);
                }
                break;

            case SEARCH_SETS:
                rowsDeleted = sqlDB.delete(DBHelper.TABLE_SEARCH_SETS,
                        s,
                        strings);
                break;

            case SEARCH_SETS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(s)) {
                    rowsDeleted = sqlDB.delete(DBHelper.TABLE_SEARCH_SETS,
                            DBHelper.KEY_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DBHelper.TABLE_SEARCH_SETS,
                            DBHelper.KEY_ID + "=" + id
                                    + " and " + s,
                            strings);
                }
            case BEARINGS:
                rowsDeleted = sqlDB.delete(DBHelper.TABLE_BEARINGS,
                        s,
                        strings);
                break;

            case BEARINGS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(s)) {
                    rowsDeleted = sqlDB.delete(DBHelper.TABLE_BEARINGS,
                            DBHelper.KEY_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DBHelper.TABLE_BEARINGS,
                            DBHelper.KEY_ID + "=" + id
                                    + " and " + s,
                            strings);
                }
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        String id = null;

        switch (uriType) {
            case SEARCHES:
                rowsUpdated = sqlDB.update(DBHelper.TABLE_SEARCHES,
                        contentValues,
                        s,
                        strings);
                break;
            case SEARCHES_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(s)) {
                    rowsUpdated =
                            sqlDB.update(DBHelper.TABLE_SEARCHES,
                                    contentValues,
                                    DBHelper.KEY_ID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(DBHelper.TABLE_SEARCHES,
                                    contentValues,
                                    DBHelper.KEY_ID + "=" + id
                                            + " and "
                                            + s,
                                    strings);
                }
                break;
            case SEARCH_SETS:
                rowsUpdated = sqlDB.update(DBHelper.TABLE_SEARCH_SETS,
                        contentValues,
                        s,
                        strings);
                break;
            case SEARCH_SETS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(s)) {
                    rowsUpdated =
                            sqlDB.update(DBHelper.TABLE_SEARCH_SETS,
                                    contentValues,
                                    DBHelper.KEY_ID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(DBHelper.TABLE_SEARCH_SETS,
                                    contentValues,
                                    DBHelper.KEY_ID + "=" + id
                                            + " and "
                                            + s,
                                    strings);
                }
                break;
            case BEARINGS:
                rowsUpdated = sqlDB.update(DBHelper.TABLE_BEARINGS,
                        contentValues,
                        s,
                        strings);
                break;
            case BEARINGS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(s)) {
                    rowsUpdated =
                            sqlDB.update(DBHelper.TABLE_BEARINGS,
                                    contentValues,
                                    DBHelper.KEY_ID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(DBHelper.TABLE_BEARINGS,
                                    contentValues,
                                    DBHelper.KEY_ID + "=" + id
                                            + " and "
                                            + s,
                                    strings);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
