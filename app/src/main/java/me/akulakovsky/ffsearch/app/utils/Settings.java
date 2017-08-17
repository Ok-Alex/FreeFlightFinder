package me.akulakovsky.ffsearch.app.utils;



import android.content.Context;
import android.content.SharedPreferences;

import me.akulakovsky.ffsearch.app.R;

/**
 * User: Alexander Kulakovsky
 * Date: 09.10.13
 * Time: 19:33
 * E-Mail: akulakovskyy@gmail.com
 */

public class Settings {

    private static Settings instance;

    private SharedPreferences prefs;

    public static final String KEY_LAST_MODEL_NAME = "model_name";
    public static final String KEY_LAST_FREQUENCY = "frequency";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_SAVE_ENABLED = "save";
    public static final String KEY_BEARING_MISTAKE = "bearing_mistake";
    public static final String KEY_NOTES = "notes";

    public Settings(Context context) {
        this.prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    public static Settings get() {
        if (instance == null) {
            throw new IllegalStateException("should use SettingsUtils.init(context) first");
        }
        return instance;
    }

    public static void init(Context context) {
        instance = new Settings(context);
    }

    public void putValue(String key, String value) {
        prefs.edit().putString(key, value).commit();
    }

    public void putValue(String key, int value) {
        prefs.edit().putInt(key, value).commit();
    }

    public void putValue(String key, boolean value) {
        prefs.edit().putBoolean(key, value).commit();
    }

    public void putValue(String key, long value) {
        prefs.edit().putLong(key, value).commit();
    }

    public void putValue(String key, float value) {
        prefs.edit().putFloat(key, value).commit();
    }

    public String getValue(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public int getValue(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public boolean getValue(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public long getValue(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public float getValue(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }
}