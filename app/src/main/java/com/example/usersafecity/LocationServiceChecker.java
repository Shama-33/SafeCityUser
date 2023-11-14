package com.example.usersafecity;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

public class LocationServiceChecker {

    private Context context;

    public LocationServiceChecker(Context context) {
        this.context = context;
    }

    public boolean isLocationServiceEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Check if GPS or Network location providers are enabled
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGpsEnabled || isNetworkEnabled;
    }

    // Open the device's location settings
    public void openLocationSettings() {
        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
