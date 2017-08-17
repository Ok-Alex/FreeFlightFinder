package me.akulakovsky.ffsearch.app.events;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import me.akulakovsky.ffsearch.app.entities.SearchRealm;

/**
 * Created by Ok-Alex on 7/7/17.
 */

public class LocationEvent {

    public SearchRealm searchRealm;
    public List<LatLng> route;
    public LatLng lastLocation;
    public int totalLocations;
    public float accuracy;

    public LocationEvent(SearchRealm searchRealm, List<LatLng> route, LatLng lastLocation, int totalLocations, float accuracy) {
        this.searchRealm = searchRealm;
        this.route = route;
        this.lastLocation = lastLocation;
        this.totalLocations = totalLocations;
        this.accuracy = accuracy;
    }
}
