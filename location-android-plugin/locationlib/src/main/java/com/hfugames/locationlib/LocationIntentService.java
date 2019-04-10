package com.hfugames.locationlib;

import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

public class LocationIntentService extends IntentService {

    private static final String TAG = "LocationIntentService";
    private PowerManager.WakeLock wakeLock;
    private boolean isRunning = false;

    public LocationIntentService()
    {
        super(TAG);
        // should service be restarted on if activity is turn on again
        // setIntentRedelivery(true);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate");

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "LocationService:WakeLock");

        wakeLock.acquire();
        Log.d(TAG, "wakeClock acquired!");

        // startForeground();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.d(TAG,"onHandleIntent");
        isRunning = true;

        for (int i= 0; i < 10000; i++) {
            if (!isRunning) break;
            Log.d(TAG, i + "");
            SystemClock.sleep(1000);

        }
    }

    @Override
    public void onDestroy()
    {
        // working threads or other work in onHandleIntent must be shut down in this method
        isRunning = false;
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        wakeLock.release();
        Log.d(TAG, "wakeClock released!");
    }
}
