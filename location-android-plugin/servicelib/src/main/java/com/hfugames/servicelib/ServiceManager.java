package com.hfugames.servicelib;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.unity3d.player.UnityPlayer;

public class ServiceManager
{
    public static final String SERVICE_CHANNEL_ID = "ServiceChannel";
    public static final String NOTIFICATION_CHANNEL_ID = "NotificationChannel";
    public static final String INTENT_FOREGROUND_EXTRA_NAME = "startAsForeground";

    private static Activity unityActivity;
    private static String unityClassName;
    private static Intent currentIntent;

    // Set unity activity instance
    public static void setUnityActivityInstance(Activity _unityActivity) {
        unityActivity = _unityActivity;
    }

    // Set unity class reference
    public static void setUnityClassName(String _className) {
       unityClassName = _className;
    }

    public static void setupServiceManager() {
        // create notification channels for API >= 26
        createNotificationChannels();
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

    // Start location intentService
    public static void startLocationService()
    {
        startLocationService(false);
    }

    public static void startLocationService(boolean _asForeground)
    {
        stopLocationService();
        currentIntent = new Intent(unityActivity, LocationIntentService.class);
        currentIntent.putExtra(INTENT_FOREGROUND_EXTRA_NAME, _asForeground);
        // unityActivity.startService(currentIntent);
        LocationIntentService.enqueueWork(unityActivity, currentIntent);
    }

    public static void stopLocationService() {
        if (currentIntent != null) unityActivity.stopService(currentIntent);
    }
}
