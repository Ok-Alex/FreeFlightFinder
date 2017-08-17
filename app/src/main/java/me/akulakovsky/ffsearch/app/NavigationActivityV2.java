package me.akulakovsky.ffsearch.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

import io.realm.Realm;
import me.akulakovsky.ffsearch.app.entities.SearchRealm;
import me.akulakovsky.ffsearch.app.fragments.NavigationFragmentV2;
import me.akulakovsky.ffsearch.app.services.LocationService;

/**
 * Created by Ok-Alex on 7/6/17.
 */

public class NavigationActivityV2 extends AppCompatActivity implements LocationService.LocationServiceCallbacks {

    public static final String TAG = NavigationActivityV2.class.getSimpleName();

    private static final String KEY_SEARCH_ID = "SEARCH_ID";
    private static final String KEY_PREVIEW = "PREVIEW";

    private boolean isPreviewMode = false;

    private LocationService locationService;
    private LocationServiceConnection mConnection = new LocationServiceConnection();
    private int searchId;
    private SearchRealm mSearch;

    public static void start(Context context, int searchId, boolean isPreviewMode) {
        Intent intent = new Intent(context, NavigationActivityV2.class);
        intent.putExtra(KEY_SEARCH_ID, searchId);
        intent.putExtra(KEY_PREVIEW, isPreviewMode);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        isPreviewMode = getIntent().getBooleanExtra(KEY_PREVIEW, false);

        searchId = getIntent().getIntExtra(KEY_SEARCH_ID, -1);
        if (searchId == -1) {
            finish();
        } else {
            mSearch = Realm.getDefaultInstance().where(SearchRealm.class)
                    .equalTo("id", searchId).findFirst();
            mSearch = Realm.getDefaultInstance().copyFromRealm(mSearch);
            if (mSearch == null) {
                finish();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new NavigationFragmentV2(), NavigationFragmentV2.TAG).commit();
            }
        }
    }

    public boolean isPreviewMode() {
        return isPreviewMode;
    }

    public SearchRealm getSearch() {
        return mSearch;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPreviewMode) {
            bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (locationService != null) {
            locationService.stopNavigation();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (locationService != null) {
            locationService.setCallback(null);
        }
        if (!isPreviewMode) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    private void handleModelFound() {
//        if (locationService != null) {
//            if (locationService.getMySearch().getStartPoint() == null) {
//                Toast.makeText(this, "Start point not available!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            locationService.getMySearch().drawEndPoint(locationService.getLastLocation());
//            double startAngle = locationService.getMySearch().getBearing();
//            LatLng start = locationService.getMySearch().getStartPoint();
//            LatLng end = locationService.getMySearch().getEndPoint();
//            com.javadocmd.simplelatlng.LatLng startPoint = new com.javadocmd.simplelatlng.LatLng(start.latitude, start.longitude);
//            com.javadocmd.simplelatlng.LatLng endPoint = new com.javadocmd.simplelatlng.LatLng(end.latitude, end.longitude);
//            double finalAngle = LatLngTool.initialBearing(startPoint, endPoint);
//            final double diff = locationService.getMySearch().getBearing() + error - finalAngle;
//            NavigationFragment navigationFragment = (NavigationFragment) findFragmentByPosition(1);
//            if (navigationFragment != null) {
//                navigationFragment.drawEndPoint(locationService.getLastLocation());
//            }
//            locationService.stopNavigation();
//
//            DecimalFormat decimalFormat = new DecimalFormat("#.#");
//
//            new AlertDialog.Builder(this)
//                    .setTitle(getString(R.string.dialog_title_result))
//                    .setMessage(
//                            getString(R.string.dialog_message_start_angle) + decimalFormat.format(startAngle + error)
//                                    + getString(R.string.dialog_message_error) + decimalFormat.format(error) +
//                            getString(R.string.dialog_message_final_angle) + decimalFormat.format(finalAngle) +
//                            getString(R.string.dialog_message_difference) + decimalFormat.format(diff) +
//                            getString(R.string.dialog_message_question_save_difference))
//                    .setPositiveButton(getString(R.string.dialog_button_yes), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Settings.get().putValue(Settings.KEY_BEARING_MISTAKE, (float) diff);
//                        }
//                    })
//                    .setNegativeButton(getString(R.string.dialog_button_no), null).create().show();
//
//        }
    }

    @Override
    public void onHighAccuracyReady(LatLng location) {

    }

    @Override
    public void onLocationReceived(int num) {

    }

    public LocationService getLocationService() {
        return locationService;
    }

    private class LocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected()");

            locationService = ((LocationService.ServiceBinder) iBinder).getService();
            locationService.setCallback(NavigationActivityV2.this);
            LatLng lastLocation = locationService.getLastLocation();
            if (lastLocation == null) {
                Log.d(TAG, "lastLocation == null!!!!");
            } else {
                Log.d(TAG, "onServiceConnected() - lastLocation: lat = " + lastLocation.latitude + ", lng = " + lastLocation.longitude);
            }

            locationService.startNavigation(mSearch);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected()");
            locationService = null;
        }
    }
}
