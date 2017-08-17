package me.akulakovsky.ffsearch.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.List;

import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.entities.MySearch;
import me.akulakovsky.ffsearch.app.utils.SphericalFunctionEngine;

public class NavigationFragment extends SupportMapFragment implements GoogleMap.OnCameraChangeListener, OnMapReadyCallback {

    private double bearing;
    private double bearing2;
    private int lastZoom = -1;
    private Polyline bearingLine;
    private Polyline bearingLine2;
    private Polyline mRouteLine;

    private TextView tvOffset;
    private TextView tvToStart;
    private TextView tvTotal;
    private TextView tvMapSize;
    private TextView tvDirection;
    private View naviPanel;

    private LatLng startPoint;
    
    private GoogleMap mMap;

    public static NavigationFragment newInstance(MySearch mySearch) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("search", mySearch);
        NavigationFragment navigationFragment = new NavigationFragment();
        navigationFragment.setArguments(bundle);
        return navigationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        tvOffset = (TextView) rootView.findViewById(R.id.offset);
        tvToStart = (TextView) rootView.findViewById(R.id.to_start);
        tvTotal = (TextView) rootView.findViewById(R.id.total);
        tvMapSize = (TextView) rootView.findViewById(R.id.map_size);
        tvDirection = rootView.findViewById(R.id.direction);
        naviPanel = rootView.findViewById(R.id.navi_panel);

        FrameLayout containerMap = (FrameLayout) rootView.findViewById(R.id.map_container);

        View mapView = super.onCreateView(inflater, container, savedInstanceState);

        containerMap.addView(mapView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
        
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setup map
        getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        setupSearch();
    }

    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(
                0,
                getResources().getDimensionPixelOffset(R.dimen.map_size_panel),
                0,
                getResources().getDimensionPixelSize(R.dimen.navi_panel_height));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setOnCameraChangeListener(this);
    }

    private void setupSearch() {
//        MySearch mySearch = getArguments().getParcelable("search");
//        if (mySearch != null) {
//            initIfNeeded(mySearch.getStartPoint(), mySearch.getBearing(), mySearch.getBearing2(), -1, MySearch.getRoute(getActivity(), mySearch.getId()));
//            drawEndPoint(mySearch.getEndPoint());
//            naviPanel.setVisibility(View.INVISIBLE);
//            mMap.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.map_size_panel), 0, 0);
//        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (lastZoom != (int) cameraPosition.zoom) {
            lastZoom = (int) cameraPosition.zoom;
        }
        drawBearingLine();

        double mapSize = SphericalUtil.computeDistanceBetween(
                mMap.getProjection().getVisibleRegion().nearLeft,
                mMap.getProjection().getVisibleRegion().nearRight);

        tvMapSize.setText(new DecimalFormat("#.##").format(mapSize) + getActivity().getString(R.string.m));
    }

    private void drawBearingLine() {
        if (bearingLine != null) {
            bearingLine.remove();
        }
        if (bearingLine2 != null) {
            bearingLine2.remove();
        }

        if (startPoint != null) {
            double distance = SphericalUtil.computeDistanceBetween(
                    startPoint,
                    mMap.getProjection().getVisibleRegion().farRight);

            bearingLine = mMap.addPolyline(new PolylineOptions()
                    .add(startPoint)
                    .add(SphericalUtil.computeOffset(startPoint, distance, bearing))
                    .color(Color.RED)
                    .width(5));

            if (bearing2 != 0) {
                bearingLine2 = mMap.addPolyline(new PolylineOptions()
                        .add(startPoint)
                        .add(SphericalUtil.computeOffset(startPoint, distance, bearing2))
                        .color(Color.YELLOW)
                        .width(5));
            }
        }
    }

    public void drawRouteLine(List<LatLng> route) {
        if (mRouteLine != null) {
            mRouteLine.remove();
        }
        mRouteLine = mMap.addPolyline(new PolylineOptions()
                .addAll(route)
                .color(Color.GREEN)
                .width(5)
        );
    }

    public void setNewOffset(LatLng currentLocation) {
        if (currentLocation != null && bearingLine != null) {
            double distance = pointToLineDistance(bearingLine.getPoints().get(0),
                    bearingLine.getPoints().get(bearingLine.getPoints().size() - 1),
                    currentLocation);
            tvOffset.setText(new DecimalFormat("#.##").format(distance) + getActivity().getString(R.string.m));
        }
    }

    public double pointToLineDistance(LatLng pointA, LatLng pointB, LatLng pointC) {
        return new SphericalFunctionEngine().calculatePerpendicularDistance(
                pointA.longitude, pointA.latitude,
                pointB.longitude, pointB.latitude,
                pointC.longitude, pointC.latitude,
                "m"
        );
    }

    public boolean initIfNeeded(LatLng startPoint, double bearing, double bearing2, double distance, List<LatLng> route) {
        Log.d(TAG, "initIfNeeded() - STARTPOINT = " + startPoint + ", BEARING = " + bearing + ", ROUTE POINTS = " + route.size());
        boolean wasInited = false;
        if (this.startPoint == null) {
            this.bearing = bearing;
            this.bearing2 = bearing2;
            this.startPoint = startPoint;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .bearing((float) bearing)
                    .target(startPoint)
                    .tilt(0)
                    .zoom(18)
                    .build();

            mMap.addMarker(new MarkerOptions()
                    .position(startPoint)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_map_pin_start_normal))
                    .title(getActivity().getString(R.string.marker_start_point))
            );

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (distance > 0) {
                setDistance(distance);
            }

            drawBearingLine();
            drawRouteLine(route);

            wasInited = true;
        }

        return wasInited;
    }

    public void moveTo(LatLng location) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
    }

    public Polyline getBearingLine() {
        return bearingLine;
    }

    public Polyline getBearingLine2() {
        return bearingLine2;
    }

    public void setDistances(double deviation, double toStart, double total, String direction) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        tvOffset.setText(decimalFormat.format(deviation) + getActivity().getString(R.string.m));
        tvToStart.setText(decimalFormat.format(toStart) + getActivity().getString(R.string.m));
        tvTotal.setText(decimalFormat.format(total) + getActivity().getString(R.string.m));
        tvDirection.setText(direction);
    }

    public void setEndPoint(LatLng lastLocation) {
        if (lastLocation != null) {
            mMap.addMarker(new MarkerOptions()
                    .title(getActivity().getString(R.string.pin_title_model))
                    .position(lastLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_map_pin_finish_normal)));
        }
    }

    public void setDistance(double distance) {
        LatLng approximateLocation = SphericalUtil.computeOffset(startPoint, distance, bearing);
        mMap.addCircle(new CircleOptions()
                .center(approximateLocation)
                .radius(100) //100 meters
                .strokeColor(Color.RED)
                .strokeWidth(5)
                .zIndex(100));
    }

    public static final String TAG = NavigationFragment.class.getName();
}
