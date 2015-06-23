package com.largerlife.demo.weatherwhere.model;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

/**
 * Created by LargerLife on 22/06/15.
 */
public class Constants {
    //Intent and Broadcast Receiver related constants.
    public static final String PACKAGE_NAME = "com.largerlife.demo.weatherwhere.model";
    public static final String FETCH_DATA_ACTION = PACKAGE_NAME + ".FETCH";
    public static final String FETCH_DATA_CATEGORY = PACKAGE_NAME + ".FETCH_DEFAULT";
    public static final String BROADCAST_DATA_ACTION = PACKAGE_NAME + ".BROADCAST";
    public static final String BROADCAST_EXTRA_NEAREST = PACKAGE_NAME + ".NEAREST";
    public static final String BROADCAST_EXTRA_ERROR = PACKAGE_NAME + ".ERROR";

    public static final String FETCH_EXTRA_LATITUDE = PACKAGE_NAME + ".QUERY_LATITUDE";
    public static final String FETCH_EXTRA_LONGITUDE = PACKAGE_NAME + ".QUERY_LONGITUDE";
    public static final String FETCH_EXTRA_UNITS = PACKAGE_NAME + ".QUERY_METRICS";
    public static final String FETCH_EXTRA_COUNT = PACKAGE_NAME + ".QEURY_COUNT";

    /**
     * Maximum number of places within the specified location for the openweathermap request.
     */
    public static final int MAX_CLUSTER_COUNT = 5;
    /**
     * OpenWeatherMap request which returns a json string containing places in the vicinity of the
     * latitude and longitude specified by 'lat' and 'lon' parameter in the query string,
     * limited by the 'cnt' parameter.
     */
    public static final String OPENWEATHER_BASE_URL =
            "http://api.openweathermap.org/data/2.5/find?";

    public static final String UNITS_METRIC = "metric";
    static final String UNITS_IMPERIAL = "imperial";

    //OpenWeatherMap query related parameter constants.
    public static final String QUERY_PARAM_LATITUDE = "lat";
    public static final String QUERY_PARAM_LONGITUDE = "lon";
    public static final String QUERY_PARAM_COUNT = "cnt";
    public static final String QUERY_PARAM_UNITS = "units";

    //OpenWeatherMap JSON response related parameter constants.
    public static final String PARAM_RESULT_LIST = "list";
    public static final String PARAM_ID = "id";
    public static final String PARAM_LOCATION_NAME = "name";
    public static final String PARAM_LATITUDE = "coord:lat";
    public static final String PARAM_LONGITUDE = "coord:lon";
    public static final String PARAM_TEMP = "main:temp";
    public static final String PARAM_TEMP_MIN = "main:temp_min";
    public static final String PARAM_TEMP_MAX = "main:temp_max";
    public static final String PARAM_PRESSURE = "main:pressure";
    public static final String PARAM_HUMIDITY = "main:humidity";

    public static final String PARAM_WIND_SPEED = "wind:speed";
    public static final String PARAM_WEATHER_MAIN = "weather:main";
    public static final String PARAM_WEATHER_DESC = "weather:description";
    public static final String PARAM_WEATHER_ICON = "weather:icon";

    static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    //Map related constants.
    public static final int CAMERA_ZOOM_DURATION = 500;
    public static final int CAMERA_MOVE_DURATION = 1000;
    public static final int MAP_ZOOM = 11;
    public static final int CAMERA_ANIMATION_INSTANT = 0;
    public static final int CAMERA_ANIMATION_MOVE = 1;
    public static final int CAMERA_ANIMATION_ZOOM = 2;

    public static final LatLng LAT_LNG_BUDAPEST = new LatLng(47.4812134, 19.1303031);


}
