package com.example.user.weatherapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.example.user.weatherapp.MainActivity;
import com.example.user.weatherapp.R;
import com.example.user.weatherapp.dialogs.WarningDialog;

/**
 * Created by User on 19.05.2015.
 */
public class GPSUtil implements LocationListener {
   private LocationManager locationManager ;
   private String provider;
   private Location curLocation=null;
    Context context;
    Location location;
    public GPSUtil(Context context){
        this.context=context;
        currentLocation();
    }
    private void currentLocation(){
        // Getting LocationManager object
        locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
           curLocation=null;
        }

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        if(provider!=null && !provider.equals("")) {

            // Get the location from the given provider
            Location location = locationManager.getLastKnownLocation(provider);


            if (location != null)
                onLocationChanged(location);
        }


    }
    @Override
    public void onLocationChanged(Location location) {
        curLocation=location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }
    public Location getLocation(){
        return curLocation;
    }
    @Override
    public void onProviderDisabled(String provider) {

    }
}
