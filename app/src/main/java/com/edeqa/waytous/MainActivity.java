package com.edeqa.waytous;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.helpers.interfaces.BiConsumer;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.ContinueDialog;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.holders.view.CameraViewHolder;
import com.edeqa.waytous.holders.view.DrawerViewHolder;
import com.edeqa.waytous.holders.view.FabViewHolder;
import com.edeqa.waytous.holders.view.MapButtonsViewHolder;
import com.edeqa.waytous.holders.view.SettingsViewHolder;
import com.edeqa.waytous.holders.view.SnackbarViewHolder;
import com.edeqa.waytous.holders.view.SocialViewHolder;
import com.edeqa.waytous.holders.view.TrackingViewHolder;
import com.edeqa.waytous.holders.view.UserProfileViewHolder;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;

import io.nlopez.smartlocation.SmartLocation;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_CREATE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_DESTROY;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_PAUSE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESULT;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.BACK_PRESSED;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.MAP_READY;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.holders.property.GpsHolder.REQUEST_LOCATION_SINGLE;
import static com.edeqa.waytous.interfaces.Tracking.TRACKING_URI;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public final static int REQUEST_PERMISSION_LOCATION = 1;

    public final static String PREFERENCE_INTRO = "intro";  //NON-NLS

    public final static String ACTIVITY_RESULT_REQUEST_CODE = "requestCode";  //NON-NLS
    public final static String ACTIVITY_RESULT_RESULT_CODE = "resultCode";  //NON-NLS
    public final static String ACTIVITY_RESULT_DATA = "data";  //NON-NLS

    private GoogleMap map;
    private State state;
    private static boolean isVisible = false;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.main);

        state = State.getInstance();

