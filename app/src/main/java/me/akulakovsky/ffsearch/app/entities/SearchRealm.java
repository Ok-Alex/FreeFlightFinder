package me.akulakovsky.ffsearch.app.entities;


import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Ok-Alex on 7/6/17.
 */

public class SearchRealm extends RealmObject {

    @PrimaryKey
    public int id;

    public double startPointLat;
    public double startPointLng;
    public RealmList<BearingRealm> bearings;
    public RealmList<CheckPointRealm> checkPoints;
    public String encodedRoute;
    public Date date;

    public LatLng getStartPoint() {
        return (startPointLat != 0 && startPointLng != 0) ? new LatLng(startPointLat, startPointLng) : null;
    }
}
