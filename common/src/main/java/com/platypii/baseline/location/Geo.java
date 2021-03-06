package com.platypii.baseline.location;

import android.support.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;

/**
 * Geographic helpers
 */
public class Geo {

    private static final double R = 6371000; // meters

    /**
     * Computes the distance between two points
     * @return the distance in meters
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final double φ1 = Math.toRadians(lat1);
        final double φ2 = Math.toRadians(lat2);
        final double Δφ = Math.toRadians(lat2 - lat1);
        final double Δλ = Math.toRadians(lon2 - lon1);

        final double sin_φ = Math.sin(Δφ/2);
        final double sin_λ = Math.sin(Δλ/2);

        // Haversine formula
        final double a = sin_φ * sin_φ +
                Math.cos(φ1) * Math.cos(φ2) *
                        sin_λ * sin_λ;
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

    /**
     * Computes the bearing from location1 to location2
     * @return the bearing in degrees (relative to true north, not magnetic)
     */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        final double φ1 = Math.toRadians(lat1);
        final double φ2 = Math.toRadians(lat2);
        final double Δλ = Math.toRadians(lon2 - lon1);
        final double y = Math.sin(Δλ) * Math.cos(φ2);
        final double x = Math.cos(φ1) * Math.sin(φ2) - Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Moves the location along a bearing (degrees) by a given distance (meters)
     */
    @NonNull
    public static LatLng moveDirection(double latitude, double longitude, double bearing, double distance) {
        final double d = distance / R;

        final double lat = radians(latitude);
        final double lon = radians(longitude);
        final double bear = radians(bearing);

        // Precompute trig
        final double sin_d = Math.sin(d);
        final double cos_d = Math.cos(d);
        final double sin_lat = Math.sin(lat);
        final double cos_lat = Math.cos(lat);
        final double sin_d_cos_lat = sin_d * cos_lat;

        final double lat2 = Math.asin(sin_lat * cos_d + sin_d_cos_lat * Math.cos(bear));
        final double lon2 = lon + Math.atan2(Math.sin(bear) * sin_d_cos_lat, cos_d - sin_lat * Math.sin(lat2));

        final double lat3 = degrees(lat2);
        final double lon3 = mod360(degrees(lon2));

        return new LatLng(lat3, lon3);
    }

    // Helpers
    private static double radians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
    private static double degrees(double radians) {
        return radians * 180.0 / Math.PI;
    }
    private static double mod360(double degrees) {
        return ((degrees + 540) % 360) - 180;
    }

}
