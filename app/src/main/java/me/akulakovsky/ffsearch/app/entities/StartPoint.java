package me.akulakovsky.ffsearch.app.entities;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Ok-Alex on 7/24/17.
 */

public class StartPoint extends RealmObject {

    @PrimaryKey
    public int id;
    public double lat;
    public double lng;
    public Date date;
    public String name;

    public StartPoint() {
    }

    public StartPoint(double lat, double lng, Date date, String name) {
        this.lat = lat;
        this.lng = lng;
        this.date = date;
        this.name = name;
    }


}