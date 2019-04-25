package com.hfugames.servicelib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class PluginActivity extends UnityPlayerActivity {
    private static final String TAG = "LocationServicePlugin";
    private static final int REQUEST_CODE = 1000;
    private static Activity unityActivity;
    private static String unityClassName;

    private Intent currentIntent;
    private boolean isLocationServiceRunning;
    public static Handler messageHandler = new MessageHandler();


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
            }
        } catch(Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    private void setUnityAcitvityContext(Activity _context) {
        unityActivity = _context;
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
            // Log.d(TAG, "Start location service...");
            if (isLocationServiceRunning) throw new Exception("Location Service already running!");
            if (unityActivity == null) throw new Exception("Undefined UnityActivity!");

            if (ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(unityActivity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
            } else {
                currentIntent = new Intent(unityActivity, LocationService.class);
                currentIntent.putExtra("MESSENGER", new Messenger(messageHandler));
                unityActivity.startService(currentIntent);
                isLocationServiceRunning = true;
            }
        } catch (Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    public void stopLocationService() {
        try {
            if (currentIntent != null) unityActivity.stopService(currentIntent);
            isLocationServiceRunning = false;
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

    public static class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message _message) {
            try {
                int type = _message.arg1;
                Object msg = _message.obj;
                switch(type) {
                    case 0:
                        // location message received
                        Location newLocation = (Location)msg;
                        UnityPlayer.UnitySendMessage(unityClassName, "OnLocationReceived", newLocation.getLatitude() + " : " + newLocation.getLongitude());
                        break;
                }
            } catch(Exception _e) {
                Log.e(TAG, _e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        stopLocationService();
        super.onDestroy();
    }
}
