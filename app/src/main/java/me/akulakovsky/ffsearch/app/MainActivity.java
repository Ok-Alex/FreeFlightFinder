package me.akulakovsky.ffsearch.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import me.akulakovsky.ffsearch.app.fragments.NewSearchDialogFragment;
import me.akulakovsky.ffsearch.app.fragments.NewSearchDialogFragmentV2;
import me.akulakovsky.ffsearch.app.fragments.NewStartPointDialogFragment;
import me.akulakovsky.ffsearch.app.fragments.SavedSearchesFragment;
import me.akulakovsky.ffsearch.app.fragments.StartPointsFragment;
import me.akulakovsky.ffsearch.app.services.LocationService;
import me.akulakovsky.ffsearch.app.utils.Settings;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_flight_takeoff_white_24dp,
            R.drawable.ic_location_on_white_24dp};

    private LocationService locationService;
    private LocationServiceConnection mConnection = new LocationServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        findViewById(R.id.plus).setOnClickListener(this);

        Dexter.withActivity(this)
                .withPermissions(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SavedSearchesFragment(), "SEARCHES");
        adapter.addFragment(new StartPointsFragment(), "LOCATIONS");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            showBearingErrorDialog();
        }  else if (id == R.id.action_delete_all) {
            showDeleteAllDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cleaning")
                .setMessage("Are you sure want to delete all searches and locations?")
                .setPositiveButton("Yes,", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.deleteAll();
                            }
                        });
                    }
                }).setNegativeButton("No", null)
                .create().show();
    }

    private boolean checkGpsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        } else {
            return true;
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS");
        builder.setMessage("Your GPS seems to be disabled.\nDo you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void showBearingErrorDialog() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        float error = Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);
        editText.setText(String.valueOf(error));
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_action_reset)
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String errorStr = editText.getText().toString();
                        float error = Float.parseFloat(errorStr);
                        Settings.get().putValue(Settings.KEY_BEARING_MISTAKE, error);
                    }
                }).setNegativeButton(R.string.dialog_button_cancel, null)
                .create().show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.plus:
                if (checkGpsEnabled()) {
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        new NewSearchDialogFragmentV2().show(getSupportFragmentManager(), "NEW_SEARCH");
                    } else {
                        new NewStartPointDialogFragment().show(getSupportFragmentManager(), "NEW_LOCATION");
                    }
                }
                break;
        }
    }

    public void startLocationService() {
        if (locationService == null) {
            bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        } else {
            locationService.startNavigation(null);
        }
    }

    public void stopLocationService() {
        if (locationService != null) {
            locationService.stopNavigation();
        }
    }

    public LocationService getLocationService() {
        return locationService;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private class LocationServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected()");
            locationService = ((LocationService.ServiceBinder) iBinder).getService();
            locationService.startNavigation(null);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected()");
            locationService = null;
        }
    }
}