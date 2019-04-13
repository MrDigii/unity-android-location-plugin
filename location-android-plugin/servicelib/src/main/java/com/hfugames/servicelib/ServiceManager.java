package com.hfugames.servicelib;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.unity3d.player.UnityPlayer;

public class ServiceManager
{
    public static final String CHANNEL_ID = "LocationServiceChannel";

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

    public static void startLocationService()
    {
        stopLocationService();
        currentIntent = new Intent(unityActivity, LocationIntentService.class);
        unityActivity.startService(currentIntent);
    }

    public static void stopLocationService()
    {
        if (currentIntent != null) unityActivity.stopService(currentIntent);
    }

    public void LogNativeLogcatMessage()
    {
        Log.d("Unity", "Native Logcat Message");
    }

    public void LogNumberSentFromUnity(int _passedNumber)
    {
        Log.d("Unity", "Number passed is: " + _passedNumber);
    }

    public int AddToNumber(int _number, int _addAmount)
    {
        return _number + _addAmount;
    }

    public void CallAOrB(String _value)
    {
        if(_value.equals("A")) {
            DoSomethingA();
        } else if (_value.equals("B")) {
            DoSomethingB();
        }
    }

    public void DoSomethingA()
    {
        UnityPlayer.UnitySendMessage(unityClassName, "ChangeTextToA", "2");
    }

    public void DoSomethingB()
    {
        UnityPlayer.UnitySendMessage(unityClassName, "ChangeTextToB", "3");
    }
}
