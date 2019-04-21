using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LocationPlugin : MonoBehaviour
{
    protected AndroidJavaObject pluginJavaClass;
    protected AndroidJavaClass unityJavaClass;
    protected AndroidJavaObject unityJavaActivity;

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

    /// <summary>
    /// Start google player location service
    /// </summary>
    public void StartLocationService()
    {
        GetPluginClass().Call("startLocationService");
    }

    /// <summary>
    /// Stop google player location service
    /// </summary>
    public void StopLocationService()
    {
        GetPluginClass().Call("stopLocationService");
    }

    public bool IsLocationServiceRunning()
    {
        return GetPluginClass().Call<bool>("isLocationServiceRunning");
    }

    protected virtual void OnLocationReceived(string _locationData)
    {
        
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
