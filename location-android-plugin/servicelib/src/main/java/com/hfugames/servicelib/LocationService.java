package com.hfugames.servicelib;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private Messenger messageHandler;
    private final IBinder mBinder = new LocationBinder();

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private int interval;
    private int fastestInterval;
    private int smallestDisplacement;


    @Override
    public void onCreate() {
        super.onCreate();
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent _intent, int _flags, int _startId) {
        try {
            Log.d(TAG, "Start location service...");
            Bundle extras = _intent.getExtras();
            messageHandler = (Messenger) extras.get("MESSENGER");
            interval = (int) extras.get("INTERVAL");
            fastestInterval = (int) extras.get("FASTEST_INTERVAL");
            smallestDisplacement = (int) extras.get("SMALLEST_DISPLACEMENT");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return START_NOT_STICKY;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } catch (Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } catch (Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent _intent) {
        return mBinder;
    }

    // singelton pattern
    public class LocationBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    private void buildLocationCallback() {
        try {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult _locationResult) {
                    lastLocation = _locationResult.getLastLocation();
                    sendMessageToActivity(0, lastLocation);
                }

                @Override
                public void onLocationAvailability(LocationAvailability _locationAvailability) {
                    sendMessageToActivity(1, _locationAvailability.isLocationAvailable());
                }
            };
        } catch(Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    private void buildLocationRequest() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
        locationRequest.setSmallestDisplacement(smallestDisplacement);
    }

    private void sendMessageToActivity(int _type, Object _msg) {
        try {
            Message message = Message.obtain();
            switch(_type) {
                case 0:
                    Location lastLocation = (Location)_msg;
                    message.arg1 = _type;
                    message.obj = lastLocation;
                    messageHandler.send(message);
                    break;
                case 1:
                    boolean locationAvailable = (boolean)_msg;
                    message.arg1 = _type;
                    message.obj = locationAvailable;
                    messageHandler.send(message);
                    break;
            }
        } catch(Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }
}
