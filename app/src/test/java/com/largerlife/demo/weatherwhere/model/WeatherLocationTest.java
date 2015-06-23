package com.largerlife.demo.weatherwhere.model;

import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_HUMIDITY;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_PRESSURE;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_TEMP;
import static com.largerlife.demo.weatherwhere.model.Constants.PARAM_WIND_SPEED;


/**
 * Created by LargerLife on 22/06/15.
 */
public class WeatherLocationTest extends TestCase {

    static final String TARDOS_JSON = "{\"id\":3044122,\"name\":\"Tardos\",\"coord\":{\"lon\":18.44416,\"lat\":47.661919},\"main\":{\"temp\":293.778,\"temp_min\":293.778,\"temp_max\":293.778,\"pressure\":1008.59,\"sea_level\":1030.85,\"grnd_level\":1008.59,\"humidity\":66},\"dt\":1434959628,\"wind\":{\"speed\":4.66,\"deg\":213.001},\"sys\":{\"country\":\"\"},\"clouds\":{\"all\":20},\"weather\":[{\"id\":801,\"main\":\"Clouds\",\"description\":\"few clouds\",\"icon\":\"02d\"}]}";
    static final String NESZMELY_JSON = "{\"id\":3047364,\"name\":\"Neszmely\",\"coord\":{\"lon\":18.359659,\"lat\":47.735851},\"main\":{\"temp\":293.178,\"temp_min\":293.178,\"temp_max\":293.178,\"pressure\":1007.86,\"sea_level\":1030.44,\"grnd_level\":1007.86,\"humidity\":65},\"dt\":1434959628,\"wind\":{\"speed\":4.81,\"deg\":214.001},\"sys\":{\"country\":\"\"},\"clouds\":{\"all\":44},\"weather\":[{\"id\":802,\"main\":\"Clouds\",\"description\":\"scattered clouds\",\"icon\":\"03d\"}]}";

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        WeatherLocation.setUnitsIn(Constants.UNITS_METRIC);
    }

    @Test
    public void test_GetJsonParamValueForValidParamPath() throws JSONException {
        String actualParamValue = WeatherLocation.getJSonParamValueByParamPath("name",
                TARDOS_JSON);
        assertEquals("Tardos", actualParamValue);

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("name",
                NESZMELY_JSON);
        assertEquals("Neszmely", actualParamValue);

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("main:temp",
                TARDOS_JSON);
        assertEquals("293.778", actualParamValue);


        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("main:temp",
                NESZMELY_JSON);
        assertEquals("293.178", actualParamValue);

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("weather:main",
                TARDOS_JSON);
        assertEquals("Clouds", actualParamValue);

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("weather:description",
                TARDOS_JSON);
        assertEquals("few clouds", actualParamValue);

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("weather:main",
                NESZMELY_JSON);
        assertEquals("Clouds", actualParamValue);

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("weather:description",
                NESZMELY_JSON);
        assertEquals("scattered clouds", actualParamValue);

    }

    @Test
    public void test_GetTemperature() throws Exception {
        double actual = WeatherLocation.getUnitsIn(293.778d, PARAM_TEMP);
        assertTrue(293.78d == actual);
    }

    @Test
    public void test_GetSpeed() throws Exception {
        double actual = WeatherLocation.getUnitsIn(29.736d, PARAM_WIND_SPEED);
        assertTrue(107.05d == actual);
    }

    @Test
    public void test_BuildFromJSon() throws JSONException {
        WeatherLocation result = WeatherLocation.builder().buildFromJson(TARDOS_JSON);
        assertTrue(result.uuid == 3044122);
        assertEquals("Tardos", result.locationName);
        assertTrue(result.coord.latitude == 47.661919d);
        assertTrue(result.coord.longitude == 18.44416d);
        assertTrue(result.temperature == WeatherLocation.getUnitsIn(293.778d, PARAM_TEMP));
        assertTrue(result.minTemperature == WeatherLocation.getUnitsIn(293.778d, PARAM_TEMP));
        assertTrue(result.maxTemperatue == WeatherLocation.getUnitsIn(293.778d, PARAM_TEMP));
        assertTrue(result.pressure == WeatherLocation.getUnitsIn(1008.59d, PARAM_PRESSURE));
        assertTrue(result.humidity == WeatherLocation.getUnitsIn(66.0d, PARAM_HUMIDITY));
        assertTrue(result.windSpeed == 4.66d);
        assertEquals("Clouds", result.weatherMain);
        assertEquals("few clouds", result.weatherDescription);
        assertEquals("02d", result.weatherIcon);

        result = WeatherLocation.builder().buildFromJson(NESZMELY_JSON);
        assertTrue(result.uuid == 3047364);
        assertEquals("Neszmely", result.locationName);
        assertTrue(result.coord.latitude == 47.735851d);
        assertTrue(result.coord.longitude == 18.359659d);
        assertTrue(result.temperature == WeatherLocation.getUnitsIn(293.178d, PARAM_TEMP));
        assertTrue(result.minTemperature == WeatherLocation.getUnitsIn(293.178d, PARAM_TEMP));
        assertTrue(result.maxTemperatue == WeatherLocation.getUnitsIn(293.178d, PARAM_TEMP));
        assertTrue(result.pressure == WeatherLocation.getUnitsIn(1007.86d, PARAM_PRESSURE));
        assertTrue(result.humidity == WeatherLocation.getUnitsIn(65.0d, PARAM_HUMIDITY));
        assertTrue(result.windSpeed == 4.81d);
        assertEquals("Clouds", result.weatherMain);
        assertEquals("scattered clouds", result.weatherDescription);
        assertEquals("03d", result.weatherIcon);
    }

    @Test
    public void test_GetJsonParamValueForNotExistingParamPath() throws JSONException {
        String actualParamValue = WeatherLocation.getJSonParamValueByParamPath("",
                TARDOS_JSON);
        assertTrue(actualParamValue.isEmpty());

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("not_exist",
                TARDOS_JSON);
        assertTrue(actualParamValue.isEmpty());

        actualParamValue = WeatherLocation.getJSonParamValueByParamPath("main:not_exist",
                TARDOS_JSON);
        assertTrue(actualParamValue.isEmpty());
    }

    @Test
    public void test_GetJsonParamValueForExceedingParamPath() throws JSONException {
        String actualParamValue = WeatherLocation.getJSonParamValueByParamPath("main:temp:not_exists",
                TARDOS_JSON);
        assertEquals("", actualParamValue);
    }
}