//        if(OPTIONS.isDebugMode()){
//            getSharedPreferences("intro", MODE_PRIVATE).edit().clear().commit();
//            state.setPreference("intro",false);
//        }

        Utils.log(this, "BUILDCONFIG: debug=" + BuildConfig.DEBUG + ", build_type=" + BuildConfig.BUILD_TYPE + ", flavor=" + BuildConfig.FLAVOR); //NON-NLS

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        state.registerEntityHolder(new FabViewHolder(this));
        state.registerEntityHolder(new DrawerViewHolder(this));
        state.registerEntityHolder(new SnackbarViewHolder(this));
        state.registerEntityHolder(new SocialViewHolder(this));
        state.registerEntityHolder(new UserProfileViewHolder(this));
        state.registerEntityHolder(new SettingsViewHolder(this));
        state.registerEntityHolder(new TrackingViewHolder(this));

        state.fire(ACTIVITY_CREATE, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;

        /*if(!state.getBooleanPreference(PREFERENCE_INTRO,false)){
            state.setPreference(PREFERENCE_INTRO,true);
            startActivityForResult(new Intent(MainActivity.this, IntroActivity.class), 1);
            return;
        }*/

        if(!state.isGpsAccessRequested()) {
            state.setGpsAccessRequested(true);
            checkPermissions(REQUEST_PERMISSION_LOCATION,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
        } else if(state.isGpsAccessAllowed()) {
            onRequestPermissionsResult(REQUEST_PERMISSION_LOCATION, new String[]{}, new int[]{});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        state.fire(ACTIVITY_PAUSE);
        isVisible = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        state.getUsers().forAllUsers(new BiConsumer<Integer, MyUser>() {
            @Override
            public void accept(Integer number, MyUser myUser) {
                myUser.removeViews();
            }
        });
        state.fire(ACTIVITY_DESTROY);
        state.getSystemViewBus().clear();
    }

    @Override
    public void onBackPressed() {
//        findViewById(100000).setTag(1);
        state.fire(BACK_PRESSED);

        /*DrawerViewHolder holder = (DrawerViewHolder) state.getEntityHolder(DrawerViewHolder.TYPE);
        if (holder != null && holder.isDrawerOpen()) {
            holder.closeDrawer();
        } else {
            if(state.getBooleanPreference(PREFERENCE_BACKGROUND_ALERT_SHOWN, false) || state.tracking_disabled()) {
                super.onBackPressed();
            } else {
                final AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle(getString(R.string.alert));
                dialog.setMessage(getString(R.string.you_want_to_minimize_waytous));

                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        state.setPreference(PREFERENCE_BACKGROUND_ALERT_SHOWN, true);
                        MainActivity.super.onBackPressed();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
        state.fire(CREATE_OPTIONS_MENU, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        state.fire(PREPARE_OPTIONS_MENU, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle m = new Bundle();
        m.putInt(ACTIVITY_RESULT_REQUEST_CODE,requestCode);
        m.putInt(ACTIVITY_RESULT_RESULT_CODE,resultCode);
        m.putParcelable(ACTIVITY_RESULT_DATA,data);
        state.fire(ACTIVITY_RESULT, m);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);

        String action = newIntent.getStringExtra("action");
        if(action != null) {
            switch(action) {
                case "fire":
                    String fire = newIntent.getStringExtra("fire");
                    Integer number = newIntent.getIntExtra("number", 0);
                    newIntent.removeExtra("fire");
                    newIntent.removeExtra("number");
                    state.fire(fire, number);
            }
            return;
        }

        Uri data = newIntent.getData();
        newIntent.setData(null);
        String link = null;
        if(data != null) {
            link = data.toString();
        } else if(!state.tracking_active()) {
            link = state.getStringPreference(TRACKING_URI, null);
        }
        if(link != null) {
            state.fire(TRACKING_JOIN, link);
        }
    }

    public void checkPermissions(final int requestCode, String[] permissionsForCheck) {
        final ArrayList<String> permissions = new ArrayList<>();
        int[] grants = new int[permissionsForCheck.length];
        for (int i = 0; i < permissionsForCheck.length; i++) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsForCheck[i]) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permissionsForCheck[i]);
                grants[i] = PackageManager.PERMISSION_DENIED;
            } else {
                grants[i] = PackageManager.PERMISSION_GRANTED;
            }
        }

        if (permissions.size() > 0) {
            Thread.dumpStack();
            new ContinueDialog(this).setMessage(getString(R.string.application_must_continuously_get_your_locations)).setCallback(new Consumer<Void>() {
                @Override
                public void accept(Void arg) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            permissions.toArray(new String[permissions.size()]),
                            requestCode);
                }
            }).show();
        } else {
            onRequestPermissionsResult(requestCode, permissionsForCheck, grants);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                state.setGpsAccessRequested(true);
                int res = 0;
                for (int i : grantResults) {
                    res += i;
                }
                if (res == 0) {
                    state.setGpsAccessAllowed(true);
                    onMapReadyPermitted();
                } else {
                    state.setGpsAccessAllowed(false);
                    Toast.makeText(getApplicationContext(), R.string.gps_access_is_not_granted, Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if(state.isGpsAccessAllowed()) {
            onMapReadyPermitted();
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public void onMapReadyPermitted() {
        if(map == null) return;

        if(!SmartLocation.with(MainActivity.this).location().state().locationServicesEnabled()){
            new ContinueDialog(this).setMessage(getString(R.string.application_needs_the_enabled_location_services)).setCallback(new Consumer() {
                @Override
                public void accept(Object arg) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            }).show();
            return;
        }

        new MapButtonsViewHolder(this);

        LinkedList<String> classes = new LinkedList<>();
//        classes.add("SocialViewHolder");
        classes.add("ButtonViewHolder");
        classes.add("MenuViewHolder");
        classes.add("MarkerViewHolder");
        classes.add("AddressViewHolder");
        classes.add("CameraViewHolder");
        classes.add("SavedLocationViewHolder");
        classes.add("PlaceViewHolder");
        classes.add("TrackViewHolder");
        classes.add("NavigationViewHolder");
        classes.add("DistanceViewHolder");
        classes.add("MessagesViewHolder");
        classes.add("SensorsViewHolder");
        classes.add("StreetsViewHolder");
        classes.add("InfoViewHolder");
        classes.add("NmeaStatusViewHolder");

        // IntroViewHolder must be registered last

//        classes.add("IntroViewHolder");
        Utils.log(this,"Reinitialize");
        for(String s:classes){
            try {
                //noinspection unchecked
                Class<AbstractViewHolder> _tempClass = (Class<AbstractViewHolder>) Class.forName("com.edeqa.waytous.holders.view."+s);
                Constructor<AbstractViewHolder> ctor = _tempClass.getDeclaredConstructor(MainActivity.class);
                state.registerEntityHolder(ctor.newInstance(this));
            } catch (Exception e) {
                System.err.println("with " + s);
                e.printStackTrace();
            }
        }

//        state.registerEntityHolder(new LoggerViewHolder(this),this);

        state.getUsers().setMe();
        state.getMe().addLocation(SmartLocation.with(MainActivity.this).location().getLastLocation());

        map.setBuildingsEnabled(true);
        map.setIndoorEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!OPTIONS.isDebugMode()) return;
                try {
                    Location location = state.getMe().getLocation();
                    Location loc = new Location("touch");
                    loc.setLatitude(latLng.latitude);
                    loc.setLongitude(latLng.longitude);
                    loc.setAltitude(location.getAltitude());
                    loc.setAccuracy(location.getAccuracy());
                    loc.setBearing(location.getBearing());
                    loc.setSpeed(location.getSpeed());
                    loc.setTime(location.getTime());
                    state.getMe().addLocation(loc);
                }catch(Exception e){
                    Utils.err(MainActivity.this, "onMapReadyPermitted:", e.getMessage(), e);
                }
            }
        });
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        state.getUsers().forAllUsers(new BiConsumer<Integer, MyUser>() {
            @Override
            public void accept(Integer number, MyUser myUser) {
                myUser.createViews();
            }
        });
        ((CameraViewHolder) state.getEntityHolder(CameraViewHolder.TYPE)).move();

        state.fire(REQUEST_LOCATION_SINGLE);
        state.fire(ACTIVITY_RESUME, this);
        state.fire(MAP_READY, map);

        onNewIntent(getIntent());
    }

    public GoogleMap getMap() {
        return map;
    }

    /**
     * Due to onBackPressed() fires Events.BACK_PRESSED this method calls super.onBackPressed() for holders purposes.
     */
    public void onBackPressedSuper() {
        super.onBackPressed();
    }

    public static boolean isVisible() {
        return isVisible;
    }

}
