package me.akulakovsky.ffsearch.app;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.javadocmd.simplelatlng.LatLngTool;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import me.akulakovsky.ffsearch.app.entities.MySearch;
import me.akulakovsky.ffsearch.app.fragments.InfoFragment;
import me.akulakovsky.ffsearch.app.fragments.NavigationFragment;
import me.akulakovsky.ffsearch.app.services.LocationService;
import me.akulakovsky.ffsearch.app.utils.DateUtils;
import me.akulakovsky.ffsearch.app.utils.MapUtils;
import me.akulakovsky.ffsearch.app.utils.Settings;


public class NavigationActivity extends AppCompatActivity implements ActionBar.TabListener, LocationService.LocationServiceCallbacks {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private MySearch mySearch;

    private LocationService locationService;

    private LocationServiceConnection mConnection = new LocationServiceConnection();

    private boolean isPreviewMode = false;

    private double distance;
    private double error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isPreviewMode = getIntent().getBooleanExtra("preview", false);

//        distance = getIntent().getDoubleExtra("distance", -1);
//
//        mySearch = getIntent().getParcelableExtra("search");
//        if (mySearch == null) {
//            finish();
//        }
//
//        error = Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);
//
//        DecimalFormat format = new DecimalFormat("#.#");
//        if (mySearch.getBearing2() != 0) {
//            getSupportActionBar().setTitle(getString(R.string.activity_title_bearing) + " " + format.format(mySearch.getBearing()) + "°," + getString(R.string.activity_title_bearing2) + format.format(mySearch.getBearing2())  + "°");
//        } else {
//            getSupportActionBar().setTitle(getString(R.string.activity_title_bearing) + " " + format.format(mySearch.getBearing() + error) + "°," + getString(R.string.error) + format.format(error) + "°");
//        }
//
//
//        getSupportActionBar().setSubtitle(getString(R.string.activity_subtitle_time) + " " + DateUtils.toPrettyDate(mySearch.getDate()) + " " + DateUtils.toPrettyTime(mySearch.getDate()));
//
//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
//        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//
//        mViewPager = (ViewPager) findViewById(R.id.pager);
//        mViewPager.setAdapter(mSectionsPagerAdapter);
//
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });
//
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this));
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPreviewMode) {
            bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        } else {
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isPreviewMode) {
            getMenuInflater().inflate(R.menu.navigation, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_found:
                handleModelFound();
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onHighAccuracyReady(LatLng location) {
//        Log.d(TAG, "onHighAccuracyReady()");
//        InfoFragment infoFragment = (InfoFragment) findFragmentByPosition(0);
//        NavigationFragment navigationFragment = (NavigationFragment) findFragmentByPosition(1);
//
//        Log.d(TAG, "SERVICE ROUTE LENGTH = " + locationService.getRoute().size());
//
//        double deviation = 0;
//        double deviation2 = 0;
//        double distanceToStart = SphericalUtil.computeDistanceBetween(
//                locationService.getMySearch().getStartPoint(),
//                lastLocation);
//        double totalDistance = SphericalUtil.computeLength(locationService.getRoute());
//
//        double currentHeading = SphericalUtil.computeHeading(locationService.getMySearch().getStartPoint(), lastLocation);
//        if (currentHeading > 180) {
//            currentHeading = currentHeading - 360;
//        }
//        double navDirectionValue = locationService.getMySearch().getBearing() - currentHeading;
//        String direction;
//        if (navDirectionValue > 0) {
//            direction = "Keep Right";
//        } else if (navDirectionValue < 0) {
//            direction = "Keep Left";
//        } else {
//            direction = "";
//        }
//
//        if (navigationFragment != null) {
//            Log.d(TAG, "onHighAccuracyReady() - Updating map fragment...");
//            boolean wasInitiated = navigationFragment.initIfNeeded(
//                    locationService.getMySearch().getStartPoint(),
//                    locationService.getMySearch().getBearing(),
//                    locationService.getMySearch().getBearing2(),
//                    distance,
//                    locationService.getRoute());
//            if (!wasInitiated) {
//                if (navigationFragment.getBearingLine() != null) {
//                    deviation = MapUtils.pointToLineDistance(
//                            mySearch.getStartPoint(),
//                            navigationFragment.getBearingLine().getPoints().get(navigationFragment.getBearingLine().getPoints().size() - 1),
//                            lastLocation
//                    );
//
//                    if (locationService.getMySearch().getBearing2() != 0) {
//                        deviation2 = MapUtils.pointToLineDistance(
//                                mySearch.getStartPoint(),
//                                navigationFragment.getBearingLine().getPoints().get(navigationFragment.getBearingLine2().getPoints().size() - 1),
//                                lastLocation
//                        );
//                    }
//                }
//
//                navigationFragment.drawRouteLine(locationService.getRoute());
//                navigationFragment.setDistances(deviation, distanceToStart, totalDistance, direction);
//                navigationFragment.moveTo(lastLocation);
//            }
//        }
//
//        if (infoFragment != null) {
//            infoFragment.setDistances(deviation, deviation2, distanceToStart, totalDistance);
//        }

    }

    @Override
    public void onLocationReceived(int num) {
        if (num <= 5) {
            Toast.makeText(this, "Received lastLocation " + num + " of 5", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Find fragment for provided position in tabs adapter
     * @param position of fragment to search
     * @return fragment if found or null
     */
    public Fragment findFragmentByPosition(int position) {
        Log.d(TAG, "findFragmentByPosition() - " + position);
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + mSectionsPagerAdapter.getItemId(position));
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new InfoFragment();
                case 1:
                    return NavigationFragment.newInstance(isPreviewMode ? mySearch : null);
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_tab_info).toUpperCase(l);
                case 1:
                    return getString(R.string.title_tab_map).toUpperCase(l);
            }
            return null;
        }
    }

    private class LocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            Log.d(TAG, "onServiceConnected()");
//            locationService = ((LocationService.ServiceBinder) iBinder).getService();
//            locationService.setCallback(NavigationActivity.this);
//            LatLng lastLocation = locationService.getLastLocation();
//            if (lastLocation == null) {
//                Log.d(TAG, "lastLocation == null!!!!");
//            } else {
//                Log.d(TAG, "onServiceConnected() - lastLocation: lat = " + lastLocation.latitude + ", lng = " + lastLocation.longitude);
//            }
//
//            if (mySearch.getStartPoint() != null) {
//                //old search. lets add route to service
//                List<LatLng> route = MySearch.getRoute(NavigationActivity.this, mySearch.getId());
//                locationService.setRoute(route);
//
//                //lets fill fragments
//                NavigationFragment navigationFragment = (NavigationFragment) findFragmentByPosition(1);
//                if (navigationFragment != null) {
//                    navigationFragment.initIfNeeded(
//                            mySearch.getStartPoint(),
//                            mySearch.getBearing(),
//                            mySearch.getBearing2(),
//                            distance,
//                            route);
//                }
//            }
//            locationService.startNavigation(mySearch);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected()");
            locationService = null;
        }
    }

    public static final String TAG = NavigationActivity.class.getName();

}
