package me.akulakovsky.ffsearch.app;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;


import io.realm.Realm;
import me.akulakovsky.ffsearch.app.utils.Settings;

public class TheApp extends Application {

    private LatLng location = null;

    private static TheApp instance;

    public static TheApp get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Settings.init(this);
        //Stetho.initializeWithDefaults(this);

        Realm.init(this);
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
