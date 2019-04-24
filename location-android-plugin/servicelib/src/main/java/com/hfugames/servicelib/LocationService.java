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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.unity3d.player.UnityPlayer;

import java.text.MessageFormat;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private String unityClassName;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;


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
            unityClassName = _intent.getStringExtra("UnityClassName");

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
            Log.d(TAG, "Stop location service...");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } catch (Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent _intent) {
        return null;
    }

    private void buildLocationCallback() {
        try {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult _locationResult) {
                    lastLocation = _locationResult.getLastLocation();
                    String locationMsg = MessageFormat.format("Location received: Lat {0} Lng {1}.", lastLocation.getLatitude(), lastLocation.getLongitude());
                    Log.d(TAG, locationMsg);
                    UnityPlayer.UnitySendMessage(unityClassName, "OnLocationReceived", locationMsg);
                }
            };
        } catch(Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    private void buildLocationRequest() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }
}
