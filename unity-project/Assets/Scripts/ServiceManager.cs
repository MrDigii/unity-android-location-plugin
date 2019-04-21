using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ServiceManager : LocationPlugin
{
    [SerializeField]
    private Text locationText;

    protected override void OnLocationReceived(string _locationData)
    {
        Debug.Log(_locationData);
        locationText.text = _locationData;
    }
}
