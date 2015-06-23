package com.largerlife.demo.weatherwhere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_HUMIDITY;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_PRESSURE;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_TEMP;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_TEMP_MAX;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_TEMP_MIN;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_WIND_SPEED;


/**
 * Data model containing the OpenWeatherApi response data.
 */
public class WeatherLocation implements Parcelable {


    private static String unitsIn;

    int uuid;
    String locationName;
    LatLng coord;
    double temperature;
    double minTemperature;
    double maxTemperatue;
    double pressure;
    double humidity;
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
        this.pressure = b.pressure;
        this.humidity = b.humidity;
        this.windSpeed = b.windSpeed;
        this.weatherMain = b.weatherMain;
        this.weatherIcon = b.weatherIcon;
        this.weatherDescription = b.weatherDescription;
    }

    private WeatherLocation(Parcel in) {
        this(new Builder().builder(in));
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

    public double getMaxTemperature() {
        return maxTemperatue;
    }

    public double getPressure() {
        return pressure;
    }

    public WeatherLocation setPressure(double pressure) {
        this.pressure = pressure;
        return this;
    }

    public double getHumidity() {
        return humidity;
    }

    public WeatherLocation setHumidity(double humidity) {
        this.humidity = humidity;
        return this;
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
     * @throws IllegalArgumentException only {@link Constants#UNITS_METRIC} supported yet
     */
    public static String setUnitsIn(final String units) throws IllegalArgumentException {
        if (!units.equals(Constants.UNITS_METRIC)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet.Please, set to %s only.",
                            units, Constants.UNITS_METRIC));
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
     * Returns a truncated and converted double
     * value of the given para in m/s value only supported in metric units in km/h.
     *
     * @param rawValue the raw value from the json string in m/sec metric unit.
     * @return the computed speed value converted to km/h
     */
    public static double getUnitsIn(double rawValue, String paramName) throws IllegalArgumentException {
        if (WeatherLocation.unitsIn == null || unitsIn.equals(Constants.UNITS_IMPERIAL)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet or not set.Please, set to %s only.",
                            unitsIn, Constants.UNITS_METRIC));
        }
        double converted = rawValue;
        if (paramName.equals(PARAM_WIND_SPEED)) {
            converted = (rawValue * 18) / 5;
        }
        Double truncatedDouble = new BigDecimal(converted).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return truncatedDouble;
    }


    /**
     * Returns a formatted string to display for the specified parameter.
     *
     * @param metricValue the value in metric units
     * @param paramName   the parameter type.
     * @param localized   a localized string to display
     * @return
     * @throws IllegalArgumentException thrown if not unit mode is not configured.
     */
    public String getFormattedString(double metricValue, String paramName, String localized) throws IllegalArgumentException {
        if (WeatherLocation.unitsIn == null || !unitsIn.equals(Constants.UNITS_METRIC)) {
            throw new IllegalArgumentException(
                    String.format("%s mode not supported yet or not set. Please, set to %s only.",
                            unitsIn, Constants.UNITS_METRIC));
        }
        String paramType = paramName.substring(paramName.lastIndexOf(":") + 1);
        if (paramName.equals(PARAM_TEMP) || paramName.equals(PARAM_TEMP_MAX)
                || paramName.equals(PARAM_TEMP_MIN)) {
            final String DEGREE = "\u00b0";
            return String.format("%s: %s C%s", localized,
                    Constants.DECIMAL_FORMAT.format(metricValue),
                    DEGREE);
        } else if (paramName.equals(PARAM_WIND_SPEED)) {
            return String.format(localized,
                    Constants.DECIMAL_FORMAT.format(metricValue));
        }
        return String.format("%s km/h", Constants.DECIMAL_FORMAT.format(metricValue));
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

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uuid);
        dest.writeString(locationName);
        dest.writeParcelable(coord, flags);
        dest.writeDouble(temperature);
        dest.writeDouble(minTemperature);
        dest.writeDouble(maxTemperatue);
        dest.writeDouble(pressure);
        dest.writeDouble(humidity);
        dest.writeDouble(windSpeed);
        dest.writeString(weatherMain);
        dest.writeString(weatherIcon);
        dest.writeString(weatherDescription);
    }

    public static final Parcelable.Creator<WeatherLocation> CREATOR
            = new Parcelable.Creator<WeatherLocation>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public WeatherLocation createFromParcel(Parcel in) {
            return new WeatherLocation(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public WeatherLocation[] newArray(int size) {
            return new WeatherLocation[size];
        }
    };

    public static class Builder {
        int uuid;
        String locationName;
        LatLng coord;
        double temperature;
        double minTemperature;
        double maxTemperature;
        public double pressure;

        public double humidity;

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

        public Builder setHumidity(double humidity) {
            this.humidity = humidity;
            return this;
        }

        public Builder setPressure(double pressure) {
            this.pressure = pressure;
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

        public Builder builder(Parcel in) {
            uuid = in.readInt();
            locationName = in.readString();
            coord = in.readParcelable(WeatherLocation.class.getClassLoader());
            temperature = in.readDouble();
            minTemperature = in.readDouble();
            maxTemperature = in.readDouble();
            pressure = in.readDouble();
            humidity = in.readDouble();
            windSpeed = in.readDouble();
            weatherMain = in.readString();
            weatherIcon = in.readString();
            weatherDescription = in.readString();
            return this;
        }

        public WeatherLocation buildFromJson(final String jsonString)
                throws JSONException, NumberFormatException {
            int uuid = Integer.parseInt(getJSonParamValueByParamPath(Constants.PARAM_ID, jsonString));
            String placeName = getJSonParamValueByParamPath(Constants.PARAM_LOCATION_NAME, jsonString);
            double latitude = Double.parseDouble(
                    getJSonParamValueByParamPath(Constants.PARAM_LATITUDE, jsonString));
            double longitude = Double.parseDouble(
                    getJSonParamValueByParamPath(Constants.PARAM_LONGITUDE, jsonString));
            LatLng coord = new LatLng(latitude, longitude);
            double tempMain = getUnitsIn(
                    Double.parseDouble(
                            getJSonParamValueByParamPath(Constants.PARAM_TEMP, jsonString))
                    , PARAM_TEMP);
            double tempMax = getUnitsIn(
                    Double.parseDouble(
                            getJSonParamValueByParamPath(Constants.PARAM_TEMP_MAX, jsonString)),
                    PARAM_TEMP_MAX);
            double tempMin = getUnitsIn(
                    Double.parseDouble(
                            getJSonParamValueByParamPath(Constants.PARAM_TEMP_MIN, jsonString)),
                    PARAM_TEMP_MIN);
            double pressure = getUnitsIn(
                    Double.parseDouble(
                            getJSonParamValueByParamPath(Constants.PARAM_PRESSURE, jsonString)),
                    PARAM_PRESSURE);
            double humidity = getUnitsIn(
                    Double.parseDouble(
                            getJSonParamValueByParamPath(Constants.PARAM_HUMIDITY, jsonString)),
                    PARAM_HUMIDITY);
            double windSpeed =
                    Double.parseDouble(
                            getJSonParamValueByParamPath(Constants.PARAM_WIND_SPEED, jsonString));
            String weatherMain = getJSonParamValueByParamPath(Constants.PARAM_WEATHER_MAIN, jsonString);
            String weatherDesc = getJSonParamValueByParamPath(Constants.PARAM_WEATHER_DESC, jsonString);
            String weatherIcon = getJSonParamValueByParamPath(Constants.PARAM_WEATHER_ICON, jsonString);

            WeatherLocation weatherLocationResult = this.setUuid(uuid)
                    .setLocationName(placeName)
                    .setCoord(coord)
                    .setTemperature(tempMain)
                    .setMinTemperature(tempMin)
                    .setMaxTemperature(tempMax)
                    .setPressure(pressure)
                    .setHumidity(humidity)
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
