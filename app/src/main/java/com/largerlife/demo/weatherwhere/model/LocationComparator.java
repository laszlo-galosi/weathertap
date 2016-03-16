package com.largerlife.demo.weatherwhere.model;

import android.location.Location;
import java.util.Comparator;

/**
 * Created by LargerLife on 22/06/15.
 */
public class LocationComparator implements Comparator<WeatherLocation> {

    private Location centerLocation;

    /**
     * {@link WeatherLocation} comparator based on the distance from the specified
     * center position.
     * {@link com.google.android.gms.maps.model.LatLng}
     */
    public LocationComparator(Location center) {
        this.centerLocation = center;
    }

    @Override
    public int compare(WeatherLocation weatherL, WeatherLocation weatherR) {
        Location locLeft = new Location("left");
        locLeft.setLatitude(weatherL.getCoord().latitude);
        locLeft.setLongitude(weatherL.getCoord().longitude);

        Location locRight = new Location("right");
        locRight.setLatitude(weatherR.getCoord().latitude);
        locRight.setLongitude(weatherR.getCoord().longitude);

        Float distanceL = centerLocation.distanceTo(locLeft);
        Float distanceR = centerLocation.distanceTo(locRight);

        if (distanceL.compareTo(distanceR) < 0) {
            return -1;
        } else if (distanceL.compareTo(distanceR) > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
