using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LocationManager : MonoBehaviour
{
    private AndroidJavaObject javaClass;

    // Start is called before the first frame update
    void Start()
    {
        javaClass = new AndroidJavaObject("com.hfugames.locationlib.LocationManager");
        javaClass.Call("LogNativeLogcatMessage");
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
