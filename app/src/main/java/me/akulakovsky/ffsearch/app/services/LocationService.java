package me.akulakovsky.ffsearch.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import me.akulakovsky.ffsearch.app.MainActivity;
import me.akulakovsky.ffsearch.app.NavigationActivityV2;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.entities.SearchRealm;
import me.akulakovsky.ffsearch.app.events.LocationEvent;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "FreeFlightSearchChannel";

    public static final String ACTION_STOP = "stop_navigation";

    public static final int LOCATION__UPDATE_INTERVAL = 1000;

    IBinder mBinder = new ServiceBinder();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    // Latest obtained lastLocation
    private LatLng lastLocation;

    private LocationServiceCallbacks callback;
    private LocationRequest mLocationRequest;

    private SearchRealm mySearch;
    private List<LatLng> mRoute = new ArrayList<>();

    private static boolean isServiceRunning = false;

    private int receivedLocationPoints = 0;

    public class ServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    onLocationChanged(location);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        //super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_STOP)) {
            if (isServiceRunning) {
                stopNavigation();
            }
        }

        return START_STICKY;
    }

    public void onLocationChanged(Location location) {
        receivedLocationPoints++;
        Log.d(TAG, "onLocationChanged() - " + location.toString());
        // Report to the UI that the lastLocation was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        Log.d(TAG, msg);

        if (location.hasAccuracy() && location.getAccuracy() <= 10 && receivedLocationPoints >= 5) {
            lastLocation = new LatLng(location.getLatitude(), location.getLongitude());

            if (mySearch != null && (mySearch.startPointLat == 0 || mySearch.startPointLng == 0)) {
                mySearch.startPointLat = lastLocation.latitude;
                mySearch.startPointLng = lastLocation.longitude;
            }

            mRoute.add(lastLocation);

            LocationEvent event = new LocationEvent(mySearch, mRoute, lastLocation, receivedLocationPoints, location.getAccuracy());
            EventBus.getDefault().post(event);
        } else {
            if (callback != null) {
                callback.onLocationReceived(receivedLocationPoints);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    public LatLng getLastLocation() {
        Log.d(TAG, "getLastLocation()");
        return lastLocation;
    }

    public void startNavigation(SearchRealm mySearch) {
        Log.d(TAG, "startNavigation()");
        this.mySearch = mySearch;
        createLocationRequest();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        createNotification(true);
        isServiceRunning = true;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION__UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION__UPDATE_INTERVAL);
    }

    public void stopNavigation() {
        receivedLocationPoints = 0;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        saveSearch();
        mySearch = null;
        mRoute.clear();
        hideNotification();
        isServiceRunning = false;
    }

    public void endBearing(int index) {
        mySearch.bearings.get(index).endLat = lastLocation.latitude;
        mySearch.bearings.get(index).endLng = lastLocation.longitude;
        saveSearch();
    }

    private void hideNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(12092);
    }

    public void createNotification(boolean isStart) {
        createNotificationChannel();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher) // notification icon
                .setContentTitle(isStart ? getString(R.string.navigation_going) : getString(R.string.navigation_stopped)) // title for notification
                .setContentText(getString(R.string.app_name)) // message for notification
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{500, 500, 500})
                .setOngoing(isStart)
                .setAutoCancel(!isStart); // clear notification after click

        Intent newintent;
        if (mySearch != null) {
            newintent = new Intent(this, NavigationActivityV2.class);
            newintent.putExtra("search", mySearch.id);
        } else {
            newintent = new Intent(this, MainActivity.class);
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0, newintent, Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(12092, mBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Navigation status notification");
        notificationManager.createNotificationChannel(channel);
    }

    public int getReceivedLocationPoints() {
        return receivedLocationPoints;
    }

    public SearchRealm getMySearch() {
        return mySearch;
    }

    public List<LatLng> getRoute() {
        return mRoute;
    }

    public void setRoute(List<LatLng> mRoute) {
        this.mRoute = mRoute;
    }

    public LocationServiceCallbacks getCallback() {
        return callback;
    }

    public void setCallback(LocationServiceCallbacks callback) {
        this.callback = callback;
    }

    public interface LocationServiceCallbacks {
        public void onHighAccuracyReady(LatLng location);
        public void onLocationReceived(int num);
    }

    private void saveSearch() {
        if (mySearch != null) {
            mySearch.encodedRoute = PolyUtil.encode(mRoute);
            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Realm.getDefaultInstance().copyToRealmOrUpdate(mySearch);
                }
            });
        }
    }

//    private class SaveTrackTask extends AsyncTask<Void, Void, Void> {
//
//        private boolean runAgain = false;
//
//        public void setRunAgain(boolean value) {
//            runAgain = value;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            if (mySearch != null) {
//                String route = PolyUtil.encode(mRoute);
//                mySearch.encodedRoute = route;
//                Realm.getDefaultInstance().copyToRealmOrUpdate(mySearch);
//                //MySearch.saveSearch(LocationService.this, mySearch.getId(), mRoute);
//            }
//            mRoute.clear();
//            mySearch = null;
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            if (runAgain) {
//                runAgain = false;
//                execute();
//            }
//        }
//    }
    private static final String TAG = LocationService.class.getName();
}