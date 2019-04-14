using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ServiceManager : MonoBehaviour
{
    private AndroidJavaObject serviceJavaClass;
    private AndroidJavaClass unityJavaClass;
    private AndroidJavaObject unityJavaActivity;
    private string servicePackagePath = "com.hfugames.servicelib.ServiceManager";

    [SerializeField]
    private Text numberText;

    // Start is called before the first frame update
    void Start()
    {
        // setup class references between unity and android service module
        serviceJavaClass = new AndroidJavaObject(servicePackagePath);
        SetUnityActivityReference(servicePackagePath);
        SetUnityClassName();
        serviceJavaClass.CallStatic("setupServiceManager");
    }

    /// <summary>
    /// Send unity activity reference to java service package
    /// </summary>
    /// <param name="packageName"></param>
    private void SetUnityActivityReference(string packageName)
    {
        unityJavaClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        unityJavaActivity = unityJavaClass.GetStatic<AndroidJavaObject>("currentActivity");
        serviceJavaClass.CallStatic("setUnityActivityInstance", unityJavaActivity);
    }

    private void SetUnityClassName()
    {
        serviceJavaClass.CallStatic("setUnityClassName", this.gameObject.name);
    }

    public void StartService(bool _asForeground)
    {
        serviceJavaClass.CallStatic("startLocationService", _asForeground);
    }

    public void StopService()
    {
        serviceJavaClass.CallStatic("stopLocationService");
    }

    void OnApplicationQuit()
    {
        StopService();
    }
}
