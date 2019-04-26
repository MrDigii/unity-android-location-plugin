package com.hfugames.servicelib;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

    public static final String SERVICE_CHANNEL_ID = "ServiceChannel";
    public static final String NOTIFICATION_CHANNEL_ID = "NotificationChannel";
    public static final String INTENT_FOREGROUND_EXTRA = "startAsForeground";
    public static final String INTENT_FOREGROUND_ICON_EXTRA = "foregroundIcon";
    public static final String INTENT_NOTIFICATION_ICON_EXTRA = "notificationIcon";
    public static final String MESSENGER_EXTRA = "messenger";
    public static final String INTERVAL_EXTRA = "interval";
    public static final String FASTEST_INTERVAL_EXTRA = "fastestInterval";
    public static final String SMALLEST_DISPLACEMENT_EXTRA = "smallestDisplacement";

    private LocationService mService;
    private boolean mBound;

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
            createNotificationChannels();

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

    // Set notification channel for API Level >= 26
    public static void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    SERVICE_CHANNEL_ID,
                    "ServiceChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("Notification Channel for Intent Service");

            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "NotificationChannel",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.setDescription("Notification Channel for other Notifications");

            NotificationManager manager = unityActivity.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(notificationChannel);
        }
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

    public void startLocationService(int _interval, int _fastestInterval, int _smallestDisplacement, String _foregroundIcon, String _notificationIcon) {
        try {
            if (isLocationServiceRunning) throw new Exception("Location Service already running!");
            if (unityActivity == null) throw new Exception("Undefined UnityActivity!");

            if (ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(unityActivity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
            } else {
                currentIntent = new Intent(unityActivity, LocationService.class);
                currentIntent.putExtra(MESSENGER_EXTRA, new Messenger(messageHandler));
                currentIntent.putExtra(SMALLEST_DISPLACEMENT_EXTRA, _smallestDisplacement);
                currentIntent.putExtra(INTERVAL_EXTRA, _interval);
                currentIntent.putExtra(FASTEST_INTERVAL_EXTRA, _fastestInterval);
                currentIntent.putExtra(INTENT_FOREGROUND_EXTRA, true);
                currentIntent.putExtra(INTENT_FOREGROUND_ICON_EXTRA, _foregroundIcon);
                currentIntent.putExtra(INTENT_NOTIFICATION_ICON_EXTRA, _notificationIcon);

                unityActivity.startService(currentIntent);
                unityActivity.bindService(currentIntent, mConnection, Context.BIND_AUTO_CREATE);
                isLocationServiceRunning = true;
            }
        } catch (Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void stopLocationService() {
        Log.d(TAG, "Stop location service...");

        if (isLocationServiceRunning) {
            unityActivity.unbindService(mConnection);
            unityActivity.stopService(currentIntent);
        }
        isLocationServiceRunning = false;
    }

    // get last location latitude from locationService
    public double getLatitude() {
        double lat = -1;
        if (mBound) {
            lat = mService.getLastLocation().getLatitude();
        }
        return lat;
    }

    // get last location longitude from locationService
    public double getLongitude() {
        double lng = -1;
        if (mBound) {
            lng = mService.getLastLocation().getLongitude();
        }
        return lng;
    }

    // get last location altitude from locationService
    public double getAltitude() {
        double alt = -1;
        if (mBound) {
            alt = mService.getLastLocation().getAltitude();
        }
        return alt;
    }

    public boolean hasAltitude() {
        boolean hasIt = false;
        if (mBound) {
            hasIt = mService.getLastLocation().hasAltitude();
        }
        return hasIt;
    }

    // get last location accuracy from locationService
    public double getAccuracy() {
        double acc = -1;
        if (mBound) {
            acc = mService.getLastLocation().getAccuracy();
        }
        return acc;
    }

    public boolean hasAccuracy() {
        boolean hasIt = false;
        if (mBound) {
            hasIt = mService.getLastLocation().hasAccuracy();
        }
        return hasIt;
    }

    // get last location bearing from locationService
    public double getBearing() {
        double bearing = -1;
        if (mBound) {
            bearing = mService.getLastLocation().getBearing();
        }
        return bearing;
    }

    public boolean hasBearing() {
        boolean hasIt = false;
        if (mBound) {
            hasIt = mService.getLastLocation().hasBearing();
        }
        return hasIt;
    }

    // get last location speed from locationService
    public double getSpeed() {
        double speed = -1;
        if (mBound) {
            speed = mService.getLastLocation().getSpeed();
        }
        return speed;
    }

    public boolean hasSpeed() {
        boolean hasIt = false;
        if (mBound) {
            hasIt = mService.getLastLocation().hasSpeed();
        }
        return hasIt;
    }

    // get last location provider from locationService
    public String getProvider() {
        String prov = "";
        if (mBound) {
            prov = mService.getLastLocation().getProvider();
        }
        return prov;
    }

    public boolean isLocationServiceRunning() {
        return isLocationServiceRunning;
    }

    public void onBackPressed() {
        // instead of calling UnityPlayerActivity.onBackPressed() we just ignore the back button event
        // super.onBackPressed();
    }

    // Manage connection with bound service
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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
                        UnityPlayer.UnitySendMessage(
                                unityClassName,
                                "OnLocationReceived",
                                newLocation.getLatitude()
                                        + ":" + newLocation.getLongitude()
                                        + ":" + newLocation.getAltitude()
                                        + ":" + newLocation.getAccuracy()
                                        + ":" + newLocation.getBearing()
                                        + ":" + newLocation.getSpeed()
                                        + ":" + newLocation.getProvider()
                        );
                        break;
                    case 1:
                        // location service availability changed
                        boolean locationSensorAvailable = (boolean)msg;
                        UnityPlayer.UnitySendMessage(unityClassName, "OnLocationAvailability", locationSensorAvailable + "");
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
