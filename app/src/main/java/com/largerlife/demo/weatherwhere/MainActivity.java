package com.largerlife.demo.weatherwhere;

import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.ColorUtils;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.largerlife.demo.weatherwhere.model.WeatherLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import trikita.log.Log;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.CancelableCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Maximum number of places within the specified location for the openweathermap request.
     */
    public static final int MAX_CLUSTER_COUNT = 7;

    /**
     * OpenWeatherMap request which returns a json string containing places in the vicinity of the
     * latitude and longitude specified by 'lat' and 'lon' parameter in the query string,
     * limited by the 'cnt' parameter.
     */
    public static final String OPENWEATHER_BASE_URL =
            "http://api.openweathermap.org/data/2.5/find?";

    static final String QUERY_PARAM_LATITUDE = "lat";
    static final String QUERY_PARAM_LONGITUDE = "lon";
    static final String QUERY_PARAM_COUNT = "cnt";
    static final String QUERY_PARAM_UNITS = "units";

    final static String TAG = "WeatherWhere App";
    public static final int MAP_ANIMATION_MS = 1000;
    public static final int MAP_ZOOM = 10;


    @InjectView(R.id.mainLayout) CoordinatorLayout mCoordinatorLayout;
    @InjectView(R.id.fab) FloatingActionButton mFabMyLocation;

    private boolean mMapReady = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mTargetLocation;
    private CameraPosition.Builder mCameraPositionBuilder;
    private CircleOptions mCircleOptions;
    private Circle mMapRange;
    private PolygonOptions mPolygonOptions;
    private Marker[] mMarkers = new Marker[MAX_CLUSTER_COUNT];

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

        LatLng targetBudapest = LatLngUtil.LAT_LNG_BUDAPEST;
        mCameraPositionBuilder = CameraPosition.builder()
                .target(targetBudapest).zoom(MAP_ZOOM);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPositionBuilder.build()));

//        displaySearchZone();
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
            onFinish();
        } else if (mLastLocation != null && mMapReady) {
            mTargetLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mCameraPositionBuilder.target(mTargetLocation);
            //disconnecting GoogleApiClient when the animation is finished via CancelableCallback.
//            displaySearchRange(mTargetLocation);
            String[] coordinates = new String[]{
                    Double.toString(mTargetLocation.latitude),
                    Double.toString(mTargetLocation.longitude),
                    "" + MAX_CLUSTER_COUNT
            };
            new FetchWeatherTask().execute(coordinates);
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

    private void displaySearchRange(LatLng center) {
        if (mMapRange != null) {
            mMapRange.remove();
        }

        @ColorInt int colorRange = getResources().getColor(R.color.secondary);
        @ColorInt int fillColor = ColorUtils.setAlphaComponent(colorRange, 85);
        mCircleOptions = new CircleOptions()
                .center(center)
                .radius(10000)
                .fillColor(fillColor)
                .strokeColor(colorRange);
        mMapRange = mMap.addCircle(mCircleOptions);
    }

    private void displayBoundingRange(LatLng center, double radius) {
        if (mMapRange != null) {
            mMapRange.remove();
        }
        @ColorInt int colorRange = getResources().getColor(R.color.secondary);
        @ColorInt int fillColor = ColorUtils.setAlphaComponent(colorRange, 85);
        mCircleOptions = new CircleOptions()
                .center(center)
                .radius(10000)
                .fillColor(fillColor)
                .strokeColor(colorRange);
        mMapRange = mMap.addCircle(mCircleOptions);

    }

    private void makeSnackBar(int messageResId,
                              int actionResId,
                              @Nullable View.OnClickListener clickListener) {
        Log.w(getString(messageResId));
        Snackbar.make(mCoordinatorLayout, getString(messageResId), Snackbar.LENGTH_LONG)
                .setAction(actionResId, clickListener).show();
    }

    private static WeatherLocation[] getWeatherLocationsFromJson(String weatherJsonStr) throws JSONException {
        JSONObject jsonRawData = new JSONObject(weatherJsonStr);
        JSONArray jsonRawArray = jsonRawData.getJSONArray(WeatherLocation.PARAM_RESULT_LIST);
        WeatherLocation[] locations = new WeatherLocation[jsonRawArray.length()];
        for (int i = 0; i < jsonRawArray.length(); i++) {
            Log.d("buildFromJson: ", jsonRawArray.optString(i));
            locations[i] = WeatherLocation.builder()
                    .buildFromJson(jsonRawArray.optString(i));
        }
        return locations;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, WeatherLocation[]> {

        @Override
        protected WeatherLocation[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String weatherJsonStr = null;

            try {
                Uri builtUri = Uri.parse(OPENWEATHER_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM_LATITUDE, params[0])
                        .appendQueryParameter(QUERY_PARAM_LONGITUDE, params[1])
                        .appendQueryParameter(QUERY_PARAM_COUNT, params[2])
                        .appendQueryParameter(QUERY_PARAM_UNITS, WeatherLocation.setUnitsIn("metric"))
                        .build();

                URL url = new URL(builtUri.toString());
                Log.d("Url ", builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                weatherJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("Error fetching weather data ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherLocationsFromJson(weatherJsonStr);
            } catch (JSONException e) {
                Log.e("Error while parsing API response:", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(WeatherLocation[] locations) {
            super.onPostExecute(locations);
            MarkerOptions markerOptions;
            for (int i = 0; i < locations.length; i++) {
                markerOptions = new MarkerOptions()
                        .position(locations[i].getCoord())
                        .title(locations[i].getLocationName());
                if (mMarkers[i] != null) {
                    mMarkers[i].remove();
                }
                mMarkers[i] = mMap.addMarker(markerOptions);
            }
        }
    }
}
