package com.hfugames.locationlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.unity3d.player.UnityPlayer;

public class LocationManager
{
    public static final String CHANNEL_ID = "LocationServiceChannel";
    static Activity myActivity;

    // Called From C# to get the Activity Instance
    public static void receiveActivityInstance(Activity tempActivity) {
        myActivity = tempActivity;
    }

    public static void startLocationService() {
        myActivity.startService(new Intent(myActivity, LocationIntentService.class));
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
        UnityPlayer.UnitySendMessage("LocationManager", "ChangeTextToA", "2");
    }

    public void DoSomethingB()
    {
        UnityPlayer.UnitySendMessage("LocationManager", "ChangeTextToB", "3");
    }
}
