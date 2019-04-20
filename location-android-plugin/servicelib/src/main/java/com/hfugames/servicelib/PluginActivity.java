package com.hfugames.servicelib;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.unity3d.player.UnityPlayerActivity;

public class PluginActivity extends UnityPlayerActivity {
    private static final String TAG = "LocationServicePlugin";
    private static final int REQUEST_CODE = 1000;
    private static Activity unityActivity;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    protected void onCreate(Bundle savedInstanceState) {
        // call UnityPlayerActivity.onCreate()
        super.onCreate(savedInstanceState);
        // print debug message to logcat
        Log.d(TAG, "onCreate called!");


    }

    private void startPlugin() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(unityActivity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE);
        } else {
            // If permission is granted
            buildLocationRequest();
            buildLocationCallback();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(unityActivity);
        }
    }

    private static void setUnityAcitvityContext(Activity _context) {
        unityActivity = _context;
    }

    private void buildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult _locationResult) {
                Log.d(TAG, "Location received!");
            }
        };

    }

    private void buildLocationRequest() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);

    }

    @Override
    public void onRequestPermissionsResult(int _requestCode, @NonNull String[] _permissions, @NonNull int[] _grantResults) {
        super.onRequestPermissionsResult(_requestCode, _permissions, _grantResults);

        switch (_requestCode) {
            case REQUEST_CODE:
                if (_grantResults.length > 0) {
                    if (_grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Location Permission granted!");
                    } else if (_grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "Location Permission denied!");
                    }
                }
                break;
        }
    }


    public void onBackPressed() {
        // instead of calling UnityPlayerActivity.onBackPressed() we just ignore the back button event
        // super.onBackPressed();
    }

    public void startLocationService() {
        Log.d(TAG, "Start location service...");
        if (ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(unityActivity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE);
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    public void stopLocationService() {
        Log.d(TAG, "Stop location service...");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
