package com.largerlife.demo.weatherwhere;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.largerlife.demo.weatherwhere.model.Constants;
import com.largerlife.demo.weatherwhere.model.LocationComparator;
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
import java.util.ArrayList;
import java.util.Collections;

import trikita.log.Log;

import static com.largerlife.demo.weatherwhere.model.Constants.BROADCAST_DATA_ACTION;
import static com.largerlife.demo.weatherwhere.model.Constants.BROADCAST_EXTRA_ERROR;
import static com.largerlife.demo.weatherwhere.model.Constants.BROADCAST_EXTRA_NEAREST;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_DATA_ACTION;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_COUNT;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_LATITUDE;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_LONGITUDE;
import static com.largerlife.demo.weatherwhere.model.Constants.FETCH_EXTRA_UNITS;
import static com.largerlife.demo.weatherwhere.model.Constants.MAX_CLUSTER_COUNT;

/**
 * Created by LargerLife on 22/06/15.
 */
public class FetchWeatherDataService extends IntentService {

    static final String TAG = "FetchWeatherDataService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FetchWeatherDataService() {
        super(TAG);
    }

    /**
     * The specified intent is called from an Activity, to connect to OpenWeather API.
     * Retrieves the weather data converts to object model, and sorts the collection of
     * WeatherLocation objects to find the nearest from the center point passed in an extra
     * intent parameter.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w("onHandleIntent ", intent);
        if (!intent.getAction().equals(FETCH_DATA_ACTION)) {
            Log.e("Invalid intent action", intent.getAction());
            return;
        }
        double latitude = intent.getDoubleExtra(FETCH_EXTRA_LATITUDE, -1.0d);
        double longitude = intent.getDoubleExtra(FETCH_EXTRA_LONGITUDE, -1.0d);
        String units = intent.getStringExtra(FETCH_EXTRA_UNITS);
        int count = intent.getIntExtra(FETCH_EXTRA_COUNT, MAX_CLUSTER_COUNT);

        Log.d("Extras:", latitude, longitude, units, count);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String weatherJsonStr = null;
        Intent broadCastIntent = new Intent(BROADCAST_DATA_ACTION);
        try {
            Uri builtUri = Uri.parse(Constants.OPENWEATHER_BASE_URL).buildUpon()
                    .appendQueryParameter(Constants.QUERY_PARAM_LATITUDE, Double.toString(latitude))
                    .appendQueryParameter(Constants.QUERY_PARAM_LONGITUDE, Double.toString(longitude))
                    .appendQueryParameter(Constants.QUERY_PARAM_COUNT, Integer.toString(count))
                    .appendQueryParameter(Constants.QUERY_PARAM_UNITS, WeatherLocation.setUnitsIn(units))
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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            weatherJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e("Error fetching weather data sending empty broadcast", broadCastIntent, e);
            //sending vroadcast about the error.
            broadCastIntent.putExtra(BROADCAST_EXTRA_ERROR, R.string.snack_error_fetch_data);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(broadCastIntent);
            return;
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
            Location tappedLocation = new Location("tapped");
            tappedLocation.setLatitude(latitude);
            tappedLocation.setLongitude(longitude);
            ArrayList<WeatherLocation> locations = getWeatherLocationsFromJson(weatherJsonStr);

            Log.d("Sorting results according the distance from ", tappedLocation.toString());
            Collections.sort(locations, new LocationComparator(tappedLocation));
            WeatherLocation nearest = locations.get(0);


            broadCastIntent.putExtra(BROADCAST_EXTRA_NEAREST, nearest);
            Log.d("Broadcasting data ", intent);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(broadCastIntent);
        } catch (JSONException e) {
            Log.e("Error while parsing API response:", e.getMessage(), e);
        }
    }

    private static ArrayList<WeatherLocation> getWeatherLocationsFromJson(String weatherJsonStr)
            throws JSONException {
        JSONObject jsonRawData = new JSONObject(weatherJsonStr);
        JSONArray jsonRawArray = jsonRawData.getJSONArray(Constants.PARAM_RESULT_LIST);
        ArrayList<WeatherLocation> locations = new ArrayList<>(jsonRawArray.length());
        Log.d("building %s objects from json", jsonRawArray.length());
        for (int i = 0; i < jsonRawArray.length(); i++) {
            locations.add(WeatherLocation.builder().buildFromJson(jsonRawArray.optString(i)));
        }
        return locations;
    }
}
