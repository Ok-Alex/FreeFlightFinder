package me.akulakovsky.ffsearch.app.entities;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;

/**
 * Created by Ok-Alex on 7/6/17.
 */

public class BearingRealm extends RealmObject {

    public double bearing;
    public String description;
    public int color;
    public double endLat;
    public double endLng;

    public LatLng getEndLocation() {
        return (endLat == 0 && endLng == 0) ? null: new LatLng(endLat, endLng);
    }

}
