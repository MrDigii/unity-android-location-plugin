package com.hfugames.servicelib;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.text.MessageFormat;

public class PluginActivity extends UnityPlayerActivity {
    private static final String TAG = "LocationServicePlugin";
    private static final int REQUEST_CODE = 1000;
    private static Activity unityActivity;
    private static String unityClassName;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isLocationServiceRunning;


    protected void onCreate(Bundle savedInstanceState) {
        // call UnityPlayerActivity.onCreate()
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called!");
    }

    // Set unity class reference
    public void setUnityClassName(String _className) {
        unityClassName = _className;
    }

    private void initPlugin() {
        try {
            if (unityActivity == null) throw new Exception("Undefined UnityActivity!");

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
        } catch(Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    private void setUnityAcitvityContext(Activity _context) {
        unityActivity = _context;
    }

    private void buildLocationCallback() {
        try {
            if (unityActivity == null) throw new Exception("Undefined UnityActivity!");
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult _locationResult) {
                    Location lastLocation = _locationResult.getLastLocation();
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

    public void startLocationService() {
        try {
            Log.d(TAG, "Start location service...");
            if (isLocationServiceRunning) throw new Exception("Location Service already running!");
            if (unityActivity == null) throw new Exception("Undefined UnityActivity!");

            if (ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(unityActivity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
            } else {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                isLocationServiceRunning = true;
            }
        } catch (Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    public void stopLocationService() {
        try {
            Log.d(TAG, "Stop location service...");
            isLocationServiceRunning = false;
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } catch (Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    public boolean isLocationServiceRunning() {
        return isLocationServiceRunning;
    }

    public void onBackPressed() {
        // instead of calling UnityPlayerActivity.onBackPressed() we just ignore the back button event
        // super.onBackPressed();
    }
}
