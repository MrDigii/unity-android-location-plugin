package com.hfugames.servicelib;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static com.hfugames.servicelib.ServiceManager.SERVICE_CHANNEL_ID;
import static com.hfugames.servicelib.ServiceManager.INTENT_FOREGROUND_EXTRA_NAME;

public class LocationIntentService extends IntentService {

    private static final String TAG = "LocationIntentService";
    private PowerManager.WakeLock wakeLock;
    private boolean isRunning = false;

    public LocationIntentService()
    {
        super(TAG);
        setIntentRedelivery(false);
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
    }

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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopForeground(true);
        // working threads or other work in onHandleIntent must be shut down in this method
        isRunning = false;
        wakeLock.release();
        Log.d(TAG, "wakeClock released!");
        Log.d(TAG, "onDestroy");
    }
}
