package com.hfugames.servicelib;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import static com.hfugames.servicelib.PluginActivity.FASTEST_INTERVAL_EXTRA;
import static com.hfugames.servicelib.PluginActivity.INTENT_FOREGROUND_EXTRA;
import static com.hfugames.servicelib.PluginActivity.INTENT_FOREGROUND_ICON_EXTRA;
import static com.hfugames.servicelib.PluginActivity.INTENT_NOTIFICATION_ICON_EXTRA;
import static com.hfugames.servicelib.PluginActivity.INTERVAL_EXTRA;
import static com.hfugames.servicelib.PluginActivity.MESSENGER_EXTRA;
import static com.hfugames.servicelib.PluginActivity.NOTIFICATION_CHANNEL_ID;
import static com.hfugames.servicelib.PluginActivity.SERVICE_CHANNEL_ID;
import static com.hfugames.servicelib.PluginActivity.SMALLEST_DISPLACEMENT_EXTRA;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private Messenger messageHandler;
    private final IBinder mBinder = new LocationBinder();

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;
    NotificationManager notificationManager;
    Notification locationNotification;
    NotificationCompat.Builder locationNotificationBuilder;

    private int interval;
    private int fastestInterval;
    private int smallestDisplacement;
    private boolean asForeground;
    private Destination currentDestination;

    @Override
    public void onCreate() {
        super.onCreate();
        buildLocationRequest();
        buildLocationCallback();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent _intent, int _flags, int _startId) {
        try {
            Log.d(TAG, "Start location service...");
            Bundle extras = _intent.getExtras();
            messageHandler = (Messenger) extras.get(MESSENGER_EXTRA);
            interval = (int) extras.get(INTERVAL_EXTRA);
            fastestInterval = (int) extras.get(FASTEST_INTERVAL_EXTRA);
            smallestDisplacement = (int) extras.get(SMALLEST_DISPLACEMENT_EXTRA);
            asForeground = (boolean) extras.get(INTENT_FOREGROUND_EXTRA);
            String serviceServiceIconName = (String) extras.get(INTENT_FOREGROUND_ICON_EXTRA);
            String serviceNotificationIconName = (String) extras.get(INTENT_NOTIFICATION_ICON_EXTRA);

            Resources res = this.getResources();

            if (asForeground) {
                // create notification
                Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                        .setContentTitle("Location Service")
                        .setContentText("Service Running...")
                        .setSmallIcon(res.getIdentifier(serviceServiceIconName, "drawable", this.getPackageName()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .build();

                // create notification
                locationNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                locationNotification = locationNotificationBuilder
                        .setContentTitle("Location reached!")
                        .setContentText("You reached your destination!")
                        .setSmallIcon(res.getIdentifier(serviceNotificationIconName, "drawable", this.getPackageName()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setLights(Color.GREEN, 3000, 3000)
                        .build();

                // start as foreground service
                startForeground(1, notification);
            }

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
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (asForeground) stopForeground(true);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
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

                    // check if destination has been reached
                    if (currentDestination != null) {
                        Location destLocation = new Location("destLocation");
                        destLocation.setLatitude(currentDestination.latitude);
                        destLocation.setLongitude(currentDestination.longitude);

                        double distance = lastLocation.distanceTo(destLocation);
                        // Log.d(TAG, "Distance to destination: " + distance );
                        sendMessageToActivity(2, distance);

                        // check if device is in destinations triggerRadius
                        if(distance <= currentDestination.triggerRadius) {
                            locationNotificationBuilder.setContentText(currentDestination.destinationName);
                            notificationManager.notify(2, locationNotificationBuilder.build());
                            currentDestination = null;
                        }
                    }
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
                case 2:
                    double distanceToDestination = (double)_msg;
                    message.arg1 = _type;
                    message.obj = distanceToDestination;
                    messageHandler.send(message);
                    break;
            }
        } catch(Exception _e) {
            Log.e(TAG, _e.getMessage());
        }
    }

    public void setNewDestination(Destination _newDestination) {
        currentDestination = _newDestination;
    }
}
