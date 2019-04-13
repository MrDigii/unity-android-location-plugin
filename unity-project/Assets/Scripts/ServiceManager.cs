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

        serviceJavaClass.Call("LogNativeLogcatMessage");
        serviceJavaClass.Call("LogNumberSentFromUnity", 20);
        numberText.text = serviceJavaClass.Call<int>("AddToNumber", 10, 5).ToString();
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

    public void StartService()
    {
        serviceJavaClass.CallStatic("startLocationService");
    }

    public void StopService()
    {
        serviceJavaClass.CallStatic("stopLocationService");
    }

    public void CallAOrBInJava(string _value)
    {
        serviceJavaClass.Call("CallAOrB", _value);     
    }

    public void ChangeTextToA(string _value)
    {
        numberText.text = "A: " + _value;
    }

    public void ChangeTextToB(string _value)
    {
        numberText.text = "B: " + _value;
    }

    void OnApplicationQuit()
    {
        StopService();
    }
}
