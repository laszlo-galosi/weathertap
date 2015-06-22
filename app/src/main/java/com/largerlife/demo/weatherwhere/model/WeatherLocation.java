package com.largerlife.demo.weatherwhere.model;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by LargerLife on 22/06/15.
 */
public class WeatherLocation {
    public static final String PARAM_RESULT_LIST = "list";

    public static final String PARAM_ID = "id";
    public static final String PARAM_LOCATION_NAME = "name";
    public static final String PARAM_LATITUDE = "coord:lat";
    public static final String PARAM_LONGITUDE = "coord:lon";
    public static final String PARAM_TEMP = "main:temp";
    public static final String PARAM_TEMP_MIN = "main:temp_min";
    public static final String PARAM_TEMP_MAX = "main:temp_max";
    public static final String PARAM_WIND_SPEED = "wind:speed";
    public static final String PARAM_WEATHER_MAIN = "weather:main";
    public static final String PARAM_WEATHER_DESC = "weather:description";

    public static final String PARAM_WEATHER_ICON = "weather:icon";
    static final String UNITS_METRIC = "metric";

    static final String UNITS_IMPERIAL = "imperial";
    static final int TEMP_CELSIUS = 0;
    static final int TEMP_FAHRENHEIT = 1;
    static final int TEMP_KELVIN = 2;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private static String unitsIn;

    int uuid;
    String locationName;
    LatLng coord;
    double temperature;
    double minTemperature;
    double maxTemperatue;
    double windSpeed;
    String weatherMain;
    String weatherIcon;
    String weatherDescription;

    public WeatherLocation(Builder b) {
        this.uuid = b.uuid;
        this.locationName = b.locationName;
        this.coord = b.coord;
        this.temperature = b.temperature;
        this.minTemperature = b.minTemperature;
        this.maxTemperatue = b.maxTemperature;
        this.windSpeed = b.windSpeed;
        this.weatherMain = b.weatherMain;
        this.weatherIcon = b.weatherIcon;
        this.weatherDescription = b.weatherDescription;
    }

    public int getUuid() {
        return uuid;
    }

    public String getLocationName() {
        return locationName;
    }

    public LatLng getCoord() {
        return coord;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperatue() {
        return maxTemperatue;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    /**
     * Returns the correct units query parameter, and sets the initialization of this value.
     *
     * @param units units query parameter.
     * @return the correct param value
     * @throws IllegalArgumentException only {@link #UNITS_METRIC} supported yet
     */
    public static String setUnitsIn(final String units) throws IllegalArgumentException {
        if (!units.equals(UNITS_METRIC)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet.Please, set to %s only.",
                            units, UNITS_METRIC));
        }
        WeatherLocation.unitsIn = units;
        return WeatherLocation.unitsIn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof WeatherLocation) {
            WeatherLocation other = (WeatherLocation) o;
            return uuid == other.uuid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uuid;
    }

    public static WeatherLocation.Builder builder() {
        return new WeatherLocation.Builder();
    }


