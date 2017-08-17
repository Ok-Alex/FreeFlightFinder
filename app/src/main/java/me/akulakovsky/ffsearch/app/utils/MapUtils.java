package me.akulakovsky.ffsearch.app.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ok-Alex on 4/10/14.
 */
public class MapUtils {

    public static double pointToLineDistance(LatLng pointA, LatLng pointB, LatLng pointC) {
        return new SphericalFunctionEngine().calculatePerpendicularDistance(
                pointA.longitude, pointA.latitude,
                pointB.longitude, pointB.latitude,
                pointC.longitude, pointC.latitude,
                "m"
        );
    }

}
