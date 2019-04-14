package com.hfugames.servicelib;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static com.hfugames.servicelib.ServiceManager.SERVICE_CHANNEL_ID;
import static com.hfugames.servicelib.ServiceManager.INTENT_FOREGROUND_EXTRA_NAME;

public class LocationService extends Service {

    private static final String TAG = "LocationIntentService";
    private PowerManager.WakeLock wakeLock;
    private boolean isRunning = false;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private volatile boolean stopThread = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        /*
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "LocationService:WakeLock");

        wakeLock.acquire();
        Log.d(TAG, "wakeClock acquired!");
        */
    }

    @Override
    public int onStartCommand(Intent _intent, int flags, int startId) {

        if (_intent.getBooleanExtra(INTENT_FOREGROUND_EXTRA_NAME, false)) {
            // create notification
            Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Service Running...")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .build();

            // start as foreground service
            startForeground(1, notification);
        }

        isRunning = true;
        stopThread = false;
        LocationRunnable locationRunnable = new LocationRunnable(20);
        Thread locationThread = new Thread(locationRunnable);
        locationThread.start();

        return START_NOT_STICKY;
    }

    class LocationRunnable implements Runnable {
        int seconds;

        LocationRunnable(int _seconds) {
            this.seconds = _seconds;
        }

        @Override
        public void run() {
            for (int i= 0; i < this.seconds; i++) {
                if (stopThread)
                    return;
                Log.d(TAG, i + "");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    @Override
    protected void onHandleWork(@NonNull Intent _intent) {
        Log.d(TAG, "onHandleIntent");

        if (_intent.getBooleanExtra(INTENT_FOREGROUND_EXTRA_NAME, false)) {
            // create notification
            Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Service Running...")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .build();

            // start as foreground service
            startForeground(1, notification);
        }

        isRunning = true;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "locationChanged");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, locationListener);
    }
    */

    /*
    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.d(TAG,"onHandleIntent");

        if (intent.getBooleanExtra(INTENT_FOREGROUND_EXTRA_NAME, false)) {
            // create notification
            Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Service Running...")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .build();

            // start as foreground service
            startForeground(1, notification);
        }

        isRunning = true;

        for (int i= 0; i < 10000; i++) {
            if (!isRunning) break;
            Log.d(TAG, i + "");
            SystemClock.sleep(1000);

        }
    }
    */

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // locationManager.removeUpdates(locationListener);
        stopForeground(true);
        // working threads or other work in onHandleIntent must be shut down in this method
        isRunning = false;
        stopThread = true;
        // wakeLock.release();
        // Log.d(TAG, "wakeClock released!");
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