    /**
     * Returns a truncated value of the given value only supported in metric units.
     *
     * @param rawValue the raw value from the json string in metric unit.
     * @return the computed temperature value
     */
    public static double getTemperature(double rawValue) throws IllegalArgumentException {
        if (WeatherLocation.unitsIn == null || unitsIn.equals(UNITS_IMPERIAL)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet or not set.Please, set to %s only.",
                            unitsIn, UNITS_METRIC));
        }
        double tempValue = rawValue;
        Double truncatedDouble = new BigDecimal(tempValue).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return truncatedDouble;
    }

    /**
     * Returns a truncated and converted km/h
     * value of the given speed in m/s value only supported in metric units in km/h.
     *
     * @param rawValue the raw value from the json string in m/sec metric unit.
     * @return the computed speed value converted to km/h
     */
    public static double getSpeed(double rawValue) throws IllegalArgumentException {
        if (WeatherLocation.unitsIn == null || unitsIn.equals(UNITS_IMPERIAL)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet or not set.Please, set to %s only.",
                            unitsIn, UNITS_METRIC));
        }
        double kmPerHourValue = (rawValue * 18) / 5;
        Double truncatedDouble = new BigDecimal(kmPerHourValue).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return truncatedDouble;
    }

    public static String getFormattedTemperature(double metricValue) throws IllegalArgumentException {
        if (WeatherLocation.unitsIn == null || !unitsIn.equals(UNITS_METRIC)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet or not set. Please, set to %s only.",
                            unitsIn, UNITS_METRIC));
        }
        return String.format("%s C", DECIMAL_FORMAT.format(metricValue));
    }

    public static String getFormattedSpeed(double metricValue) throws IllegalArgumentException {
        if (WeatherLocation.unitsIn == null || !unitsIn.equals(UNITS_METRIC)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet or not set. Please, set to %s only.",
                            unitsIn, UNITS_METRIC));
        }
        return String.format("%s km/h", DECIMAL_FORMAT.format(metricValue));
    }

    /**
     * Returns a json value from the json string, based on a tree-like parameter path delimited
     * by ':'
     *
     * @param paramPath  the colon separated object path of the leaf node.
     * @param jsonString the json string of one result row
     * @return the object value
     * @throws JSONException thrown if the json string or is invalid
     */
    public static String getJSonParamValueByParamPath(final String paramPath, final String jsonString)
            throws JSONException {
        final String[] paramNames = paramPath.split(":");
        JSONObject json = new JSONObject(jsonString);
        String leafValue = null;
        for (int i = 0; i < paramNames.length; i++) {
            if (json.optJSONArray(paramNames[i]) != null) {
                JSONArray jsonArray = json.optJSONArray(paramNames[i]);
                //simply getting the first element of the 'weather' tag
                json = jsonArray.getJSONObject(0);
                continue;
            }
            if (json.optJSONObject(paramNames[i]) != null) {
                json = json.optJSONObject(paramNames[i]);
                continue;
            }
            leafValue = json.optString(paramNames[i]);
        }
        return leafValue;
    }

    public static class Builder {
        int uuid;
        String locationName;
        LatLng coord;
        double temperature;
        double minTemperature;
        double maxTemperature;
        double windSpeed;
        String weatherMain;
        String weatherIcon;
        String weatherDescription;

        public Builder setUuid(int uuid) {

            this.uuid = uuid;
            return this;
        }

        public Builder setLocationName(String locationName) {
            this.locationName = locationName;
            return this;
        }

        public Builder setCoord(LatLng coord) {
            this.coord = coord;
            return this;
        }

        public Builder setTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder setMinTemperature(double minTemperature) {
            this.minTemperature = minTemperature;
            return this;
        }

        public Builder setMaxTemperature(double maxTemperature) {
            this.maxTemperature = maxTemperature;
            return this;
        }

        public Builder setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }

        public Builder setWeather(String weatherMain) {
            this.weatherMain = weatherMain;
            return this;
        }

        public Builder setWeatherIcon(String weatherIcon) {
            this.weatherIcon = weatherIcon;
            return this;
        }

        public Builder setWeatherDescription(String weatherDescription) {
            this.weatherDescription = weatherDescription;
            return this;
        }

        public WeatherLocation buildFromJson(final String jsonString)
                throws JSONException, NumberFormatException {
            int uuid = Integer.parseInt(getJSonParamValueByParamPath(PARAM_ID, jsonString));
            String placeName = getJSonParamValueByParamPath(PARAM_LOCATION_NAME, jsonString);
            double latitude = Double.parseDouble(
                    getJSonParamValueByParamPath(PARAM_LATITUDE, jsonString));
            double longitude = Double.parseDouble(
                    getJSonParamValueByParamPath(PARAM_LONGITUDE, jsonString));
            LatLng coord = new LatLng(latitude, longitude);
            double tempMain = getTemperature(Double.parseDouble(
                    getJSonParamValueByParamPath(PARAM_TEMP, jsonString)));
            double tempMin = getTemperature(Double.parseDouble(
                    getJSonParamValueByParamPath(PARAM_TEMP_MIN, jsonString)));
            double tempMax = getTemperature(Double.parseDouble(
                    getJSonParamValueByParamPath(PARAM_TEMP_MAX, jsonString)));
            double windSpeed = Double.parseDouble(
                    getJSonParamValueByParamPath(PARAM_WIND_SPEED, jsonString));
            String weatherMain = getJSonParamValueByParamPath(PARAM_WEATHER_MAIN, jsonString);
            String weatherDesc = getJSonParamValueByParamPath(PARAM_WEATHER_DESC, jsonString);
            String weatherIcon = getJSonParamValueByParamPath(PARAM_WEATHER_ICON, jsonString);

            WeatherLocation weatherLocationResult = this.setUuid(uuid)
                    .setLocationName(placeName)
                    .setCoord(coord)
                    .setTemperature(tempMain)
                    .setMinTemperature(tempMin)
                    .setMaxTemperature(tempMax)
                    .setWindSpeed(windSpeed)
                    .setWeather(weatherMain)
                    .setWeatherDescription(weatherDesc)
                    .setWeatherIcon(weatherIcon)
                    .build();
            return weatherLocationResult;
        }

        public WeatherLocation build() {
            return new WeatherLocation(this);
        }
    }
}
