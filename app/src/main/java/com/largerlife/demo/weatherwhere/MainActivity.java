package com.largerlife.demo.weatherwhere;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import trikita.log.Log;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.CancelableCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final static String TAG = "WeatherWhere App";
    public static final int MAP_ANIMATION_MS = 1000;
    public static final int MAP_ZOOM = 10;
    public static final LatLng LAT_LNG_BUDAPEST = new LatLng(47.4812134, 19.1303031);

    @InjectView(R.id.mainLayout) CoordinatorLayout mCoordinatorLayout;
    @InjectView(R.id.fab) FloatingActionButton mFabMyLocation;
    private boolean mMapReady = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mTargetLocation;
    private CameraPosition.Builder mCameraPositionBuilder;

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
    protected void onStart() {
        Log.d("onStart");
        super.onStart();
//        mGoogleApiClient.connect();
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
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady");
        mMapReady = true;
        mMap = googleMap;
        makeSnackBar(R.string.snack_tap_place, R.string.snack_action_ok);

        LatLng targetBudapest = LAT_LNG_BUDAPEST;
        mCameraPositionBuilder = CameraPosition.builder()
                .target(targetBudapest).zoom(MAP_ZOOM);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPositionBuilder.build()));

//        MarkerOptions markerHome = new MarkerOptions()
//                .position(new LatLng(47.4948, 19.0348))
//                .title("Home")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.weather_cloudy));
//        mMap.addMarker(markerHome);
    }

    @OnClick(R.id.fab)
    public void moveToMyLocation(View v) {
        mGoogleApiClient.connect();
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("GoogleApiClient.onConnected",
                mGoogleApiClient.getConnectionResult(LocationServices.API));
        //Get the current location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation == null) {
            makeSnackBar(R.string.snack_no_location_service, R.string.snack_action_retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFinish();
                    moveToMyLocation(v);
                }
            });
            //disconnect GoogleApiClient
            onFinish();
        } else if (mLastLocation != null && mMapReady) {
            mTargetLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mCameraPositionBuilder.target(mTargetLocation);
            //disconnecting GoogleApiClient when the animation is finished via CancelableCallback.
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPositionBuilder.build())
                    , MAP_ANIMATION_MS, this);

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("onConnectionFailed", connectionResult);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w("onConnectionSuspended", i);
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void makeSnackBar(int messageResId, int actionResId) {
        makeSnackBar(messageResId, actionResId, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //empty click listemer for dismissing snackbar
            }
        });
    }

    private void makeSnackBar(int messageResId,
                              int actionResId,
                              @Nullable View.OnClickListener clickListener) {
        Log.w(getString(messageResId));
        Snackbar.make(mCoordinatorLayout, getString(messageResId), Snackbar.LENGTH_LONG)
                .setAction(actionResId, clickListener).show();
    }
}
