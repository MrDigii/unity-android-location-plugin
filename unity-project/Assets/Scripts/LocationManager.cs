using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class LocationManager : MonoBehaviour
{
    private AndroidJavaObject javaClass;
    [SerializeField]
    private Text numberText;

    // Start is called before the first frame update
    void Start()
    {
        javaClass = new AndroidJavaObject("com.hfugames.locationlib.LocationManager");
        javaClass.Call("LogNativeLogcatMessage");
        javaClass.Call("LogNumberSentFromUnity", 20);
        numberText.text = javaClass.Call<int>("AddToNumber", 10, 5).ToString();
    }

    public void CallAOrBInJava(string _value)
    {
        javaClass.Call("CallAOrB", _value);     
    }

    public void ChangeTextToA(string _value)
    {
        numberText.text = "A: " + _value;
    }

    public void ChangeTextToB(string _value)
    {
        numberText.text = "B: " + _value;
    }
}
