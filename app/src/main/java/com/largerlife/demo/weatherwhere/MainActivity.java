package com.largerlife.demo.weatherwhere;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.largerlife.demo.weatherwhere.model.Constants;
import com.largerlife.demo.weatherwhere.model.WeatherLocation;
import java.util.List;
import trikita.log.Log;

import static com.largerlife.demo.weatherwhere.model.Constants.BROADCAST_DATA_ACTION;
import static com.largerlife.demo.weatherwhere.model.Constants.BROADCAST_EXTRA_ERROR;
import static com.largerlife.demo.weatherwhere.model.Constants.BROADCAST_EXTRA_NEAREST;
import static com.largerlife.demo.weatherwhere.model.Constants.CAMERA_ANIMATION_INSTANT;
import static com.largerlife.demo.weatherwhere.model.Constants.CAMERA_ANIMATION_MOVE;
import static com.largerlife.demo.weatherwhere.model.Constants.CAMERA_ANIMATION_ZOOM;
import static com.largerlife.demo.weatherwhere.model.Constants.CAMERA_MOVE_DURATION;
import static com.largerlife.demo.weatherwhere.model.Constants.CAMERA_ZOOM_DURATION;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_DATA_ACTION;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_DATA_CATEGORY;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_COUNT;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_LATITUDE;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_LONGITUDE;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_UNITS;
import static com.largerlife.demo.weatherwhere.model.Constants.LAT_LNG_BUDAPEST;
import static com.largerlife.demo.weatherwhere.model.Constants.MAP_ZOOM;
import static com.largerlife.demo.weatherwhere.model.Constants.MAX_CLUSTER_COUNT;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_TEMP;
import static com.largerlife.demo.weatherwhere.model.Constants.UNITS_METRIC;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                               GoogleMap.CancelableCallback,
                                                               GoogleApiClient.ConnectionCallbacks,
                                                               GoogleApiClient
                                                                     .OnConnectionFailedListener,
                                                               GoogleMap.OnMapClickListener {

    final static String TAG = "Weather Where App";

    @InjectView(R.id.mainLayout) CoordinatorLayout mCoordinatorLayout;

    private boolean mMapReady = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mTargetLocation;
    private CameraPosition.Builder mCameraPositionBuilder;
    private Marker[] mMarkers = new Marker[Constants.MAX_CLUSTER_COUNT];

    private IntentFilter mReceiverIntentFilter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNewIntent(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        buildGoogleApiClient();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
              .addApi(LocationServices.API)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .build();
    }

    @Override
    protected void onStop() {
        Log.d("onStop");
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("OnPause");
        LocalBroadcastManager.getInstance(getApplicationContext())
                             .unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.w("onNewIntent", intent);
        super.onNewIntent(intent);
        if (intent.getAction().equals(BROADCAST_DATA_ACTION)) {
            if (intent.hasExtra(BROADCAST_EXTRA_ERROR)) {
                @StringRes int errorResId = intent.getIntExtra(
                      BROADCAST_EXTRA_ERROR,
                      R.string.snack_general_error);
                makeWarningSnackBar(errorResId, R.string.snack_action_retry,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (mTargetLocation != null) {
                                                startFetchDataService(mTargetLocation);
                                            }
                                        }
                                    });
            } else if (intent.hasExtra(BROADCAST_EXTRA_NEAREST)) {
                WeatherLocation location = intent.getParcelableExtra(BROADCAST_EXTRA_NEAREST);
                displayMarker(location);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("OnResume");
        if (mReceiverIntentFilter != null) {
            LocalBroadcastManager.getInstance(getApplicationContext())
                                 .registerReceiver(mBroadcastReceiver, mReceiverIntentFilter);
        }
    }

    @Override
    protected void onStart() {
        Log.d("onStart");
        super.onStart();
    }

    private void displayMarker(final WeatherLocation location) {

        String localized = String.format(getResources().getString(R.string.temperature));
        final String temperature = location.getFormattedString(location.getTemperature()
              , PARAM_TEMP, localized);
        MarkerOptions markerOptions = new MarkerOptions()
              .position(location.getCoord())
              .title(location.getLocationName())
              .snippet(temperature);
        if (mMarkers[0] != null) {
            mMarkers[0].remove();
        }
        mMarkers[0] = mMap.addMarker(markerOptions);
        moveMapCamera(location.getCoord(), CAMERA_ANIMATION_ZOOM,
                      new GoogleMap.CancelableCallback() {
                          @Override
                          public void onFinish() {

                              if (mMarkers[0] != null && !mMarkers[0].isInfoWindowShown()) {
                                  mMarkers[0].showInfoWindow();
                              }
                              /**
                               * TODO:  creating custom InfoWindow with multiple properties
                               makeSnackBar(temperature, R.string.snack_action_ok, R.color.primary,
                               Snackbar.LENGTH_LONG);
                               **/
                          }

                          @Override
                          public void onCancel() {
                          }
                      });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady");
        mMapReady = true;
        mMap = googleMap;

        LatLng targetBudapest = LAT_LNG_BUDAPEST;
        mCameraPositionBuilder = CameraPosition.builder()
                                               .target(targetBudapest).zoom(Constants.MAP_ZOOM);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPositionBuilder.build()));
        mMap.setOnMapClickListener(this);
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        mReceiverIntentFilter = new IntentFilter(BROADCAST_DATA_ACTION);
        Log.d("registering broadcast receiver for ", mReceiverIntentFilter);
        LocalBroadcastManager.getInstance(getApplicationContext())
                             .registerReceiver(mBroadcastReceiver, mReceiverIntentFilter);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("GoogleApiClient.onConnected",
              mGoogleApiClient.getConnectionResult(LocationServices.API));
        //Get the current location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation == null) {
            makeWarningSnackBar(
                  R.string.snack_no_location_service,
                  R.string.snack_action_retry,
                  new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          onFinish();
                          moveToMyLocation(v);
                      }
                  });
            onFinish();
        } else if (mLastLocation != null && mMapReady) {
            mTargetLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            startFetchDataService(mTargetLocation);
            moveMapCamera(mTargetLocation, CAMERA_ANIMATION_MOVE);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w("onConnectionSuspended", i);
        mGoogleApiClient.connect();
    }

    private void makeWarningSnackBar(@StringRes int messageResId,
          @StringRes int actionResId,
          @Nullable View.OnClickListener clickListener) {
        Log.w(getString(messageResId));

        @ColorInt int actionColor = getResources().getColor(R.color.snack_warning);

        Snackbar.make(mCoordinatorLayout, getString(messageResId), Snackbar.LENGTH_LONG)
                .setAction(actionResId, clickListener)
                .setActionTextColor(actionColor)
                .show();
    }

    @OnClick(R.id.fab)
    public void moveToMyLocation(View v) {
        mGoogleApiClient.connect();
    }

    /**
     * Creates an IntentService wich connects to the openWeatherMap API.
     */
    private void startFetchDataService(final LatLng target) {
        Intent fetchIntent = new Intent(FETCH_DATA_ACTION);
        fetchIntent.addCategory(FETCH_DATA_CATEGORY);
        fetchIntent.putExtra(FETCH_EXTRA_LATITUDE, target.latitude);
        fetchIntent.putExtra(FETCH_EXTRA_LONGITUDE, target.longitude);
        fetchIntent.putExtra(FETCH_EXTRA_UNITS, UNITS_METRIC);
        fetchIntent.putExtra(FETCH_EXTRA_COUNT, MAX_CLUSTER_COUNT);
        Log.d("startFetchDataService with ", fetchIntent);
        //we need an explicit intent since Android L
        //see: https://commonsware.com/blog/2014/06/29/dealing-deprecations-bindservice.html
        startService(convertToExplicitIntent(this, fetchIntent));
    }

    /**
     * Moves or animate the camera to the specified target location.
     *
     * @param target the target location
     * @param animationMode can be {@link Constants#CAMERA_ANIMATION_INSTANT},
     * {@link Constants#CAMERA_ANIMATION_MOVE}, {@link Constants#CAMERA_ANIMATION_ZOOM}
     */
    private void moveMapCamera(final LatLng target, int animationMode) {
        moveMapCamera(target, animationMode, this);
    }

    /**
     * Moves or animate the camera to the specified target location.
     *
     * @param target the target location
     * @param animationMode can be {@link Constants#CAMERA_ANIMATION_INSTANT},
     * {@link Constants#CAMERA_ANIMATION_MOVE}, {@link Constants#CAMERA_ANIMATION_ZOOM}
     * @param customCallback callback after the animation is finished.
     */
    private void moveMapCamera(final LatLng target, int animationMode,
          GoogleMap.CancelableCallback customCallback) {
        mCameraPositionBuilder.target(target);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int includedMarkers = 0;
        for (Marker marker : mMarkers) {
            if (marker != null) {
                builder.include(marker.getPosition());
                includedMarkers++;
            }
        }

        CameraUpdate cameraUpdate;
        if (includedMarkers > 1) {
            LatLngBounds bounds = builder.build();
            int padding = 50; // offset from edges of the map in pixels
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        } else {
            if (animationMode == CAMERA_ANIMATION_MOVE ||
                  animationMode == CAMERA_ANIMATION_INSTANT) {
                cameraUpdate = CameraUpdateFactory
                      .newCameraPosition(mCameraPositionBuilder.build());
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(target, MAP_ZOOM);
            }
        }

        if (animationMode == CAMERA_ANIMATION_INSTANT) {
            mMap.moveCamera(cameraUpdate);
        } else {
            int duration = animationMode == CAMERA_ANIMATION_MOVE
                           ? CAMERA_MOVE_DURATION
                           : CAMERA_ZOOM_DURATION;
            mMap.animateCamera(cameraUpdate, duration, customCallback);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("onMapClick ", latLng);
        mTargetLocation = latLng;
        startFetchDataService(latLng);
    }

    /**
     * Callback for Google map animateCamera, calls when animation finished.
     */
    @Override
    public void onFinish() {
        Log.d("GoogleMap animateCamera finished.");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("onConnectionFailed", connectionResult);
    }

    /**
     * Callback for Google map animateCamera, calls when animation cancelled.
     */
    @Override
    public void onCancel() {
        Log.d("GoogleMap.animateCamera cancelled.");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Converts an implicit intent to an explicit intent, required since Android L.
     * see : https://commonsware.com/blog/2014/06/29/dealing-deprecations-bindservice.html
     *
     * @param context the context
     * @param implicitIntent implicit intent which to be converted.
     * @return the explicit intent from the specified implicit intent.
     */
    public static Intent convertToExplicitIntent(final Context context,
          final Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        final ResolveInfo serviceInfo = resolveInfo.get(0);
        final String packageName = serviceInfo.serviceInfo.packageName;
        final String className = serviceInfo.serviceInfo.name;
        final ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
