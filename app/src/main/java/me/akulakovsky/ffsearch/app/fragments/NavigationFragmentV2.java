package me.akulakovsky.ffsearch.app.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.javadocmd.simplelatlng.LatLngTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmList;
import me.akulakovsky.ffsearch.app.NavigationActivityV2;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.adapters.BearingRealmAdapter;
import me.akulakovsky.ffsearch.app.entities.BearingRealm;
import me.akulakovsky.ffsearch.app.entities.SearchRealm;
import me.akulakovsky.ffsearch.app.events.LocationEvent;
import me.akulakovsky.ffsearch.app.utils.MapUtils;
import me.akulakovsky.ffsearch.app.utils.Settings;

/**
 * Created by Ok-Alex on 7/6/17.
 */

public class NavigationFragmentV2 extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMapClickListener, GoogleMap.OnCameraMoveListener {

    public static final String TAG = NavigationFragmentV2.class.getSimpleName();

    private TextView tvOffset;
    private TextView tvToStart;
    private TextView tvTotal;
    private TextView tvMapSize;
    private TextView tvDirection;
    private TextView tvModelName;
    private View naviPanel;

    private GoogleMap mMap;
    private LatLng lastLocation;

    private RealmList<BearingRealm> bearingList = new RealmList<>();
    private List<Polyline> mBearingLines = new ArrayList<>();
    private Polyline mRouteLine;
    private Marker startMarker;
    private List<Marker> endMarkers = new ArrayList<>();
    private int currentBearing = 0;
    private DecimalFormat mDistanceFormat;
    private boolean previewMode;
    private float lastZoom = 18;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, rootView);
        tvOffset = rootView.findViewById(R.id.offset);
        tvToStart = rootView.findViewById(R.id.to_start);
        tvTotal = rootView.findViewById(R.id.total);
        tvMapSize = rootView.findViewById(R.id.map_size);
        tvDirection = rootView.findViewById(R.id.direction);
        tvModelName = rootView.findViewById(R.id.model_name);
        naviPanel = rootView.findViewById(R.id.navi_panel);

        FrameLayout containerMap = rootView.findViewById(R.id.map_container);

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

        mDistanceFormat = new DecimalFormat("#.##");

        previewMode = ((NavigationActivityV2) getActivity()).isPreviewMode();

        //setup map
        getMapAsync(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!previewMode) {
            inflater.inflate(R.menu.navigation, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_found:
                handleModelFound();
                break;
            case R.id.action_add:
                addExtraBearingToSearch();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addExtraBearingToSearch() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_dialog_bearing, null);
        view.findViewById(R.id.remove).setVisibility(View.GONE);

        final EditText bearingView = view.findViewById(R.id.bearing);
        final EditText notesView = view.findViewById(R.id.notes);

        int[] lineColors = getResources().getIntArray(R.array.line_colors);
        int lineIndex = bearingList.size();

        while (lineIndex >= lineColors.length) {
            lineIndex = Math.abs((lineColors.length) - lineIndex);
        }

        final View line = view.findViewById(R.id.line);
        line.setBackgroundColor(lineColors[lineIndex]);
        TextView lineName = view.findViewById(R.id.line_name);
        lineName.setTextColor(lineColors[lineIndex]);

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Add Model")
                .setView(view)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button positive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                if (positive != null) {
                    positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ColorDrawable lineColor = (ColorDrawable) line.getBackground();

                            if (!TextUtils.isEmpty(bearingView.getText())) {
                                double bearing = Double.parseDouble(bearingView.getText().toString());
                                while (bearing > 360) {
                                    bearing = bearing - 360;
                                }
                                BearingRealm bearingRealm = new BearingRealm();
                                bearingRealm.bearing = bearing;
                                bearingRealm.description = notesView.getText().toString();
                                bearingRealm.color = lineColor.getColor();
                                NavigationActivityV2 activity = ((NavigationActivityV2) getActivity());
                                activity.getLocationService().getMySearch().bearings.add(bearingRealm);
                                drawAllMap(activity.getLocationService().getMySearch(), activity.getLocationService().getRoute());
                            } else {
                                bearingView.setError(getString(R.string.dialog_bearing_error));
                            }
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        });
        alertDialog.show();
    }

    private void handleModelFound() {
        final BearingRealmAdapter adapter = new BearingRealmAdapter(getContext());
        adapter.addAll(bearingList);

        new AlertDialog.Builder(getActivity())
                .setTitle("Finish")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handleEndBearing(adapter.getItem(i));
                        ((NavigationActivityV2) getActivity()).getLocationService().endBearing(i);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    private void handleEndBearing(BearingRealm bearingRealm) {
            double error = Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);

            double startAngle = bearingRealm.bearing;
            LatLng start = startMarker.getPosition();
            LatLng end = lastLocation;
            com.javadocmd.simplelatlng.LatLng startPoint = new com.javadocmd.simplelatlng.LatLng(start.latitude, start.longitude);
            com.javadocmd.simplelatlng.LatLng endPoint = new com.javadocmd.simplelatlng.LatLng(end.latitude, end.longitude);
            double finalAngle = LatLngTool.initialBearing(startPoint, endPoint);
            final double diff = startAngle - finalAngle;

            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.dialog_title_result))
                    .setMessage(
                            getString(R.string.dialog_message_start_angle) + decimalFormat.format(startAngle)
                                    + getString(R.string.dialog_message_error) + decimalFormat.format(error) +
                            getString(R.string.dialog_message_final_angle) + decimalFormat.format(finalAngle) +
                            getString(R.string.dialog_message_difference) + decimalFormat.format(diff) +
                            getString(R.string.dialog_message_question_save_difference))
                    .setPositiveButton(getString(R.string.dialog_button_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Settings.get().putValue(Settings.KEY_BEARING_MISTAKE, (float) diff);
                        }
                    })
                    .setNegativeButton(getString(R.string.dialog_button_no), null).create().show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();

        if (previewMode) {
            naviPanel.setVisibility(View.GONE);
            SearchRealm searchRealm = ((NavigationActivityV2) getActivity()).getSearch();
            drawAllMap(searchRealm, PolyUtil.decode(searchRealm.encodedRoute));
            setCameraPositionTo(searchRealm.getStartPoint(), 0);
        } else {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(LocationEvent event) {
        lastLocation = event.lastLocation;
        if (event.searchRealm.getStartPoint() != null) {
            drawAllMap(event.searchRealm, event.route);
            setCameraPositionTo(event.lastLocation, currentBearing);
        }
    }

    @OnClick(R.id.offset)
    public void onDeviationClick(View view) {
        if (bearingList.size() > 1 && lastLocation != null) {
            if (currentBearing < bearingList.size() - 1) {
                currentBearing++;
            } else {
                currentBearing = 0;
            }
            BearingRealm bearingRealm = bearingList.get(currentBearing);
            if (TextUtils.isEmpty(bearingRealm.description)) {
                tvModelName.setText(mDistanceFormat.format(bearingRealm.bearing));
            } else {
                tvModelName.setText(bearingRealm.description + " / " + mDistanceFormat.format(bearingRealm.bearing));
            }
            tvOffset.setTextColor(bearingList.get(currentBearing).color);
            setCameraPositionTo(lastLocation, currentBearing);
            drawDistances();
        }
    }

    private void drawAllMap(SearchRealm searchRealm, List<LatLng> route) {
        drawRouteLine(route);

        bearingList.clear();
        bearingList.addAll(searchRealm.bearings);

        drawBearingLines(searchRealm.getStartPoint());
        //drawLandingCircles(searchRealm.getStartPoint(), searchRealm.bearings, 150, 5);

        if (startMarker == null) {
            drawStartPoint(searchRealm.getStartPoint());
        }

        drawEndPoints(bearingList);

        drawDistances();

        drawNeededDirection();
    }

    private void drawNeededDirection() {
        if (startMarker != null && lastLocation != null && bearingList.size() > 0) {
            double currentHeading = SphericalUtil.computeHeading(startMarker.getPosition(), lastLocation);
            if (currentHeading > 180) {
                currentHeading = currentHeading - 360;
            }
            double error = Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);
            double navDirectionValue = bearingList.get(currentBearing).bearing - error - currentHeading;
            String direction;
            if (navDirectionValue > 0) {
                direction = "Keep Right --->";
            } else if (navDirectionValue < 0) {
                direction = "<--- Keep Left";
            } else {
                direction = "";
            }
            tvDirection.setText(direction);
            tvDirection.setVisibility(View.VISIBLE);
        } else {
            tvDirection.setVisibility(View.GONE);
        }
    }

    private void drawDistances() {
        if (mRouteLine != null) {
            double total = SphericalUtil.computeLength(mRouteLine.getPoints());
            tvTotal.setText(mDistanceFormat.format(total) + getActivity().getString(R.string.m));
        }

        BearingRealm bearingRealm = bearingList.get(currentBearing);

        if (lastLocation != null) {
            double distanceToStart = SphericalUtil.computeDistanceBetween(lastLocation, startMarker.getPosition());
            tvToStart.setText(mDistanceFormat.format(distanceToStart) + getActivity().getString(R.string.m));

            LatLng lastPointOnBearingLine = mBearingLines.get(currentBearing).getPoints().get(mBearingLines.get(currentBearing).getPoints().size() - 1);
            double deviation = MapUtils.pointToLineDistance(startMarker.getPosition(), lastPointOnBearingLine, lastLocation);
            tvOffset.setText(mDistanceFormat.format(deviation) + getActivity().getString(R.string.m));
        }

        if (TextUtils.isEmpty(bearingRealm.description)) {
            tvModelName.setText(mDistanceFormat.format(bearingRealm.bearing));
        } else {
            tvModelName.setText(bearingRealm.description + " / " + mDistanceFormat.format(bearingRealm.bearing));
        }

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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMapClickListener(this);
    }

    private void setCameraPositionTo(LatLng position, int bearingIndex) {
        if (position != null) {
            double error = Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .bearing((float) (bearingList.get(bearingIndex).bearing - error))
                    .target(position)
                    .tilt(0)
                    .zoom(lastZoom)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void drawEndPoints(RealmList<BearingRealm> bearingList) {
        for (Marker marker: endMarkers) {
            marker.remove();
        }
        endMarkers.clear();
        for (int i = 0; i < bearingList.size(); i++) {
            BearingRealm bearing = bearingList.get(i);
            if (bearing.getEndLocation() != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .title(!TextUtils.isEmpty(bearing.description) ? bearing.description : getActivity().getString(R.string.pin_title_model) + " " + (i+1))
                        .position(bearing.getEndLocation())
                        .icon(BitmapDescriptorFactory.fromBitmap(getEndMarker(bearing))).anchor(0.5f, 0.5f);
                Marker endMarker = mMap.addMarker(markerOptions);
                endMarkers.add(endMarker);
            }
        }
    }

    private Bitmap getEndMarker(BearingRealm bearingRealm) {
        Resources res = getResources();
        Drawable background = res.getDrawable(R.mipmap.ic_launcher);
        int primaryColor = bearingRealm.color;
        background.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
        return drawableToBitmap(background);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void drawBearingLines(LatLng startPoint) {
        if (startPoint != null) {
            for (Polyline polyline: mBearingLines) {
                polyline.remove();
            }
            mBearingLines.clear();

            double distance = SphericalUtil.computeDistanceBetween(
                    startPoint,
                    mMap.getProjection().getVisibleRegion().farRight);

            double error = Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);

            for (BearingRealm bearing: bearingList) {
                Polyline bearingLine = mMap.addPolyline(new PolylineOptions()
                        .add(startPoint)
                        .add(SphericalUtil.computeOffset(startPoint, distance, bearing.bearing - error))
                        .color(bearing.color)
                        .width(5));
                mBearingLines.add(bearingLine);
            }
        }
    }

    public void drawRouteLine(List<LatLng> route) {
        if (route != null && route.size() > 0) {
            if (mRouteLine != null) {
                mRouteLine.remove();
            }
            mRouteLine = mMap.addPolyline(new PolylineOptions()
                    .addAll(route)
                    .color(Color.GREEN)
                    .width(5)
            );
        }
    }

    private void drawStartPoint(LatLng startPoint) {
        startMarker = mMap.addMarker(new MarkerOptions()
                .position(startPoint)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_map_pin_start_normal))
                .title(getActivity().getString(R.string.marker_start_point)));
    }

    private void drawLandingCircles(LatLng startPoint, RealmList<BearingRealm> bearingList, double flightTime, double windSpeed) {
        final double maxHeight = 150; //100 meters
        final double groundHeight = 3; //wind measurement at Hground ~ 3 meters height
        final double coefficient = 0.12;
        final double ascentTime = 42; //42 sec for F1B

        for (BearingRealm bearingRealm: bearingList) {
            double descentSpeed = maxHeight / flightTime;
            double distance = 0;

            StringBuilder speeds = new StringBuilder();

            double flightTimeCopy = flightTime;

            if (flightTime > ascentTime) {
                double ascentSpeed = maxHeight / ascentTime;
                for (int i = 0; i < ascentTime; i++) {
                    double currentHeight = ascentSpeed * i;
                    double windAt = windSpeed * Math.pow((currentHeight / groundHeight), coefficient);
                    distance += windAt;
                    speeds.append("[H=").append(mDistanceFormat.format(currentHeight)).append(",V=").append(mDistanceFormat.format(windAt)).append(",T=").append(i).append("];");
                }
                flightTimeCopy = flightTimeCopy - ascentTime;
            }

            for (int i = 0; i <= flightTimeCopy; i++) {
                double currentHeight = (flightTime > ascentTime) ? (maxHeight - descentSpeed * (ascentTime + i)) : (maxHeight - descentSpeed * i);
                double windAt = windSpeed * Math.pow((currentHeight / groundHeight), coefficient);
                speeds.append("[H=").append(mDistanceFormat.format(currentHeight)).append(",V=").append(mDistanceFormat.format(windAt)).append(",T=").append(ascentTime + i).append("];");
                distance += windAt;
            }

            LatLng landingPointMath = SphericalUtil.computeOffset(startPoint, distance, bearingRealm.bearing);
            LatLng landingPointSimple = SphericalUtil.computeOffset(startPoint, flightTime * windSpeed, bearingRealm.bearing);

            Log.d(TAG, "DISTANCE MATH = " + distance);
            Log.d(TAG, "DISTANCE SIMPLE = " + flightTime * windSpeed);
            Log.d(TAG, "SPEEDS = " + speeds.toString());
            Log.d(TAG, "DESCEND = " + descentSpeed);

            mMap.addCircle(new CircleOptions().center(landingPointSimple).radius(100).strokeWidth(5).strokeColor(bearingRealm.color));
            mMap.addCircle(new CircleOptions().center(landingPointMath).radius(100).strokeWidth(5).strokeColor(bearingRealm.color));
        }
    }

    @Override
    public void onCameraIdle() {
        if (startMarker != null) {
            drawBearingLines(startMarker.getPosition());
        }

        double mapSize = SphericalUtil.computeDistanceBetween(
                mMap.getProjection().getVisibleRegion().nearLeft,
                mMap.getProjection().getVisibleRegion().nearRight);

        tvMapSize.setText(mDistanceFormat.format(mapSize) + getActivity().getString(R.string.m));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions()
            .position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_map_pin_normal)));
    }

    @Override
    public void onCameraMove() {
        lastZoom = mMap.getCameraPosition().zoom;
    }
}