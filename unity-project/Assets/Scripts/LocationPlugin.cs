using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public struct LocationData
{
    public double latitude;
    public double longitude;
    public double altitude;
    public double accuracy;
    public double bearing;
    public double speed;
    public string provider;
}

public struct Destination
{
    public double latitude;
    public double longitude;
    public string destinationName;
    public double triggerRadius;
}

public class LocationPlugin : MonoBehaviour
{
    protected AndroidJavaObject pluginJavaClass;
    protected AndroidJavaClass unityJavaClass;
    protected AndroidJavaObject unityJavaActivity;

    // events
    public delegate void LocationHandler(LocationData _location);
    public delegate void LocationAvailabilityHandler(bool _isAvailable);
    public delegate void LocationDistanceHandler(double _distance);
    public event LocationHandler OnLocation;
    public event LocationAvailabilityHandler OnAvailability;
    public event LocationDistanceHandler OnDistanceChanged;

    private void Awake()
    {
        SetUnityActivityReference();
        SetUnityClassName();
        InitServicePlugin();
    }

    /// <summary>
    /// Send unity activity reference to java service package
    /// </summary>
    /// <param name="packageName"></param>
    private void SetUnityActivityReference()
    {
        unityJavaClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        unityJavaActivity = unityJavaClass.GetStatic<AndroidJavaObject>("currentActivity");
        GetPluginClass().Call("setUnityAcitvityContext", unityJavaActivity);
    }

    private void InitServicePlugin()
    {
        GetPluginClass().Call("initPlugin");
    }

    private void SetUnityClassName()
    {
        pluginJavaClass.Call("setUnityClassName", this.gameObject.name);
    }

    public void StartLocationService(int _interval, int _fastestInterval, int _smallestDisplacement)
    {
        StartLocationService(_interval, _fastestInterval, _smallestDisplacement, true, "ic_stat_my_location", "ic_stat_my_location");
    }

    /// <summary>
    /// Start google player location service
    /// </summary>
    public void StartLocationService(int _interval, int _fastestInterval, int _smallestDisplacement, bool _asForeground, String _foregroundIcon, String _notificationIcon)
    {
        GetPluginClass().Call("startLocationService", _interval, _fastestInterval, _smallestDisplacement, _asForeground, _foregroundIcon, _notificationIcon);
    }

    /// <summary>
    /// Stop google player location service
    /// </summary>
    public void StopLocationService()
    {
        GetPluginClass().Call("stopLocationService");
    }

    /// <summary>
    /// Set new destination
    /// Trigger radius of destination is in meters
    /// </summary>
    /// <param name="_dest"></param>
    public void setDestination(Destination _dest)
    {
        GetPluginClass().Call("setDestination", _dest.latitude, _dest.longitude, _dest.destinationName, _dest.triggerRadius);
    }

    public bool IsLocationServiceRunning()
    {
        return GetPluginClass().Call<bool>("isLocationServiceRunning");
    }

    public LocationData LastLocation
    {
        get
        {
            LocationData lastLocation = new LocationData();
            lastLocation.latitude = GetPluginClass().Call<double>("getLatitude");
            lastLocation.longitude = GetPluginClass().Call<double>("getLongitude");
            lastLocation.altitude = GetPluginClass().Call<double>("getAltitude");
            lastLocation.accuracy = GetPluginClass().Call<double>("getAccuracy");
            lastLocation.bearing = GetPluginClass().Call<double>("getBearing");
            lastLocation.speed = GetPluginClass().Call<double>("getSpeed");
            lastLocation.provider = GetPluginClass().Call<string>("getProvider");
            return lastLocation;
        }        
    }

    public bool HasAltitude()
    {
        return GetPluginClass().Call<bool>("hasAltitude");
    }

    public bool HasAccuracy()
    {
        return GetPluginClass().Call<bool>("hasAccuracy");
    }

    public bool HasBearing()
    {
        return GetPluginClass().Call<bool>("hasBearing");
    }

    public bool HasSpeed()
    {
        return GetPluginClass().Call<bool>("hasSpeed");
    }

    private void OnLocationReceived(string _locationData)
    {
        LocationData location = new LocationData();
        System.Globalization.CultureInfo cultureInfo = System.Globalization.CultureInfo.InvariantCulture;
        string[] splittedLocationString = _locationData.Split(':');
        location.latitude = double.Parse(splittedLocationString[0], cultureInfo);
        location.longitude = double.Parse(splittedLocationString[1], cultureInfo);
        location.altitude = double.Parse(splittedLocationString[2], cultureInfo);
        location.accuracy = double.Parse(splittedLocationString[3], cultureInfo);
        location.bearing = double.Parse(splittedLocationString[4], cultureInfo);
        location.speed = double.Parse(splittedLocationString[5], cultureInfo);
        location.provider = splittedLocationString[6];
        OnLocation?.Invoke(location);
    }

    private void OnLocationAvailability(string _locationAvailable)
    {
        bool isAvailable = Convert.ToBoolean(_locationAvailable);
        OnAvailability?.Invoke(isAvailable);
    }

    private void OnDistance(string _distanceToDestination)
    {
        System.Globalization.CultureInfo cultureInfo = System.Globalization.CultureInfo.InvariantCulture;
        double distance = double.Parse(_distanceToDestination, cultureInfo);
        OnDistanceChanged?.Invoke(distance);
    }

    private AndroidJavaObject GetAndroidActivity()
    {
        if (unityJavaClass == null || unityJavaActivity == null)
        {
            unityJavaClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            unityJavaActivity = unityJavaClass.GetStatic<AndroidJavaObject>("currentActivity");
        }
        return unityJavaActivity;
    }

    private AndroidJavaObject GetPluginClass()
    {
        if (pluginJavaClass == null)
        {
            pluginJavaClass = new AndroidJavaObject("com.hfugames.servicelib.PluginActivity");
        }
        return pluginJavaClass;
    }

    private void OnApplicationQuit()
    {
        StopLocationService();
    }
}
