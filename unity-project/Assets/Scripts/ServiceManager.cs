using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ServiceManager : MonoBehaviour
{
    private AndroidJavaObject pluginJavaClass;
    private AndroidJavaClass unityJavaClass;
    private AndroidJavaObject unityJavaActivity;
    private string servicePackagePath = "com.hfugames.servicelib.ServiceManager";

    [SerializeField]
    private Text numberText;

    // Start is called before the first frame update
    void Start()
    {
        // setup class references between unity and android service module
        SetUnityActivityReference();
        StartServicePlugin();
        // SetUnityClassName();
        // serviceJavaClass.CallStatic("setupServiceManager");
    }

    /// <summary>
    /// Send unity activity reference to java service package
    /// </summary>
    /// <param name="packageName"></param>
    private void SetUnityActivityReference()
    {
        unityJavaClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        unityJavaActivity = unityJavaClass.GetStatic<AndroidJavaObject>("currentActivity");
        GetPluginClass().CallStatic("setUnityAcitvityContext", unityJavaActivity);
    }

    private void StartServicePlugin()
    {
        GetPluginClass().Call("startPlugin");
    }

    private void SetUnityClassName()
    {
        pluginJavaClass.CallStatic("setUnityClassName", this.gameObject.name);
    }

    public void StartService()
    {
        GetPluginClass().Call("startLocationService");
    }

    public void StopService()
    {
        GetPluginClass().Call("stopLocationService");
    }

    public AndroidJavaObject GetAndroidActivity()
    {
        if (unityJavaClass == null || unityJavaActivity == null)
        {
            unityJavaClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            unityJavaActivity = unityJavaClass.GetStatic<AndroidJavaObject>("currentActivity");
        }
        return unityJavaActivity;
    }

    public AndroidJavaObject GetPluginClass()
    {
        if (pluginJavaClass == null)
        {
            pluginJavaClass = new AndroidJavaObject("com.hfugames.servicelib.PluginActivity");
        }
        return pluginJavaClass;
    }

    void OnApplicationQuit()
    {
        StopService();
    }
}
